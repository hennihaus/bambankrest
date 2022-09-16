package de.hennihaus.services.resourceservices

import de.hennihaus.models.generated.RatingLevel
import de.hennihaus.objectmothers.ConfigurationObjectMother.getConfigBackendConfiguration
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.DEFAULT_MAX_AMOUNT_IN_EUROS
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.DEFAULT_MAX_TERM_IN_MONTHS
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.DEFAULT_MIN_AMOUNT_IN_EUROS
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.DEFAULT_MIN_TERM_IN_MONTHS
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.getCreditConfiguration
import de.hennihaus.objectmothers.CreditObjectMother.getMinValidCreditResource
import de.hennihaus.plugins.ValidationException
import de.hennihaus.routes.resources.CreditResource
import de.hennihaus.services.callservices.BankCallService
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.ktor.client.engine.cio.CIO
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreditResourceServiceTest {

    private val config = getConfigBackendConfiguration()

    private val bankCall = spyk(
        objToCopy = BankCallService(
            engine = CIO.create(),
            config = config,
        ),
    )

    private val classUnderTest = CreditResourceService(
        bankCall = bankCall,
    )

    @Nested
    inner class Validate {

        @BeforeEach
        fun init() {
            clearAllMocks()

            // default behaviour
            coEvery { bankCall.getCreditConfigByJmsQueue() } returns getCreditConfiguration()
        }

        @Test
        fun `should return same resource when valid and ratingLevel = A`() = runBlocking {
            val resource = getMinValidCreditResource(ratingLevel = "${RatingLevel.A}")

            val result: CreditResource = classUnderTest.validate(resource = resource)

            result shouldBe resource
            coVerifySequence {
                bankCall.getCreditConfigByJmsQueue(
                    jmsQueue = config.defaultJmsQueue,
                )
            }
        }

        @Test
        fun `should return same resource when valid and ratingLevel = a`() = runBlocking {
            val resource = getMinValidCreditResource(ratingLevel = "${RatingLevel.A}".lowercase())

            val result: CreditResource = classUnderTest.validate(resource = resource)

            result shouldBe resource
        }

        @Test
        fun `should throw exception when amountInEuros = null`() = runBlocking {
            val resource = getMinValidCreditResource(amountInEuros = null)

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[amountInEuros is required]"
            coVerify(exactly = 1) {
                bankCall.getCreditConfigByJmsQueue(
                    jmsQueue = config.defaultJmsQueue,
                )
            }
        }

        @Test
        fun `should throw exception when amountInEuros too small`() = runBlocking {
            val resource = getMinValidCreditResource(
                amountInEuros = DEFAULT_MIN_AMOUNT_IN_EUROS.dec(),
            )
            coEvery { bankCall.getCreditConfigByJmsQueue(jmsQueue = any()) } returns getCreditConfiguration(
                minAmountInEuros = DEFAULT_MIN_AMOUNT_IN_EUROS,
            )

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[amountInEuros must be at least '$DEFAULT_MIN_AMOUNT_IN_EUROS']"
        }

        @Test
        fun `should throw exception when amountInEuros too high`() = runBlocking {
            val resource = getMinValidCreditResource(
                amountInEuros = DEFAULT_MAX_AMOUNT_IN_EUROS.inc(),
            )
            coEvery { bankCall.getCreditConfigByJmsQueue(jmsQueue = any()) } returns getCreditConfiguration(
                maxAmountInEuros = DEFAULT_MAX_AMOUNT_IN_EUROS,
            )

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[amountInEuros must be at most '$DEFAULT_MAX_AMOUNT_IN_EUROS']"
        }

        @Test
        fun `should throw exception when termInMonths = null`() = runBlocking {
            val resource = getMinValidCreditResource(termInMonths = null)

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[termInMonths is required]"
        }

        @Test
        fun `should throw exception when termInMonths too small`() = runBlocking {
            val resource = getMinValidCreditResource(
                termInMonths = DEFAULT_MIN_TERM_IN_MONTHS.dec(),
            )
            coEvery { bankCall.getCreditConfigByJmsQueue(jmsQueue = any()) } returns getCreditConfiguration(
                minTermInMonths = DEFAULT_MIN_TERM_IN_MONTHS,
            )

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[termInMonths must be at least '$DEFAULT_MIN_TERM_IN_MONTHS']"
        }

        @Test
        fun `should throw exception when termInMonths too high`() = runBlocking {
            val resource = getMinValidCreditResource(
                termInMonths = DEFAULT_MAX_TERM_IN_MONTHS.inc(),
            )
            coEvery { bankCall.getCreditConfigByJmsQueue(jmsQueue = any()) } returns getCreditConfiguration(
                maxTermInMonths = DEFAULT_MAX_TERM_IN_MONTHS,
            )

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[termInMonths must be at most '$DEFAULT_MAX_TERM_IN_MONTHS']"
        }

        @Test
        fun `should throw exception when ratingLevel = null`() = runBlocking {
            val resource = getMinValidCreditResource(ratingLevel = null)

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[ratingLevel is required]"
        }

        @Test
        fun `should throw exception when ratingLevel = M`() = runBlocking {
            val resource = getMinValidCreditResource(ratingLevel = "M")

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[ratingLevel must be one of: 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'N', 'O', 'P', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p']"
        }

        @Test
        fun `should throw exception when ratingLevel too small`() = runBlocking {
            val resource = getMinValidCreditResource(ratingLevel = "${RatingLevel.A}")
            coEvery { bankCall.getCreditConfigByJmsQueue(jmsQueue = any()) } returns getCreditConfiguration(
                minSchufaRating = RatingLevel.B,
            )

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[ratingLevel must be one of: 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'N', 'O', 'P', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p']"
        }

        @Test
        fun `should throw exception when ratingLevel too high`() = runBlocking {
            val resource = getMinValidCreditResource(ratingLevel = "${RatingLevel.P}")
            coEvery { bankCall.getCreditConfigByJmsQueue(jmsQueue = any()) } returns getCreditConfiguration(
                maxSchufaRating = RatingLevel.O,
            )

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[ratingLevel must be one of: 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'N', 'O', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o']"
        }

        @Test
        fun `should throw exception when delayInMilliseconds = null`() = runBlocking {
            val resource = getMinValidCreditResource(delayInMilliseconds = null)

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[delayInMilliseconds is required]"
        }

        @Test
        fun `should throw exception when username = null`() = runBlocking {
            val resource = getMinValidCreditResource(username = null)

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[username is required]"
        }

        @Test
        fun `should throw exception when username = empty`() = runBlocking {
            val resource = getMinValidCreditResource(username = "")

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[username must have at least 1 characters]"
        }

        @Test
        fun `should throw exception when password = null`() = runBlocking {
            val resource = getMinValidCreditResource(password = null)

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[password is required]"
        }

        @Test
        fun `should throw exception when password = empty`() = runBlocking {
            val resource = getMinValidCreditResource(password = "")

            val result: ValidationException = shouldThrowExactly {
                classUnderTest.validate(resource = resource)
            }

            result shouldHaveMessage "[password must have at least 1 characters]"
        }
    }
}
