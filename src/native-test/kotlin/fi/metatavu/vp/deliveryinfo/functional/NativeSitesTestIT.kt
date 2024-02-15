package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for Sites API
 */
@QuarkusIntegrationTest
@TestProfile(DefaultTestProfile::class)
class NativeSitesTestIT: SitesTestIT()
