package fi.metatavu.vp.deliveryinfo.temperature

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
class TemperatureTranslator: AbstractTranslator<Temperature, fi.metatavu.vp.api.model.Temperature>() {

    override suspend fun translate(entity: Temperature): fi.metatavu.vp.api.model.Temperature {
        return fi.metatavu.vp.api.model.Temperature(
            id = entity.id,
            value = entity.value!!,
            timestamp = entity.timestamp!!,
            thermometerId = entity.thermometer.id
        )
    }
}
