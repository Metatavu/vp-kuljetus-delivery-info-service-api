package fi.metatavu.vp.deliveryinfo.freights

import fi.metatavu.vp.api.model.Freight
import fi.metatavu.vp.api.spec.FreightsApi
import fi.metatavu.vp.deliveryinfo.freights.freightunits.FreightUnitController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.sites.SiteController
import fi.metatavu.vp.deliveryinfo.tasks.TaskController
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
 * Freight API implementation
 */
@RequestScoped
@Suppress("unused")
@WithSession
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

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listFreights(first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val ( freights, count ) = freightController.list(first, max)
        createOk(freights.map { freightTranslator.translate(it) }, count)
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createFreight(freight: Freight): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        val pointOfDepartureSite = siteController.findSite(freight.pointOfDepartureSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.pointOfDepartureSiteId))
        val destinationSite = siteController.findSite(freight.destinationSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.destinationSiteId))
        val senderSite = siteController.findSite(freight.senderSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.senderSiteId))
        val recipientSite = siteController.findSite(freight.recipientSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.recipientSiteId))

        val createdFreight = freightController.create(
            pointOfDepartureSite = pointOfDepartureSite,
            destinationSite = destinationSite,
            senderSite = senderSite,
            recipientSite = recipientSite,
            userId = userId
        )
        createOk(freightTranslator.translate(createdFreight))
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findFreight(freightId: UUID): Uni<Response> = withCoroutineScope {
        val site = freightController.findFreight(freightId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(FREIGHT, freightId))
        createOk(freightTranslator.translate(site))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateFreight(freightId: UUID, freight: Freight): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        val existingFreight = freightController.findFreight(freightId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(FREIGHT, freightId))

        val pointOfDepartureSite = siteController.findSite(freight.pointOfDepartureSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.pointOfDepartureSiteId))
        val destinationSite = siteController.findSite(freight.destinationSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.destinationSiteId))
        val senderSite = siteController.findSite(freight.senderSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.senderSiteId))
        val recipientSite = siteController.findSite(freight.recipientSiteId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(SITE, freight.recipientSiteId))

        val updatedSite = freightController.updateFreight(
            existingFreight = existingFreight,
            pointOfDepartureSite = pointOfDepartureSite,
            destinationSite = destinationSite,
            senderSite = senderSite,
            recipientSite = recipientSite,
            userId = userId
        )
        createOk(freightTranslator.translate(updatedSite))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteFreight(freightId: UUID): Uni<Response> = withCoroutineScope {
        val freight = freightController.findFreight(freightId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(FREIGHT, freightId))

        val freightUnits = freightUnitController.list(freight = freight)
        if (freightUnits.first.isNotEmpty()) {
            return@withCoroutineScope createConflict("Freight has freight units")
        }

        val tasks = taskController.listTasks(freight = freight)
        if (tasks.first.isNotEmpty()) {
            return@withCoroutineScope createConflict("Freight has tasks")
        }

        freightController.delete(freight)
        createNoContent()
    }

}