package fi.metatavu.vp.deliveryinfo.freights.freightunits

import fi.metatavu.vp.deliveryinfo.freights.Freight
import fi.metatavu.vp.deliveryinfo.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for FreightUnit
 */
@ApplicationScoped
class FreightUnitRepository: AbstractRepository<FreightUnit, UUID>() {

    /**
     * Creates a new freight unit
     *
     * @param id id
     * @param freight freight
     * @param type type
     * @param quantity quantity
     * @param reservations reservations
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created freight unit
     */
    suspend fun create(
        id: UUID,
        freight: Freight,
        type: String,
        quantity: Double?,
        reservations: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): FreightUnit {
        val freightUnit = FreightUnit()
        freightUnit.id = id
        freightUnit.freight = freight
        freightUnit.type = type
        freightUnit.quantity = quantity
        freightUnit.reservations = reservations
        freightUnit.creatorId = creatorId
        freightUnit.lastModifierId = lastModifierId
        return persistSuspending(freightUnit)
    }

    /**
     * Lists freight units
     *
     * @param freight freight
     * @param first first result
     * @param max max results
     * @return pair of list of freight units and total count
     */
    suspend fun list(freight: Freight?, first: Int?, max: Int?): Pair<List<FreightUnit>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (freight != null) {
            stringBuilder.append("freight = :freight")
            parameters.and("freight", freight)
        }

        return applyFirstMaxToQuery(find(stringBuilder.toString(), parameters), first, max)
    }
}