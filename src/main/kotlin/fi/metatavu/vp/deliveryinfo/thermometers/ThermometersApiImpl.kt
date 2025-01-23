package fi.metatavu.vp.deliveryinfo.thermometers

import fi.metatavu.vp.api.model.UpdateThermometerRequest
import fi.metatavu.vp.api.spec.ThermometersApi
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.sites.Site
import fi.metatavu.vp.deliveryinfo.sites.SiteController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*

/**
 * Thermometers API implementation
 */
@RequestScoped
@Suppress("unused")
@WithSession
class ThermometersApiImpl: ThermometersApi, AbstractApi() {

    @Inject
    lateinit var thermometerController: ThermometerController

    @Inject
    lateinit var siteController: SiteController

    @Inject
    lateinit var thermometerTranslator: ThermometerTranslator

    @RolesAllowed(MANAGER_ROLE)
    override fun findThermometer(thermometerId: UUID): Uni<Response> = withCoroutineScope {
        val thermometer = thermometerController.findThermometer(thermometerId) ?: return@withCoroutineScope createNotFound("Thermometer with id $thermometerId not found")
        createOk(thermometerTranslator.translate(thermometer))
    }

    @RolesAllowed(MANAGER_ROLE)
    override fun listThermometers(siteId: UUID?, includeArchived: Boolean, first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val site = getSiteIfExists(siteId)
        val thermometers = thermometerController.listThermometers(site = site, includeArchived = includeArchived)
        val translated = thermometers.first.map { thermometerTranslator.translate(it) }
        createOk(translated, thermometers.second)
    }

    @RolesAllowed(MANAGER_ROLE)
    override fun updateThermometer(
        thermometerId: UUID,
        updateThermometerRequest: UpdateThermometerRequest
    ): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        val foundThermometer = thermometerController.findThermometer(thermometerId) ?: return@withCoroutineScope createNotFound("Thermometer with id $thermometerId not found")
        val updatedThermometer = thermometerController.updateThermometerName(foundThermometer, name = updateThermometerRequest.name, userId = userId)
        createOk(thermometerTranslator.translate(updatedThermometer))
    }

    /**
     * Returns the site with the siteId if such a site exists
     *
     * @param siteId
     *
     * @return site or null
     */
    suspend fun getSiteIfExists(siteId: UUID?): Site? {
        return siteId?.let { siteController.findSite(it) }
    }
}