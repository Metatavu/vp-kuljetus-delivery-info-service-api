package fi.metatavu.vp.deliveryinfo.temperature

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for temperature records
 */
@ApplicationScoped
class TemperatureRecordController {
    @Inject
    lateinit var temperatureRecordRepository: TemperatureRecordRepository

    /**
     * Creates a new device
     *
     * @param deviceId device id
     * @param sensorId sensor id
     * @param value value
     * @param timestamp timestamp
     * @param userId user id
     * @return new device
     */
    suspend fun create(
        deviceId: String,
        sensorId: String,
        value: Float,
        timestamp: Long,
        userId: UUID,
    ): TemperatureRecord {
        return temperatureRecordRepository.create(
            deviceId = deviceId,
            sensorId = sensorId,
            value = value,
            timestamp = timestamp,
            userId = userId
        )
    }

    suspend fun findById(id: UUID): TemperatureRecord? {
        return temperatureRecordRepository.findByIdSuspending(id)
    }
    suspend fun delete(temperatureRecord: TemperatureRecord) {
        temperatureRecordRepository.deleteSuspending(temperatureRecord)
    }
}