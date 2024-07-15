package fi.metatavu.coroutine

import io.quarkus.arc.Arc
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import org.hibernate.reactive.common.spi.Implementor
import org.hibernate.reactive.context.Context.Key
import org.hibernate.reactive.context.impl.BaseKey
import org.hibernate.reactive.mutiny.Mutiny
import org.hibernate.reactive.mutiny.Mutiny.Session
import org.jboss.logging.Logger

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
        val context = Vertx.currentContext()
        return CoroutineScope(context.dispatcher())
            .async {
                try {
                    withTimeout(requestTimeOut) {
                        var currentSession: Session? = null
                        if (session || transaction) {
                            if (transaction) {
                                log.info("Opening session with transaction")
                            } else {
                                log.info("Opening session for reading")
                            }

                            currentSession = openSession(context)
                        } else {
                            log.info("Running without session")
                        }

                        if (transaction) {
                            currentSession?.withTransaction {
                                CoroutineScope(context.dispatcher()).async {
                                    block()
                                }.asUni()
                            }?.awaitSuspending() ?: throw IllegalStateException("Using transaction without session open")
                        } else {
                            block()
                        }
                    }
                } finally {
                    if (session || transaction) {
                        closeSession(context)
                        log.info("Closed session. Current open session count: $openSessionCount")
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
            log.warn("Reusing existing session")
            return current
        }

        val session = sessionFactory.openSession().awaitSuspending()
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
            log.warn("Session already closed")
        }
    }

}