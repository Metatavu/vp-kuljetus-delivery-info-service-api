package fi.metatavu.vp.deliveryinfo.functional.impl

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import fi.metatavu.vp.workplanning.model.Route
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import java.time.OffsetDateTime
import java.util.*

/**
 * Wiremock for work planning service
 */
class WorkPlanningMock : QuarkusTestResourceLifecycleManager {
    private var wireMockServer: WireMockServer? = null

    private val authHeader = "Authorization"
    private val bearerPattern: StringValuePattern = WireMock.containing("Bearer")

    private val objectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())
    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(8080)
        wireMockServer!!.start()

        wireMockServer!!.stubFor(
            WireMock.get(WireMock.urlPathMatching("/v1/routes/.*"))
                .withHeader(authHeader, bearerPattern)
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(404)
                )
        )

        wireMockServer!!.stubFor(
            WireMock.get(WireMock.urlEqualTo("/v1/routes/$routeId"))
                .withHeader(authHeader, bearerPattern)
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            objectMapper.writeValueAsString(
                                Route(
                                    id = routeId,
                                    truckId = truckId,
                                    driverId = driverId,
                                    departureTime = OffsetDateTime.now(),
                                    name = "standard route"
                                )
                            )
                        )
                )
        )


        return java.util.Map.of(
            "quarkus.rest-client.\"fi.metatavu.vp.workplanning.spec.RoutesApi\".url",
            wireMockServer!!.baseUrl()
        )
    }

    override fun stop() {
        if (null != wireMockServer) {
            wireMockServer!!.stop()
        }
    }

    companion object {
        val routeId: UUID = UUID.randomUUID()
        val truckId: UUID = UUID.randomUUID()
        val driverId: UUID = UUID.randomUUID()
    }
}