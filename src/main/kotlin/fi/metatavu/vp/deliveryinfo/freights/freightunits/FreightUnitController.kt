package fi.metatavu.vp.deliveryinfo.freights.freightunits

import fi.metatavu.vp.deliveryinfo.freights.Freight
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for FreightUnits
 */
@ApplicationScoped
class FreightUnitController {

    @Inject
    lateinit var freightUnitRepository: FreightUnitRepository

    /**
     * Lists freight units
     *
     * @param freight freight
     * @param first first result
     * @param max max results
     * @return pair of list of freight units and total count
     */
    suspend fun list(freight: Freight?, first: Int? = null, max: Int? = null): Pair<List<FreightUnit>, Long> {
        return freightUnitRepository.list(freight, first, max)
    }

    /**
     * Creates a new freight unit
     *
     * @param freight freight
     * @param freightUnit freight unit
     * @param userId user id
     * @return created freight unit
     */
    suspend fun create(
        freight: Freight,
        freightUnit: fi.metatavu.vp.api.model.FreightUnit,
        userId: UUID
    ): FreightUnit {
        return freightUnitRepository.create(
            id = UUID.randomUUID(),
            freight = freight,
            type = freightUnit.type,
            quantity = freightUnit.quantity,
            quantityUnit = freightUnit.quantityUnit,
            reservations = freightUnit.reservations,
            creatorId = userId,
            lastModifierId = userId
        )
    }

    /**
     * Finds a freight unit
     *
     * @param freightUnitId freight unit id
     * @return found freight unit or null if not found
     */
    suspend fun findFreightUnit(freightUnitId: UUID): FreightUnit? {
        return freightUnitRepository.findByIdSuspending(freightUnitId)
    }

    /**
     * Updates a freight unit
     *
     * @param freightUnit freight unit
     * @param newFreight new freight
     * @param freightUnitRest rest model
     * @param userId user id
     * @return updated freight unit
     */
    suspend fun updateFreight(
        freightUnit: FreightUnit,
        newFreight: Freight,
        freightUnitRest: fi.metatavu.vp.api.model.FreightUnit,
        userId: UUID
    ): FreightUnit {
        freightUnit.freight = newFreight
        freightUnit.type = freightUnitRest.type
        freightUnit.quantity = freightUnitRest.quantity
        freightUnit.quantityUnit = freightUnitRest.quantityUnit
        freightUnit.reservations = freightUnitRest.reservations
        freightUnit.lastModifierId = userId
        return freightUnitRepository.persistSuspending(freightUnit)
    }

    /**
     * Deletes a freight unit
     *
     * @param freightUnit freight unit
     */
    suspend fun delete(freightUnit: FreightUnit) {
        freightUnitRepository.deleteSuspending(freightUnit)
    }

}