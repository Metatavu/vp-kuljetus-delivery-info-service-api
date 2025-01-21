package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.deliveryinfo.functional.impl.InvalidTestValues
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.Site
import fi.metatavu.vp.test.client.models.SiteType
import fi.metatavu.vp.test.client.models.TemperatureReading
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

/**
 * Sites test
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class SitesTestIT : AbstractFunctionalTest() {

    @Test
    fun testArchiving() = createTestBuilder().use {
        val site1 = it.manager.sites.create()

        // Archive site 1
        val archived = it.manager.sites.updateSite(site1.id!!, site1.copy(archivedAt = site1.createdAt))
        assertNotNull(archived.archivedAt)
        val archivedList = it.manager.sites.listSites(archived = true)
        assertEquals(1, archivedList.size)
        val unarchivedList = it.manager.sites.listSites(archived = false)
        assertEquals(0, unarchivedList.size)

        // Cannot update archived sites
        it.manager.sites.assertUpdateSiteFail(site1.id, 409, archived.copy(name = "Test site 2"))

        // Can un-archive sites
        val unArchived = it.manager.sites.updateSite(site1.id, archived.copy(archivedAt = null))
        assertEquals(null, unArchived.archivedAt)
        val unArchivedList2 = it.manager.sites.listSites(archived = false)
        assertEquals(1, unArchivedList2.size)
        val archivedList2 = it.manager.sites.listSites(archived = true)
        assertEquals(0, archivedList2.size)
    }

    @Test
    fun testCreateCustomerSite() = createTestBuilder().use {
        val site1 = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.CUSTOMER_SITE,
            deviceIds = emptyArray()
        )
        val site2 = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            additionalInfo = generateRandomString(1000),
            siteType = SiteType.CUSTOMER_SITE,
            deviceIds = emptyArray()
        )

        val result1 = it.manager.sites.create(site1)
        val result2 = it.manager.sites.create(site2)
        assertNotNull(result1)
        assertNotNull(result1.id)
        assertEquals(site1.name, result1.name)
        assertEquals(site1.location, result1.location)
        assertEquals(site1.address, result1.address)
        assertEquals(site1.postalCode, result1.postalCode)
        assertEquals(site1.locality, result1.locality)
        assertEquals(site1.siteType, result1.siteType)
        assertNull(result1.additionalInfo)
        assertNotNull(result2.additionalInfo)
        assertEquals(result2.additionalInfo, site2.additionalInfo)
    }

    @Test
    fun testCreateTerminal() = createTestBuilder().use {
        val site1 = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        )

        val result1 = it.manager.sites.create(site1)
        assertNotNull(result1)
        assertNotNull(result1.id)
        assertEquals(site1.name, result1.name)
        assertEquals(site1.location, result1.location)
        assertEquals(site1.address, result1.address)
        assertEquals(site1.postalCode, result1.postalCode)
        assertEquals(site1.locality, result1.locality)
        assertEquals(site1.siteType, result1.siteType)
        assertNull(result1.additionalInfo)
        assertNotNull(result1.deviceIds.find { deviceId -> deviceId == site1.deviceIds.first() })
        assertNotNull(result1.deviceIds.find { deviceId -> deviceId == site1.deviceIds.last() })
    }

    @Test
    fun testDeleteTerminal() = createTestBuilder().use {
        val site1 = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        )

        val created = it.manager.sites.create(site1)
        assertNotNull(it.manager.sites.findSite(created.id!!))
        it.manager.sites.deleteSite(created.id!!)
        val emptyList = it.manager.sites.listSites()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun testUpdateTerminal() = createTestBuilder().use {
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()
        val originalDeviceIds = arrayOf(id1, id2)
        val site = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = originalDeviceIds
        )

        val original = it.manager.sites.create(site)
        assertNotNull(original.deviceIds.find { deviceId -> deviceId == id1 })
        assertNotNull(original.deviceIds.find { deviceId -> deviceId == id2 })

        val id3 = UUID.randomUUID().toString()
        val newDeviceIds = arrayOf(id2, id3)
        val newSite = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = newDeviceIds
        )

        val updated = it.manager.sites.updateSite(original.id!!, newSite)
        assertNull(updated.deviceIds.find { deviceId -> deviceId == id1 })
        assertNotNull(updated.deviceIds.find { deviceId -> deviceId == id2 })
        assertNotNull(updated.deviceIds.find { deviceId -> deviceId == id3 })
    }


    @Test
    fun testCreateFail() = createTestBuilder().use { tb ->
        //Access rights checks
        tb.user.sites.assertCreateFail(403)
        tb.driver.sites.assertCreateFail(403)

        // Invalid values checks
        InvalidValueTestScenarioBuilder(
            path = "v1/sites",
            method = Method.POST,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_SITES,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testList() = createTestBuilder().use {
        val site1 = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.CUSTOMER_SITE,
            deviceIds = emptyArray()
        )
        val site2 = Site(
            name = "Test site 2",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.CUSTOMER_SITE,
            deviceIds = emptyArray()
        )
        val site3 = Site(
            name = "Test site 3",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.CUSTOMER_SITE,
            deviceIds = emptyArray()
        )
        it.manager.sites.create(site1)
        it.manager.sites.create(site2)
        it.manager.sites.create(site3)
        val totalList = it.manager.sites.listSites()
        assertEquals(3, totalList.size)

        val pagedList1 = it.manager.sites.listSites(first = 1, max = 1)
        assertEquals(1, pagedList1.size)

        val pagedList2 = it.manager.sites.listSites(first = 2, max = 5)
        assertEquals(1, pagedList2.size)
    }

    @Test
    fun testListFail() = createTestBuilder().use {
        //Access rights checks
        it.user.sites.assertListSitesFail(403)
        it.driver.sites.listSites()
        it.manager.sites.listSites()
        return@use
    }

    @Test
    fun testFind() = createTestBuilder().use {
        val createdSite = it.manager.sites.create()
        val foundSite = it.manager.sites.findSite(createdSite.id!!)
        assertNotNull(foundSite)
        assertEquals(createdSite.id, foundSite.id)
        assertEquals(foundSite.name, foundSite.name)
        assertEquals(foundSite.location, foundSite.location)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { tb ->
        val createdSite = tb.manager.sites.create()

        // access rights
        tb.user.sites.assertFindSiteFail(createdSite.id!!, 403)
        tb.driver.sites.findSite(createdSite.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/sites/{siteId}",
            method = Method.GET,
            token = tb.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "siteId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdSite.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testUpdate() = createTestBuilder().use {
        val createdSite = it.manager.sites.create()
        val updateData = Site(
            name = "Test site 2",
            location = "POINT (100 100)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.CUSTOMER_SITE,
            deviceIds = emptyArray()
        )
        val result1 = it.manager.sites.updateSite(createdSite.id!!, updateData)
        assertNotNull(result1)
        assertEquals(createdSite.id, result1.id)
        assertEquals(updateData.name, result1.name)
        assertEquals(updateData.location, result1.location)
        assertEquals(updateData.address, result1.address)
        assertEquals(updateData.postalCode, result1.postalCode)
        assertEquals(updateData.locality, result1.locality)
        assertNull(result1.additionalInfo)

        val updateData2 = updateData.copy(additionalInfo = generateRandomString(10000))

        val result2 = it.manager.sites.updateSite(
            id = createdSite.id,
            site = updateData2
        )
        assertNotNull(result2.additionalInfo)
        assertEquals(result2.additionalInfo, updateData2.additionalInfo)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { tb ->
        val createdSite = tb.manager.sites.create()

        // access rights
        tb.driver.sites.assertUpdateSiteFail(createdSite.id!!, 403, createdSite)
        tb.user.sites.assertUpdateSiteFail(createdSite.id, 403, createdSite)

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
                    default = createdSite.id,
                    expectedStatus = 404
                )
            )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_SITES,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val createdSite = it.manager.sites.create()
        it.manager.sites.deleteSite(createdSite.id!!)
        val emptyList = it.manager.sites.listSites()
        assertEquals(0, emptyList.size)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use {
        val createdSite = it.manager.sites.create()

        it.user.sites.assertDeleteSiteFail(createdSite.id!!, 403)
        it.driver.sites.assertDeleteSiteFail(createdSite.id, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/sites/{siteId}",
            method = Method.DELETE,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "siteId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdSite.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testListSiteTemperatures() = createTestBuilder().use {
        val deviceId = UUID.randomUUID().toString()
        val site1 = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(deviceId)
        )

        val createdSite = it.manager.sites.create(site1)

        val temperatureReading = TemperatureReading(
            espMacAddress = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        val temperatureReading2 = TemperatureReading(
            espMacAddress = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 25.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        val temperatureReading3 = TemperatureReading(
            espMacAddress = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 21.1f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        val thermometer = it.manager.thermometers.listThermometers(null, false).firstOrNull()
        val reading = it.manager.sites.listSiteTemperatures(siteId = createdSite.id!!, includeArchived = false, null, null).firstOrNull()
        assertNotNull(reading)
        assertEquals(temperatureReading.value, reading!!.value)
        assertEquals(temperatureReading.timestamp, reading.timestamp)
        assertEquals(thermometer!!.id, reading.thermometerId)

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading2)
        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading3)
        val list1 = it.manager.sites.listSiteTemperatures(siteId = createdSite.id, includeArchived = false, null, null)
        assertEquals(3, list1.size)
    }

}