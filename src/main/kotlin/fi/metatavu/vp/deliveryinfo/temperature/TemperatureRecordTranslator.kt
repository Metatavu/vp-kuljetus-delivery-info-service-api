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
 * Translator for TemperatureRecord JPA to REST entity
 */
@ApplicationScoped
class TemperatureRecordTranslator: AbstractTranslator<TemperatureRecord, fi.metatavu.vp.api.model.TemperatureRecord>() {

    override suspend fun translate(entity: TemperatureRecord): fi.metatavu.vp.api.model.TemperatureRecord {
        return fi.metatavu.vp.api.model.TemperatureRecord(
            id = entity.id,
            deviceIdentifier = entity.deviceId,
            hardwareSensorId = entity.sensorId,
            value = entity.value!!,
            timestamp = entity.timestamp!!,
            creatorId = entity.creatorId,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
