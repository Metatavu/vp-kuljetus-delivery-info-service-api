package fi.metatavu.coroutine

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Context
import io.vertx.core.Vertx
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine utilities
 */
object CoroutineUtils {

    /**
     * Executes a block with coroutine scope
     *
     * @param requestTimeOut request timeout in milliseconds. Default is 10000
     * @param block block to execute
     * @return Uni
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> withCoroutineScope(requestTimeOut: Long = 10000L, block: suspend () -> T): Uni<T> {
        val context = Vertx.currentContext()
        val dispatcher = VertxCoroutineDispatcher(context)

        return CoroutineScope(context = dispatcher)
            .async {
                withTimeout(requestTimeOut) {
                    block()
                }
            }
            .asUni()
    }

    /**
     * Custom vertx coroutine dispatcher that keeps the context stable during the execution
     */
    private class VertxCoroutineDispatcher(private val vertxContext: Context): CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            vertxContext.runOnContext {
                block.run()
            }
        }
    }
}