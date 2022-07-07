package de.hennihaus.routes.resources

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object CreditPaths {
    const val CREDIT_PATH = "/credit"
}

@Serializable
@Resource(CreditPaths.CREDIT_PATH)
data class CreditResource(
    val amountInEuros: Int? = null,
    val termInMonths: Int? = null,
    val ratingLevel: String? = null,
    val delayInMilliseconds: Long? = null,
    val username: String? = null,
    val password: String? = null,
)
