package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.freights.freightunits.FreightUnit
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending

/**
 * Repository for sites
 */
@ApplicationScoped
class DeviceRepository: AbstractRepository<Device, UUID>() {

    /**
     * Creates a new device
     *
     * @param deviceId device id
     * @param site site
     * @param userId user id
     * @return new device
     */
    suspend fun create(
        deviceId: String,
        site: Site,
        userId: UUID,
    ): Device {
        val device = Device()
        device.id = UUID.randomUUID()
        device.deviceId = deviceId
        device.site = site
        device.creatorId = userId
        device.lastModifierId = userId
        return persistSuspending(device)
    }

    /**
     * Finds a device by its device id
     *
     * @param deviceId device id
     * @return found device or null
     */
    suspend fun findByDeviceId(deviceId: String): Device? {
        return find("deviceId = :deviceId", Parameters.with("deviceId", deviceId)).firstResult<Device().awaitSuspending()`
    }

    /**
     * Lists devices
     *
     * @param site site
     * @return pair of list of devices and total count
     */
    suspend fun listBySite(site: Site): Pair<List<Device>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        stringBuilder.append("site = :site")
        parameters.and("site", site)

        return applyFirstMaxToQuery(find(stringBuilder.toString(), Sort.by("modifiedAt").descending(), parameters ))
    }

    /**
     * Lists all devices
     *
     * @return list of devices
     */
    suspend fun list(): List<Device> {
        return listAll(Sort.by("modifiedAt").descending()).awaitSuspending()
    }


}