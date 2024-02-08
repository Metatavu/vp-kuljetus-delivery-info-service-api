package fi.metatavu.vp.deliveryinfo.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.TestBuilder
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.apis.SitesApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.Site
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
     * @return created site
     */
    fun create() :Site {
        return create(Site(name = "Test site", location = "POINT (60.16952 24.93545)"))
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
     * Finds site
     *
     * @param siteId site id
     * @return found site
     */
    fun findSite(siteId: UUID): Site {
        return api.findSite(siteId)
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
     * Lists sites
     *
     * @param first first result
     * @param max max results
     * @return list of sites
     */
    fun listSites(first: Int? = null, max: Int? = null): Array<Site> {
        return api.listSites(first = first, max = max)
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
}