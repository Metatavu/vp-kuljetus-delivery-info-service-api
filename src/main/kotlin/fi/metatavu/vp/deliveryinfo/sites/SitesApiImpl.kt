package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.api.model.Site
import fi.metatavu.vp.api.spec.SitesApi
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import java.util.*

@RequestScoped
@WithSession
@OptIn(ExperimentalCoroutinesApi::class)
class SitesApiImpl: SitesApi, AbstractApi() {

    @Inject
    lateinit var siteController: SiteController

    @Inject
    lateinit var siteTranslator: SiteTranslator

    @Inject
    lateinit var vertx: Vertx

    override fun listSites(first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val ( sites, count ) = siteController.listSites(first, max)
        createOk(sites.map { siteTranslator.translate(it) }, count)
    }.asUni()

    @WithTransaction
    override fun createSite(site: Site): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        if (!siteController.isValidPoint(site.location) || site.name.isEmpty()) {
            return@async createBadRequest(INVALID_REQUEST_BODY)
        }
        val createdSite = siteController.createSite(site, userId)
        createOk(siteTranslator.translate(createdSite))
    }.asUni()

    override fun findSite(siteId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val site = siteController.findSite(siteId) ?: return@async createNotFound(createNotFoundMessage(SITE, siteId))
        createOk(siteTranslator.translate(site))
    }.asUni()

    @WithTransaction
    override fun updateSite(siteId: UUID, site: Site): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)
        if (!siteController.isValidPoint(site.location) || site.name.isEmpty()) {
            return@async createBadRequest(INVALID_REQUEST_BODY)
        }
        val existingSite = siteController.findSite(siteId) ?: return@async createNotFound(createNotFoundMessage(SITE, siteId))
        val updatedSite = siteController.updateSite(existingSite, site, userId)
        createOk(siteTranslator.translate(updatedSite))
    }.asUni()

    @WithTransaction
    override fun deleteSite(siteId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val site = siteController.findSite(siteId) ?: return@async createNotFound(createNotFoundMessage(SITE, siteId))
        siteController.deleteSite(site)
        createNoContent()
    }.asUni()
}