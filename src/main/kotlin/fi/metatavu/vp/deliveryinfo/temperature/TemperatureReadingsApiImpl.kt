package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.api.model.TerminalTemperatureReading
import fi.metatavu.vp.api.spec.TemperatureReadingsApi
import fi.metatavu.vp.deliveryinfo.devices.DeviceController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.thermometers.ThermometerController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
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

    @Inject
    lateinit var temperatureController: TemperatureController

    @WithTransaction
    override fun createTemperatureReading(temperatureReading: TerminalTemperatureReading): Uni<Response> = withCoroutineScope {
        if (requestTerminalDeviceKey != terminalDeviceApiKeyValue) return@withCoroutineScope createForbidden(INVALID_API_KEY)
        val device = deviceController.findByDeviceId(temperatureReading.deviceIdentifier)
            ?: return@withCoroutineScope createBadRequest("Device with address ${temperatureReading.deviceIdentifier} does not exist")

        val thermometer = thermometerController.onNewSensorData(hardwareSensorId = temperatureReading.hardwareSensorId, device = device)
        temperatureController.create(
            thermometer = thermometer,
            timestamp = temperatureReading.timestamp,
            value = temperatureReading.value
        )

        createNoContent()
    }
}