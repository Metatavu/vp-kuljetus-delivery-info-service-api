package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.TemperatureRecordsApi
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.TemperatureRecord
import java.util.UUID


/**
 * Test builder resource for Sites API
 */
class TemperatureRecordTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<TemperatureRecord, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: TemperatureRecord) {
        api.deleteTemperatureRecord(t.id!!)
    }

    override fun getApi(): TemperatureRecordsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TemperatureRecordsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new temperature record
     *
     * @param temperatureRecord temperature record
     * @return created site
     */
    fun create(temperatureRecord: TemperatureRecord): TemperatureRecord {
        return addClosable(api.createTemperatureRecord(temperatureRecord))
    }

    fun list(terminalId: UUID?, deviceId: String?): List<TemperatureRecord> {
        return api.listTemperatureRecords(terminalId = terminalId, deviceId = deviceId).toList()
    }
}