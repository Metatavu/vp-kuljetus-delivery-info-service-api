package fi.metatavu.vp.deliveryinfo.temperature

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
    suspend fun list(thermometer: Thermometer?): List<Temperature> {
        return temperatureRepository.list(thermometer).component1()
    }

    suspend fun delete(temperature: Temperature) {
        temperatureRepository.deleteSuspending(temperature)
    }
}