package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.freights.freightunits.FreightUnit
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort

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
     * @return pair of list of devices and total count
     */
    suspend fun list(): Pair<List<Device>, Long> {
        return applyFirstMaxToQuery(
            query = findAll(Sort.by("modifiedAt").descending())
        )
    }


}