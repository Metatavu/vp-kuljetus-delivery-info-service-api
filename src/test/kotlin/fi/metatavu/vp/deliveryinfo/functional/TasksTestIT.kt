package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.deliveryinfo.functional.impl.InvalidTestValues
import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock
import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock.Companion.routeId
import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock.Companion.routeId2
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
            routeId = routeId,
            orderNumber = 0
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
    fun testCreate() = createTestBuilder().use { tb ->
        val site1 = tb.manager.sites.create()
        val freight1 = tb.manager.freights.create(site1, site1)
        val routeId = routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "1",
            routeId = routeId,
            status = TaskStatus.TODO,
            groupNumber = 10,
            orderNumber = 0
        )

        val createdTask = tb.manager.tasks.create(taskData)

        assertNotNull(createdTask.freightId)
        assertEquals(taskData.customerSiteId, createdTask.customerSiteId)
        assertEquals(taskData.type, createdTask.type)
        assertEquals(taskData.remarks, createdTask.remarks)
        assertEquals(taskData.freightId, createdTask.freightId)
        assertEquals(taskData.routeId, createdTask.routeId)
        assertEquals(taskData.status, createdTask.status)
        assertEquals(taskData.groupNumber, createdTask.groupNumber)
        assertEquals(taskData.orderNumber, createdTask.orderNumber)
        assertNull(createdTask.startedAt)
        assertNull(createdTask.finishedAt)

        // add another task to the same route to the end
        val createdTask2 = tb.manager.tasks.create(taskData.copy(orderNumber = 1, remarks = "2"))
        val allTasks = tb.manager.tasks.listTasks().sortedBy { it.orderNumber }.map { it.remarks }
        assertEquals(2, allTasks.size)
        assertEquals(arrayListOf("1", "2"), allTasks)

        // add another task to the same route and start
        val createdTask3 = tb.manager.tasks.create(taskData.copy(orderNumber = 0, remarks = "3"))
        val allTasks2 = tb.manager.tasks.listTasks().sortedBy { it.orderNumber }.map { it.remarks }
        assertEquals(3, allTasks2.size)
        assertEquals(arrayListOf("3", "1", "2"), allTasks2)
    }

    @Test
    fun testCreateFail() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val routeId = routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
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
        val routeId = routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
            routeId = routeId,
            orderNumber = 0,
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
        assertEquals(createdTask.orderNumber, foundTask.orderNumber)
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

        val routeId = routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            remarks = "remarks",
            routeId = routeId,
            orderNumber = 0,
            status = TaskStatus.TODO,
            groupNumber = 0
        )

        val createdTask = it.manager.tasks.create(taskData)
        val updateData = createdTask.copy(
            freightId = freight2.id!!,
            customerSiteId = site2.id!!,
            type = TaskType.UNLOAD,
            remarks = "remarks2",
            orderNumber = null,
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
        assertEquals(updateData.orderNumber, updated.orderNumber)

        assertEquals(updateData.status, updated.status)
        assertEquals(updateData.groupNumber, updated.groupNumber)
        assertNotNull(updated.startedAt)
        assertNull(updated.finishedAt)
    }

    /**
     * Tests that order numbers are updated correctly within the same route
     */
    @Test
    fun testOrderNumberUpdates() = createTestBuilder().use { tb ->
        val site1 = tb.manager.sites.create()
        val freight1 = tb.manager.freights.create(site1, site1)
        val routeId = routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            routeId = routeId,
            status = TaskStatus.TODO,
            groupNumber = 0
        )

        val task1 = tb.manager.tasks.create(taskData.copy(orderNumber = 0, remarks = "1"))
        val task2 = tb.manager.tasks.create(taskData.copy(orderNumber = 1, remarks = "2"))
        val task3 = tb.manager.tasks.create(taskData.copy(orderNumber = 2, remarks = "3"))

        val allTasks = tb.manager.tasks.listTasks()
        assertEquals(3, allTasks.size)
        // Move T1 to position 1 (second one), expected result T2 T1 T3
        val updated = tb.manager.tasks.updateTask(task1.id!!, task1.copy(orderNumber = 1))
        assertEquals(1, updated.orderNumber)
        val reorderedTasks1 = tb.manager.tasks.listTasks()
        assertEquals(3, reorderedTasks1.size)
        assertEquals(arrayListOf("2", "1", "3"), reorderedTasks1.map { it.remarks })

        // Move T3 to position 1, expecting to get T3 T2 T1
        tb.manager.tasks.updateTask(task3.id!!, task3.copy(orderNumber = 0))
        val reorderedTasks2 = tb.manager.tasks.listTasks()
        assertEquals(3, reorderedTasks2.size)
        assertEquals(arrayListOf("3", "2", "1"), reorderedTasks2.map { it.remarks })

        //Move T3 to last position, expecting T2 T1 T3
        tb.manager.tasks.updateTask(task3.id, task3.copy(orderNumber = 2))
        val reorderedTasks3 = tb.manager.tasks.listTasks()
        assertEquals(3, reorderedTasks3.size)
        assertEquals(arrayListOf("2", "1", "3"), reorderedTasks3.map { it.remarks })

        //Move T3 to first position, expecting T3 T2 T1
        tb.manager.tasks.updateTask(task3.id, task3.copy(orderNumber = 0))
        val reorderedTasks4 = tb.manager.tasks.listTasks()
        assertEquals(3, reorderedTasks4.size)
        assertEquals(arrayListOf(0, 1, 2), reorderedTasks4.map { it.orderNumber })
        assertEquals(arrayListOf("3", "2", "1"), reorderedTasks4.map { it.remarks })

        // Move T3 to position 10, xepecting T2 T1 T3
        tb.manager.tasks.updateTask(task3.id, task3.copy(orderNumber = 3))
        val reorderedTasks5 = tb.manager.tasks.listTasks()
        assertEquals(3, reorderedTasks5.size)
        assertEquals(arrayListOf(0, 1, 2), reorderedTasks5.map { it.orderNumber })
        assertEquals(arrayListOf("2", "1", "3"), reorderedTasks5.map { it.remarks })
    }

    /**
     * Tests that order numbers are updated correctly when moving tasks between routes
     */
    @Test
    fun testRouteUpdates() = createTestBuilder().use { tb ->
        val site1 = tb.manager.sites.create()
        val freight1 = tb.manager.freights.create(site1, site1)
        val routeId = routeId
        val taskData = Task(
            freightId = freight1.id!!,
            customerSiteId = site1.id!!,
            type = TaskType.LOAD,
            routeId = routeId,
            status = TaskStatus.TODO,
            groupNumber = 0
        )

        // Create 3 tasks for route 1
        val task1 = tb.manager.tasks.create(taskData.copy(orderNumber = 0, remarks = "1"))
        val task2 = tb.manager.tasks.create(taskData.copy(orderNumber = 1, remarks = "2"))
        val task3 = tb.manager.tasks.create(taskData.copy(orderNumber = 2, remarks = "3"))

        // Create 2 tasks for route 2
        tb.manager.tasks.create(taskData.copy(routeId = routeId2, orderNumber = 0, remarks = "r2t1"))
        tb.manager.tasks.create(taskData.copy(routeId = routeId2, orderNumber = 1, remarks = "r2t2"))

        // Move task 1 from route 1 to route 2 position 2
        tb.manager.tasks.updateTask(task1.id!!, task1.copy(routeId = routeId2, orderNumber = 2))

        // Verify tasks in route 2
        val allRoute2Tasks1 = tb.manager.tasks.listTasks(routeId = routeId2)
        assertEquals(3, allRoute2Tasks1.size)
        assertEquals(arrayListOf(0, 1, 2), allRoute2Tasks1.map { it.orderNumber })
        val allRoute2TasksRemarks = allRoute2Tasks1.sortedBy { it.orderNumber }.map { it.remarks }
        assertEquals(3, allRoute2TasksRemarks.size)
        assertEquals(arrayListOf("r2t1", "r2t2", "1"), allRoute2TasksRemarks)

        // Verify tasks in route 1
        val allRoute1Tasks = tb.manager.tasks.listTasks(routeId = routeId)
        assertEquals(2, allRoute1Tasks.size)
        assertEquals(arrayListOf(0, 1), allRoute1Tasks.map { it.orderNumber })
        val allRoute1TasksRemarks = allRoute1Tasks.sortedBy { it.orderNumber }.map { it.remarks }
        assertEquals(arrayListOf("2", "3"), allRoute1TasksRemarks)

        //Move task 2 from route 1 to route 2 position 0
        tb.manager.tasks.updateTask(task3.id!!, task3.copy(routeId = routeId2, orderNumber = 0))
        // Verify tasks in route 2
        val allRoute2Tasks2 = tb.manager.tasks.listTasks(routeId = routeId2)
        assertEquals(4, allRoute2Tasks2.size)
        assertEquals(arrayListOf(0, 1, 2, 3), allRoute2Tasks2.map { it.orderNumber })
        val allRoute2TasksRemarks2 = allRoute2Tasks2.sortedBy { it.orderNumber }.map { it.remarks }
        assertEquals(4, allRoute2TasksRemarks2.size)
        assertEquals(arrayListOf("3", "r2t1", "r2t2", "1"), allRoute2TasksRemarks2)

        val allRoute1Tasks1 = tb.manager.tasks.listTasks(routeId = routeId)
        assertEquals(1, allRoute1Tasks1.size)
        assertEquals(0, allRoute1Tasks1[0].orderNumber)

        // Move task 2 from route 1 to route 2 position 10
        tb.manager.tasks.updateTask(task2.id!!, task2.copy(routeId = routeId2, orderNumber = 10))
        // Verify tasks in route 2
        val allRoute2Tasks3 = tb.manager.tasks.listTasks(routeId = routeId2)
        assertEquals(5, allRoute2Tasks3.size)
        assertEquals(arrayListOf(0, 1, 2, 3, 4), allRoute2Tasks3.map { it.orderNumber })
        val allRoute2TasksRemarks3 = allRoute2Tasks3.sortedBy { it.orderNumber }.map { it.remarks }
        assertEquals(5, allRoute2TasksRemarks3.size)
        assertEquals(arrayListOf("3", "r2t1", "r2t2", "1", "2"), allRoute2TasksRemarks3)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use {
        val site1 = it.manager.sites.create()
        val freight1 = it.manager.freights.create(site1, site1)
        val createdTask = it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = routeId,
            orderNumber = 0
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
            routeId = routeId,
            orderNumber = 0
        )
        it.manager.tasks.create(
            customerSiteId = site1.id!!,
            freightId = freight1.id!!,
            routeId = routeId,
            orderNumber = 1
        )

        it.manager.tasks.deleteTask(createdTask.id!!)
        it.manager.tasks.assertFindTaskFail(createdTask.id, 404)

        // verify that order numbers for the left tasks did not change
        val remainingTasks = it.manager.tasks.listTasks()
        assertEquals(1, remainingTasks.size)
        assertEquals(0, remainingTasks[0].orderNumber)
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