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

    override fun findThermometer(thermometerId: UUID): Uni<Response> {
        TODO("Not yet implemented")
    }

    @RolesAllowed(MANAGER_ROLE)
    override fun listThermometers(siteId: UUID?, includeArchived: Boolean, first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val site = getSiteIfExists(siteId)
        val thermometers = thermometerController.listThermometers(site = site, includeArchived = includeArchived).map { thermometerTranslator.translate(it) }
        createOk(thermometers)
    }

    override fun updateThermometer(
        thermometerId: UUID,
        updateThermometerRequest: UpdateThermometerRequest
    ): Uni<Response> {
        TODO("Not yet implemented")
    }

    /**
     * Returns the site with the siteId if such a site exists
     *
     * @param siteId
     *
     * @return site or null
     */
    suspend fun getSiteIfExists(siteId: UUID?): Site? {
        if (siteId != null) {
            return siteController.findSite(siteId)
        }

        return null
    }
}