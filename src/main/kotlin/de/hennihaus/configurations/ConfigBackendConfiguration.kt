package de.hennihaus.configurations

import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.BANK_NAME
import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.CONFIG_BACKEND_HOST
import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.CONFIG_BACKEND_PORT
import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.CONFIG_BACKEND_PROTOCOL
import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.CONFIG_BACKEND_RETRIES
import org.koin.dsl.module

val configBackendModule = module {
    single {
        val protocol = getProperty<String>(key = CONFIG_BACKEND_PROTOCOL)
        val host = getProperty<String>(key = CONFIG_BACKEND_HOST)
        val port = getProperty<String>(key = CONFIG_BACKEND_PORT)
        val maxRetries = getProperty<String>(key = CONFIG_BACKEND_RETRIES)
        val defaultJmsQueue = getProperty<String>(key = BANK_NAME)

        ConfigBackendConfiguration(
            protocol = protocol,
            host = host,
            port = port.toInt(),
            maxRetries = maxRetries.toInt(),
            defaultJmsQueue = defaultJmsQueue,
        )
    }
}

data class ConfigBackendConfiguration(
    val protocol: String,
    val host: String,
    val port: Int,
    val maxRetries: Int,
    val defaultJmsQueue: String,
) {
    companion object {
        const val ID_FIELD = "_id"

        const val BANK_NAME = "bank.name"
        const val CONFIG_BACKEND_PROTOCOL = "configBackend.protocol"
        const val CONFIG_BACKEND_HOST = "configBackend.host"
        const val CONFIG_BACKEND_PORT = "configBackend.port"
        const val CONFIG_BACKEND_RETRIES = "configBackend.retries"
    }
}
