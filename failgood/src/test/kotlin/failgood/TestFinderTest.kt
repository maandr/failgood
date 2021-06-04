package failgood

import failgood.docs.ClassTestContextTest
import failgood.docs.ObjectMultipleContextsTest
import failgood.docs.ObjectTestContextTest
import failgood.docs.TestContextExampleTest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import org.junit.platform.commons.annotation.Testable
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder

@Testable
class TestFinderTest {
    val context =
        describe("test finder") {
            it("can find Test classes") {
                expectThat(
                    FailGood.findTestClasses(classIncludeRegex = Regex(".*docs.*Test.class\$"))
                        .toCollection(mutableListOf())
                )
                    .containsExactlyInAnyOrder(
                        ClassTestContextTest::class,
                        ObjectTestContextTest::class,
                        TestFinderTest::class.java.classLoader.loadClass("failgood.docs.TestContextOnTopLevelTest").kotlin,
                        ObjectMultipleContextsTest::class,
                        TestContextExampleTest::class
                    )
            }
            it("runs async") {
                val flow = FailGood.findTestClasses(classIncludeRegex = Regex(".*docs.*Test.class\$"))
                val started = System.currentTimeMillis()
                var firstItemTime: Long? = null
                var lastItemTime: Long = 0
                var firstItemTime2: Long? = null
                var lastItemTime2: Long = 0
                flow.map {
                    if (firstItemTime == null)
                        firstItemTime = System.currentTimeMillis()
                    lastItemTime = System.currentTimeMillis()
                    ObjectContextProvider(it).getContexts()
                }.map {
                    if (firstItemTime2 == null)
                        firstItemTime2 = System.currentTimeMillis()
                    lastItemTime2 = System.currentTimeMillis()
                }.toList(mutableListOf())
                println(
                    "firstItem: ${firstItemTime!! - started} lastItem: ${lastItemTime - started} " +
                            "firstItem2: ${firstItemTime2!! - started} lastItem2: ${lastItemTime2 - started} " +
                            "end: ${System.currentTimeMillis() - started}"
                )
            }
        }
}
