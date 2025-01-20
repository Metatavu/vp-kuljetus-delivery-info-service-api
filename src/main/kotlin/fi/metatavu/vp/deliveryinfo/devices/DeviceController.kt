package fi.metatavu.vp.deliveryinfo.devices

import fi.metatavu.vp.deliveryinfo.freights.Freight
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.kotlin.coroutines.awaitResult
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
     * @param userId
     * @return new device
     */
    suspend fun create(
        deviceId: String,
        site: Site,
        userId: UUID
    ): Device {
        return deviceRepository.create(deviceId, site, userId)
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
        return deviceRepository.list()
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
     * @param userId user id
     */
    suspend fun updateDevices(site: Site, deviceIds: List<String>, userId: UUID) {
        val existingDevices = listBySite(site).component1()
        val devicesToRemove = existingDevices.filter { device -> deviceIds.find { it == device.deviceId } == null  }
        val deviceIdsToAdd = deviceIds.filter { deviceId -> existingDevices.find { it.deviceId == deviceId } == null  }

        deviceIdsToAdd.forEach { create(it, site, userId) }
        devicesToRemove.forEach { delete(it) }
    }
}