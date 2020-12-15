package nanotest

import strikt.api.expectThat
import strikt.assertions.isEqualTo

object ContextTest {
    private val automaticallyNamedContext = Context {}
    val context = Context {
        test("root context can get name from enclosing object") {
            expectThat(automaticallyNamedContext.name).isEqualTo(ContextTest::class.simpleName)
        }
    }
}