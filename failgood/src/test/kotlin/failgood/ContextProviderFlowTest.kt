package failgood

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.toCollection
import org.junit.platform.commons.annotation.Testable
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.util.concurrent.CopyOnWriteArrayList

@Testable
class ContextProviderFlowTest {
    val context = describe("Context providing implemented via flow") {
        pending("works") {
            val events = CopyOnWriteArrayList<String>()
            coroutineScope {
                val flow = flow {
                    events.add("flow")
                    emit(1)
                    emit(2)
                }
                val sharedFlow = flow.shareIn(this, SharingStarted.Eagerly)

                expectThat(sharedFlow.toCollection(mutableListOf())).containsExactly(1, 2)
                expectThat(sharedFlow.toCollection(mutableListOf())).containsExactly(1, 2)
                expectThat(events).containsExactly("flow")
            }
        }

    }
}
