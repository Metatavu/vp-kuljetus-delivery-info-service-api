package fi.metatavu.vp.deliveryinfo.sites

import fi.metatavu.vp.api.model.SiteType
import fi.metatavu.vp.deliveryinfo.devices.DeviceController
import fi.metatavu.vp.deliveryinfo.temperature.TemperatureController
import fi.metatavu.vp.deliveryinfo.thermometers.ThermometerController
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.WKTReader
import java.util.*

/**
 * Controller for sites
 */
@ApplicationScoped
class SiteController {

    @Inject
    lateinit var siteRepository: SiteRepository

    @Inject
    lateinit var deviceController: DeviceController

    @Inject
    lateinit var thermometerController: ThermometerController

    @Inject
    lateinit var temperatureController: TemperatureController

    private val reader = WKTReader()

    /**
     * Checks if the given location is a valid point
     *
     * @param location location
     * @return parsed geography or null if not valid
     */
    fun parsePoint(location: String): Geometry? {
        return try {
            reader.read(location)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lists sites
     *
     * @param archived archived status
     * @param first first result
     * @param max max results
     * @return list of sites
     */
    suspend fun listSites(archived: Boolean?, first: Int?, max: Int?): Pair<List<Site>, Long> {
        return siteRepository.list(archived, first, max)
    }

    /**
     * Creates a new site
     *
     * @param site site
     * @param parsedPoint parsed point
     * @param userId user id
     * @return created site
     */
    suspend fun createSite(
        site: fi.metatavu.vp.api.model.Site,
        parsedPoint: Geometry,
        userId: UUID
    ): Site {
        val ( lat, lon ) = getLatLon(parsedPoint)

        val createdSite = siteRepository.create(
            name = site.name,
            latitude = lat,
            longitude = lon,
            address = site.address,
            postalCode = site.postalCode,
            locality = site.locality,
            siteType = site.siteType,
            additionalInfo = site.additionalInfo,
            creatorId = userId
        )

        if (site.siteType == SiteType.TERMINAL) {
            site.deviceIds.forEach { deviceId ->
                deviceController.create(deviceId, createdSite, userId)
            }
        }

        return createdSite
    }

    /**
     * Finds a site by id
     *
     * @param siteId site id
     * @return found site or null if not found
     */
    suspend fun findSite(siteId: UUID): Site? {
        return siteRepository.findByIdSuspending(siteId)
    }

    /**
     * Updates a site
     *
     * @param existingSite existing site
     * @param site site
     * @param userId user id
     * @return updated site
     */
    suspend fun updateSite(existingSite: Site, site: fi.metatavu.vp.api.model.Site, newLocation: Geometry, userId: UUID): Site {
        existingSite.name = site.name
        val ( lat, lon ) = getLatLon(newLocation)
        existingSite.latitude = lat
        existingSite.longitude = lon
        existingSite.archivedAt = site.archivedAt
        existingSite.address = site.address
        existingSite.postalCode = site.postalCode
        existingSite.locality = site.locality
        existingSite.additionalInfo = site.additionalInfo
        existingSite.lastModifierId = userId
        return siteRepository.persistSuspending(existingSite)
    }

    /**
     * Deletes a site
     *
     * @param site site
     */
    suspend fun deleteSite(site: Site) {
        if (site.siteType == "TERMINAL") {
            deviceController.listBySite(site).first.forEach { deviceController.delete(it) }

            thermometerController.listThermometers(site, true).first.forEach {
                temperatureController.listByThermometer(it).forEach { temperature ->
                    temperatureController.delete(temperature)
                }
                thermometerController.deleteThermometer(it)
            }
        }
        siteRepository.deleteSuspending(site)
    }

    /**
     * Validates that Site has all required fields
     *
     * @param site site
     * @return true if site has all required fields, false otherwise
     */
    fun validateSite(site: fi.metatavu.vp.api.model.Site, parsedPoint: Geometry?): Boolean {
        return parsedPoint != null &&
                site.name.isNotBlank() &&
                site.address.isNotBlank() &&
                site.postalCode.isNotBlank() &&
                site.locality.isNotBlank()
    }

    /**
     * Parses lat lon from geography object
     *
     * @param point geography object
     * @return lat lon pair
     */
    private fun getLatLon(point: Geometry): Pair<Double, Double> {
        val lat = point.coordinate.x
        val lon = point.coordinate.y
        return Pair(lat, lon)
    }
}