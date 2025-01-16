package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.api.model.SiteType
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import io.quarkus.panache.common.Sort
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
     * @param name name
     * @param latitude latitude
     * @param longitude longitude
     * @param address address
     * @param postalCode postal code
     * @param locality locality
     * @param siteType site type
     * @param additionalInfo additional info
     * @param creatorId creator id
     * @return created site
     */
    suspend fun create(
        name: String,
        latitude: Double,
        longitude: Double,
        address: String,
        postalCode: String,
        locality: String,
        siteType: SiteType,
        additionalInfo: String?,
        creatorId: UUID
    ): Site {
        val site = Site()
        site.id = UUID.randomUUID()
        site.name = name
        site.latitude = latitude
        site.longitude = longitude
        site.address = address
        site.postalCode = postalCode
        site.locality = locality
        site.siteType = siteType.toString()
        site.additionalInfo = additionalInfo
        site.creatorId = creatorId
        site.lastModifierId = creatorId
        return persistSuspending(site)
    }

    /**
     * Lists sites
     *
     * @param archived archived
     * @param first first result
     * @param max max results
     * @return list of sites
     */
    suspend fun list(archived: Boolean?, first: Int?, max: Int?): Pair<List<Site>, Long> {
        val query = if (archived == null || archived == false) {
            "archivedAt IS NULL"
        } else {
            "archivedAt IS NOT NULL"
        }

        return applyFirstMaxToQuery(
            query = find(query, Sort.by("modifiedAt").descending()),
            firstIndex = first,
            maxResults = max
        )
    }

}