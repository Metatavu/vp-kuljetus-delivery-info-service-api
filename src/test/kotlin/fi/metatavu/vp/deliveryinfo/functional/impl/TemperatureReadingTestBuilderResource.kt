package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.TemperatureReadingsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.TemperatureReading

/**
 * Test builder resource for Tasks API
 */
class TemperatureReadingTestBuilderResource(
    testBuilder: TestBuilder,
    private val terminalDeviceApiKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<TemperatureReading, ApiClient>(testBuilder, apiClient) {
    override fun clean(p0: TemperatureReading?) {}


    override fun getApi(): TemperatureReadingsApi {
        if (terminalDeviceApiKey != null) {
            ApiClient.apiKey["X-DataReceiver-API-Key"] = terminalDeviceApiKey
        }
        return TemperatureReadingsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Sends a new temperature reading
     *
     * @param temperatureReading temperature reading
     */
    fun createTemperatureReading(
        temperatureReading: TemperatureReading
    ) {
        api.createTemperatureReading(temperatureReading)
    }


}