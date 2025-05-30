package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.SitesApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Site
import fi.metatavu.vp.test.client.models.SiteType
import fi.metatavu.vp.test.client.models.TerminalTemperature
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Sites API
 */
class SiteTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Site, ApiClient>(testBuilder, apiClient) {

    override fun clean(t: Site) {
        api.deleteSite(t.id!!)
    }

    override fun getApi(): SitesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return SitesApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new site
     *
     * @param name optional site name
     * @return created site
     */
    fun create(name: String = "Test site") :Site {
        return create(
            Site(
                name = name,
                location = "POINT (60.16952 24.93545)",
                address = "Test address",
                postalCode = "00100",
                locality = "Helsinki",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            )
        )
    }

    /**
     * Creates new site
     *
     * @param site site data
     * @return created site
     */
    fun create(site: Site): Site {
        return addClosable(api.createSite(site))
    }

    /**
     * Lists site temperatures
     *
     * @param siteId site id
     * @param includeArchived include archived
     * @param first first result index
     * @param max amount of results
     *
     * @return site temperatures
     */
    fun listSiteTemperatures(siteId: UUID, includeArchived: Boolean, first: Int?, max: Int?): List<TerminalTemperature> {
        return api.listSiteTemperatures(siteId = siteId, includeArchived = includeArchived, first = first, max = max).toList()
    }

    /**
     * Asserts that site creation fails with expected status
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
     * Finds site
     *
     * @param siteId site id
     * @return found site
     */
    fun findSite(siteId: UUID): Site {
        return api.findSite(siteId)
    }

    /**
     * Asserts that site find fails with expected status
     *
     * @param siteId site id
     * @param expectedStatus expected status
     */
    fun assertFindSiteFail(siteId: UUID, expectedStatus: Int) {
        try {
            findSite(siteId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Updates site
     *
     * @param id site id
     * @param site site to update
     * @return updated site
     */
    fun updateSite(id: UUID, site: Site): Site {
        return api.updateSite(id, site)
    }

    /**
     * Asserts that site update fails with expected status
     *
     * @param id site id
     * @param expectedStatus expected status
     * @param updateData update data
     */
    fun assertUpdateSiteFail(id: UUID, expectedStatus: Int, updateData: Site) {
        try {
            updateSite(id, updateData)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
    /**
     * Lists sites
     *
     * @param archived archived
     * @param thermometerId
     * @param first first result
     * @param max max results
     * @return list of sites
     */
    fun listSites(archived: Boolean? = null, thermometerId: UUID? = null, first: Int? = null, max: Int? = null): Array<Site> {
        return api.listSites(archived = archived, thermometerId = thermometerId, first = first, max = max)
    }

    /**
     * Asserts that site listing fails with expected status
     *
     * @param expectedStatus expected status
     */
    fun assertListSitesFail(expectedStatus: Int, thermometerId: UUID? = null) {
        try {
            listSites(thermometerId = thermometerId)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that site temperature listing fails with expected status
     *
     * @param siteId site id
     * @param expectedStatus expected status
     */
    fun assertListSiteTemperaturesFail(siteId: UUID, expectedStatus: Int) {
        try {
            listSiteTemperatures(siteId = siteId, includeArchived = false, null, null)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes site
     *
     * @param siteId site id
     */
    fun deleteSite(siteId: UUID) {
        api.deleteSite(siteId)
        removeCloseable { closable: Any ->
            if (closable !is Site) {
                return@removeCloseable false
            }

            closable.id == siteId
        }
    }

    /**
     * Asserts that site deletion fails with expected status
     *
     * @param siteId site id
     * @param expectedStatus expected status
     */
    fun assertDeleteSiteFail(siteId: UUID, expectedStatus: Int) {
        try {
            deleteSite(siteId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}