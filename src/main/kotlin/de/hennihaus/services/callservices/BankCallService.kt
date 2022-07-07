package de.hennihaus.services.callservices

import de.hennihaus.configurations.ConfigBackendConfiguration
import de.hennihaus.models.Bank
import de.hennihaus.models.CreditConfiguration
import de.hennihaus.services.callservices.resources.Banks
import de.hennihaus.utils.configureDefaultRequests
import de.hennihaus.utils.configureMonitoring
import de.hennihaus.utils.configureRetryBehavior
import de.hennihaus.utils.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.resources.get
import org.koin.core.annotation.Single

@Single
class BankCallService(private val engine: HttpClientEngine, private val config: ConfigBackendConfiguration) {

    private val client = HttpClient(engine = engine) {
        expectSuccess = true
        configureMonitoring()
        configureSerialization()
        configureRetryBehavior(
            maxRetries = config.maxRetries,
        )
        configureDefaultRequests(
            protocol = config.protocol,
            host = config.host,
            port = config.port,
        )
    }

    suspend fun getCreditConfigByJmsQueue(jmsQueue: String = config.defaultJmsQueue): CreditConfiguration {
        val response = client.get(
            resource = Banks.JmsQueue(
                jmsQueue = jmsQueue,
            ),
        )
        return response.body<Bank>().creditConfiguration ?: throw IllegalStateException(
            CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
        )
    }

    companion object {
        const val CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE = "[creditConfiguration not found]"
    }
}
