package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.api.model.TaskStatus
import fi.metatavu.vp.api.model.TaskType
import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.messaging.GlobalEventController
import fi.metatavu.vp.messaging.events.TaskGlobalEvent
import fi.metatavu.vp.workplanning.spec.RoutesApi
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.util.*

/**
 * Controller for tasks
 */
@ApplicationScoped
class TaskController {

    @Inject
    lateinit var taskRepository: TaskRepository

    @RestClient
    lateinit var routesApi: RoutesApi

    @Inject
    lateinit var logger: org.jboss.logging.Logger

    @Inject
    lateinit var globalEventController: GlobalEventController

    /**
     * Checks if route exists
     *
     * @param routeId route id
     * @return true if route exists, false otherwise
     */
    suspend fun routeExists(routeId: UUID): Boolean {
        return try {
            routesApi.findRoute(routeId).awaitSuspending().status == 200
        } catch (e: Exception) {
            logger.error("Error while searching for route $routeId", e)
            false
        }
    }

    /**
     * Lists tasks
     *
     * @param routeId route id
     * @param assignedToRoute assigned to route
     * @param freight freight
     * @param site site
     * @param type type
     * @param first first result
     * @param max max results
     * @return pair of list of tasks and total count
     */
    suspend fun listTasks(
        routeId: UUID? = null,
        assignedToRoute: Boolean? = null,
        freight: Freight? = null,
        site: Site? = null,
        type: TaskType? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<Task>, Long> {
        return taskRepository.list(
            routeId = routeId,
            assignedToRoute = assignedToRoute,
            freight = freight,
            site = site,
            type = type,
            first = first,
            max = max
        )
    }

    /**
     * Creates a new task
     *
     * @param freight freight
     * @param site site
     * @param type type
     * @param status status
     * @param groupNumber group number
     * @param remarks remarks
     * @param routeId route id
     * @param orderNumber order number
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created task
     * @throws IllegalArgumentException if new order number is higher than max allowed order number in route
     */
    suspend fun createTask(
        freight: Freight,
        site: Site,
        type: TaskType,
        status: TaskStatus,
        groupNumber: Int,
        remarks: String?,
        routeId: UUID?,
        orderNumber: Int?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Task {
        val startedAt = if (status == TaskStatus.IN_PROGRESS) {
            java.time.OffsetDateTime.now()
        } else null

        val finishedAt = if (status == TaskStatus.DONE) {
            java.time.OffsetDateTime.now()
        } else null

        // If we assign it to the route then manage order numbers of other tasks in the route
        if (routeId != null && orderNumber != null) {
            updateOrderNumbers(null, orderNumber, routeId)
        }

        return taskRepository.create(
            id = UUID.randomUUID(),
            freight = freight,
            site = site,
            type = type,
            status = status,
            groupNumber = groupNumber,
            remarks = remarks,
            routeId = routeId,
            startedAt = startedAt,
            finishedAt = finishedAt,
            orderNumber = orderNumber,
            creatorId = creatorId,
            lastModifierId = lastModifierId
        )
    }

    /**
     * Finds a task
     *
     * @param taskId task id
     * @return found task or null if not found
     */
    suspend fun findTask(taskId: UUID): Task? {
        return taskRepository.findByIdSuspending(taskId)
    }

    /**
     * Updates a task
     *
     * @param existingTask existing task
     * @param freight freight
     * @param site site
     * @param restTask task rest data
     * @param modifierId modifier id
     * @return updated task
     */
    suspend fun update(
        existingTask: Task,
        freight: Freight,
        site: Site,
        restTask: fi.metatavu.vp.api.model.Task,
        modifierId: UUID
    ): Task {
        globalEventController.publish(
            TaskGlobalEvent(
                userId = modifierId,
                taskType = restTask.type,
                taskStatus = restTask.status,
            )
        )

        if (existingTask.status == TaskStatus.TODO && restTask.status == TaskStatus.IN_PROGRESS) {
            existingTask.startedAt = java.time.OffsetDateTime.now()
        }
        if (existingTask.status == TaskStatus.IN_PROGRESS && restTask.status == TaskStatus.DONE) {
            existingTask.finishedAt = java.time.OffsetDateTime.now()
        }

        if (restTask.routeId == null && restTask.orderNumber == null && existingTask.routeId != null) {
            // task was removed from route, update order numbers of the other tasks of the route
            existingTask.orderNumber = null
            reorderTasksInRoute(existingTask.routeId!!, existingTask)
        } else if (restTask.routeId != existingTask.routeId && existingTask.routeId != null) {
            // route changed, update order numbers in the new and old routes
            val newNumber = updateOrderNumbers(null, restTask.orderNumber!!, restTask.routeId!!)
            existingTask.orderNumber = newNumber
            reorderTasksInRoute(existingTask.routeId!!, existingTask)
        } else if (restTask.orderNumber != existingTask.orderNumber ) {
            // order number changed, update order numbers in the route
            existingTask.orderNumber = updateOrderNumbers(existingTask.orderNumber, restTask.orderNumber!!, restTask.routeId!!)
        }

        existingTask.freight = freight
        existingTask.site = site
        existingTask.taskType = restTask.type
        existingTask.status = restTask.status
        existingTask.groupNumber = restTask.groupNumber
        existingTask.remarks = restTask.remarks
        existingTask.routeId = restTask.routeId
        existingTask.lastModifierId = modifierId
        return taskRepository.persistSuspending(existingTask)
    }

    /**
     * Updates order numbers of tasks in route
     *
     * @param currentOrderNumber current order number
     * @param newOrderNumber new order number
     * @param routeId route id
     */
    private suspend fun updateOrderNumbers(currentOrderNumber: Int?, newOrderNumber: Int, routeId: UUID): Int {
        val allTasksInRoute = taskRepository.list(routeId = routeId).first
        val tasksSize = allTasksInRoute.size
        val newSelectedOrderNumber = if (currentOrderNumber == null && newOrderNumber > tasksSize) {
            tasksSize
        } else if (currentOrderNumber != null && newOrderNumber >= tasksSize) {
            tasksSize - 1
        } else newOrderNumber

        // Re-arrange the order numbers of the tasks in the route based on the new order number
        val updatableTasks = if (currentOrderNumber == null) {
            allTasksInRoute.filter { it.orderNumber in newSelectedOrderNumber until allTasksInRoute.size }
                .map { it.orderNumber = it.orderNumber!! + 1; it }
        } else {
            if (currentOrderNumber < newSelectedOrderNumber) {
                allTasksInRoute.filter { it.orderNumber in (currentOrderNumber + 1)..newSelectedOrderNumber }
                    .map { it.orderNumber = it.orderNumber!! - 1; it }
            } else if (currentOrderNumber > newSelectedOrderNumber) {
                allTasksInRoute.filter { it.orderNumber in newSelectedOrderNumber until currentOrderNumber }
                    .map { it.orderNumber = it.orderNumber!! + 1; it }
            }
            else emptyList()
        }

        updatableTasks.forEach { taskRepository.persistSuspending(it) }
        return newSelectedOrderNumber
    }

    /**
     * Deletes a task, also updates the order numbers of other tasks that belong to the same route
     *
     * @param foundTask found task
     */
    suspend fun deleteTask(foundTask: Task) {
        taskRepository.deleteSuspending(foundTask)

        if (foundTask.orderNumber != null && foundTask.routeId != null) {
            reorderTasksInRoute(foundTask.routeId!!, foundTask)
        }
    }

    /**
     * Reorders the tasks in the route based on their index
     *
     * @param routeId route id
     * @param taskToExclude task to exclude from the reordering
     */
    private suspend fun reorderTasksInRoute(routeId: UUID, taskToExclude: Task) {
        val allTasksInRoute = taskRepository.list(routeId = routeId).first.filter { it.id != taskToExclude.id}
        allTasksInRoute.sortedBy { it.orderNumber }.forEachIndexed { index, task ->
            if (task.orderNumber != null && task.orderNumber!! > index) {
                task.orderNumber = index
                taskRepository.persistSuspending(task)
            }
        }
    }

}