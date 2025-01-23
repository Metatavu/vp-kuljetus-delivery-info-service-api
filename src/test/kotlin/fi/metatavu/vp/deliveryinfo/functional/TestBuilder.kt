package fi.metatavu.vp.deliveryinfo.functional

import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.NullAccessTokenProvider
import fi.metatavu.vp.deliveryinfo.functional.auth.TestBuilderAuthentication
import fi.metatavu.vp.deliveryinfo.functional.settings.DefaultTestProfile.Companion.TERMINAL_DEVICE_API_KEY
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Abstract test builder class
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>): AbstractAccessTokenTestBuilder<ApiClient>() {

    var user = createTestBuilderAuthentication(username = "user", password = "userPassword")
    val driver = createTestBuilderAuthentication(username = "driver", password = "driverPassword")
    val manager = createTestBuilderAuthentication(username = "manager", password = "managerPassword")

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, null, authProvider)
    }

    /**
     * Creates test builder authenticatior for given user
     *
     * @param username username
     * @param password password
     * @return test builder authenticatior for given user
     */
    private fun createTestBuilderAuthentication(username: String, password: String): TestBuilderAuthentication {
        val serverUrl = ConfigProvider.getConfig().getValue("quarkus.oidc.auth-server-url", String::class.java).substringBeforeLast("/").substringBeforeLast("/")
        val realm: String = ConfigProvider.getConfig().getValue("quarkus.keycloak.devservices.realm-name", String::class.java)
        val clientId = "test"
        val clientSecret = "secret"
        return TestBuilderAuthentication(this, null, KeycloakAccessTokenProvider(serverUrl, realm, clientId, username, password, clientSecret))
    }

    /**
     * Returns authentication with terminal device api key
     *
     * @param apiKey device key
     * @return authorized client
     */
    fun setTerminalDeviceApiKey(apiKey: String? = null): TestBuilderAuthentication {
        val key = apiKey ?: TERMINAL_DEVICE_API_KEY
        return TestBuilderAuthentication(
            testBuilder = this,
            accessTokenProvider = NullAccessTokenProvider(),
            terminalDeviceApiKey = key
        )
    }

}