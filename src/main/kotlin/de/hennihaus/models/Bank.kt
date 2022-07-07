package de.hennihaus.models

import de.hennihaus.configurations.ConfigBackendConfiguration.Companion.ID_FIELD
import de.hennihaus.models.generated.RatingLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bank(
    @SerialName(ID_FIELD)
    val jmsQueue: String,
    val name: String,
    val thumbnailUrl: String,
    val isAsync: Boolean,
    val isActive: Boolean,
    val creditConfiguration: CreditConfiguration?,
    val groups: List<Group>,
)

@Serializable
data class CreditConfiguration(
    val minAmountInEuros: Int,
    val maxAmountInEuros: Int,
    val minTermInMonths: Int,
    val maxTermInMonths: Int,
    val minSchufaRating: RatingLevel,
    val maxSchufaRating: RatingLevel,
)
