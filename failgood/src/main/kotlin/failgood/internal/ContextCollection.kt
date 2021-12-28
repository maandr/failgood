package failgood.internal

import failgood.ContextProvider
import failgood.ExecutionListener
import failgood.RootContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

internal class ContextCollection(private val contextProviders: Collection<ContextProvider>) {
    suspend fun investigate(
        coroutineScope: CoroutineScope,
        executionFilter: TestFilterProvider,
        executeTests: Boolean,
        listener: ExecutionListener,
        timeoutMillis: Long
    ): List<FoundContext> = contextProviders
        .map { coroutineScope.async { it.getContexts() } }.flatMap { it.await() }.sortedBy { it.order }
        .map { context: RootContext ->
            val testFilter = executionFilter.forClass(context.sourceInfo.className)
            FoundContext(
                context,
                coroutineScope.async {
                    if (!context.disabled) {
                        ContextExecutor(
                            context,
                            coroutineScope,
                            !executeTests,
                            listener,
                            testFilter,
                            timeoutMillis
                        ).execute()
                    } else
                        ContextInfo(emptyList(), mapOf(), setOf())
                }
            )
        }

    data class FoundContext(val context: RootContext, val result: Deferred<ContextResult>)
}
