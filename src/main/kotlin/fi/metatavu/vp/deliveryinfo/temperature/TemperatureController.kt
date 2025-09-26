package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.deliveryinfo.thermometers.Thermometer
import fi.metatavu.vp.messaging.GlobalEventController
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

/**
 * Controller for temperature
 */
@ApplicationScoped
class TemperatureController {

    @Inject
    lateinit var temperatureRepository: TemperatureRepository

    @Inject
    lateinit var globalEventController: GlobalEventController

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
        globalEventController.publish(
            TemperatureGlobalEvent(
                temperature = value,
                thermometerId = thermometer.id,
                timestamp = timestamp
            )
        )

        return temperatureRepository.create(
            id = UUID.randomUUID(),
            thermometer = thermometer,
            timestamp = timestamp,
            value = value
        )
    }

    /**
     * Lists temperature by thermometer
     *
     * @param thermometer thermometer
     *
     * @return temperature
     */
    suspend fun listByThermometer(thermometer: Thermometer): List<Temperature> {
        return temperatureRepository.listByThermometer(thermometer)
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
    suspend fun list(site: Site, includeArchived: Boolean, first: Int?, max: Int?, createdAfter: OffsetDateTime?, createdBefore: OffsetDateTime?): Pair<List<Temperature>, Long> {
        val createdBeforeEpochSeconds = createdBefore?.atZoneSameInstant(ZoneId.of("Europe/Helsinki"))?.toEpochSecond()
        val createdAfterEpochSeconds = createdAfter?.atZoneSameInstant(ZoneId.of("Europe/Helsinki"))?.toEpochSecond()
        return temperatureRepository.list(
            thermometer = null,
            site = site,
            includeArchived = includeArchived,
            first = first,
            max = max,
            createdBefore = createdBeforeEpochSeconds,
            createdAfter = createdAfterEpochSeconds)
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