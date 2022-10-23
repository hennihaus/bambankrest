package de.hennihaus.services.validationservices

import de.hennihaus.bamdatamodel.RatingLevel
import de.hennihaus.services.callservices.BankCallService
import de.hennihaus.services.validationservices.resources.CreditResource
import io.konform.validation.Constraint
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import org.koin.core.annotation.Single

@Single
class CreditValidationService(private val bankCall: BankCallService) : ValidationService<CreditResource> {

    override suspend fun urlValidation(resource: CreditResource) = bankCall.getCreditConfigByBankId().let {
        Validation {
            CreditResource::amountInEuros required {
                numberType()
                minimum(minimumInclusive = it.minAmountInEuros)
                maximum(maximumInclusive = it.maxAmountInEuros)
            }
            CreditResource::termInMonths required {
                numberType()
                minimum(minimumInclusive = it.minTermInMonths)
                maximum(maximumInclusive = it.maxTermInMonths)
            }
            CreditResource::ratingLevel required {
                ratingLevelIgnoreCase(
                    minRatingLevelInclusive = it.minSchufaRating,
                    maxRatingLevelInclusive = it.maxSchufaRating,
                )
            }
            CreditResource::delayInMilliseconds required {
                numberType()
            }
            CreditResource::username required {
                minLength(length = USERNAME_MIN_LENGTH)
                maxLength(length = USERNAME_MAX_LENGTH)
            }
            CreditResource::password required {
                minLength(length = PASSWORD_MIN_LENGTH)
                maxLength(length = PASSWORD_MAX_LENGTH)
            }
        }
    }

    private fun ValidationBuilder<String>.numberType() = addConstraint(
        errorMessage = "must be a whole number",
    ) {
        isNumber(text = it)
    }

    private fun ValidationBuilder<String>.minimum(minimumInclusive: Number) = addConstraint(
        errorMessage = "must be at least '{0}'",
        templateValues = arrayOf(
            minimumInclusive.toString(),
        ),
    ) {
        if (isNumber(text = it)) it.toDouble() >= minimumInclusive.toDouble() else true
    }

    private fun ValidationBuilder<String>.maximum(maximumInclusive: Number) = addConstraint(
        errorMessage = "must be at most '{0}'",
        templateValues = arrayOf(
            maximumInclusive.toString(),
        )
    ) {
        if (isNumber(text = it)) it.toDouble() <= maximumInclusive.toDouble() else true
    }

    private fun ValidationBuilder<String>.ratingLevelIgnoreCase(
        minRatingLevelInclusive: RatingLevel,
        maxRatingLevelInclusive: RatingLevel,
    ): Constraint<String> {
        val enumNames = enumValues<RatingLevel>().filter {
            (it >= minRatingLevelInclusive) and (it <= maxRatingLevelInclusive)
        }
        val enumNamesUpperCase = enumNames.map { it.name.uppercase() }
        val enumNamesLowerCase = enumNames.map { it.name.lowercase() }

        return addConstraint(
            errorMessage = "must be one of: {0}",
            templateValues = arrayOf(
                (enumNamesUpperCase + enumNamesLowerCase).joinToString(
                    separator = "', '",
                    prefix = "'",
                    postfix = "'",
                ),
            ),
        ) {
            it in enumNamesUpperCase + enumNamesLowerCase
        }
    }

    private fun isNumber(text: String) = text.all { it.isDigit() }

    companion object {
        const val USERNAME_MIN_LENGTH = 6
        const val USERNAME_MAX_LENGTH = 50
        const val PASSWORD_MIN_LENGTH = 8
        const val PASSWORD_MAX_LENGTH = 50
    }
}
