package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.devices.Device
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository for thermometers
 */
@ApplicationScoped
class ThermometerRepository : AbstractRepository<Thermometer, UUID>() {
    /**
     * Inserts a new thermometer into the database
     *
     * @param id id
     * @param hardwareSensorId sensor id
     * @param espMacAddress ESP ḾAC address
     * @param site site (terminal)
     * @param userId user id
     */
    suspend fun create(
        id: UUID,
        hardwareSensorId: String,
        espMacAddress: String,
        site: Site
    ): Thermometer {
        val thermometer = Thermometer()
        thermometer.id = id
        thermometer.hardwareSensorId = hardwareSensorId
        thermometer.espMacAddress = espMacAddress
        thermometer.site = site
        return persistSuspending(thermometer)
    }

    /**
     * Updates thermometer
     *
     * @param thermometer thermometer to update
     * @param name new name
     * @param archivedAt time of archiving
     * @param modifierId modifier id
     *
     * @return updated thermometer
     */
    suspend fun update(thermometer: Thermometer, name: String?, archivedAt: OffsetDateTime?, modifierId: UUID?): Thermometer {
        thermometer.name = name
        thermometer.archivedAt = archivedAt
        thermometer.lastModifierId = modifierId
        return persistSuspending(thermometer)
    }

    /**
     * Finds a thermometer by sensor id
     *
     * @param sensorId sensor id
     * @return found thermometer
     */
    suspend fun findActiveThermometerByDeviceId(sensorId: String): Pair<List<Thermometer>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()
        stringBuilder.append("archivedAt IS NULL AND ")
        stringBuilder.append("hardwareSensorId = :sensorid")
        parameters.and("sensorid", sensorId)

        return applyFirstMaxToQuery(find(stringBuilder.toString(), parameters))
    }

    /**
     * List thermometers
     *
     * @param site site
     * @param includeArchived include archived
     * @return thermometers
     */
    suspend fun list(site: Site?, includeArchived: Boolean): Pair<List<Thermometer>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (site != null) {
            addCondition(stringBuilder, "site = :site")
            parameters.and("site", site)
        }

        if (!includeArchived) {
            addCondition(stringBuilder, "archivedAt is null")
        }


        return applyFirstMaxToQuery(find(stringBuilder.toString(), parameters))
    }
}