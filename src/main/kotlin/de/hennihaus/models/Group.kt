package de.hennihaus.models

import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.ID_FIELD
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    @SerialName(ID_FIELD)
    val id: String,
    val username: String,
    val password: String,
    val jmsQueue: String,
    val students: List<String>,
    val stats: Map<String, Int>,
    val hasPassed: Boolean,
)
