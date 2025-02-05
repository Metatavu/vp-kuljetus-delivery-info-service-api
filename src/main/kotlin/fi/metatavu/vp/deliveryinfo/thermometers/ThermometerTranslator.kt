package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Thermometer JPA to REST entity
 */
@ApplicationScoped
class ThermometerTranslator: AbstractTranslator<Thermometer, fi.metatavu.vp.api.model.TerminalThermometer>() {

    override suspend fun translate(entity: Thermometer): fi.metatavu.vp.api.model.TerminalThermometer {
        return fi.metatavu.vp.api.model.TerminalThermometer(
            id = entity.id,
            name = entity.name,
            hardwareSensorId = entity.hardwareSensorId,
            siteId = entity.site.id,
            deviceIdentifier = entity.deviceIdentifier,
            archivedAt = entity.archivedAt,
            lastModifierId = entity.lastModifierId,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }
}
