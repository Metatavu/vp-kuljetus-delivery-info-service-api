package fi.metatavu.coroutine

import io.quarkus.arc.Arc
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*
import org.hibernate.reactive.common.spi.Implementor
import org.hibernate.reactive.context.Context.Key
import org.hibernate.reactive.context.impl.BaseKey
import org.hibernate.reactive.mutiny.Mutiny
import org.hibernate.reactive.mutiny.Mutiny.Session
import org.jboss.logging.Logger
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine utilities
 */
object CoroutineUtils {

    private val log: Logger = Logger.getLogger(CoroutineUtils::class.java)

    private val sessionKey: Key<Session> by lazy {
        BaseKey(Session::class.java, (sessionFactory as Implementor).uuid)
    }

    private val sessionFactory: Mutiny.SessionFactory by lazy {
        val sessionFactory = Arc.container().instance(Mutiny.SessionFactory::class.java).get()
            ?: throw IllegalStateException("Mutiny.SessionFactory bean not found")
        sessionFactory
    }

    private val sessionFactoryStatistics
        get() = sessionFactory.statistics

    private val openSessionCount
        get() = sessionFactoryStatistics.sessionOpenCount - sessionFactoryStatistics.sessionCloseCount

    private var requestId: String?
        get() {
            return try {
                Vertx.currentContext()?.getLocal<String>("fi.metatavu.coroutine.requestId")
            } catch (e: Exception) {
                "null"
            }
        }
        set(value) = Vertx.currentContext()?.putLocal("fi.metatavu.coroutine.requestId", value)!!

    /**
     * Returns the current session
     *
     * @param context context to use. Default is Vertx.currentContext()
     * @return Current session
     */
    fun getCurrentSession(context: Context = Vertx.currentContext()): Session? {
        return context.getLocal(sessionKey)
    }

    /**
     * Logs current session details
     */
    fun logSessionDetails(context: Context = Vertx.currentContext()) {
        val session = getCurrentSession(context)
        if (session != null && session.isOpen) {
            logInfo("Session is not null and open")
        } else {
            if (session == null) {
                logInfo("Session is null")
            } else {
                logInfo("Session is closed")
            }
        }

        logInfo("Context: $context")
    }

    /**
     * Executes a block with coroutine scope
     *
     * @param session if true, opens a new session. Default is true
     * @param transaction if true, opens a new session with transaction. Default is false
     * @param requestTimeOut request timeout in milliseconds. Default is 10000
     * @param block block to execute
     * @return Uni
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> withCoroutineScope(session: Boolean = true, transaction: Boolean = false, requestTimeOut: Long = 10000L, block: suspend () -> T): Uni<T> {
        requestId = java.util.UUID.randomUUID().toString()

        val context = Vertx.currentContext()
        val dispatcher = VertxCoroutineDispatcher(context)

        return CoroutineScope(context = dispatcher)
            .async {
                try {
                    withTimeout(requestTimeOut) {
                        var currentSession: Session? = null
                        if (session || transaction) {
                            if (transaction) {
                                logInfo("Opening session with transaction")
                            } else {
                                logInfo("Opening session for reading")
                            }

                            currentSession = openSession(context)
                        } else {
                            logInfo("Running without session")
                        }

                        if (transaction) {
                            currentSession?.withTransaction {
                                CoroutineScope(context.dispatcher()).async {
                                    try {
                                        block()
                                    } catch (e: Exception) {
                                        logError("Transaction failed: ${e.message}")
                                        logSessionDetails(context)
                                        throw e
                                    }
                                }.asUni()
                            }?.awaitSuspending() ?: throw IllegalStateException("Using transaction without session open")
                        } else {
                            try {
                                block()
                            } catch (e: Exception) {
                                logError("Read-only session failed: ${e.message}")
                                logSessionDetails(context)
                                throw e
                            }
                        }
                    }
                } finally {
                    if (session || transaction) {
                        closeSession(context)
                        logInfo("Closed session. Current open session count: $openSessionCount")
                    }
                }
            }
            .asUni()
    }

    /**
     * Opens a new session
     */
    private suspend fun openSession(context: Context): Session {
        val key = sessionKey

        val current = context.getLocal<Session?>(key)
        if (current != null && current.isOpen) {
            logWarn("Reusing existing session")
            return current
        }

        val session = sessionFactory.openSession().awaitSuspending()

        if (session.isOpen) {
            logInfo("Opened new session. Current open session count: $openSessionCount")
        } else {
            logError("Failed to open session. Session is not open")
        }

        context.putLocal(key, session)

        return session
    }

    /**
     * Closes the current session
     */
    private suspend fun closeSession(context: Context) {
        val key = sessionKey
        val current = context.getLocal<Session>(key)
        if (current != null && current.isOpen) {
            current.close().awaitSuspending()
            context.removeLocal(key)
        } else {
            logWarn("Session already closed")
        }
    }

    /**
     * Logs an info message
     *
     * @param message message
     */
    private fun logInfo(message: String) = log.info("$requestId: $message")

    /**
     * Logs a warning message
     *
     * @param message message
     */
    private fun logWarn(message: String) = log.warn("$requestId: $message")

    /**
     * Logs an error message
     *
     * @param message message
     */
    private fun logError(message: String) = log.error("$requestId: $message")

    private class VertxCoroutineDispatcher(private val vertxContext: Context): CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            vertxContext.runOnContext {
                block.run()
            }
        }
    }
}