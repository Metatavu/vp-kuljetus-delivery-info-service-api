package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.freights.Freight
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Sort
import java.util.*


/**
 * Controller for devices
 */
@ApplicationScoped
class DeviceController {
    @Inject
    lateinit var deviceRepository: DeviceRepository

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
        return deviceRepository.create(deviceId, site)
    }

    /**
     * Lists devices by site
     *
     * @param site site
     * @return pair of list of devices and total count
     */
    suspend fun listBySite(site: Site): Pair<List<Device>, Long> {
        return deviceRepository.listBySite(site)
    }
}