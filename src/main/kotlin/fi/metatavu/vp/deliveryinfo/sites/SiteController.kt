package fi.metatavu.vp.deliveryinfo.sites

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.locationtech.jts.io.WKTReader
import java.util.*

/**
 * Controller for sites
 */
@ApplicationScoped
class SiteController {

    @Inject
    lateinit var siteRepository: SiteRepository

    private val reader = WKTReader();

    /**
     * Checks if the given location is a valid point
     *
     * @param location location
     * @return true if the location is a valid point
     */
    fun isValidPoint(location: String): Boolean {
        try {
            reader.read(location)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Lists sites
     *
     * @param first first result
     * @param max max results
     * @return list of sites
     */
    suspend fun listSites(first: Int?, max: Int?): Pair<List<Site>, Long> {
        return siteRepository.listAllSuspending(first, max)
    }

    /**
     * Creates a new site
     *
     * @param site site
     * @param userId user id
     * @return created site
     */
    suspend fun createSite(site: fi.metatavu.vp.api.model.Site, userId: UUID): Site {
        return siteRepository.create(
            id = UUID.randomUUID(),
            name = site.name,
            location = site.location,
            creatorId = userId
        )
    }

    /**
     * Finds a site by id
     *
     * @param siteId site id
     * @return found site or null if not found
     */
    suspend fun findSite(siteId: UUID): Site? {
        return siteRepository.findByIdSuspending(siteId)
    }

    /**
     * Updates a site
     *
     * @param existingSite existing site
     * @param site site
     * @param userId user id
     * @return updated site
     */
    suspend fun updateSite(existingSite: Site, site: fi.metatavu.vp.api.model.Site, userId: UUID): Site {
        existingSite.name = site.name
        existingSite.location = site.location
        existingSite.lastModifierId = userId
        return siteRepository.persistSuspending(existingSite)
    }

    /**
     * Deletes a site
     *
     * @param site site
     */
    suspend fun deleteSite(site: Site) {
        siteRepository.deleteSuspending(site)
    }
}