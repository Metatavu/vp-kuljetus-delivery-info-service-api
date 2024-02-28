package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.TasksApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Task
import fi.metatavu.vp.test.client.models.TaskType
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Tasks API
 */
class TaskTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Task, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: Task) {
        api.deleteTask(t.id!!)
    }

    override fun getApi(): TasksApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TasksApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new Task
     *
     * @param freightId freight id
     * @param customerSiteId customer site id
     * @param routeId route id
     * @return created Task
     */
    fun create(
        freightId: UUID,
        customerSiteId: UUID,
        routeId: UUID? = null,
        orderNumber: Int? = null
    ): Task {
        return create(
            Task(
                freightId = freightId,
                customerSiteId = customerSiteId,
                type = TaskType.LOAD,
                remarks = "remarks",
                routeId = routeId,
                orderNumber = orderNumber,
                status = fi.metatavu.vp.test.client.models.TaskStatus.TODO,
                groupNumber = 1
            )
        )
    }

    /**
     * Creates new Task
     *
     * @param task Task data
     * @return created Task
     */
    fun create(task: Task): Task {
        return addClosable(api.createTask(task))
    }

    /**
     * Asserts that Task creation fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertCreateFail(expectedStatus: Int, task: Task) {
        try {
            create(task)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds Task
     *
     * @param id id
     * @return found Task
     */
    fun findTask(id: UUID): Task {
        return api.findTask(id)
    }

    /**
     * Asserts that Task find fails with expected status
     *
     * @param id id
     * @param expectedStatus expected status
     */
    fun assertFindTaskFail(id: UUID, expectedStatus: Int) {
        try {
            findTask(id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates Task
     *
     * @param id Task id
     * @param task Task to update
     * @return updated Task
     */
    fun updateTask(id: UUID, task: Task): Task {
        return api.updateTask(id, task)
    }

    /**
     * Asserts that Task update fails with expected status
     *
     * @param id task id
     * @param expectedStatus expected status
     */
    fun assertUpdateTaskFail(expectedStatus: Int, id: UUID, taskData: Task) {
        try {
            updateTask(id, taskData)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists tasks
     *
     * @param first first result
     * @param max max results
     * @return list of tasks
     */
    fun listTasks(
        routeId: UUID? = null,
        assignedToRoute: Boolean? = null,
        freightId: UUID? = null,
        customerSiteId: UUID? = null,
        type: TaskType? = null,
        first: Int? = null,
        max: Int? = null
    ): Array<Task> {
        return api.listTasks(
            routeId = routeId,
            assignedToRoute = assignedToRoute,
            freightId = freightId,
            customerSiteId = customerSiteId,
            type = type,
            first = first,
            max = max
        )
    }

    /**
     * Asserts that task listing fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListTasksFail(expectedStatus: Int) {
        try {
            listTasks()
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes task
     *
     * @param taskId task id
     */
    fun deleteTask(taskId: UUID) {
        api.deleteTask(taskId)
        removeCloseable { closable: Any ->
            if (closable !is Task) {
                return@removeCloseable false
            }

            closable.id == taskId
        }
    }

    /**
     * Asserts that task deletion fails with expected status
     *
     * @param taskId task id
     * @param expectedStatus expected status
     */
    fun assertDeleteTaskFail(expectedStatus: Int, taskId: UUID) {
        try {
            deleteTask(taskId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that task is found
     *
     * @param expected expected
     * @param actual actual
     */
    fun assertFindFail(expectedStatus: Int, taskId: UUID) {
        try {
            findTask(taskId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}