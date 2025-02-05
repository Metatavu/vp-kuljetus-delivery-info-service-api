package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.deliveryinfo.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Thermometer JPA to REST entity
 */
@ApplicationScoped
class TemperatureTranslator: AbstractTranslator<Temperature, fi.metatavu.vp.api.model.TerminalTemperature>() {

    override suspend fun translate(entity: Temperature): fi.metatavu.vp.api.model.TerminalTemperature {
        return fi.metatavu.vp.api.model.TerminalTemperature(
            id = entity.id,
            value = entity.value!!,
            timestamp = entity.timestamp!!,
            thermometerId = entity.thermometer.id
        )
    }
}
