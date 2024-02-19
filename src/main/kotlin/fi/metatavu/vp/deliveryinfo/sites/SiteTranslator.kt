package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTWriter

/**
 * Translator for Site JPA to REST entity
 */
@ApplicationScoped
class SiteTranslator: AbstractTranslator<Site, fi.metatavu.vp.api.model.Site>() {

    private val writer = WKTWriter()
    private val geomFactory = GeometryFactory()

    override suspend fun translate(entity: Site): fi.metatavu.vp.api.model.Site {
        val point: Point = Coordinate(entity.latitude, entity.longitude).let { geomFactory.createPoint(Coordinate(entity.latitude, entity.longitude)) }
        return fi.metatavu.vp.api.model.Site(
            id = entity.id,
            name = entity.name,
            location = writer.write(point),
            archivedAt = entity.archivedAt,
            address = entity.address,
            postalCode = entity.postalCode,
            locality = entity.locality,
            additionalInfo = entity.additionalInfo,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
