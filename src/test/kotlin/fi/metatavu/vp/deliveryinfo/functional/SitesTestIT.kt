package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.deliveryinfo.functional.impl.InvalidTestValues
import fi.metatavu.vp.deliveryinfo.functional.settings.ApiTestSettings
import fi.metatavu.vp.test.client.models.Site
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Sites test
 */
@QuarkusTest
class SitesTestIT : AbstractFunctionalTest() {

    @Test
    fun testCreate() = createTestBuilder().use {
        val site1 = Site(
            name = "Test site 1",
            location = "POINT(60.16952 24.93545)"
        )

        val result = it.user.sites.create(site1)
        assertNotNull(result)
        assertNotNull(result.id)
        assertEquals(site1.name, result.name)
        assertEquals(site1.location, result.location)
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { tb ->
        InvalidValueTestScenarioBuilder(
            path = "v1/sites",
            method = Method.POST,
            token = tb.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_SITES,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testList() = createTestBuilder().use {
        val site1 = Site(
            name = "Test site 1",
            location = "POINT(60.16952 24.93545)"
        )
        val site2 = Site(
            name = "Test site 2",
            location = "POINT(60.16952 24.93545)"
        )
        val site3 = Site(
            name = "Test site 3",
            location = "POINT(60.16952 24.93545)"
        )
        it.user.sites.create(site1)
        it.user.sites.create(site2)
        it.user.sites.create(site3)
        val totalList = it.user.sites.listSites()
        assertEquals(3, totalList.size)

        val pagedList1 = it.user.sites.listSites(first = 1, max = 1)
        assertEquals(1, pagedList1.size)

        val pagedList2 = it.user.sites.listSites(first = 2, max = 5)
        assertEquals(1, pagedList2.size)
    }

    @Test
    fun testFind() = createTestBuilder().use {
        val createdSite = it.user.sites.create()
        val foundSite = it.user.sites.findSite(createdSite.id!!)
        assertNotNull(foundSite)
        assertEquals(createdSite.id, foundSite.id)
        assertEquals(foundSite.name, foundSite.name)
        assertEquals(foundSite.location, foundSite.location)
    }

    @Test
    fun testUpdate() = createTestBuilder().use {
        val createdSite = it.user.sites.create()
        val updateData = Site(
            name = "Test site 2",
            location = "POINT(100 100)"
        )
        val result = it.user.sites.updateSite(createdSite.id!!, updateData)
        assertNotNull(result)
        assertEquals(createdSite.id, result.id)
        assertEquals(updateData.name, result.name)
        assertEquals(updateData.location, result.location)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { tb ->
        val createdSite = tb.user.sites.create()

        InvalidValueTestScenarioBuilder(
            path = "v1/sites/{siteId}",
            method = Method.PUT,
            token = tb.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "siteId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdSite.id!!,
                    expectedStatus = 404
                )
            )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.INVALID_SITES,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val createdSite = it.user.sites.create()
        it.user.sites.deleteSite(createdSite.id!!)
        val emptyList = it.user.sites.listSites()
        assertEquals(0, emptyList.size)
    }

}