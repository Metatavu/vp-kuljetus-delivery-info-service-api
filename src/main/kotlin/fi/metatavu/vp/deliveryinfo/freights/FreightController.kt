package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.sites.Site
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for freights
 */
@ApplicationScoped
class FreightController {

    @Inject
    lateinit var freightRepository: FreightRepository

    /**
     * Lists freights
     *
     * @param first first result
     * @param max max results
     * @return pair of list of freights and total count
     */
    suspend fun list(first: Int?, max: Int?): Pair<List<Freight>, Long> {
        return freightRepository.list(first, max)
    }

    /**
     * Creates a new freight
     *
     * @param pointOfDepartureSite point of departure site
     * @param destinationSite destination site
     * @param senderSite sender site
     * @param recipientSite recipient site
     * @param userId user id
     * @return created freight
     */
    suspend fun create(
        pointOfDepartureSite: Site,
        destinationSite: Site,
        senderSite: Site,
        recipientSite: Site,
        userId: UUID,
    ): Freight {
        val created = freightRepository.create(
            id = UUID.randomUUID(),
            pointOfDepartureSite = pointOfDepartureSite,
            destinationSite = destinationSite,
            senderSite = senderSite,
            recipientSite = recipientSite,
            creatorId = userId,
            lastModifierId = userId
        )

        // Refresh so that auto-increment field gets filled
        freightRepository.session.awaitSuspending().refresh(created).awaitSuspending()
        return created
    }

    /**
     * Finds a freight
     *
     * @param freightId freight id
     * @return found freight or null if not found
     */
    suspend fun findFreight(freightId: UUID): Freight? {
        return freightRepository.findByIdSuspending(freightId)
    }

    /**
     * Updates a freight
     *
     * @param existingFreight existing freight
     * @param userId user id
     * @return updated freight
     */
    suspend fun updateFreight(
        existingFreight: Freight,
        pointOfDepartureSite: Site,
        destinationSite: Site,
        senderSite: Site,
        recipientSite: Site,
        userId: UUID
    ): Freight {
        existingFreight.pointOfDepartureSite = pointOfDepartureSite
        existingFreight.destinationSite = destinationSite
        existingFreight.senderSite = senderSite
        existingFreight.recipientSite = recipientSite
        existingFreight.lastModifierId = userId
        return freightRepository.persistSuspending(existingFreight)
    }

    /**
     * Deletes a freight
     *
     * @param freight freight
     */
    suspend fun delete(freight: Freight) {
        freightRepository.deleteSuspending(freight)
    }


}