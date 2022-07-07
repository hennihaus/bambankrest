package de.hennihaus.objectmothers

import de.hennihaus.models.CreditConfiguration
import de.hennihaus.models.generated.RatingLevel

object CreditConfigurationObjectMother {

    const val DEFAULT_MIN_AMOUNT_IN_EUROS = 10_000
    const val DEFAULT_MAX_AMOUNT_IN_EUROS = 50_000
    const val DEFAULT_MIN_TERM_IN_MONTHS = 6
    const val DEFAULT_MAX_TERM_IN_MONTHS = 36
    const val DEFAULT_MIN_SCHUFA_RATING = "A"
    const val DEFAULT_MAX_SCHUFA_RATING = "P"

    fun getCreditConfiguration(
        minAmountInEuros: Int = DEFAULT_MIN_AMOUNT_IN_EUROS,
        maxAmountInEuros: Int = DEFAULT_MAX_AMOUNT_IN_EUROS,
        minTermInMonths: Int = DEFAULT_MIN_TERM_IN_MONTHS,
        maxTermInMonths: Int = DEFAULT_MAX_TERM_IN_MONTHS,
        minSchufaRating: RatingLevel = RatingLevel.valueOf(value = DEFAULT_MIN_SCHUFA_RATING),
        maxSchufaRating: RatingLevel = RatingLevel.valueOf(value = DEFAULT_MAX_SCHUFA_RATING),
    ) = CreditConfiguration(
        minAmountInEuros = minAmountInEuros,
        maxAmountInEuros = maxAmountInEuros,
        minTermInMonths = minTermInMonths,
        maxTermInMonths = maxTermInMonths,
        minSchufaRating = minSchufaRating,
        maxSchufaRating = maxSchufaRating,
    )
}
