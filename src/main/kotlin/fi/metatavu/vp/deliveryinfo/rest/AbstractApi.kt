package fi.metatavu.vp.deliveryinfo.rest

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Context
import io.vertx.core.Vertx
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.*
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.jwt.JsonWebToken
import java.lang.Runnable
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Abstract base class for all API services
 *
 * @author Jari Nykänen
 */
abstract class AbstractApi {

    @ConfigProperty(name = "vp.env")
    private lateinit var environment: String

    @ConfigProperty(name = "vp.deliveryinfo.terminaldevice.apiKey")
    lateinit var terminalDeviceApiKeyValue: String

    @Inject
    private lateinit var jsonWebToken: JsonWebToken

    @jakarta.ws.rs.core.Context
    lateinit var headers: HttpHeaders

    /**
     * Returns if production environment
     */
    protected val isProduction: Boolean
        get() = environment == "PRODUCTION"

    /**
     * Returns logged user id
     *
     * @return logged user id
     */
    protected val loggedUserId: UUID?
        get() {
            if (jsonWebToken.subject != null) {
                return UUID.fromString(jsonWebToken.subject)
            }

            return null
        }

    /**
     * Returns request terminal device api key
     *
     * @return request terminal device api key
     */
    protected val requestTerminalDeviceKey: String?
        get() {
            return headers.getHeaderString("X-DataReceiver-API-Key")
        }

    /**
     * Constructs ok response
     *
     * @param entity payload
     * @param count total count
     * @return response
     */
    protected fun createOk(entity: Any?, count: Long): Response {
        return Response
            .status(Response.Status.OK)
            .header("X-Total-Count", count.toString())
            .header("Access-Control-Expose-Headers", "X-Total-Count")
            .entity(entity)
            .build()
    }

    /**
     * Constructs ok response
     *
     * @param entity payload
     * @return response
     */
    protected fun createOk(entity: Any?): Response {
        return Response
            .status(Response.Status.OK)
            .entity(entity)
            .build()
    }

    /**
     * Constructs ok response
     *
     * @return response
     */
    protected fun createOk(): Response {
        return Response
            .status(Response.Status.OK)
            .build()
    }

    /**
     * Constructs no content response
     *
     * @param entity payload
     * @return response
     */
    protected fun createAccepted(entity: Any?): Response {
        return Response
            .status(Response.Status.ACCEPTED)
            .entity(entity)
            .build()
    }

    /**
     * Constructs no content response
     *
     * @return response
     */
    protected fun createNoContent(): Response {
        return Response
            .status(Response.Status.NO_CONTENT)
            .build()
    }

    /**
     * Constructs bad request response
     *
     * @param message message
     * @return response
     */
    protected fun createBadRequest(message: String): Response {
        return createError(Response.Status.BAD_REQUEST, message)
    }

    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createNotFound(message: String): Response {
        return createError(Response.Status.NOT_FOUND, message)
    }

    /**
     * Constructs not found response
     *
     * @return response
     */
    protected fun createNotFound(): Response {
        return Response
            .status(Response.Status.NOT_FOUND)
            .build()
    }
    /**
     * Constructs not found response
     *
     * @param message message
     * @return response
     */
    protected fun createConflict(message: String): Response {
        return createError(Response.Status.CONFLICT, message)
    }

    /**
     * Constructs not implemented response
     *
     * @param message message
     * @return response
     */
    protected fun createNotImplemented(message: String): Response {
        return createError(Response.Status.NOT_IMPLEMENTED, message)
    }

    /**
     * Constructs internal server error response
     *
     * @param message message
     * @return response
     */
    protected fun createInternalServerError(message: String): Response {
        return createError(Response.Status.INTERNAL_SERVER_ERROR, message)
    }

    /**
     * Constructs forbidden response
     *
     * @param message message
     * @return response
     */
    protected fun createForbidden(message: String): Response {
        return createError(Response.Status.FORBIDDEN, message)
    }

    /**
     * Constructs unauthorized response
     *
     * @param message message
     * @return response
     */
    protected fun createUnauthorized(message: String): Response {
        return createError(Response.Status.UNAUTHORIZED, message)
    }

    /**
     * Constructs an error response
     *
     * @param status status code
     * @param message message
     *
     * @return error response
     */
    private fun createError(status: Response.Status, message: String): Response {
        val entity = fi.metatavu.vp.api.model.Error(
            message = message,
            status = status.statusCode
        )

        return Response
            .status(status)
            .entity(entity)
            .build()
    }

    fun createNotFoundMessage(entity: String, id: UUID): String {
        return "$entity with id $id not found"
    }

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

    companion object {
        const val NOT_FOUND_MESSAGE = "Not found"
        const val UNAUTHORIZED = "Unauthorized"
        const val FORBIDDEN = "Forbidden"
        const val MISSING_REQUEST_BODY = "Missing request body"
        const val INVALID_REQUEST_BODY = "Invalid request body"

        const val INVALID_API_KEY = "Invalid API key"

        const val SITE = "Site"
        const val FREIGHT = "Freight"
        const val FREIGHT_UNIT = "Freight Unit"
        const val TASK = "Task"

        const val DRIVER_ROLE = "driver"
        const val MANAGER_ROLE = "manager"
    }

}
