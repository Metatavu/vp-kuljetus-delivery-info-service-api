package fi.metatavu.vp.deliveryinfo.freights.freightunits

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for FreightUnit JPA to REST entity
 */
@ApplicationScoped
class FreightUnitTranslator: AbstractTranslator<FreightUnit, fi.metatavu.vp.api.model.FreightUnit>() {

    override suspend fun translate(entity: FreightUnit): fi.metatavu.vp.api.model.FreightUnit {
        return fi.metatavu.vp.api.model.FreightUnit(
            id = entity.id,
            freightId = entity.freight.id,
            type = entity.type,
            quantity = entity.quantity,
            reservations = entity.reservations,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}