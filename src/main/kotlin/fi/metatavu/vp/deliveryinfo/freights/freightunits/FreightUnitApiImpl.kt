package fi.metatavu.vp.deliveryinfo.freights.freightunits

import fi.metatavu.vp.api.model.FreightUnit
import fi.metatavu.vp.api.spec.FreightUnitsApi
import fi.metatavu.vp.deliveryinfo.freights.FreightController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*
import fi.metatavu.coroutine.CoroutineUtils.withCoroutineScope
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction

/**
 * Freight unit API implementation
 */
@RequestScoped
@WithSession
@Suppress("unused")
class FreightUnitApiImpl : FreightUnitsApi, AbstractApi() {

    @Inject
    lateinit var freightUnitController: FreightUnitController

    @Inject
    lateinit var freightController: FreightController

    @Inject
    lateinit var freightUnitTranslator: FreightUnitTranslator

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listFreightUnits(freightId: UUID?, first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val freightFilter = if (freightId != null) {
            freightController.findFreight(freightId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(FREIGHT, freightId))
        } else null

        val (freightUnits, count) = freightUnitController.list(freightFilter, first, max)
        createOk(freightUnits.map { freightUnitTranslator.translate(it) }, count)
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    @WithTransaction
    override fun createFreightUnit(freightUnit: FreightUnit): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        val foundFreight = freightController.findFreight(freightUnit.freightId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(FREIGHT, freightUnit.freightId)
        )
        val createdFreight = freightUnitController.create(foundFreight, freightUnit, userId)
        createOk(freightUnitTranslator.translate(createdFreight))
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findFreightUnit(freightUnitId: UUID): Uni<Response> = withCoroutineScope {
        val site = freightUnitController.findFreightUnit(freightUnitId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(FREIGHT_UNIT, freightUnitId)
        )
        createOk(freightUnitTranslator.translate(site))
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    @WithTransaction
    override fun updateFreightUnit(freightUnitId: UUID, freightUnit: FreightUnit): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        val existingFreightUnit = freightUnitController.findFreightUnit(freightUnitId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(FREIGHT_UNIT, freightUnitId)
        )
        val newFreightUnit = if (existingFreightUnit.freight.id != freightUnit.freightId) {
            freightController.findFreight(freightUnit.freightId) ?: return@withCoroutineScope createNotFound(
                createNotFoundMessage(FREIGHT, freightUnit.freightId)
            )
        } else {
            existingFreightUnit.freight
        }

        val updatedFreightUnit = freightUnitController.updateFreight(existingFreightUnit, newFreightUnit, freightUnit, userId)
        createOk(freightUnitTranslator.translate(updatedFreightUnit))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteFreightUnit(freightUnitId: UUID): Uni<Response> = withCoroutineScope {
        val freightUnit = freightUnitController.findFreightUnit(freightUnitId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(FREIGHT_UNIT, freightUnitId)
        )
        freightUnitController.delete(freightUnit)
        createNoContent()
    }

}