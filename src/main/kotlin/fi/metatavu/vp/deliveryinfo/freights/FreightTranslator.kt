package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Freight JPA to REST entity
 */
@ApplicationScoped
class FreightTranslator: AbstractTranslator<Freight, fi.metatavu.vp.api.model.Freight>() {

    override suspend fun translate(entity: Freight): fi.metatavu.vp.api.model.Freight {
        return fi.metatavu.vp.api.model.Freight(
            id = entity.id,
            pointOfDeparture = entity.pointOfDeparture,
            destination = entity.destination,
            sender = entity.sender,
            recipient = entity.recipient,
            payer = entity.payer,
            shipmentInfo = entity.shipmentInfo,
            temperatureMin = entity.temperatureMin,
            temperatureMax = entity.temperatureMax,
            reservations = entity.reservations,
            freightNumber = entity.freightNumber?.toInt(),
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}