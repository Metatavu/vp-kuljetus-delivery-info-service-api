package fi.metatavu.vp.deliveryinfo.functional.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.FreightUnit
import fi.metatavu.vp.test.client.models.Site
import java.util.*

/**
 * Invalid test values for the different entities
 */
class InvalidTestValues: InvalidValues() {

    companion object {
        val INVALID_SITES = listOf(
            Site(name = "Test site 1", location = "qqq"),
            Site(name = "", location = "POINT(60.16952 24.93545)"),
            Site(name = "Test site 1", location = "")
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHTS = listOf(
            Site(name = "Test site 1", location = "qqq"),
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHT_UNITS = listOf(
            Site(name = "Test site 1", location = "qqq"),
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }

        val INVALID_FREIGHT_UNITS_FREIGHT_ID = listOf(
            FreightUnit(freightId = UUID.randomUUID(), quantityUnit = "pc", type = "type", quantity = "quantity", reservations = "reservations")
        ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }
    }

}