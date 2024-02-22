package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.deliveryinfo.functional.impl.InvalidTestValues
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.Freight
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Freights API tests
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class FreightsTestIT : AbstractFunctionalTest() {

    @Test
    fun testCreate() = createTestBuilder().use {
        val freightData = Freight(
            pointOfDeparture = "departure",
            sender = "sender",
            recipient = "recipient",
            payer = "payer",
            temperatureMin = 1.0,
            temperatureMax = 2.0,
            reservations = "reservations",
            destination = "destination",
            shipmentInfo = "shipmentInfo"
        )

        val result = it.manager.freights.create(freightData)
        assertNotNull(result)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.creatorId)
        assertNotNull(result.freightNumber)
        assertEquals(freightData.pointOfDeparture, result.pointOfDeparture)
        assertEquals(freightData.sender, result.sender)
        assertEquals(freightData.recipient, result.recipient)
        assertEquals(freightData.payer, result.payer)
        assertEquals(freightData.temperatureMin, result.temperatureMin)
        assertEquals(freightData.temperatureMax, result.temperatureMax)
        assertEquals(freightData.reservations, result.reservations)
        assertEquals(freightData.destination, result.destination)
        assertEquals(freightData.shipmentInfo, result.shipmentInfo)
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { tb ->
        //Access rights checks
        tb.user.freights.assertCreateFail(403)
        tb.driver.freights.assertCreateFail(403)

        // Invalid values checks
        InvalidValueTestScenarioBuilder(
            path = "v1/freights",
            method = Method.POST,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_FREIGHTS,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testList() = createTestBuilder().use {
        it.manager.freights.create()
        it.manager.freights.create()
        it.manager.freights.create()
        val totalList = it.manager.freights.listFreights()
        assertEquals(3, totalList.size)
        val currentMaxNum = totalList.maxOf { f -> f.freightNumber!! }
        assertTrue(totalList.any { f -> f.freightNumber == currentMaxNum })
        assertTrue(totalList.any { f -> f.freightNumber == currentMaxNum - 1 })
        assertTrue(totalList.any { f -> f.freightNumber == currentMaxNum - 2 })

        val pagedList1 = it.manager.freights.listFreights(first = 1, max = 1)
        assertEquals(1, pagedList1.size)

        val pagedList2 = it.manager.freights.listFreights(first = 2, max = 5)
        assertEquals(1, pagedList2.size)

        // Remove 2 freights and re-create them to test freight numbers
        it.manager.freights.deleteFreight(totalList[1].id!!)
        it.manager.freights.deleteFreight(totalList[2].id!!)
        it.manager.freights.create()
        it.manager.freights.create()
        val totalList2 = it.manager.freights.listFreights()
        assertEquals(3, totalList2.size)
        assertTrue(totalList2.any { f -> f.freightNumber == currentMaxNum })
        assertTrue(totalList2.any { f -> f.freightNumber == currentMaxNum + 1 })
        assertTrue(totalList2.any { f -> f.freightNumber == currentMaxNum + 2 })
    }

    @Test
    fun testListFail() = createTestBuilder().use {
        //Access rights checks
        it.user.freights.assertListFreightsFail(403)
        it.driver.freights.listFreights()
        it.manager.freights.listFreights()
        return@use
    }

    @Test
    fun testFind() = createTestBuilder().use {
        val createdFreight = it.manager.freights.create()
        val foundFreight = it.manager.freights.findFreight(createdFreight.id!!)
        assertNotNull(foundFreight)
        assertEquals(createdFreight.id, foundFreight.id)
        assertEquals(createdFreight.pointOfDeparture, foundFreight.pointOfDeparture)
        assertEquals(createdFreight.sender, foundFreight.sender)
        assertEquals(createdFreight.recipient, foundFreight.recipient)
        assertEquals(createdFreight.payer, foundFreight.payer)
        assertEquals(createdFreight.temperatureMin, foundFreight.temperatureMin)
        assertEquals(createdFreight.temperatureMax, foundFreight.temperatureMax)
        assertEquals(createdFreight.reservations, foundFreight.reservations)
        assertEquals(createdFreight.destination, foundFreight.destination)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { tb ->
        val freight = tb.manager.freights.create()

        // access rights
        tb.user.freights.assertFindFreightFail(freight.id!!, 403)
        tb.driver.freights.findFreight(freight.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/freights/{freightId}",
            method = Method.GET,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "freightId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = freight.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testUpdate() = createTestBuilder().use {
        val createdFreight = it.manager.freights.create()
        val updateData = createdFreight.copy(
            sender = "new sender",
            recipient = "new recipient",
            payer = "new payer",
            temperatureMin = null,
            temperatureMax = null
        )
        val result = it.manager.freights.updateFreight(createdFreight.id!!, updateData)
        assertNotNull(result)
        assertEquals(createdFreight.id, result.id)
        assertEquals(updateData.sender, result.sender)
        assertEquals(updateData.recipient, result.recipient)
        assertEquals(updateData.payer, result.payer)
        assertEquals(updateData.temperatureMin, result.temperatureMin)
        assertEquals(updateData.temperatureMax, result.temperatureMax)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { tb ->
        val freight = tb.manager.freights.create()

        // access rights
        tb.driver.freights.assertUpdateFreightFail(freight.id!!, 403)
        tb.user.freights.assertUpdateFreightFail(freight.id, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/sites/{siteId}",
            method = Method.PUT,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "siteId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = freight.id,
                    expectedStatus = 404
                )
            )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_FREIGHTS,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val freight = it.manager.freights.create()
        it.manager.freights.deleteFreight(freight.id!!)
        val emptyList = it.manager.freights.listFreights()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use {
        val freight = it.manager.freights.create()

        it.user.freights.assertDeleteFreightFail(freight.id!!, 403)
        it.driver.freights.assertDeleteFreightFail(freight.id, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/freights/{freightId}",
            method = Method.DELETE,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "freightId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = freight.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

}