package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.api.model.TemperatureReading
import fi.metatavu.vp.api.spec.TemperatureReadingsApi
import fi.metatavu.vp.deliveryinfo.devices.DeviceController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.thermometers.ThermometerController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

/**
 * Temperature readings API implementation
 */
@RequestScoped
@Suppress("unused")
@WithSession
class TemperatureReadingsApiImpl: TemperatureReadingsApi, AbstractApi() {

    @Inject
    lateinit var deviceController: DeviceController

    @Inject
    lateinit var thermometerController: ThermometerController

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTemperatureReading(temperatureReading: TemperatureReading): Uni<Response> = withCoroutineScope {
        val device = deviceController.findByDeviceId(temperatureReading.espMacAddress)
            ?: return@withCoroutineScope createBadRequest("Device with address ${temperatureReading.espMacAddress} does not exist")

        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        thermometerController.onNewSensorData(temperatureReading.hardwareSensorId, device, userId)

        createNoContent()
    }
}