package fi.metatavu.vp.deliveryinfo.functional.settings

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Default test profile
 */
class DefaultTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): MutableMap<String, String> {
        val config: MutableMap<String, String> = HashMap()
        config["vp.env"] = "TEST"
        config["vp.deliveryinfo.terminaldevice.apiKey"] = "test-terminal-device-api-key"
        config["mp.messaging.outgoing.vp-out.exchange.name"] = EXCHANGE_NAME
        return config
    }

    companion object {
        const val TERMINAL_DEVICE_API_KEY = "test-terminal-device-api-key"
        const val EXCHANGE_NAME = "test-exchange"
    }
}