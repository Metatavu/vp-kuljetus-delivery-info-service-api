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

    /**
     * Create new thermometer
     *
     * @param hardwareSensorId hardware sensor id
     * @param device device
     * @param site site
     * @return created thermometer
     */
    suspend fun createNew(
        hardwareSensorId: String,
        device: Device,
        site: Site
    ): Thermometer {
        return thermometerRepository.create(
            id = UUID.randomUUID(),
            hardwareSensorId = hardwareSensorId,
            espMacAddress = device.deviceId,
            site = site
        )
    }

    /**
     * Archive the old thermometer if it exists
     *
     * @param thermometer thermometer
     * @param device device
     * @param site site
     * @return true if archiving took place
     */
    suspend fun archiveOldThermometer(thermometer: Thermometer?, device: Device, site: Site): Boolean {
        val thermometerIsUnchanged = thermometer?.espMacAddress == device.deviceId && thermometer.site!!.id == site.id

        if (thermometer == null || thermometerIsUnchanged) {
          return false
        }

        val archivedAt = OffsetDateTime.now()
        thermometerRepository.update(
            thermometer = thermometer,
            archivedAt = archivedAt,
            name = thermometer.name,
            modifierId = null
        )

        return true
    }

    /**
     * Creates a thermometer if it does not exist and archives the old one if exists on another device or terminal
     *
     * @param hardwareSensorId sensor id
     * @param device ESP device
     */
    suspend fun onNewSensorData(
        hardwareSensorId: String,
        device: Device
    ): Thermometer {
        val existing = thermometerRepository.findActiveThermometerByDeviceId(hardwareSensorId).first.firstOrNull()

        val archived = archiveOldThermometer(existing, device, device.site)

        if (existing == null || archived) {
            return createNew(hardwareSensorId, device, device.site)
        }

        return existing
    }

    /**
     * List thermometers
     *
     * @param site site
     * @param includeArchived include archived
     * @return thermometers
     */
    suspend fun listThermometers(site: Site?, includeArchived: Boolean): List<Thermometer> {
        return thermometerRepository.list(site, includeArchived).component1()
    }

    /**
     * Deletes a thermometer
     *
     * @param thermometer thermometer
     */
    suspend fun deleteThermometer(thermometer: Thermometer) {
        thermometerRepository.deleteSuspending(thermometer)
    }

    /**
     * Finds a thermometer by id
     *
     * @param id id
     *
     * @return found thermometer or null
     */
    suspend fun findThermometer(id: UUID): Thermometer? {
        return thermometerRepository.findByIdSuspending(id)
    }

    /**
     * Update thermometer name
     *
     * @param thermometerId thermometer id
     * @param name name
     * @param userId userId
     *
     * @return update thermometer
     */
    suspend fun updateThermometerName(thermometerId: UUID, name: String?, userId: UUID): Thermometer {
        val thermometer = thermometerRepository.findByIdSuspending(thermometerId)!!
        return thermometerRepository.update(thermometer = thermometer, archivedAt = thermometer.archivedAt, name = name, modifierId = userId)
    }
}