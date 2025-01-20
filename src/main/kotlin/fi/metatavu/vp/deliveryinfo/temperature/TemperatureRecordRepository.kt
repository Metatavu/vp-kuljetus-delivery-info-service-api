package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.devices.Device
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort

/**
 * Repository for temperature records
 */
@ApplicationScoped
class TemperatureRecordRepository: AbstractRepository<TemperatureRecord, UUID>() {

    /**
     * Creates a new temperature record
     *
     * @param deviceId device id
     * @param sensorId sensor id
     * @param value value
     * @param timestamp timestamp
     * @param terminalId terminal id
     * @param userId user id
     * @return new temperature record
     */
    suspend fun create(
        deviceId: String,
        sensorId: String,
        value: Float,
        timestamp: Long,
        terminalId: String,
        userId: UUID
    ): TemperatureRecord {
        val temperatureRecord = TemperatureRecord()
        temperatureRecord.id = UUID.randomUUID()
        temperatureRecord.deviceId = deviceId
        temperatureRecord.sensorId = sensorId
        temperatureRecord.timestamp = timestamp
        temperatureRecord.value = value
        temperatureRecord.terminalId = terminalId
        temperatureRecord.creatorId = userId
        temperatureRecord.lastModifierId = userId
        return persistSuspending(temperatureRecord)
    }

    /**
     * Finds a device by device id
     *
     * @param deviceId device id
     * @param terminalId terminal id
     * @return found device
     */
    suspend fun listWithFilters(deviceId: String?, terminalId: String?, first: Int?,max: Int?): Pair<List<TemperatureRecord>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (deviceId != null) {
            stringBuilder.append("deviceId = :deviceId")
            parameters.and("deviceId", deviceId)
        }

        if (terminalId != null && deviceId != null) {
            stringBuilder.append(" AND ")
        }

        if (terminalId != null) {
            stringBuilder.append("terminalId = :terminalId")
            parameters.and("terminalId", terminalId)
        }

        return applyFirstMaxToQuery(find(stringBuilder.toString(), Sort.by("modifiedAt").descending(), parameters), firstIndex = first, maxResults = max)
    }

    /**
     * Lists all temperature records
     *
     * @return pair of list of devices and total count
     */
    suspend fun list(): Pair<List<TemperatureRecord>, Long> {
        return applyFirstMaxToQuery(
            query = findAll(Sort.by("modifiedAt").descending())
        )
    }


}