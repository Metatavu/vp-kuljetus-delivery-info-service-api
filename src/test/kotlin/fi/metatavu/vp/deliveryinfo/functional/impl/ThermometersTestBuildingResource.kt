package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.ThermometersApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.TerminalThermometer
import fi.metatavu.vp.test.client.models.UpdateTerminalThermometerRequest
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Tasks API
 */
class ThermometersTestBuildingResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<TerminalThermometer, ApiClient>(testBuilder, apiClient) {
    override fun clean(p0: TerminalThermometer?) {}

    override fun getApi(): ThermometersApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ThermometersApi(ApiTestSettings.apiBasePath)
    }

    /**
     * List thermometers
     *
     * @param siteId site id
     * @param archived archived
     *
     * @return thermometers
     */
    fun listThermometers(siteId: UUID?, archived: Boolean): List<TerminalThermometer> {
        return api.listTerminalThermometers(siteId, archived).toList()
    }

    /**
     * Finds the thermometer by id
     *
     * @param id thermometer id
     *
     * @return found thermometer
     */
    fun findThermometer(id: UUID): TerminalThermometer {
        return api.findTerminalThermometer(id)
    }

    /**
     * Update thermometer
     *
     * @param id id
     * @param thermometer thermometer
     *
     * @return updated thermometer
     */
    fun updateThermometer(id: UUID, thermometer: UpdateTerminalThermometerRequest): TerminalThermometer {
        return api.updateTerminalThermometer(id, thermometer)
    }

    /**
     * Asserts that thermometer listing fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListThermometersFail(expectedStatus: Int) {
        try {
            listThermometers(null, false)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that thermometer finding fails with expected status
     *
     * @param id thermometer id
     * @param expectedStatus expected status
     */
    fun assertFindThermometerFail(id: UUID, expectedStatus: Int) {
        try {
            findThermometer(id)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that thermometer update fails with expected status
     * 
     * @param id thermometer id
     * @param expectedStatus expected status
     */
    fun assertUpdateThermometerFail(id: UUID, expectedStatus: Int) {
        try {
            updateThermometer(id, UpdateTerminalThermometerRequest(name = "name"))
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }



}