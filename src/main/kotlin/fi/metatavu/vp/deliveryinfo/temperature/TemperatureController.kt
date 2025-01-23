package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.deliveryinfo.thermometers.Thermometer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for temperature
 */
@ApplicationScoped
class TemperatureController {

    @Inject
    lateinit var temperatureRepository: TemperatureRepository

    /**
     * Create new temperature
     *
     * @param thermometer thermometer
     * @param timestamp timestamp
     * @param value value
     *
     * @return new temperature
     */
    suspend fun create(
        thermometer: Thermometer,
        timestamp: Long,
        value: Float
    ): Temperature {
        return temperatureRepository.create(
            id = UUID.randomUUID(),
            thermometer = thermometer,
            timestamp = timestamp,
            value = value
        )
    }

    /**
     * Lists temperature
     *
     * @param thermometer thermometer
     *
     * @return temperature
     */
    suspend fun listByThermometer(thermometer: Thermometer?): List<Temperature> {
        return temperatureRepository.list(thermometer = thermometer, site = null, first = null, max = null, includeArchived = true).component1()
    }

    /**
     * Lists temperature
     *
     * @param site site
     * @param includeArchived include archived
     * @param first first
     * @param max max
     *
     * @return temperature
     */
    suspend fun list(site: Site, includeArchived: Boolean, first: Int?, max: Int?): Pair<List<Temperature>, Long> {
        return temperatureRepository.list(
            thermometer = null,
            site = site,
            includeArchived = includeArchived,
            first = first,
            max = max)
    }

    /**
     * Deletes temperature
     *
     * @param temperature temperature to delete
     */
    suspend fun delete(temperature: Temperature) {
        temperatureRepository.deleteSuspending(temperature)
    }
}