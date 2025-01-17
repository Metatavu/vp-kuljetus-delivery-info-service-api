package fi.metatavu.vp.deliveryinfo.temperature

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
     * @param userId user id
     * @return new temperature record
     */
    suspend fun create(
        deviceId: String,
        sensorId: String,
        value: Float,
        timestamp: Long,
        userId: UUID,
    ): TemperatureRecord {
        val temperatureRecord = TemperatureRecord()
        temperatureRecord.id = UUID.randomUUID()
        temperatureRecord.deviceId = deviceId
        temperatureRecord.sensorId = sensorId
        temperatureRecord.timestamp = timestamp
        temperatureRecord.value = value
        temperatureRecord.creatorId = userId
        temperatureRecord.lastModifierId = userId
        return persistSuspending(temperatureRecord)
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