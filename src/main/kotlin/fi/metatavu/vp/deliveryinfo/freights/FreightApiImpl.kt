package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.api.model.Freight
import fi.metatavu.vp.api.spec.FreightsApi
import fi.metatavu.vp.deliveryinfo.freights.freightunits.FreightUnitController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.sites.SiteController
import fi.metatavu.vp.deliveryinfo.tasks.TaskController
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
 * Freight API implementation
 */
@RequestScoped
@WithSession
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("unused")
class FreightApiImpl: FreightsApi, AbstractApi() {

    @Inject
    lateinit var freightController: FreightController

    @Inject
    lateinit var freightTranslator: FreightTranslator

    @Inject
    lateinit var freightUnitController: FreightUnitController

    @Inject
    lateinit var taskController: TaskController

    @Inject
    lateinit var siteController: SiteController

    @Inject
    lateinit var vertx: Vertx

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listFreights(first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val ( freights, count ) = freightController.list(first, max)
        createOk(freights.map { freightTranslator.translate(it) }, count)
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createFreight(freight: Freight): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        val pointOfDepartureSite = siteController.findSite(freight.pointOfDepartureSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.pointOfDepartureSiteId))
        val destinationSite = siteController.findSite(freight.destinationSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.destinationSiteId))
        val senderSite = siteController.findSite(freight.senderSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.senderSiteId))
        val recipientSite = siteController.findSite(freight.recipientSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.recipientSiteId))

        val createdFreight = freightController.create(
            pointOfDepartureSite = pointOfDepartureSite,
            destinationSite = destinationSite,
            senderSite = senderSite,
            recipientSite = recipientSite,
            userId = userId
        )
        createOk(freightTranslator.translate(createdFreight))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findFreight(freightId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val site = freightController.findFreight(freightId) ?: return@async createNotFound(createNotFoundMessage(FREIGHT, freightId))
        createOk(freightTranslator.translate(site))
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateFreight(freightId: UUID, freight: Freight): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        val existingFreight = freightController.findFreight(freightId) ?: return@async createNotFound(createNotFoundMessage(FREIGHT, freightId))

        val pointOfDepartureSite = siteController.findSite(freight.pointOfDepartureSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.pointOfDepartureSiteId))
        val destinationSite = siteController.findSite(freight.destinationSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.destinationSiteId))
        val senderSite = siteController.findSite(freight.senderSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.senderSiteId))
        val recipientSite = siteController.findSite(freight.recipientSiteId) ?: return@async createBadRequest(createNotFoundMessage(SITE, freight.recipientSiteId))

        val updatedSite = freightController.updateFreight(
            existingFreight = existingFreight,
            pointOfDepartureSite = pointOfDepartureSite,
            destinationSite = destinationSite,
            senderSite = senderSite,
            recipientSite = recipientSite,
            userId = userId
        )
        createOk(freightTranslator.translate(updatedSite))
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteFreight(freightId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val freight = freightController.findFreight(freightId) ?: return@async createNotFound(createNotFoundMessage(FREIGHT, freightId))

        val freightUnits = freightUnitController.list(freight = freight)
        if (freightUnits.first.isNotEmpty()) {
            return@async createConflict("Freight has freight units")
        }

        val tasks = taskController.listTasks(freight = freight)
        if (tasks.first.isNotEmpty()) {
            return@async createConflict("Freight has tasks")
        }

        freightController.delete(freight)
        createNoContent()
    }.asUni()
}