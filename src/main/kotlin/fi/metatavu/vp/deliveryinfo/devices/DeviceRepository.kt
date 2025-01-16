package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*
import fi.metatavu.vp.deliveryinfo.sites.Site
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
        site: Site
    ): Device {
        val device = Device()
        device.id = UUID.randomUUID()
        device.deviceId = deviceId
        device.site = site
        return persistSuspending(device)
    }

    /**
     * Lists devices by site
     *
     * @param site site
     * @return pair of list of devices and total count
     */
    suspend fun listBySite(site: Site): Pair<List<Device>, Long> {
        val query = "site_id = ${site.id}"
        return applyFirstMaxToQuery(
            query = find(query, Sort.by("modifiedAt").descending())
        )
    }


}