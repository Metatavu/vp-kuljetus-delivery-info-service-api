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
            pointOfDepartureSiteId = entity.pointOfDepartureSite.id,
            destinationSiteId = entity.destinationSite.id,
            senderSiteId = entity.senderSite.id,
            recipientSiteId = entity.recipientSite.id,
            freightNumber = entity.freightNumber,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}