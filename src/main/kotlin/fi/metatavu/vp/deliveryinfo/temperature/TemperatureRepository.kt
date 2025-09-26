package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.deliveryinfo.thermometers.Thermometer
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
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
     * List temperature by thermometer
     *
     * @param thermometer thermometer
     * @return temperature
     */
    suspend fun listByThermometer(thermometer: Thermometer): List<Temperature> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        addCondition(stringBuilder, "thermometer = :thermometer")
        parameters.and("thermometer", thermometer)

        return list(stringBuilder.toString(), Sort.by("timestamp").descending(), parameters).awaitSuspending()
    }

    /**
     * List temperature
     *
     * @param thermometer thermometer
     * @param site site
     * @param includeArchived include archived
     * @param first first result index
     * @param max amount of results
     * @return temperature
     */
    suspend fun list(thermometer: Thermometer?, site: Site?, includeArchived: Boolean?, first: Int?, max: Int?, createdBefore: Long?, createdAfter: Long?): Pair<List<Temperature>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (thermometer != null) {
            addCondition(stringBuilder, "thermometer = :thermometer")
            parameters.and("thermometer", thermometer)
        }

        if (site != null) {
            addCondition(stringBuilder, "thermometer.site = :site")
            parameters.and("site", site)
        }

        if (includeArchived != true) {
            addCondition(stringBuilder, "thermometer.archivedAt is null")
        }

        if (createdAfter != null) {
            addCondition(stringBuilder, "timestamp > :createdAfter")
            parameters.and("createdAfter", createdAfter)
        }

        if (createdBefore != null) {
            addCondition(stringBuilder, "timestamp < :createdBefore")
            parameters.and("createdBefore", createdBefore)
        }

        return applyFirstMaxToQuery(find(stringBuilder.toString(), Sort.by("timestamp").descending(), parameters), firstIndex = first, maxResults = max)
    }
}