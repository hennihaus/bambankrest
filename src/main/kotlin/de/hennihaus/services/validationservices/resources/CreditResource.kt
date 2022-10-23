package de.hennihaus.services.validationservices.resources

data class CreditResource(
    val amountInEuros: String? = null,
    val termInMonths: String? = null,
    val ratingLevel: String? = null,
    val delayInMilliseconds: String? = null,
    val username: String? = null,
    val password: String? = null,
)
