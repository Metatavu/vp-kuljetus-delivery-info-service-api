package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.devices.Device
import fi.metatavu.vp.deliveryinfo.sites.Site
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Thermometer controller
 */
@ApplicationScoped
class ThermometerController {
    @Inject
    lateinit var thermometerRepository: ThermometerRepository

    suspend fun createNew(
        hardwareSensorId: String,
        device: Device,
        site: Site,
        userId: UUID
    ): Thermometer {
        return thermometerRepository.create(
            id = UUID.randomUUID(),
            hardwareSensorId = hardwareSensorId,
            espMacAddress = device.deviceId,
            site = site,
            userId = userId
        )
    }

    suspend fun archive(thermometer: Thermometer?, device: Device, site: Site): Boolean {
        if (thermometer != null && (thermometer.espMacAddress != device.deviceId && thermometer.site!!.id != site.id)) {
            val archivedAt = OffsetDateTime.now()
            thermometerRepository.update(thermometer = thermometer, archivedAt = archivedAt, name = thermometer.name)
            return true
        }

        return false

    }

    /**
     * Creates a thermometer if it does not exist and archives the old one if exists on another device or terminal
     *
     * @param hardwareSensorId sensor id
     * @param device ESP device
     * @param site site (terminal)
     * @param userId user id
     */
    suspend fun onNewSensorData(
        hardwareSensorId: String,
        device: Device,
        site: Site,
        userId: UUID
    ) {
        val existing = thermometerRepository.findActiveThermometerByDeviceId(device.deviceId).component1().firstOrNull()

        val archived = archive(existing, device, site)

        if (existing == null || archived) {
            createNew(hardwareSensorId, device, site, userId)
        }
    }
}