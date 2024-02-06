package fi.metatavu.vp.deliveryinfo.functional

import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

/**
 * Tests for System API
 */
@QuarkusTest
class SystemTestIT {

    @Test
    fun testPing() {
        When { get("/v1/system/ping") }
            .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("pong"))
    }
}