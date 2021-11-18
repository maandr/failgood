package failgood

import failgood.TestLifecycleTest.Event.CONTEXT_1_EXECUTED
import failgood.TestLifecycleTest.Event.CONTEXT_2_EXECUTED
import failgood.TestLifecycleTest.Event.DEPENDENCY_CLOSED
import failgood.TestLifecycleTest.Event.ROOT_CONTEXT_EXECUTED
import failgood.TestLifecycleTest.Event.TEST_1_EXECUTED
import failgood.TestLifecycleTest.Event.TEST_2_EXECUTED
import failgood.TestLifecycleTest.Event.TEST_3_EXECUTED
import failgood.TestLifecycleTest.Event.TEST_4_EXECUTED
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.single
import java.util.concurrent.CopyOnWriteArrayList

@Test
class TestLifecycleTest {
    private enum class Event {
        ROOT_CONTEXT_EXECUTED,
        DEPENDENCY_CLOSED,
        TEST_1_EXECUTED,
        TEST_2_EXECUTED,
        CONTEXT_1_EXECUTED,
        CONTEXT_2_EXECUTED,
        TEST_3_EXECUTED,
        TEST_4_EXECUTED
    }

    val context = describe("the test lifecycle") {
        val totalEvents = CopyOnWriteArrayList<List<Event>>()
        describe("in the default isolation mode (full isolation)") {
            it("test dependencies are recreated for each test") {
                // tests run in parallel, so the total order of events is not defined.
                // we track events in a list of lists and record the events that lead to each test
                Suite {
                    val testEvents = mutableListOf<Event>()
                    totalEvents.add(testEvents)
                    testEvents.add(ROOT_CONTEXT_EXECUTED)
                    autoClose("dependency", closeFunction = { testEvents.add(DEPENDENCY_CLOSED) })
                    test("test 1") { testEvents.add(TEST_1_EXECUTED) }
                    test("test 2") { testEvents.add(TEST_2_EXECUTED) }
                    context("context 1") {
                        testEvents.add(CONTEXT_1_EXECUTED)

                        context("context 2") {
                            testEvents.add(CONTEXT_2_EXECUTED)
                            test("test 3") { testEvents.add(TEST_3_EXECUTED) }
                        }
                    }
                    test("test4: tests can be defined after contexts") {
                        testEvents.add(TEST_4_EXECUTED)
                    }
                }.run(silent = true)

                expectThat(totalEvents)
                    .containsExactlyInAnyOrder(
                        listOf(ROOT_CONTEXT_EXECUTED, TEST_1_EXECUTED, DEPENDENCY_CLOSED),
                        listOf(ROOT_CONTEXT_EXECUTED, TEST_2_EXECUTED, DEPENDENCY_CLOSED),
                        listOf(
                            ROOT_CONTEXT_EXECUTED,
                            CONTEXT_1_EXECUTED,
                            CONTEXT_2_EXECUTED,
                            TEST_3_EXECUTED,
                            DEPENDENCY_CLOSED
                        ),
                        listOf(ROOT_CONTEXT_EXECUTED, TEST_4_EXECUTED, DEPENDENCY_CLOSED)
                    )
            }
        }
        describe("when a root context does not need isolation") {
            pending("test run without recreating the dependencies") {
                // tests run in parallel, so the total order of events is not defined.
                // we track events in a list of lists and record the events that lead to each test
                Suite(
                    describe("root context without isolation", isolation = false) {
                        val testEvents = mutableListOf<Event>()
                        totalEvents.add(testEvents)
                        testEvents.add(ROOT_CONTEXT_EXECUTED)
                        autoClose("dependency", closeFunction = { testEvents.add(DEPENDENCY_CLOSED) })
                        test("test 1") { testEvents.add(TEST_1_EXECUTED) }
                        test("test 2") { testEvents.add(TEST_2_EXECUTED) }
                        context("context 1") {
                            testEvents.add(CONTEXT_1_EXECUTED)

                            context("context 2") {
                                testEvents.add(CONTEXT_2_EXECUTED)
                                test("test 3") { testEvents.add(TEST_3_EXECUTED) }
                            }
                        }
                        test("test4: tests can be defined after contexts") {
                            testEvents.add(TEST_4_EXECUTED)
                        }
                    }
                ).run(silent = true)

                expectThat(totalEvents).single().containsExactly(
                    ROOT_CONTEXT_EXECUTED,
                    TEST_1_EXECUTED,
                    TEST_2_EXECUTED,
                    CONTEXT_1_EXECUTED,
                    CONTEXT_2_EXECUTED,
                    TEST_3_EXECUTED,
                    TEST_4_EXECUTED,
                    DEPENDENCY_CLOSED
                )
            }
        }
    }
}
