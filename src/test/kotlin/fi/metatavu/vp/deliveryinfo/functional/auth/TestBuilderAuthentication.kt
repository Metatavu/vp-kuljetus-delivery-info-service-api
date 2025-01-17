package fi.metatavu.vp.deliveryinfo.functional.auth

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.impl.*
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings

/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider
) : AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    val sites = SiteTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val freights = FreightTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val freightUnits = FreightUnitTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val tasks = TaskTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val temperatureRecords = TemperatureRecordTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))

    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider.accessToken
        return result
    }

}