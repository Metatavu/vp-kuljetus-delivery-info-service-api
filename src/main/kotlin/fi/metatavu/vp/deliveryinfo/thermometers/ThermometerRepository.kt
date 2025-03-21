package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.devices.Device
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
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
     * @param deviceIdentifier device identifier
     * @param site site (terminal)
     */
    suspend fun create(
        id: UUID,
        hardwareSensorId: String,
        deviceIdentifier: String,
        site: Site
    ): Thermometer {
        val thermometer = Thermometer()
        thermometer.id = id
        thermometer.hardwareSensorId = hardwareSensorId
        thermometer.deviceIdentifier = deviceIdentifier
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

        if (modifierId != null) {
            thermometer.lastModifierId = modifierId
        }

        return persistSuspending(thermometer)
    }

    /**
     * Finds a thermometer by sensor id
     *
     * @param sensorId sensor id
     * @return found thermometer
     */
    suspend fun findActiveThermometerByDeviceId(sensorId: String): Thermometer? {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()
        stringBuilder.append("archivedAt IS NULL AND ")
        stringBuilder.append("hardwareSensorId = :sensorid")
        parameters.and("sensorid", sensorId)

        return find(stringBuilder.toString(), parameters).firstResult<Thermometer?>().awaitSuspending()
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