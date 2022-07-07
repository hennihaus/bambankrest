package de.hennihaus.objectmothers

import de.hennihaus.configurations.ConfigBackendConfiguration
import de.hennihaus.objectmothers.BankObjectMother.SYNC_BANK_NAME

object ConfigurationObjectMother {

    const val DEFAULT_PROTOCOL = "http"
    const val DEFAULT_HOST = "0.0.0.0"
    const val DEFAULT_PORT = 8080
    const val DEFAULT_MAX_RETRIES = 2

    fun getConfigBackendConfiguration(
        protocol: String = DEFAULT_PROTOCOL,
        host: String = DEFAULT_HOST,
        port: Int = DEFAULT_PORT,
        maxRetries: Int = DEFAULT_MAX_RETRIES,
        defaultJmsQueue: String = SYNC_BANK_NAME,
    ) = ConfigBackendConfiguration(
        protocol = protocol,
        host = host,
        port = port,
        maxRetries = maxRetries,
        defaultJmsQueue = defaultJmsQueue,
    )
}
