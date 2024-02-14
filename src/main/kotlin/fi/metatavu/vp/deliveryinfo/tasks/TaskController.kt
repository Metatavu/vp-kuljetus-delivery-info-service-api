package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.api.model.TaskType
import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.sites.Site
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
        routeId: UUID?,
        assignedToRoute: Boolean?,
        freight: Freight?,
        site: Site?,
        type: TaskType?,
        first: Int?,
        max: Int?
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
     * @param remarks remarks
     * @param routeId route id
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created task
     */
    suspend fun createTask(
        freight: Freight,
        site: Site,
        type: TaskType,
        remarks: String?,
        routeId: UUID?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Task {
        return taskRepository.create(
            id = UUID.randomUUID(),
            freight = freight,
            site = site,
            type = type,
            remarks = remarks,
            routeId = routeId,
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
        existingTask.freight = freight
        existingTask.site = site
        existingTask.taskType = restTask.type
        existingTask.remarks = restTask.remarks
        existingTask.routeId = restTask.routeId
        existingTask.lastModifierId = modifierId
        return taskRepository.persistSuspending(existingTask)
    }

    /**
     * Deletes a task
     *
     * @param foundTask found task
     */
    suspend fun deleteTask(foundTask: Task) {
        taskRepository.deleteSuspending(foundTask)
    }

}