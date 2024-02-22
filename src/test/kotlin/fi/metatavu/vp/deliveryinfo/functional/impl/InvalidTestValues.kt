package fi.metatavu.vp.deliveryinfo.functional.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.FreightUnit
import fi.metatavu.vp.test.client.models.Site
import fi.metatavu.vp.test.client.models.Task
import fi.metatavu.vp.test.client.models.TaskType
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
                locality = "locality"
            ),
            Site(
                name = "",
                location = "POINT (60.16952 24.93545)",
                address = "address",
                postalCode = "postalCode",
                locality = "locality"
            ),
            Site(
                name = "Test site 1",
                location = "",
                address = "address",
                postalCode = "postalCode",
                locality = "locality"
            ),
            Site(
                name = "Test site 1",
                location = "POINT (60.16952 24.93545)",
                address = "",
                postalCode = "postalCode",
                locality = "locality"
            ),
            Site(
                name = "Test site 1",
                location = "POINT (60.16952 24.93545)",
                address = "address",
                postalCode = "",
                locality = "locality"
            ),
            Site(
                name = "Test site 1",
                location = "POINT (60.16952 24.93545)",
                address = "address",
                postalCode = "postalCode",
                locality = ""
            )
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHTS = listOf(
            Site(name = "Test site 1", location = "qqq", address = "address", postalCode = "postalCode", locality = "locality"),
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHT_UNITS = listOf(
            Site(name = "Test site 1", location = "qqq", address = "address", postalCode = "postalCode", locality = "locality"),
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHT_UNITS_FREIGHT_ID = listOf(
            FreightUnit(freightId = UUID.randomUUID(), type = "type", quantity = 1.0, reservations = "reservations")
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

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
                status = fi.metatavu.vp.test.client.models.TaskStatus.TODO
            )
            return listOf(
                Site(name = "Test site 1", location = "qqq", address = "address", postalCode = "postalCode", locality = "locality"),
                sampleValidTask.copy(freightId = UUID.randomUUID()),
                sampleValidTask.copy(customerSiteId = UUID.randomUUID()),
                sampleValidTask.copy(routeId = UUID.randomUUID())
            ).map { jacksonObjectMapper().writeValueAsString(it) }
                .map { SimpleInvalidValueProvider(it) }
        }
    }

}