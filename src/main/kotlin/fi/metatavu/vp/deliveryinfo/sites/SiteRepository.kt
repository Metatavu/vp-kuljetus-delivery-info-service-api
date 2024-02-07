package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for sites
 */
@ApplicationScoped
class SiteRepository: AbstractRepository<Site, UUID>() {

    /**
     * Creates a new site
     *
     * @param id id
     * @param name name
     * @param location location
     * @param creatorId creator id
     * @return created site
     */
    suspend fun create(id: UUID?, name: String, location: String, creatorId: UUID): Site {
        val site = Site()
        site.id = id
        site.name = name
        site.location = location
        site.creatorId = creatorId
        site.lastModifierId = creatorId
        return persistSuspending(site)
    }

}