package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for Tasks API
 */
@QuarkusIntegrationTest
@QuarkusTestResource(WorkPlanningMock::class)
@TestProfile(DefaultTestProfile::class)
class NativeTasksTestIT: TasksTestIT()
