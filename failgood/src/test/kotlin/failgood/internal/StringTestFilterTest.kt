package failgood.internal

import failgood.Test
import failgood.describe
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@Test
class StringTestFilterTest {
    val context = describe(StringTestFilter::class) {
        val f = StringTestFilter(listOf("path", "to", "context"))
        it("executes a path that leads to a context") {
            expectThat(f.shouldRun(ContextPath.fromList("path", "to"))).isTrue()
        }
        it("executes a parent of the context") {
            expectThat(f.shouldRun(ContextPath.fromList("path", "to", "context", "test"))).isTrue()
        }
        it("does not execute a different path") {
            expectThat(f.shouldRun(ContextPath.fromList("path", "to", "some", "other", "context"))).isFalse()
        }
    }
}