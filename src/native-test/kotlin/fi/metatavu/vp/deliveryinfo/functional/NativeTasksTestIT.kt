package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.vp.deliveryinfo.functional.impl.WorkPlanningMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest

/**
 * Native tests for Tasks API
 */
@QuarkusIntegrationTest
@QuarkusTestResource(WorkPlanningMock::class)
class NativeTasksTestIT: TasksTestIT()
