package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.FreightsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Freight
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Freights API
 */
class FreightTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Freight, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: Freight) {
        api.deleteFreight(t.id!!)
    }

    override fun getApi(): FreightsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return FreightsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new Freight
     *
     * @return created Freight
     */
    fun create(): Freight {
        return create(
            Freight(
                pointOfDeparture = "departure",
                sender = "sender",
                recipient = "recipient",
                payer = "payer",
                temperatureMin = 1.0,
                temperatureMax = 2.0,
                reservations = "reservations",
                destination = "destination"
            )
        )
    }

    /**
     * Creates new Freight
     *
     * @param freight Freight data
     * @return created Freight
     */
    fun create(freight: Freight): Freight {
        return addClosable(api.createFreight(freight))
    }

    /**
     * Asserts that Freight creation fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertCreateFail(expectedStatus: Int) {
        try {
            create()
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds Freight
     *
     * @param id id
     * @return found Freight
     */
    fun findFreight(id: UUID): Freight {
        return api.findFreight(id)
    }

    /**
     * Asserts that Freight find fails with expected status
     *
     * @param id id
     * @param expectedStatus expected status
     */
    fun assertFindFreightFail(id: UUID, expectedStatus: Int) {
        try {
            findFreight(id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates Freight
     *
     * @param id Freight id
     * @param freight Freight to update
     * @return updated Freight
     */
    fun updateFreight(id: UUID, freight: Freight): Freight {
        return api.updateFreight(id, freight)
    }

    /**
     * Asserts that Freight update fails with expected status
     *
     * @param id freight id
     * @param expectedStatus expected status
     */
    fun assertUpdateFreightFail(id: UUID, expectedStatus: Int) {
        try {
            updateFreight(
                id, Freight(
                    pointOfDeparture = "departure",
                    sender = "sender",
                    recipient = "recipient",
                    payer = "payer",
                    temperatureMin = 1.0,
                    temperatureMax = 2.0,
                    reservations = "reservations",
                    destination = "destination"
                )
            )
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists freights
     *
     * @param first first result
     * @param max max results
     * @return list of freights
     */
    fun listFreights(first: Int? = null, max: Int? = null): Array<Freight> {
        return api.listFreights(first = first, max = max)
    }

    /**
     * Asserts that freight listing fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListFreightsFail(expectedStatus: Int) {
        try {
            listFreights()
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes freight
     *
     * @param freightId freight id
     */
    fun deleteFreight(freightId: UUID) {
        api.deleteFreight(freightId)
        removeCloseable { closable: Any ->
            if (closable !is Freight) {
                return@removeCloseable false
            }

            closable.id == freightId
        }
    }

    /**
     * Asserts that freight deletion fails with expected status
     *
     * @param freightId freight id
     * @param expectedStatus expected status
     */
    fun assertDeleteFreightFail(freightId: UUID, expectedStatus: Int) {
        try {
            deleteFreight(freightId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}