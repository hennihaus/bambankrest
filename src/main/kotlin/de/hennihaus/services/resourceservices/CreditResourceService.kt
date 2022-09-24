package de.hennihaus.services.resourceservices

import de.hennihaus.bamdatamodel.RatingLevel
import de.hennihaus.routes.resources.CreditResource
import de.hennihaus.services.callservices.BankCallService
import io.konform.validation.Constraint
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.minimum
import org.koin.core.annotation.Single

@Single
class CreditResourceService(private val bankCall: BankCallService) : ResourceService<CreditResource> {

    override suspend fun resourceValidation() = bankCall.getCreditConfigByBankId().let {
        Validation {
            CreditResource::amountInEuros required {
                minimum(minimumInclusive = it.minAmountInEuros)
                maximum(maximumInclusive = it.maxAmountInEuros)
            }
            CreditResource::termInMonths required {
                minimum(minimumInclusive = it.minTermInMonths)
                maximum(maximumInclusive = it.maxTermInMonths)
            }
            CreditResource::ratingLevel required {
                ratingLevelIgnoreCase(
                    minRatingLevelInclusive = it.minSchufaRating,
                    maxRatingLevelInclusive = it.maxSchufaRating,
                )
            }
            CreditResource::delayInMilliseconds required {}
            CreditResource::username required {
                minLength(length = USERNAME_MIN_LENGTH)
            }
            CreditResource::password required {
                minLength(length = PASSWORD_MIN_LENGTH)
            }
        }
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

    companion object {
        private const val USERNAME_MIN_LENGTH = 1
        private const val PASSWORD_MIN_LENGTH = 1
    }
}
