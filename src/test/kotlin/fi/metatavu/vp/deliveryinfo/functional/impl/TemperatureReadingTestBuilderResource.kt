package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.TemperatureReadingsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.TerminalTemperatureReading
import org.junit.Assert

/**
 * Test builder resource for Tasks API
 */
class TemperatureReadingTestBuilderResource(
    testBuilder: TestBuilder,
    private val terminalDeviceApiKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<TerminalTemperatureReading, ApiClient>(testBuilder, apiClient) {
    override fun clean(p0: TerminalTemperatureReading?) {}


    override fun getApi(): TemperatureReadingsApi {
        if (terminalDeviceApiKey != null) {
            ApiClient.apiKey["X-TerminalDevice-API-Key"] = terminalDeviceApiKey
        }
        return TemperatureReadingsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Sends a new temperature reading
     *
     * @param temperatureReading temperature reading
     */
    fun createTemperatureReading(
        temperatureReading: TerminalTemperatureReading
    ) {
        api.createTemperatureReading(temperatureReading)
    }

    /**
     * Asserts that creating a temperature reading fails with expected status
     *
     * @param temperatureReading temperature reading
     * @param expectedStatus expected status
     */
    fun assertCreateTemperatureReadingFail(temperatureReading: TerminalTemperatureReading, expectedStatus: Int) {
        try {
            createTemperatureReading(temperatureReading)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }


}