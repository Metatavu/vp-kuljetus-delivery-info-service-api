package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.api.model.Task
import fi.metatavu.vp.api.model.TaskStatus
import fi.metatavu.vp.api.model.TaskType
import fi.metatavu.vp.api.spec.TasksApi
import fi.metatavu.vp.deliveryinfo.freights.FreightController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.sites.SiteController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RequestScoped
@WithSession
class TasksApiImpl : TasksApi, AbstractApi() {

    @Inject
    lateinit var taskController: TaskController

    @Inject
    lateinit var freightController: FreightController

    @Inject
    lateinit var siteController: SiteController

    @Inject
    lateinit var taskTranslator: TaskTranslator

    @Inject
    lateinit var vertx: Vertx

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listTasks(
        routeId: UUID?,
        assignedToRoute: Boolean?,
        freightId: UUID?,
        customerSiteId: UUID?,
        type: TaskType?,
        first: Int?,
        max: Int?
    ): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val freightFilter = freightId?.let {
            freightController.findFreight(it) ?: return@async createNotFound(createNotFoundMessage(FREIGHT, it))
        }

        val siteFilter = customerSiteId?.let {
            siteController.findSite(it) ?: return@async createNotFound(createNotFoundMessage(SITE, it))
        }

        val (tasks, count) = taskController.listTasks(
            routeId = routeId,
            assignedToRoute = assignedToRoute,
            freight = freightFilter,
            site = siteFilter,
            type = type,
            first = first,
            max = max
        )
        createOk(tasks.map { taskTranslator.translate(it) }, count)
    }.asUni()

    @WithTransaction
    @RolesAllowed(MANAGER_ROLE)
    override fun createTask(task: Task): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        val freight = task.freightId.let {
            val foundFreight =
                freightController.findFreight(it) ?: return@async createBadRequest(createNotFoundMessage(FREIGHT, it))
            foundFreight
        }

        val site = task.customerSiteId.let {
            val foundSite = siteController.findSite(it) ?: return@async createBadRequest(createNotFoundMessage(SITE, it))
            foundSite
        }

        if (task.routeId != null && !taskController.routeExists(task.routeId)) {
            return@async createBadRequest("Route does not exist")
        }
        verifyRouteIdOrderNumberCombination(task)?.let { return@async it }

        val createdTask = taskController.createTask(
            freight = freight,
            site = site,
            type = task.type,
            status = task.status,
            groupNumber = task.groupNumber,
            remarks = task.remarks,
            routeId = task.routeId,
            orderNumber = task.orderNumber,
            creatorId = userId,
            lastModifierId = userId
        )

        createOk(taskTranslator.translate(createdTask))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findTask(taskId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val task = taskController.findTask(taskId) ?: return@async createNotFound(createNotFoundMessage(TASK, taskId))
        createOk(taskTranslator.translate(task))
    }.asUni()

    @WithTransaction
    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun updateTask(taskId: UUID, task: Task): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        val foundTask = taskController.findTask(taskId) ?: return@async createNotFound(createNotFoundMessage(TASK, taskId))

        val freight = task.freightId.let {
            if (foundTask.freight.id == it) {
                return@let foundTask.freight
            }
            freightController.findFreight(it) ?: return@async createBadRequest(createNotFoundMessage(FREIGHT, it))
        }

        val site = task.customerSiteId.let {
            if (foundTask.site.id == it) {
                return@let foundTask.site
            }
            siteController.findSite(it) ?: return@async createBadRequest(createNotFoundMessage(SITE, it))
        }

        if (task.routeId != null
            && foundTask.routeId != task.routeId
            && !taskController.routeExists(task.routeId)
        ) {
            return@async createBadRequest("Bad request")
        }

        verifyRouteIdOrderNumberCombination(task)?.let { return@async it }

        val updated = taskController.update(
            existingTask = foundTask,
            freight = freight,
            site = site,
            restTask = task,
            modifierId = userId
        )

        createOk(taskTranslator.translate(updated))
    }.asUni()

    @WithTransaction
    @RolesAllowed(MANAGER_ROLE)
    override fun deleteTask(taskId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        val foundTask = taskController.findTask(taskId) ?: return@async createNotFound(createNotFoundMessage(TASK, taskId))
        if (foundTask.status == TaskStatus.DONE) {
            return@async createConflict("Done task cannot be deleted")
        }
        taskController.deleteTask(foundTask)
        createNoContent()
    }.asUni()

    /**
     * Verifies that routeId and orderNumber are either both null or both set
     *
     * @param task task
     * @return response if invalid, null if valid
     */
    private suspend fun verifyRouteIdOrderNumberCombination(task: Task): Response? {
        if ((task.routeId == null && task.orderNumber != null) || (task.routeId != null && task.orderNumber == null)) {
            return createBadRequest("Either both routeId and orderNumber must be null or both must be set")
        }

        if (task.orderNumber != null && task.orderNumber < 0){
            return createBadRequest("Order number must be positive")
        }

        return null
    }
}