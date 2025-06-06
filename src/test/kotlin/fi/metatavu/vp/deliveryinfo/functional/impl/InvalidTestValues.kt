package fi.metatavu.vp.deliveryinfo.functional.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.*
import java.util.*

/**
 * Invalid test values for the different entities
 */
class InvalidTestValues: InvalidValues() {

    companion object {
        val INVALID_SITES = listOf(
            Site(
                name = "Test site 1",
                location = "qqq",
                address = "address",
                postalCode = "postalCode",
                locality = "locality",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            ),
            Site(
                name = "",
                location = "POINT (60.16952 24.93545)",
                address = "address",
                postalCode = "postalCode",
                locality = "locality",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            ),
            Site(
                name = "Test site 1",
                location = "",
                address = "address",
                postalCode = "postalCode",
                locality = "locality",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            ),
            Site(
                name = "Test site 1",
                location = "POINT (60.16952 24.93545)",
                address = "",
                postalCode = "postalCode",
                locality = "locality",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            ),
            Site(
                name = "Test site 1",
                location = "POINT (60.16952 24.93545)",
                address = "address",
                postalCode = "",
                locality = "locality",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            ),
            Site(
                name = "Test site 1",
                location = "POINT (60.16952 24.93545)",
                address = "address",
                postalCode = "postalCode",
                locality = "",
                siteType = SiteType.CUSTOMER_SITE,
                deviceIds = emptyArray()
            )
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHT_UNITS_FREIGHT_ID = listOf(
            FreightUnit(freightId = UUID.randomUUID(), type = "type", quantity = 1.0, reservations = "reservations")
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        /**
         * Builds invalid freights
         *
         * @param validSiteId valid site id example
         * @return list of invalid body options
         */
        fun getInvalidFreights(validSiteId: UUID): List<SimpleInvalidValueProvider> {
            val sampleValidFreight = fi.metatavu.vp.test.client.models.Freight(
                pointOfDepartureSiteId = validSiteId,
                destinationSiteId = validSiteId,
                senderSiteId = validSiteId,
                recipientSiteId = validSiteId
            )
            return listOf(
                sampleValidFreight.copy(pointOfDepartureSiteId = UUID.randomUUID()),
                sampleValidFreight.copy(destinationSiteId = UUID.randomUUID()),
                sampleValidFreight.copy(senderSiteId = UUID.randomUUID()),
                sampleValidFreight.copy(recipientSiteId = UUID.randomUUID())
            ).map { jacksonObjectMapper().writeValueAsString(it) }
                .map { SimpleInvalidValueProvider(it) }
        }

        /**
         * Builds invalid tasks
         *
         * @param validRouteId valid route id examples
         * @param validFreightId valid freight id example
         * @param validSiteId valid site id example
         * @return list of invalid body options
         */
        fun getInvalidTasks(
            validRouteId: UUID,
            validFreightId: UUID,
            validSiteId: UUID
        ): List<SimpleInvalidValueProvider> {
            val sampleValidTask = Task(
                freightId = validFreightId,
                customerSiteId = validSiteId,
                type = TaskType.LOAD,
                remarks = "remarks",
                routeId = validRouteId,
                orderNumber = 0,
                status = fi.metatavu.vp.test.client.models.TaskStatus.TODO,
                groupNumber = 0
            )
            return listOf(
                Site(name = "Test site 1", location = "qqq", address = "address", postalCode = "postalCode", locality = "locality", siteType = SiteType.CUSTOMER_SITE, deviceIds = emptyArray()),
                sampleValidTask.copy(freightId = UUID.randomUUID()), //not found freight
                sampleValidTask.copy(customerSiteId = UUID.randomUUID()), //not found site
                sampleValidTask.copy(routeId = UUID.randomUUID()), //not found route
                sampleValidTask.copy(orderNumber = -1), // negative order number
                sampleValidTask.copy(orderNumber = null), // null order number while having route id
                sampleValidTask.copy(routeId = null)  // null route id while having order number
            ).map { jacksonObjectMapper().writeValueAsString(it) }
                .map { SimpleInvalidValueProvider(it) }
        }
    }

}