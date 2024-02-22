package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import fi.metatavu.vp.deliveryinfo.sites.Site
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for freights
 */
@ApplicationScoped
class FreightRepository: AbstractRepository<Freight, UUID>() {

    /**
     * Creates a new freight and flushes the changes (so that refresh() method in FreightController has some data)
     *
     * @param id id
     * @param pointOfDepartureSite point of departure site
     * @param destinationSite destination site
     * @param senderSite sender site
     * @param recipientSite recipient site
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created freight
     */
    suspend fun create(
        id: UUID,
        pointOfDepartureSite: Site,
        destinationSite: Site,
        senderSite: Site,
        recipientSite: Site,
        creatorId: UUID,
        lastModifierId: UUID
    ): Freight {
        val freight = Freight()
        freight.id = id
        freight.pointOfDepartureSite = pointOfDepartureSite
        freight.destinationSite = destinationSite
        freight.senderSite = senderSite
        freight.recipientSite = recipientSite
        freight.creatorId = creatorId
        freight.lastModifierId = lastModifierId
        return persistAndFlush(freight).awaitSuspending()
    }

    /**
     * Lists freights
     *
     * @param first first result
     * @param max max results
     * @return pair of list of freights and total count
     */
    suspend fun list(first: Int?, max: Int?): Pair<List<Freight>, Long> {
        return applyFirstMaxToQuery(findAll(Sort.descending("modifiedAt")), first, max)
    }
}