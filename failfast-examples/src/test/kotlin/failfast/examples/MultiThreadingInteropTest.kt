package failfast.examples

import failfast.FailFast
import failfast.describe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import strikt.api.expectThat
import strikt.assertions.isEqualTo

fun main() {
    FailFast.runTest()
}

class StringProvider {
    suspend fun world() = "world"
}

class Example(
    private val stringProvider: StringProvider,
) {

    suspend fun hello(): String {
        delay(250)
        return stringProvider.world()
    }
}

object MultiThreadingInteropTest {
    val context = describe("multi threading issue from spek") {
        val stringProvider = mockk<StringProvider> {
            coEvery { world() } returns "world 2"
        }
        val example = Example(stringProvider)

        describe("ExampleTest") {

            it("should work ;)") {
                expectThat(example.hello()).isEqualTo("world 2")

                coVerify {
                    stringProvider.world()
                }
            }
        }

    }
}

