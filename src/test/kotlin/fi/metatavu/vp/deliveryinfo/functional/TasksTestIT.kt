package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.deliveryinfo.functional.impl.InvalidTestValues
import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock
import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock.Companion.routeId
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.Task
import fi.metatavu.vp.test.client.models.TaskStatus
import fi.metatavu.vp.test.client.models.TaskType
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for Tasks API
 */
@QuarkusTest
@QuarkusTestResource(WorkPlanningMock::class)
@TestProfile(DefaultTestProfile::class)
class TasksTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val freight2 = it.manager.freights.create(site1, site1)
        val routeId = WorkPlanningMock.routeId
        it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = null
        )
        it.manager.tasks.create(
            customerSiteId = site1.id,
            freightId = freight2.id!!,
            routeId = routeId
        )

        val totalList = it.manager.tasks.listTasks()
        assertEquals(2, totalList.size)

        val bytRoute = it.manager.tasks.listTasks(routeId = routeId)
        assertEquals(1, bytRoute.size)

        val byAssignedToRoute = it.manager.tasks.listTasks(assignedToRoute = true)
        assertEquals(1, byAssignedToRoute.size)

        val byAssignedToRoute2 = it.manager.tasks.listTasks(assignedToRoute = false)
        assertEquals(1, byAssignedToRoute2.size)

        val byFreight = it.manager.tasks.listTasks(freightId = freight1.id)
        assertEquals(1, byFreight.size)

        val bySite = it.manager.tasks.listTasks(customerSiteId = site1.id)
        assertEquals(2, bySite.size)

        val byType = it.manager.tasks.listTasks(type = TaskType.LOAD)
        assertEquals(2, byType.size)

        val paging = it.manager.tasks.listTasks(first = 1, max = 1)
        assertEquals(1, paging.size)
    }

    @Test
    fun testListFail() = createTestBuilder().use {
        it.user.sites.assertListSitesFail(403)
    }

    @Test
    fun testCreate() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val routeId = WorkPlanningMock.routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
            routeId = routeId,
            status = TaskStatus.TODO,
            groupNumber = 10
        )

        val createdTask = it.manager.tasks.create(taskData)

        assertNotNull(createdTask.freightId)
        assertEquals(taskData.customerSiteId, createdTask.customerSiteId)
        assertEquals(taskData.type, createdTask.type)
        assertEquals(taskData.remarks, createdTask.remarks)
        assertEquals(taskData.freightId, createdTask.freightId)
        assertEquals(taskData.routeId, createdTask.routeId)
        assertEquals(taskData.status, createdTask.status)
        assertEquals(taskData.groupNumber, createdTask.groupNumber)

        assertNull(createdTask.startedAt)
        assertNull(createdTask.finishedAt)
    }

    @Test
    fun testCreateFail() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val routeId = WorkPlanningMock.routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
            routeId = routeId,
            status = TaskStatus.TODO,
            groupNumber = 0
        )

        //Access rights checks
        it.user.tasks.assertCreateFail(403, taskData)
        it.driver.tasks.assertCreateFail(403, taskData)

        // Invalid values checks
        InvalidValueTestScenarioBuilder(
            path = "v1/tasks",
            method = Method.POST,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.getInvalidTasks(
                        validRouteId = routeId,
                        validFreightId = freight1.id,
                        validSiteId = site1.id
                    ),
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testFind() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val routeId = WorkPlanningMock.routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
            routeId = routeId,
            status = TaskStatus.IN_PROGRESS,
            groupNumber = 10
        )

        val createdTask = it.manager.tasks.create(taskData)
        val foundTask = it.manager.tasks.findTask(createdTask.id!!)

        assertNotNull(foundTask)
        assertEquals(createdTask.id, foundTask.id)
        assertEquals(createdTask.customerSiteId, foundTask.customerSiteId)
        assertEquals(createdTask.type, foundTask.type)
        assertEquals(createdTask.remarks, foundTask.remarks)
        assertEquals(createdTask.freightId, foundTask.freightId)
        assertEquals(createdTask.routeId, foundTask.routeId)
        assertEquals(createdTask.status, foundTask.status)
        assertEquals(createdTask.groupNumber, foundTask.groupNumber)

        assertNull(foundTask.finishedAt)
        assertNotNull(foundTask.startedAt)
    }

    @Test
    fun testFindFail() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)

        val createdTask = it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = null
        )

        it.user.tasks.assertFindFail(403, createdTask.id!!)

        InvalidValueTestScenarioBuilder(
            path = "v1/tasks/{taskId}",
            method = Method.GET,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "taskId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTask.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testUpdate() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)

        val site2 = it.manager.sites.create()
        val freight2 = it.manager.freights.create(site2, site2)

        val routeId = WorkPlanningMock.routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
            routeId = routeId,
            status = TaskStatus.TODO,
            groupNumber = 0
        )

        val createdTask = it.manager.tasks.create(taskData)
        val updateData = createdTask.copy(
            freightId = freight2.id!!,
            customerSiteId = site2.id!!,
            type = TaskType.UNLOAD,
            remarks = "remarks2",
            routeId = null,
            status = TaskStatus.IN_PROGRESS,
            groupNumber = 1
        )

        val updated = it.manager.tasks.updateTask(createdTask.id!!, updateData)
        assertEquals(updateData.freightId, updated.freightId)
        assertEquals(updateData.customerSiteId, updated.customerSiteId)
        assertEquals(updateData.type, updated.type)
        assertEquals(updateData.remarks, updated.remarks)
        assertEquals(updateData.routeId, updated.routeId)
        assertEquals(updateData.status, updated.status)
        assertEquals(updateData.groupNumber, updated.groupNumber)
        assertNotNull(updated.startedAt)
        assertNull(updated.finishedAt)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val createdTask = it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = null
        )

        it.driver.tasks.assertUpdateTaskFail(403, createdTask.id!!, createdTask)
        it.user.tasks.assertUpdateTaskFail(403, createdTask.id, createdTask)

        // Invalid values checks
        InvalidValueTestScenarioBuilder(
            path = "v1/tasks/{taskId}",
            method = Method.PUT,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.getInvalidTasks(
                        validRouteId = routeId,
                        validFreightId = freight1.id,
                        validSiteId = site1.id
                    ),
                    expectedStatus = 400
                )
            )
            .path(
                InvalidValueTestScenarioPath(
                    name = "taskId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTask.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)

        val createdTask = it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = null
        )

        it.manager.tasks.deleteTask(createdTask.id!!)
        it.manager.tasks.assertFindTaskFail(createdTask.id, 404)
    }

    @Test
    fun testDeleteFail(): Unit = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)

        val createdTask = it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = null
        )

        // Cannot delete site with attached tasks
        it.manager.sites.assertDeleteSiteFail(site1.id, 409)
        // Cannot delete freight with attached tasks
        it.manager.freights.assertDeleteFreightFail(freight1.id, 409)

        it.user.tasks.assertDeleteTaskFail(403, createdTask.id!!)
        it.driver.tasks.assertDeleteTaskFail(403, createdTask.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/tasks/{taskId}",
            method = Method.DELETE,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "taskId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTask.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()

        //cannot delete done task
        val updated = it.manager.tasks.updateTask(createdTask.id, createdTask.copy(status = TaskStatus.DONE))
        it.manager.tasks.assertDeleteTaskFail(409, updated.id!!)
        it.manager.tasks.updateTask(updated.id, updated.copy(status = TaskStatus.TODO))
    }
}