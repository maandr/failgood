package failgood.experiments

import failgood.uptime
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

fun main() {
    asyncTest()
}

private fun asyncTest() {
    val threadPool = Executors.newWorkStealingPool(1000)
    threadPool.asCoroutineDispatcher()
        .use { dispatcher ->
            runBlocking(dispatcher) {
                (0 until 1000)
                    .map {
                        async {
                            @Suppress("BlockingMethodInNonBlockingContext")
                            Thread.sleep(1000)
                        }
                    }
                    .awaitAll()
                println(uptime() + " " + threadPool)
            }
        }
}
