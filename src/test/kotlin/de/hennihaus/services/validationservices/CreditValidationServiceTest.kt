package de.hennihaus.services.validationservices

import de.hennihaus.bamdatamodel.objectmothers.BankObjectMother.getSyncBank
import de.hennihaus.bamdatamodel.objectmothers.CreditConfigurationObjectMother.getCreditConfigurationWithNoEmptyFields
import de.hennihaus.models.generated.RatingLevel
import de.hennihaus.objectmothers.ConfigurationObjectMother.getConfigBackendConfiguration
import de.hennihaus.objectmothers.CreditObjectMother.getMinValidCreditResource
import de.hennihaus.plugins.RequestValidationException
import de.hennihaus.services.callservices.BankCallService
import de.hennihaus.services.validationservices.CreditValidationService.Companion.PASSWORD_MAX_LENGTH
import de.hennihaus.services.validationservices.CreditValidationService.Companion.PASSWORD_MIN_LENGTH
import de.hennihaus.services.validationservices.CreditValidationService.Companion.USERNAME_MAX_LENGTH
import de.hennihaus.services.validationservices.CreditValidationService.Companion.USERNAME_MIN_LENGTH
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.ktor.client.engine.cio.CIO
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import de.hennihaus.bamdatamodel.RatingLevel as SchufaRating

class CreditValidationServiceTest {

    private val bankCall = spyk(
        objToCopy = BankCallService(
            defaultBankId = "${getSyncBank().uuid}",
            engine = CIO.create(),
            config = getConfigBackendConfiguration(),
        ),
    )

    private val classUnderTest = CreditValidationService(
        bankCall = bankCall,
    )

    @BeforeEach
    fun init() = clearAllMocks()

    @Nested
    inner class ValidateUrl {
        @BeforeEach
        fun init() {
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()
        }

        @Test
        fun `should not throw an exception when resource is valid and ratingLevel is uppercase`() = runBlocking {
            val resource = getMinValidCreditResource(
                ratingLevel = "${RatingLevel.A}".uppercase(),
            )

            shouldNotThrowAny {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }
        }

        @Test
        fun `should not throw an exception when resource is valid and ratingLevel is lowercase`() = runBlocking {
            val resource = getMinValidCreditResource(
                ratingLevel = "${RatingLevel.A}".lowercase(),
            )

            shouldNotThrowAny {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }
        }

        @Test
        fun `should throw an exception with one reason when amountInEuros is null`() = runBlocking {
            val resource = getMinValidCreditResource(
                amountInEuros = null,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "amountInEuros is required",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when amountInEuros is no number`() = runBlocking {
            val resource = getMinValidCreditResource(
                amountInEuros = "no number",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "amountInEuros must be a whole number",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when amountInEuros is smaller minAmountInEuros`() = runBlocking {
            val resource = getMinValidCreditResource(
                amountInEuros = "${getCreditConfigurationWithNoEmptyFields().minAmountInEuros.dec()}",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "amountInEuros must be at least '${getCreditConfigurationWithNoEmptyFields().minAmountInEuros}'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when amountInEuros is greater maxAmountInEuros`() = runBlocking {
            val resource = getMinValidCreditResource(
                amountInEuros = "${getCreditConfigurationWithNoEmptyFields().maxAmountInEuros.inc()}",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "amountInEuros must be at most '${getCreditConfigurationWithNoEmptyFields().maxAmountInEuros}'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when termInMonths is null`() = runBlocking {
            val resource = getMinValidCreditResource(
                termInMonths = null,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "termInMonths is required",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when termInMonths is no number`() = runBlocking {
            val resource = getMinValidCreditResource(
                termInMonths = "no number",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "termInMonths must be a whole number",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when termInMonths is smaller minTermInMonths`() = runBlocking {
            val resource = getMinValidCreditResource(
                termInMonths = "${getCreditConfigurationWithNoEmptyFields().minTermInMonths.dec()}",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "termInMonths must be at least '${getCreditConfigurationWithNoEmptyFields().minTermInMonths}'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when termInMonths is greater maxTermInMonths`() = runBlocking {
            val resource = getMinValidCreditResource(
                termInMonths = "${getCreditConfigurationWithNoEmptyFields().maxTermInMonths.inc()}",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "termInMonths must be at most '${getCreditConfigurationWithNoEmptyFields().maxTermInMonths}'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when ratingLevel is null`() = runBlocking {
            val resource = getMinValidCreditResource(
                ratingLevel = null,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "ratingLevel is required",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when ratingLevel is M`() = runBlocking {
            val resource = getMinValidCreditResource(
                ratingLevel = "M",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "ratingLevel must be one of: 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'N', 'O', 'P', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when ratingLevel is smaller minSchufaRating`() = runBlocking {
            val resource = getMinValidCreditResource(
                ratingLevel = "${RatingLevel.L}"
            )
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields(
                minSchufaRating = SchufaRating.N,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "ratingLevel must be one of: 'N', 'O', 'P', 'n', 'o', 'p'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when ratingLevel is greater maxSchufaRating`() = runBlocking {
            val resource = getMinValidCreditResource(
                ratingLevel = "${RatingLevel.D}"
            )
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields(
                maxSchufaRating = SchufaRating.C,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "ratingLevel must be one of: 'A', 'B', 'C', 'a', 'b', 'c'",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when delayInMilliseconds is null`() = runBlocking {
            val resource = getMinValidCreditResource(
                delayInMilliseconds = null,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "delayInMilliseconds is required",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when delayInMilliseconds is no number`() = runBlocking {
            val resource = getMinValidCreditResource(
                delayInMilliseconds = "no number",
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "delayInMilliseconds must be a whole number",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when username is null`() = runBlocking {
            val resource = getMinValidCreditResource(
                username = null,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "username is required",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when username is too short`() = runBlocking {
            val username = Arb.string(size = USERNAME_MIN_LENGTH.dec()).single()
            val resource = getMinValidCreditResource(
                username = username,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "username must have at least $USERNAME_MIN_LENGTH characters",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when username is too long`() = runBlocking {
            val username = Arb.string(size = USERNAME_MAX_LENGTH.inc()).single()
            val resource = getMinValidCreditResource(
                username = username,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "username must have at most $USERNAME_MAX_LENGTH characters",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when password is null`() = runBlocking {
            val resource = getMinValidCreditResource(
                password = null,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "password is required",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when password is too short`() = runBlocking {
            val password = Arb.string(size = PASSWORD_MIN_LENGTH.dec()).single()
            val resource = getMinValidCreditResource(
                password = password,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "password must have at least $PASSWORD_MIN_LENGTH characters",
                ),
            )
        }

        @Test
        fun `should throw an exception with one reason when password is too long`() = runBlocking {
            val password = Arb.string(size = PASSWORD_MAX_LENGTH.inc()).single()
            val resource = getMinValidCreditResource(
                password = password,
            )

            val result = shouldThrowExactly<RequestValidationException> {
                classUnderTest.validateUrl(
                    resource = resource,
                )
            }

            result shouldBe RequestValidationException(
                reasons = listOf(
                    "password must have at most $PASSWORD_MAX_LENGTH characters",
                ),
            )
        }
    }
}
