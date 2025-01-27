package failgood

import failgood.internal.ContextPath
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.reflect.KClass
import kotlin.system.exitProcess

sealed interface LoadResult {
    val order: Int
}

data class RootContext(
    val name: String = "root",
    val disabled: Boolean = false,
    override val order: Int = 0,
    val isolation: Boolean = true,
    val sourceInfo: SourceInfo = SourceInfo(findCallerSTE()),
    val function: ContextLambda
) : LoadResult, failgood.internal.Path {
    override val path: List<String>
        get() = listOf(name)
}

data class CouldNotLoadContext(val reason: Throwable, val jClass: Class<out Any>) : LoadResult {
    override val order: Int
        get() = 0
}

data class SourceInfo(val className: String, val fileName: String?, val lineNumber: Int) {
    fun likeStackTrace(testName: String) = "$className.${testName.replace(" ", "-")}($fileName:$lineNumber)"

    constructor(ste: StackTraceElement) : this(ste.className, ste.fileName!!, ste.lineNumber)
}

typealias ContextLambda = suspend ContextDSL.() -> Unit

typealias TestLambda = suspend TestDSL.() -> Unit

fun context(description: String, disabled: Boolean = false, order: Int = 0, function: ContextLambda): RootContext =
    RootContext(description, disabled, order, function = function)

fun describe(
    subjectDescription: String,
    disabled: Boolean = false,
    order: Int = 0,
    isolation: Boolean = true,
    function: ContextLambda
):
    RootContext = RootContext(subjectDescription, disabled, order, isolation, function = function)

inline fun <reified T> describe(disabled: Boolean = false, order: Int = 0, noinline function: ContextLambda):
    RootContext = describe(T::class, disabled, order, function)

fun describe(subjectType: KClass<*>, disabled: Boolean = false, order: Int = 0, function: ContextLambda):
    RootContext = RootContext("The ${subjectType.simpleName}", disabled, order, function = function)

data class TestDescription(
    val container: TestContainer,
    val testName: String,
    val sourceInfo: SourceInfo
) {
    internal constructor(testPath: ContextPath, sourceInfo: SourceInfo) : this(
        testPath.container,
        testPath.name,
        sourceInfo
    )

    override fun toString(): String {
        return "${container.stringPath()} > $testName"
    }
}

/* something that contains tests */
interface TestContainer {
    val parents: List<TestContainer>
    val name: String
    fun stringPath(): String
}

data class Context(
    override val name: String,
    val parent: Context? = null,
    val sourceInfo: SourceInfo? = null,
    val isolation: Boolean = true
) : TestContainer {
    companion object {
        fun fromPath(path: List<String>): Context {
            return Context(path.last(), if (path.size == 1) null else fromPath(path.dropLast(1)))
        }
    }

    override val parents: List<TestContainer> = parent?.parents?.plus(parent) ?: listOf()
    val path: List<String> = parent?.path?.plus(name) ?: listOf(name)
    override fun stringPath(): String = path.joinToString(" > ")
}

object FailGood {
    /**
     * finds test classes
     *
     * @param classIncludeRegex regex that included classes must match
     *        you can also call findTestClasses multiple times to run unit tests before integration tests.
     *        for example Suite.fromClasses(findTestClasses(TestClass::class, Regex(".*Test.class\$)+findTestClasses(TestClass::class, Regex(".*IT.class\$))
     *
     * @param newerThan only return classes that are newer than this. used by autotest
     *
     * @param randomTestClass usually not needed, but you can pass any test class here,
     *        and it will be used to find the classloader and source root
     */
    fun findTestClasses(
        classIncludeRegex: Regex = Regex(".*Test.class\$"),
        newerThan: FileTime? = null,
        randomTestClass: KClass<*> = findCaller()
    ): MutableList<KClass<*>> {
        val classloader = randomTestClass.java.classLoader
        val root = Paths.get(randomTestClass.java.protectionDomain.codeSource.location.path)
        return findClassesInPath(root, classloader, classIncludeRegex, newerThan)
    }

    internal fun findClassesInPath(
        root: Path,
        classloader: ClassLoader,
        classIncludeRegex: Regex = Regex(".*Test.class\$"),
        newerThan: FileTime? = null,
        matchLambda: (String) -> Boolean = { true }
    ): MutableList<KClass<*>> {
        val results = mutableListOf<KClass<*>>()
        Files.walkFileTree(
            root,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    val path = root.relativize(file!!).toString()
                    if (path.matches(classIncludeRegex) &&
                        (newerThan == null || attrs!!.lastModifiedTime() > newerThan)
                    ) {
                        val className = path.substringBefore(".class").replace(File.separatorChar, '.')
                        if (matchLambda(className))
                            results.add(
                                classloader.loadClass(className).kotlin
                            )
                    }
                    return FileVisitResult.CONTINUE
                }
            }
        )
        return results
    }

    /**
     * runs all changes tests. use with ./gradle -t or run it manually from idea
     *
     * @param randomTestClass usually not needed, but you can pass any test class here,
     *        and it will be used to find the classloader and source root
     */
    @Suppress("BlockingMethodInNonBlockingContext", "RedundantSuspendModifier")
    suspend fun autoTest(randomTestClass: KClass<*> = findCaller()) {
        val timeStampPath = Paths.get(".autotest.failgood")
        val lastRun: FileTime? =
            try {
                Files.readAttributes(timeStampPath, BasicFileAttributes::class.java).lastModifiedTime()
            } catch (e: NoSuchFileException) {
                null
            }
        Files.write(timeStampPath, byteArrayOf())
        println("last run:$lastRun")
        val classes = findTestClasses(newerThan = lastRun, randomTestClass = randomTestClass)
        println("will run: ${classes.joinToString { it.simpleName!! }}")
        if (classes.isNotEmpty()) Suite(classes.map { ObjectContextProvider(it) }).run().check(false)
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun runAllTests(writeReport: Boolean = false, paralellism: Int = cpus()) {
        Suite(findTestClasses()).run(parallelism = paralellism).check(writeReport = writeReport)
        printThreads()
    }

    private fun printThreads() {
        val remainingThreads = Thread.getAllStackTraces().filterKeys { !it.isDaemon && it.name != "main" }
        if (remainingThreads.isNotEmpty()) {
            println("Warning: The test suite left some non daemon threads running:")
            remainingThreads
                .forEach { (t, s) -> println("\n* Thread:${t.name}: ${s.joinToString("\n")}") }
            exitProcess(0)
        }
    }

    fun runTest() {
        val classes = listOf(javaClass.classLoader.loadClass((findCallerName().substringBefore("Kt"))).kotlin)
        val suite = Suite(classes)
        suite.run().check()
    }

    // find first class that is not defined in this file.
    private fun findCaller() = javaClass.classLoader.loadClass(findCallerName()).kotlin
}

private fun findCallerName(): String = findCallerSTE().className

internal fun findCallerSTE(): StackTraceElement = Throwable().stackTrace.first {
    !(it.fileName?.endsWith("FailGood.kt") ?: true)
}
