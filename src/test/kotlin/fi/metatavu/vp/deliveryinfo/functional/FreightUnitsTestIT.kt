package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.deliveryinfo.functional.impl.InvalidTestValues
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.FreightUnit
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Freight units API tests
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class FreightUnitsTestIT : AbstractFunctionalTest() {

    @Test
    fun testCreate() = createTestBuilder().use {
        val freight = it.manager.freights.createDefaultSimpleFreight()
        val freightUnitData = FreightUnit(
            freightId = freight.id!!,
            type = "type",
            quantity = 2.0,
            reservations = "reservations"
        )

        val result = it.manager.freightUnits.create(freightUnitData)
        assertNotNull(result)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.creatorId)
        assertEquals(freightUnitData.freightId, result.freightId)
        assertEquals(freightUnitData.type, result.type)
        assertEquals(freightUnitData.quantity, result.quantity)
        assertEquals(freightUnitData.reservations, result.reservations)
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { tb ->
        //Access rights checks
        val freight = tb.manager.freights.createDefaultSimpleFreight()
        tb.user.freightUnits.assertCreateFail(403, freight.id!!)

        // Invalid values checks
        InvalidValueTestScenarioBuilder(
            path = "v1/freightUnits",
            method = Method.POST,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
        .body(
            InvalidValueTestScenarioBody(
                values = InvalidTestValues.INVALID_FREIGHT_UNITS_FREIGHT_ID,
                expectedStatus = 404
            )
        )
        .build()
        .test()
    }

    @Test
    fun testList() = createTestBuilder().use {
        val freight1 = it.manager.freights.createDefaultSimpleFreight()
        val freight2 = it.manager.freights.createDefaultSimpleFreight()

        it.manager.freightUnits.create(freight1.id!!)
        it.manager.freightUnits.create(freight1.id)
        it.manager.freightUnits.create(freight2.id!!)
        val totalList = it.manager.freightUnits.listFreightUnits()
        assertEquals(3, totalList.size)

        val pagedList1 = it.manager.freightUnits.listFreightUnits(first = 1, max = 1)
        assertEquals(1, pagedList1.size)

        val pagedList2 = it.manager.freightUnits.listFreightUnits(first = 2, max = 5)
        assertEquals(1, pagedList2.size)

        val pagedList3 = it.manager.freightUnits.listFreightUnits(first = 0, max = 5)
        assertEquals(3, pagedList3.size)

        val filtered = it.manager.freightUnits.listFreightUnits(freightId = freight1.id)
        assertEquals(2, filtered.size)
    }

    @Test
    fun testListFail() = createTestBuilder().use {
        //Access rights checks
        it.user.freightUnits.assertListFreightUnitsFail(403)
        it.driver.freightUnits.listFreightUnits()
        it.manager.freightUnits.listFreightUnits()
        return@use
    }

    @Test
    fun testFind() = createTestBuilder().use {
        val createdFreight = it.manager.freights.createDefaultSimpleFreight()
        val createdUnit = it.manager.freightUnits.create(createdFreight.id!!)
        val foundUnit = it.manager.freightUnits.findFreightUnit(createdUnit.id!!)
        assertNotNull(foundUnit)
        assertEquals(createdUnit.id, foundUnit.id)
        assertEquals(createdUnit.freightId, foundUnit.freightId)
        assertEquals(createdUnit.type, foundUnit.type)
        assertEquals(createdUnit.quantity, foundUnit.quantity)
        assertEquals(createdUnit.reservations, foundUnit.reservations)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { tb ->
        val freight = tb.manager.freights.createDefaultSimpleFreight()
        val unit = tb.manager.freightUnits.create(freight.id!!)

        // access rights
        tb.user.freightUnits.assertFindFreightUnitFail(unit.id!!, 403)
        tb.driver.freightUnits.findFreightUnit(unit.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/freightUnits/{freightUnitId}",
            method = Method.GET,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "freightUnitId",
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
        val createdFreight = it.manager.freights.createDefaultSimpleFreight()
        val createdUnit = it.manager.freightUnits.create(createdFreight.id!!)
        val updateData = createdUnit.copy(
            type = "new type",
            quantity = 1.0,
            reservations = "new reservations"
        )
        val result = it.manager.freightUnits.updateFreightUnit(createdUnit.id!!, updateData)
        assertNotNull(result)
        assertEquals(createdUnit.id, result.id)
        assertEquals(updateData.type, result.type)
        assertEquals(updateData.quantity, result.quantity)
        assertEquals(updateData.reservations, result.reservations)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { tb ->
        val freight = tb.manager.freights.createDefaultSimpleFreight()
        val unit = tb.manager.freightUnits.create(freight.id!!)

        // access rights
        tb.user.freightUnits.assertUpdateFreightUnitFail(unit.id!!, 403, unit.freightId)
        tb.driver.freightUnits.updateFreightUnit(unit.id, unit)

        InvalidValueTestScenarioBuilder(
            path = "v1/freightUnits/{freightUnitId}",
            method = Method.PUT,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "freightUnitId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = freight.id,
                    expectedStatus = 404
                )
            )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_FREIGHT_UNITS_FREIGHT_ID,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val freight = it.manager.freights.createDefaultSimpleFreight()
        val unit = it.manager.freightUnits.create(freight.id!!)

        it.manager.freightUnits.deleteFreightUnit(unit.id!!)
        val emptyList = it.manager.freightUnits.listFreightUnits()
        assertEquals(0, emptyList.size)

        it.manager.freightUnits.create(freight.id)
        val oneUnit = it.manager.freightUnits.listFreightUnits()
        assertEquals(1, oneUnit.size)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use {
        val freight = it.manager.freights.createDefaultSimpleFreight()
        val unit = it.manager.freightUnits.create(freight.id!!)

        //Cannot remove freight which has fregit units
        it.manager.freights.assertDeleteFreightFail(freight.id, 409)

        it.user.freightUnits.assertDeleteFreightUnitFail(unit.id!!, 403)
        it.driver.freightUnits.assertDeleteFreightUnitFail(unit.id, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/freightUnits/{freightUnitId}",
            method = Method.DELETE,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "freightUnitId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = freight.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

}