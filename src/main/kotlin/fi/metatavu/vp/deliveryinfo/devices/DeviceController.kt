package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.freights.Freight
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
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

    /**
     * Lists all devices
     *
     * @return pair of list of devices and total count
     */
    suspend fun listAll(): List<Device> {
        val (devices, count) = deviceRepository.list()
        return devices
    }

    /**
     * Deletes a device
     */
    suspend fun delete(device: Device) {
        deviceRepository.deleteSuspending(device)
    }

    /**
     * Updates device sites
     *
     * @param site site
     * @param deviceIds new device ids
     */
    suspend fun updateDevices(site: Site, deviceIds: List<String>) {
        deviceRepository.listBySite(site).component1().forEach { delete(it) }
        deviceIds.forEach { create(it, site) }
    }
}