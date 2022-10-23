package de.hennihaus.objectmothers

import de.hennihaus.bamdatamodel.objectmothers.CreditConfigurationObjectMother.DEFAULT_MIN_AMOUNT_IN_EUROS
import de.hennihaus.bamdatamodel.objectmothers.CreditConfigurationObjectMother.DEFAULT_MIN_SCHUFA_RATING
import de.hennihaus.bamdatamodel.objectmothers.CreditConfigurationObjectMother.DEFAULT_MIN_TERM_IN_MONTHS
import de.hennihaus.bamdatamodel.objectmothers.TeamObjectMother.DEFAULT_PASSWORD
import de.hennihaus.bamdatamodel.objectmothers.TeamObjectMother.FIRST_TEAM_USERNAME
import de.hennihaus.models.generated.Credit
import de.hennihaus.services.validationservices.resources.CreditResource

object CreditObjectMother {

    const val DEFAULT_LOWEST_LENDING_RATE_IN_PERCENT = 0.0
    const val DEFAULT_HIGHEST_LENDING_RATE_IN_PERCENT = 10.0

    const val DEFAULT_DELAY_IN_MILLISECONDS = 0L

    fun getLowestCredit(
        lendingRateInPercent: Double = DEFAULT_LOWEST_LENDING_RATE_IN_PERCENT,
    ) = Credit(
        lendingRateInPercent = lendingRateInPercent,
    )

    fun getHighestCredit(
        lendingRateInPercent: Double = DEFAULT_HIGHEST_LENDING_RATE_IN_PERCENT,
    ) = Credit(
        lendingRateInPercent = lendingRateInPercent,
    )

    fun getMinValidCreditResource(
        amountInEuros: String? = "$DEFAULT_MIN_AMOUNT_IN_EUROS",
        termInMonths: String? = "$DEFAULT_MIN_TERM_IN_MONTHS",
        ratingLevel: String? = DEFAULT_MIN_SCHUFA_RATING.name,
        delayInMilliseconds: String? = "$DEFAULT_DELAY_IN_MILLISECONDS",
        username: String? = FIRST_TEAM_USERNAME,
        password: String? = DEFAULT_PASSWORD,
    ) = CreditResource(
        amountInEuros = amountInEuros,
        termInMonths = termInMonths,
        ratingLevel = ratingLevel,
        delayInMilliseconds = delayInMilliseconds,
        username = username,
        password = password,
    )
}
