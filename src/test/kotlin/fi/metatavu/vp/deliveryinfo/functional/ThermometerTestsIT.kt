package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.messaging.RoutingKey
import fi.metatavu.vp.messaging.client.MessagingClient
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.test.client.models.Site
import fi.metatavu.vp.test.client.models.SiteType
import fi.metatavu.vp.test.client.models.TerminalTemperatureReading
import fi.metatavu.vp.test.client.models.UpdateTerminalThermometerRequest
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
class ThermometerTestsIT: AbstractFunctionalTest() {

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

        val timeStamp = Instant.now().toEpochMilli()
        val temperatureReading = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = timeStamp
        )

        val messageConsumer = MessagingClient.setConsumer<TemperatureGlobalEvent>(RoutingKey.TEMPERATURE)
        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)

        val messages = messageConsumer.consumeMessages(1)
        assertEquals(1, messages.size)

        val thermometer = it.manager.thermometers.listThermometers(null, false).firstOrNull()
        assertNotNull(thermometer)
        assertEquals(thermometer!!.id, messages.first().thermometerId)
        assertEquals(23.2f, messages.first().temperature)
        assertEquals(timeStamp, messages.first().timestamp)
        assertEquals(deviceId, thermometer.deviceIdentifier)
        assertEquals(temperatureReading.hardwareSensorId, thermometer.hardwareSensorId)
        assertEquals(createdSite.id, thermometer.siteId)
        assertNull(thermometer.archivedAt)
    }

    @Test
    fun testListThermometers() = createTestBuilder().use { it ->
        val deviceId = UUID.randomUUID().toString()
        val device2Id = UUID.randomUUID().toString()
        val site1 = it.manager.sites.create(Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(deviceId, device2Id)
        ))

        val temperatureReading = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        val temperatureReading2 = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf2",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        val temperatureReading3 = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
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
        val list2 = it.manager.thermometers.listThermometers(site1.id, false)
        assertEquals(3, list2.size)
        val list3 = it.manager.thermometers.listThermometers(null, true)
        assertEquals(3, list3.size)

        val temperatureReading4 = TerminalTemperatureReading(
            deviceIdentifier = device2Id,
            hardwareSensorId = "wgrewgerf3",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading4)
        val list4 = it.manager.thermometers.listThermometers(null, false)
        assertEquals(3, list4.size)
        val list5 = it.manager.thermometers.listThermometers(null, true)
        assertEquals(4, list5.size)
        val archived = list5.find { reading ->
            reading.deviceIdentifier == temperatureReading3.deviceIdentifier && reading.hardwareSensorId == temperatureReading3.hardwareSensorId
        }
        assertNotNull(archived!!.archivedAt)


        it.manager.sites.updateSite(site1.id!!, site1.copy(deviceIds = arrayOf(deviceId)))
        val site2 = it.manager.sites.create(Site(
            name = "Test site 1",
            location = "POINT (60.16952 24.93545)",
            address = "Test address",
            postalCode = "00100",
            locality = "Helsinki",
            siteType = SiteType.TERMINAL,
            deviceIds = arrayOf(device2Id)
        ))

        val temperatureReading5 = TerminalTemperatureReading(
            deviceIdentifier = device2Id,
            hardwareSensorId = "wgrewgerf3",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )
        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading5)
        val list6 = it.manager.thermometers.listThermometers(null, false)
        assertEquals(3, list6.size)
        val list7 = it.manager.thermometers.listThermometers(null, true)
        assertEquals(5, list7.size)
        val list8 = it.manager.thermometers.listThermometers(site1.id, true)
        assertEquals(4, list8.size)
        val list9 = it.manager.thermometers.listThermometers(site2.id, false)
        assertEquals(1, list9.size)
        val list10 = it.manager.thermometers.listThermometers(site1.id, false)
        assertEquals(2, list10.size)
    }

    @Test
    fun testFindThermometer() = createTestBuilder().use {
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

        val temperatureReading = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        val thermometer = it.manager.thermometers.listThermometers(null, false).first()
        val found = it.manager.thermometers.findThermometer(thermometer.id!!)
        assertNotNull(found)
        assertEquals(thermometer.id, found.id)
    }

    @Test
    fun testUpdateThermometer() = createTestBuilder().use {
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

        val temperatureReading = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        val thermometer = it.manager.thermometers.listThermometers(null, false).first()
        assertNull(thermometer.name)
        val updated = it.manager.thermometers.updateThermometer(
            id = thermometer.id!!,
            thermometer = UpdateTerminalThermometerRequest(name = "Sensor1")
        )

        val foundUpdated = it.manager.thermometers.findThermometer(
            id = updated.id!!
        )
        assertEquals("Sensor1", foundUpdated.name)
    }

    @Test
    fun testListFail() = createTestBuilder().use {
        //Access rights checks
        it.user.thermometers.assertListThermometersFail(403)
        it.driver.thermometers.assertListThermometersFail(403)
        it.manager.thermometers.listThermometers(null, false)
        return@use
    }

    @Test
    fun testFindFail() = createTestBuilder().use {
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

        it.manager.sites.create(site1)

        val temperatureReading = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        val thermometer = it.manager.thermometers.listThermometers(null, false).firstOrNull()
        //Access rights checks
        it.user.thermometers.assertFindThermometerFail(thermometer!!.id!!, 403)
        it.driver.thermometers.assertFindThermometerFail(thermometer.id!!, 403)
        it.manager.thermometers.findThermometer(thermometer.id)
        return@use
    }

    @Test
    fun testUpdateThermometerFail() = createTestBuilder().use {
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

        val temperatureReading = TerminalTemperatureReading(
            deviceIdentifier = deviceId,
            hardwareSensorId = "wgrewgerf",
            value = 23.2f,
            timestamp = Instant.now().toEpochMilli()
        )

        it.setTerminalDeviceApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        val thermometer = it.manager.thermometers.listThermometers(null, false).first()
        it.user.thermometers.assertUpdateThermometerFail(thermometer.id!!, 403)
        it.driver.thermometers.assertUpdateThermometerFail(thermometer.id, 403)
        it.manager.thermometers.updateThermometer(thermometer.id, UpdateTerminalThermometerRequest(name = "name"))
        return@use
    }
}