package fi.metatavu.vp.deliveryinfo.temperature

import fi.metatavu.vp.api.model.TemperatureRecord
import fi.metatavu.vp.api.spec.TemperatureRecordsApi
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*

/**
 * Temperature records API implementation
 */
@RequestScoped
@Suppress("unused")
@WithSession
class TemperatureRecordsApiImpl: TemperatureRecordsApi, AbstractApi() {
    @Inject
    lateinit var temperatureRecordController: TemperatureRecordController

    @Inject
    lateinit var temperatureRecordTranslator: TemperatureRecordTranslator

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTemperatureRecord(temperatureRecord: TemperatureRecord): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        createOk(temperatureRecordTranslator.translate(temperatureRecordController.create(
            deviceId = temperatureRecord.deviceIdentifier,
            sensorId = temperatureRecord.hardwareSensorId,
            value = temperatureRecord.value,
            timestamp = temperatureRecord.timestamp,
            userId = userId
        )))
    }

    override fun deleteTemperatureRecord(temperatureRecordId: UUID): Uni<Response> = withCoroutineScope {
        val found = temperatureRecordController.findById(temperatureRecordId) ?: return@withCoroutineScope createNotFound()
        temperatureRecordController.delete(found)
        createNoContent()
    }

    override fun listTemperatureRecords(terminalId: UUID?, deviceId: String?, first: Int?, max: Int?): Uni<Response> {
        TODO("Not yet implemented")
    }
}