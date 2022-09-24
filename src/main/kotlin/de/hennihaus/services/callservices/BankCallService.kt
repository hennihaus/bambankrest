package de.hennihaus.services.callservices

import de.hennihaus.bamdatamodel.Bank
import de.hennihaus.bamdatamodel.CreditConfiguration
import de.hennihaus.configurations.ConfigBackendConfiguration
import de.hennihaus.configurations.Configuration.BANK_UUID
import de.hennihaus.services.callservices.resources.Banks
import de.hennihaus.utils.configureDefaultRequests
import de.hennihaus.utils.configureMonitoring
import de.hennihaus.utils.configureRetryBehavior
import de.hennihaus.utils.configureSerialization
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.resources.get
import io.ktor.server.plugins.NotFoundException
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class BankCallService(
    @Property(BANK_UUID) private val defaultBankId: String,
    private val engine: HttpClientEngine,
    private val config: ConfigBackendConfiguration,
) {

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
            apiVersion = config.apiVersion,
        )
    }

    suspend fun getBankById(id: UUID = UUID.fromString(defaultBankId)): Bank {
        val response = client.get(
            resource = Banks.Id(
                id = "$id",
            ),
        )
        return response.body()
    }

    suspend fun getCreditConfigByBankId(id: UUID = UUID.fromString(defaultBankId)): CreditConfiguration {
        val response = client.get(
            resource = Banks.Id(
                id = "$id",
            ),
        )
        return response.body<Bank>().creditConfiguration ?: throw NotFoundException(
            message = CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE,
        )
    }

    companion object {
        const val CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE = "[creditConfiguration not found]"
    }
}
