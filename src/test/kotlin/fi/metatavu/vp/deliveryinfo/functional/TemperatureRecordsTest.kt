package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import fi.metatavu.vp.test.client.models.TemperatureRecord
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Test
import java.time.Instant
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for Temperature records API
 */
@QuarkusTest
@QuarkusTestResource(WorkPlanningMock::class)
@TestProfile(DefaultTestProfile::class)
class TemperatureRecordsTest: AbstractFunctionalTest() {
    @Test
    fun testCreateTemperatureRecord() = createTestBuilder().use {
        val record = TemperatureRecord(
            deviceIdentifier = "ABC",
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


}