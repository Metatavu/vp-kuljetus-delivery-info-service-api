package fi.metatavu.vp.deliveryinfo.thermometers

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
 * Translator for Thermometer JPA to REST entity
 */
@ApplicationScoped
class ThermometerTranslator: AbstractTranslator<Thermometer, fi.metatavu.vp.api.model.Thermometer>() {

    override suspend fun translate(entity: Thermometer): fi.metatavu.vp.api.model.Thermometer {
        return fi.metatavu.vp.api.model.Thermometer(
            id = entity.id,
            name = entity.name,
            hardwareSensorId = entity.hardwareSensorId,
            siteId = entity.site!!.id,
            espMacAddress = entity.espMacAddress,
            archivedAt = entity.archivedAt,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
