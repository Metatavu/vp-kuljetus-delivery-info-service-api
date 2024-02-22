package fi.metatavu.vp.deliveryinfo.tasks

import fi.metatavu.vp.api.model.TaskStatus
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
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created task
     */
    suspend fun createTask(
        freight: Freight,
        site: Site,
        type: TaskType,
        status: TaskStatus,
        groupNumber: Int,
        remarks: String?,
        routeId: UUID?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Task {
        val startedAt = if (status == TaskStatus.IN_PROGRESS) {
            java.time.OffsetDateTime.now()
        } else null

        val finishedAt = if (status == TaskStatus.DONE) {
            java.time.OffsetDateTime.now()
        } else null

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
        if (existingTask.status == TaskStatus.TODO && restTask.status == TaskStatus.IN_PROGRESS) {
            existingTask.startedAt = java.time.OffsetDateTime.now()
        }
        if (existingTask.status == TaskStatus.IN_PROGRESS && restTask.status == TaskStatus.DONE) {
            existingTask.finishedAt = java.time.OffsetDateTime.now()
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
     * Deletes a task
     *
     * @param foundTask found task
     */
    suspend fun deleteTask(foundTask: Task) {
        taskRepository.deleteSuspending(foundTask)
    }

}