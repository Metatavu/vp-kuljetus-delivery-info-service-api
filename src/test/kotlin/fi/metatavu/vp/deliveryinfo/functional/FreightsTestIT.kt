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
        val site1 = it.manager.sites.create()
        val site2 = it.manager.sites.create()
        val freightData = Freight(
            pointOfDepartureSiteId = site1.id!!,
            senderSiteId = site1.id,
            recipientSiteId = site2.id!!,
            destinationSiteId = site2.id,
        )

        val result = it.manager.freights.create(freightData)
        assertNotNull(result)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.creatorId)
        assertNotNull(result.freightNumber)
        assertEquals(freightData.pointOfDepartureSiteId, result.pointOfDepartureSiteId)
        assertEquals(freightData.senderSiteId, result.senderSiteId)
        assertEquals(freightData.recipientSiteId, result.recipientSiteId)
        assertEquals(freightData.destinationSiteId, result.destinationSiteId)

        // Customer wants their freight numbers to start incrementing from 100000001
        // It is impossible to tell what test will be ran first so it is simply asserted that freightNumber is at least 100000001
        assertTrue(result.freightNumber!! >= 100000001)
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { tb ->
        val site = tb.manager.sites.create()
        val freightData = Freight(
            pointOfDepartureSiteId = site.id!!,
            senderSiteId = site.id,
            recipientSiteId = site.id,
            destinationSiteId = site.id,
        )
        //Access rights checks
        tb.user.freights.assertCreateFail(expectedStatus = 403, freight = freightData)
        tb.driver.freights.assertCreateFail(expectedStatus = 403, freight = freightData)

        // Invalid values checks
        InvalidValueTestScenarioBuilder(
            path = "v1/freights",
            method = Method.POST,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.getInvalidFreights(site.id),
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testList() = createTestBuilder().use {
        it.manager.freights.createDefaultSimpleFreight()
        it.manager.freights.createDefaultSimpleFreight()
        it.manager.freights.createDefaultSimpleFreight()
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
        it.manager.freights.createDefaultSimpleFreight()
        it.manager.freights.createDefaultSimpleFreight()
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
        val createdFreight = it.manager.freights.createDefaultSimpleFreight()
        val foundFreight = it.manager.freights.findFreight(createdFreight.id!!)
        assertNotNull(foundFreight)
        assertEquals(createdFreight.id, foundFreight.id)
        assertEquals(createdFreight.pointOfDepartureSiteId, foundFreight.pointOfDepartureSiteId)
        assertEquals(createdFreight.senderSiteId, foundFreight.senderSiteId)
        assertEquals(createdFreight.recipientSiteId, foundFreight.recipientSiteId)
        assertEquals(createdFreight.destinationSiteId, foundFreight.destinationSiteId)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { tb ->
        val freight = tb.manager.freights.createDefaultSimpleFreight()

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
        val site = it.manager.sites.create()
        val createdFreight = it.manager.freights.createDefaultSimpleFreight()
        val updateData = createdFreight.copy(destinationSiteId = site.id!!, pointOfDepartureSiteId = site.id)
        val result = it.manager.freights.updateFreight(createdFreight.id!!, updateData)
        assertNotNull(result)
        assertEquals(createdFreight.id, result.id)
        assertNotEquals(createdFreight.destinationSiteId, result.destinationSiteId)
        assertNotEquals(createdFreight.pointOfDepartureSiteId, result.pointOfDepartureSiteId)
        assertEquals(updateData.destinationSiteId, result.destinationSiteId)
        assertEquals(updateData.pointOfDepartureSiteId, result.pointOfDepartureSiteId)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { tb ->
        val freight = tb.manager.freights.createDefaultSimpleFreight()

        // access rights
        tb.driver.freights.assertUpdateFreightFail(expectedStatus = 403, id = freight.id!!, freight = freight)
        tb.user.freights.assertUpdateFreightFail(expectedStatus = 403, id = freight.id, freight = freight)

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
                    values = InvalidTestValues.getInvalidFreights(freight.destinationSiteId),
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val freight = it.manager.freights.createDefaultSimpleFreight()
        it.manager.freights.deleteFreight(freight.id!!)
        val emptyList = it.manager.freights.listFreights()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use {
        val freight = it.manager.freights.createDefaultSimpleFreight()

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