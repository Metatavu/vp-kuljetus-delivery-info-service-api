package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.Site
import fi.metatavu.vp.test.client.models.SiteType
import fi.metatavu.vp.test.client.models.TemperatureRecord
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Test
import java.time.Instant
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Tests for Temperature records API
 */
@QuarkusTest
@QuarkusTestResource(WorkPlanningMock::class)
@TestProfile(DefaultTestProfile::class)
class TemperatureRecordsTest: AbstractFunctionalTest() {
    @Test
    fun testCreateTemperatureRecord() = createTestBuilder().use {
        val deviceId = UUID.randomUUID().toString()
        val site = Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(deviceId)
        )
        it.manager.sites.create(site)
        val record = TemperatureRecord(
            deviceIdentifier = deviceId,
            hardwareSensorId = "DEF",
            value = 25f,
            timestamp = Instant.now().toEpochMilli()
        )
        val created = it.manager.temperatureRecords.create(record)
        assertEquals(record.deviceIdentifier, created.deviceIdentifier)
        assertEquals(record.hardwareSensorId, created.hardwareSensorId)
        assertEquals(record.value, created.value)
        assertEquals(record.timestamp, created.timestamp)
    }

    @Test
    fun testListTemperatureRecords() = createTestBuilder().use {
        val deviceId = UUID.randomUUID().toString()
        val deviceId2 = UUID.randomUUID().toString()
        val deviceId3 = UUID.randomUUID().toString()

        val site =  it.manager.sites.create(Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(deviceId, deviceId2)
        ))

        val site2 = it.manager.sites.create(Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(deviceId3)
        ))

        val record = TemperatureRecord(
            deviceIdentifier = deviceId,
            hardwareSensorId = "DEF",
            value = 25f,
            timestamp = Instant.now().toEpochMilli()
        )

        val record2 = TemperatureRecord(
            deviceIdentifier = deviceId2,
            hardwareSensorId = "ABGF",
            value = 25f,
            timestamp = Instant.now().toEpochMilli()
        )

        val record3 = TemperatureRecord(
            deviceIdentifier = deviceId3,
            hardwareSensorId = "EAEETG",
            value = 25f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.manager.temperatureRecords.create(record)
        it.manager.temperatureRecords.create(record)
        it.manager.temperatureRecords.create(record)

        it.manager.temperatureRecords.create(record2)
        it.manager.temperatureRecords.create(record2)

        it.manager.temperatureRecords.create(record3)

        assertEquals(3, it.manager.temperatureRecords.list(terminalId = site.id, deviceId = deviceId).count())
        assertEquals(2, it.manager.temperatureRecords.list(terminalId = site.id, deviceId = deviceId2).count())
        assertEquals(5, it.manager.temperatureRecords.list(terminalId = site.id, null).count())
        assertEquals(1, it.manager.temperatureRecords.list(terminalId = site2.id, null).count())
        assertEquals(6, it.manager.temperatureRecords.list(terminalId = null, null).count())
    }
}