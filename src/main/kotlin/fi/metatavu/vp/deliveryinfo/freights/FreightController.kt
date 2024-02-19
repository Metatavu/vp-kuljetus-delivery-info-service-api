package fi.metatavu.vp.deliveryinfo.freights

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
     * @param freight freight rest data
     * @param userId user id
     * @return created freight
     */
    suspend fun create(
        freight: fi.metatavu.vp.api.model.Freight,
        userId: UUID,
    ): Freight {
        val created = freightRepository.create(
            id = UUID.randomUUID(),
            pointOfDeparture = freight.pointOfDeparture,
            destination = freight.destination,
            sender = freight.sender,
            recipient = freight.recipient,
            shipmentInfo = freight.shipmentInfo,
            payer = freight.payer,
            temperatureMin = freight.temperatureMin,
            temperatureMax = freight.temperatureMax,
            reservations = freight.reservations,
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
     * @param freight freight rest data
     * @param userId user id
     * @return updated freight
     */
    suspend fun updateFreight(existingFreight: Freight, freight: fi.metatavu.vp.api.model.Freight, userId: UUID): Freight {
        existingFreight.sender = freight.sender
        existingFreight.recipient = freight.recipient
        existingFreight.payer = freight.payer
        existingFreight.shipmentInfo = freight.shipmentInfo
        existingFreight.temperatureMin = freight.temperatureMin
        existingFreight.temperatureMax = freight.temperatureMax
        existingFreight.reservations = freight.reservations
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