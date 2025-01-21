package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.deliveryinfo.thermometers.Thermometer
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for temperature
 */
@ApplicationScoped
class TemperatureRepository: AbstractRepository<Temperature, UUID>() {

    /**
     * Creates new temperature reading
     *
     * @param id id
     * @param thermometer thermometer
     * @param value value
     * @param timestamp timestamp
     * @return created temperature
     */
    suspend fun create(
        id: UUID,
        thermometer: Thermometer,
        value: Float,
        timestamp: Long
    ): Temperature {
        val temperatureReading = Temperature()
        temperatureReading.id = id
        temperatureReading.thermometer = thermometer
        temperatureReading.value = value
        temperatureReading.timestamp = timestamp
        return persistSuspending(temperatureReading)
    }

    /**
     * List thermometer
     *
     * @param thermometer thermometer
     * @return thermometers
     */
    suspend fun list(thermometer: Thermometer?): Pair<List<Temperature>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (thermometer != null) {
            addCondition(stringBuilder, "thermometer = :thermometer")
            parameters.and("thermometer", thermometer)
        }

        return applyFirstMaxToQuery(find(stringBuilder.toString(), parameters))
    }
}