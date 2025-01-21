package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.Site
import fi.metatavu.vp.test.client.models.SiteType
import fi.metatavu.vp.test.client.models.TemperatureReading
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

/**
 * Thermometer tests
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class ThermometerTests: AbstractFunctionalTest() {

    @Test
    fun testCreateThermometer() = createTestBuilder().use {
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

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        val thermometer = it.manager.thermometers.listThermometers(null, false).firstOrNull()
        assertNotNull(thermometer)
        assertEquals(deviceId, thermometer!!.espMacAddress)
        assertEquals(temperatureReading.hardwareSensorId, thermometer.hardwareSensorId)
        assertEquals(createdSite.id, thermometer.siteId)
    }

    @Test
    fun testListThermometers() = createTestBuilder().use {
        val deviceId = UUID.randomUUID().toString()

        it.manager.sites.create(Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(deviceId)
        ))

        val temperatureReading = TemperatureReading(
            espMacAddress = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        val temperatureReading2 = TemperatureReading(
            espMacAddress = deviceId,
            hardwareSensorId = "wgrewgerf2",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        val temperatureReading3 = TemperatureReading(
            espMacAddress = deviceId,
            hardwareSensorId = "wgrewgerf3",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading2)
        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading3)
        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading3)
        val list1 = it.manager.thermometers.listThermometers(null, false)
        assertEquals(3, list1.size)
    }
}