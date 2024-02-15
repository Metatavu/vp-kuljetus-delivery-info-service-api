package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.FreightUnitsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.FreightUnit
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for FreightUnits API
 */
class FreightUnitTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<FreightUnit, ApiClient>(testBuilder, apiClient) {
    override fun clean(t: FreightUnit) {
        api.deleteFreightUnit(t.id!!)
    }

    override fun getApi(): FreightUnitsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return FreightUnitsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new FreightUnit
     *
     * @return created FreightUnit
     */
    fun create(freightId: UUID): FreightUnit {
        return create(
            FreightUnit(
                freightId = freightId,
                quantityUnit = "pc",
                type = "type",
                quantity = "quantity",
                reservations = "reservations"
            )
        )
    }

    /**
     * Creates new FreightUnit
     *
     * @param freightUnit FreightUnit data
     * @return created FreightUnit
     */
    fun create(freightUnit: FreightUnit): FreightUnit {
        return addClosable(api.createFreightUnit(freightUnit))
    }

    /**
     * Asserts that FreightUnit creation fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertCreateFail(expectedStatus: Int, freightId: UUID) {
        try {
            create(freightId)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Finds FreightUnit
     *
     * @param id id
     * @return found FreightUnit
     */
    fun findFreightUnit(id: UUID): FreightUnit {
        return api.findFreightUnit(id)
    }

    /**
     * Asserts that FreightUnit find fails with expected status
     *
     * @param id id
     * @param expectedStatus expected status
     */
    fun assertFindFreightUnitFail(id: UUID, expectedStatus: Int) {
        try {
            findFreightUnit(id)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates FreightUnit
     *
     * @param id FreightUnit id
     * @param freightUnit FreightUnit to update
     * @return updated FreightUnit
     */
    fun updateFreightUnit(id: UUID, freightUnit: FreightUnit): FreightUnit {
        return api.updateFreightUnit(id, freightUnit)
    }

    /**
     * Asserts that FreightUnit update fails with expected status
     *
     * @param id freightUnit id
     * @param expectedStatus expected status
     */
    fun assertUpdateFreightUnitFail(id: UUID, expectedStatus: Int, freightId: UUID) {
        try {
            updateFreightUnit(
                id, FreightUnit(
                    freightId = freightId,
                    quantityUnit = "pc",
                    type = "type",
                    quantity = "quantity",
                    reservations = "reservations"
                )
            )
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists freightUnits
     *
     * @param freightId freight id
     * @param first first result
     * @param max max results
     * @return list of freightUnits
     */
    fun listFreightUnits(freightId: UUID? = null, first: Int? = null, max: Int? = null): Array<FreightUnit> {
        return api.listFreightUnits(freightId = freightId, first = first, max = max)
    }

    /**
     * Asserts that freightUnit listing fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListFreightUnitsFail(expectedStatus: Int) {
        try {
            listFreightUnits()
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes freightUnit
     *
     * @param freightUnitId freightUnit id
     */
    fun deleteFreightUnit(freightUnitId: UUID) {
        api.deleteFreightUnit(freightUnitId)
        removeCloseable { closable: Any ->
            if (closable !is FreightUnit) {
                return@removeCloseable false
            }

            closable.id == freightUnitId
        }
    }

    /**
     * Asserts that freightUnit deletion fails with expected status
     *
     * @param id id
     * @param expectedStatus expected status
     */
    fun assertDeleteFreightUnitFail(id: UUID, expectedStatus: Int) {
        try {
            deleteFreightUnit(id)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}