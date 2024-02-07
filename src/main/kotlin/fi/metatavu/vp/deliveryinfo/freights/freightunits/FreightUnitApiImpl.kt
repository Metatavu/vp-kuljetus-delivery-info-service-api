package fi.metatavu.vp.deliveryinfo.freights.freightunits

import fi.metatavu.vp.api.model.FreightUnit
import fi.metatavu.vp.api.spec.FreightUnitsApi
import fi.metatavu.vp.deliveryinfo.freights.FreightController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import java.util.*

/**
 * Freight unit API implementation
 */
@RequestScoped
@WithSession
@OptIn(ExperimentalCoroutinesApi::class)
class FreightUnitApiImpl : FreightUnitsApi, AbstractApi() {

    @Inject
    lateinit var freightUnitController: FreightUnitController

    @Inject
    lateinit var freightController: FreightController

    @Inject
    lateinit var freightUnitTranslator: FreightUnitTranslator

    @Inject
    lateinit var vertx: Vertx

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listFreightUnits(freightId: UUID?, first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val freightFilter = if (freightId != null) {
        val freight = freightController.findFreight(freightId) ?: return@async createNotFound(createNotFoundMessage(FREIGHT, freightId))
            freight
        } else null
        val (freightUnits, count) = freightUnitController.list(freightFilter, first, max)
        createOk(freightUnits.map { freightUnitTranslator.translate(it) }, count)
        }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    @WithTransaction
    override fun createFreightUnit(freightUnit: FreightUnit): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        val foundFreight = freightController.findFreight(freightUnit.freightId) ?: return@async createNotFound(
            createNotFoundMessage(FREIGHT, freightUnit.freightId)
        )
        val createdFreight = freightUnitController.create(foundFreight, freightUnit, userId)
        createOk(freightUnitTranslator.translate(createdFreight))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findFreightUnit(freightUnitId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val site = freightUnitController.findFreightUnit(freightUnitId) ?: return@async createNotFound(
            createNotFoundMessage(FREIGHT_UNIT, freightUnitId)
        )
        createOk(freightUnitTranslator.translate(site))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    @WithTransaction
    override fun updateFreightUnit(freightUnitId: UUID, freightUnit: FreightUnit): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        val existingFreightUnit = freightUnitController.findFreightUnit(freightUnitId) ?: return@async createNotFound(
            createNotFoundMessage(FREIGHT_UNIT, freightUnitId)
        )
        val newFreightUnit = if (existingFreightUnit.freight.id != freightUnit.freightId) {
            freightController.findFreight(freightUnit.freightId) ?: return@async createNotFound(
                createNotFoundMessage(FREIGHT, freightUnit.freightId)
            )
        } else {
            existingFreightUnit.freight
        }

        val updatedFreightUnit = freightUnitController.updateFreight(existingFreightUnit, newFreightUnit, freightUnit, userId)
        createOk(freightUnitTranslator.translate(updatedFreightUnit))
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteFreightUnit(freightUnitId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val freightUnit = freightUnitController.findFreightUnit(freightUnitId) ?: return@async createNotFound(
            createNotFoundMessage(FREIGHT_UNIT, freightUnitId)
        )
        freightUnitController.delete(freightUnit)
        createNoContent()
    }.asUni()
}