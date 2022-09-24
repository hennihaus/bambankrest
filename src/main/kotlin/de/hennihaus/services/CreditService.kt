package de.hennihaus.services

import de.hennihaus.models.generated.Credit
import de.hennihaus.services.callservices.BankCallService
import kotlinx.coroutines.delay
import org.koin.core.annotation.Single

@Single
class CreditService(private val bankCall: BankCallService) {

    suspend fun calculateCredit(
        amountInEuros: Int,
        termInMonths: Int,
        ratingLevel: String,
        delayInMilliseconds: Long?,
    ): Credit {
        delay(timeMillis = delayInMilliseconds ?: ZERO_DELAY)

        return Credit(
            lendingRateInPercent = calculateLendingRate(
                amountInEuros = amountInEuros,
                termInMonths = termInMonths,
                ratingLevel = ratingLevel,
            ),
        )
    }

    private suspend fun calculateLendingRate(amountInEuros: Int, termInMonths: Int, ratingLevel: String): Double {
        var creditRate = termInMonths.mod(other = DIVISOR_TERM_IN_MONTHS).toDouble()

        creditRate += amountInEuros.mod(other = DIVISOR_AMOUNT_IN_EUROS)
        creditRate += ratingLevel.getMinSchufaRating().mod(other = DIVISOR_RATING_LEVEL)
        creditRate += Math.random().times(other = MULTIPLIER_LENDING_RATE)

        return creditRate.mod(other = DIVISOR_LENDING_RATE)
    }

    private suspend fun String.getMinSchufaRating(): Int = bankCall.getCreditConfigByBankId().let {
        uppercase().first().minus(
            other = it.minSchufaRating.name.first(),
        )
    }

    companion object {
        private const val ZERO_DELAY = 0L

        private const val DIVISOR_TERM_IN_MONTHS = 12
        private const val DIVISOR_AMOUNT_IN_EUROS = 1_000
        private const val DIVISOR_RATING_LEVEL = 10
        private const val MULTIPLIER_LENDING_RATE = 3
        private const val DIVISOR_LENDING_RATE = 10.0
    }
}
