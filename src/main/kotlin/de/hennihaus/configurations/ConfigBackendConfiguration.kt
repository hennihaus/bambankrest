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

        const val BANK_NAME = "ktor.application.bankName"
        const val CONFIG_BACKEND_PROTOCOL = "ktor.configBackend.protocol"
        const val CONFIG_BACKEND_HOST = "ktor.configBackend.host"
        const val CONFIG_BACKEND_PORT = "ktor.configBackend.port"
        const val CONFIG_BACKEND_RETRIES = "ktor.configBackend.retries"
    }
}
