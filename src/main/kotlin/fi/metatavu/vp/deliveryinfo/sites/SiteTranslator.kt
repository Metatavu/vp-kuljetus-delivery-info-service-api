package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.api.model.SiteType
import fi.metatavu.vp.deliveryinfo.devices.DeviceController
import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTWriter

/**
 * Translator for Site JPA to REST entity
 */
@ApplicationScoped
class SiteTranslator: AbstractTranslator<Site, fi.metatavu.vp.api.model.Site>() {
    @Inject
    lateinit var deviceController: DeviceController

    private val writer = WKTWriter()
    private val geomFactory = GeometryFactory()

    override suspend fun translate(entity: Site): fi.metatavu.vp.api.model.Site {
        var deviceIds = emptyList<String>()
        if (entity.siteType == "TERMINAL") {
            deviceIds = deviceController.listBySite(entity).component1().map { device -> device.deviceId }
        }

        val point: Point = Coordinate(entity.latitude, entity.longitude).let { geomFactory.createPoint(Coordinate(entity.latitude, entity.longitude)) }
        return fi.metatavu.vp.api.model.Site(
            id = entity.id,
            name = entity.name,
            location = writer.write(point),
            archivedAt = entity.archivedAt,
            address = entity.address,
            postalCode = entity.postalCode,
            locality = entity.locality,
            deviceIds = deviceIds,
            siteType = SiteType.valueOf(entity.siteType),
            additionalInfo = entity.additionalInfo,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
