package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Site JPA to REST entity
 */
@ApplicationScoped
class SiteTranslator: AbstractTranslator<Site, fi.metatavu.vp.api.model.Site>() {

    override suspend fun translate(entity: Site): fi.metatavu.vp.api.model.Site {
        return fi.metatavu.vp.api.model.Site(
            id = entity.id,
            name = entity.name,
            location = entity.location,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
