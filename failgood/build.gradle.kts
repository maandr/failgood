import failgood.versions.*
import info.solidsoft.gradle.pitest.PitestPluginExtension

plugins {
    kotlin("jvm")
    `maven-publish`
    id("info.solidsoft.pitest")
    signing
    id("failgood.common")
    id("failgood.publishing")
    id("com.bnorm.power.kotlin-power-assert").version("0.11.0")
}
// to release:
// ./gradlew publishToSonatype closeSonatypeStagingRepository (or ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository)

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    api("org.junit.platform:junit-platform-commons:$junitPlatformVersion")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutinesVersion")
    // to enable running test in idea without having to add the dependency manually
    api("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    compileOnly("org.junit.platform:junit-platform-engine:$junitPlatformVersion")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("org.pitest:pitest:$pitestVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    testImplementation("io.strikt:strikt-core:$striktVersion")
    testImplementation("org.pitest:pitest:$pitestVersion")
    testImplementation("org.junit.platform:junit-platform-engine:$junitPlatformVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testImplementation(kotlin("test"))
}

val testMain =
    task("testMain", JavaExec::class) {
        mainClass.set("failgood.FailGoodBootstrapKt")
        classpath = sourceSets["test"].runtimeClasspath
    }
val multiThreadedTest =
    task("multiThreadedTest", JavaExec::class) {
        mainClass.set("failgood.MultiThreadingPerformanceTestXKt")
        classpath = sourceSets["test"].runtimeClasspath
        systemProperties = mapOf("kotlinx.coroutines.scheduler.core.pool.size" to "1000")
    }
task("autotest", JavaExec::class) {
    mainClass.set("failgood.AutoTestMainKt")
    classpath = sourceSets["test"].runtimeClasspath
}

tasks.check { dependsOn(testMain, multiThreadedTest) }

plugins.withId("info.solidsoft.pitest") {
    configure<PitestPluginExtension> {
        //        verbose.set(true)
        jvmArgs.set(listOf("-Xmx512m")) // necessary on CI
        testPlugin.set("failgood")
        avoidCallsTo.set(setOf("kotlin.jvm.internal", "kotlin.Result"))
        targetClasses.set(setOf("failgood.*")) // by default "${project.group}.*"
        targetTests.set(setOf("failgood.*Test", "failgood.**.*Test"))
        pitestVersion.set(failgood.versions.pitestVersion)
        threads.set(
            System.getenv("PITEST_THREADS")?.toInt() ?: Runtime.getRuntime().availableProcessors()
        )
        outputFormats.set(setOf("XML", "HTML"))
    }
}

configure<com.bnorm.power.PowerAssertGradleExtension> {
    functions = listOf("kotlin.assert", "kotlin.test.assertTrue", "kotlin.test.assertNotNull")
}
