package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.api.model.Site
import fi.metatavu.vp.api.model.SiteType
import fi.metatavu.vp.api.spec.SitesApi
import fi.metatavu.vp.deliveryinfo.devices.DeviceController
import fi.metatavu.vp.deliveryinfo.rest.AbstractApi
import fi.metatavu.vp.deliveryinfo.tasks.TaskController
import fi.metatavu.vp.deliveryinfo.temperature.TemperatureController
import fi.metatavu.vp.deliveryinfo.temperature.TemperatureTranslator
import fi.metatavu.vp.deliveryinfo.thermometers.ThermometerController
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction

/**
 * Sites API implementation
 */
@RequestScoped
@Suppress("unused")
@WithSession
class SitesApiImpl: SitesApi, AbstractApi() {

    @Inject
    lateinit var siteController: SiteController

    @Inject
    lateinit var deviceController: DeviceController

    @Inject
    lateinit var thermometerController: ThermometerController

    @Inject
    lateinit var temperatureController: TemperatureController

    @Inject
    lateinit var siteTranslator: SiteTranslator

    @Inject
    lateinit var temperatureTranslator: TemperatureTranslator

    @Inject
    lateinit var taskController: TaskController

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listSites(archived: Boolean?, first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val ( sites, count ) = siteController.listSites(archived, first, max)
        createOk(sites.map { siteTranslator.translate(it) }, count)
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createSite(site: Site): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        val parsedPoint = siteController.parsePoint(site.location)

        if (!siteController.validateSite(site, parsedPoint)) {
            return@withCoroutineScope createBadRequest(INVALID_REQUEST_BODY)
        }

        if (site.siteType == SiteType.TERMINAL) {
            val devices = deviceController.listAll()
            site.deviceIds.forEach { deviceId ->
                if (devices.find { device -> device.deviceId == deviceId } != null) {
                    return@withCoroutineScope createBadRequest("Device $deviceId already exists")
                }
            }
        }

        val createdSite = siteController.createSite(site, parsedPoint!!, userId)

        createOk(siteTranslator.translate(createdSite))
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findSite(siteId: UUID): Uni<Response> = withCoroutineScope {
        val site = siteController.findSite(siteId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(SITE, siteId))
        createOk(siteTranslator.translate(site))
    }

    @RolesAllowed(MANAGER_ROLE)
    override fun listSiteTemperatures(siteId: UUID, includeArchived: Boolean, first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val site = siteController.findSite(siteId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(SITE, siteId))
        val temperatures = temperatureController.list(site = site, includeArchived = includeArchived, first = first, max = max).map {
            temperatureTranslator.translate(it)
        }
        createOk(temperatures)
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateSite(siteId: UUID, site: Site): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        val parsedPoint = siteController.parsePoint(site.location)
        if (!siteController.validateSite(site, parsedPoint)) {
            return@withCoroutineScope createBadRequest(INVALID_REQUEST_BODY)
        }
        val existingSite = siteController.findSite(siteId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(SITE, siteId))

        if (existingSite.archivedAt != null && site.archivedAt != null) {
            return@withCoroutineScope createConflict("Cannot update archived site")
        }

        val updatedSite = siteController.updateSite(existingSite, site, parsedPoint!!, userId)

        if (site.siteType == SiteType.TERMINAL) {
            deviceController.updateDevices(existingSite, site.deviceIds, loggedUserId!!)
        }

        createOk(siteTranslator.translate(updatedSite))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteSite(siteId: UUID): Uni<Response> = withCoroutineScope {
        if (isProduction) return@withCoroutineScope createForbidden(FORBIDDEN)

        val site = siteController.findSite(siteId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(SITE, siteId))
        if (taskController.listTasks(site = site).first.isNotEmpty()) {
            return@withCoroutineScope createConflict("Cannot delete site with tasks")
        }

        if (site.siteType == "TERMINAL") {
            deviceController.listBySite(site).component1().forEach { deviceController.delete(it) }

            thermometerController.listThermometers(site, true).forEach {
                temperatureController.listByThermometer(it).forEach { temperature ->
                    temperatureController.delete(temperature)
                }
                thermometerController.deleteThermometer(it)
            }
        }

        siteController.deleteSite(site)
        createNoContent()
    }

}