package de.hennihaus.services

import de.hennihaus.bamdatamodel.objectmothers.BankObjectMother
import de.hennihaus.bamdatamodel.objectmothers.CreditConfigurationObjectMother.getCreditConfigurationWithNoEmptyFields
import de.hennihaus.models.generated.Credit
import de.hennihaus.models.generated.RatingLevel
import de.hennihaus.objectmothers.ConfigurationObjectMother.getConfigBackendConfiguration
import de.hennihaus.objectmothers.CreditObjectMother.getHighestCredit
import de.hennihaus.objectmothers.CreditObjectMother.getLowestCredit
import de.hennihaus.objectmothers.CreditObjectMother.getMinValidCreditResource
import de.hennihaus.services.callservices.BankCallService
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.kotest.property.exhaustive.map
import io.ktor.client.engine.cio.CIO
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class CreditServiceTest {

    private val bankCall = spyk(
        objToCopy = BankCallService(
            defaultBankId = "${BankObjectMother.getSyncBank().uuid}",
            engine = CIO.create(),
            config = getConfigBackendConfiguration(),
        ),
    )

    private val classUnderTest = CreditService(
        bankCall = bankCall,
    )

    @BeforeEach
    fun init() = clearAllMocks()

    @Nested
    inner class CalculateCredit {
        @Test
        fun `should return credit with 0 and 10 percent when all values realistic`() = runBlocking<Unit> {
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()

            checkAll(
                genA = Arb.nonNegativeInt(max = 1_000_000_000),
                genB = Arb.nonNegativeInt(max = 6_000),
                genC = Exhaustive.enum<RatingLevel>().map { "$it".uppercase() },
            ) { amountInEuros, termInMonths, ratingLevel ->

                val result: Credit = classUnderTest.calculateCredit(
                    amountInEuros = amountInEuros,
                    termInMonths = termInMonths,
                    ratingLevel = ratingLevel,
                    delayInMilliseconds = null,
                )

                result.lendingRateInPercent.shouldBeBetween(
                    a = getLowestCredit().lendingRateInPercent,
                    b = getHighestCredit().lendingRateInPercent,
                    tolerance = 0.0,
                )
                coVerify { bankCall.getCreditConfigByBankId() }
            }
        }

        @Test
        fun `should return credit with 0 and 10 percent when ratingLevel = uppercase`() = runBlocking<Unit> {
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()

            checkAll(
                genA = Arb.nonNegativeInt(),
                genB = Arb.nonNegativeInt(),
                genC = Exhaustive.enum<RatingLevel>().map { "$it".uppercase() },
            ) { amountInEuros, termInMonths, ratingLevel ->

                val result: Credit = classUnderTest.calculateCredit(
                    amountInEuros = amountInEuros,
                    termInMonths = termInMonths,
                    ratingLevel = ratingLevel,
                    delayInMilliseconds = null,
                )

                result.lendingRateInPercent.shouldBeBetween(
                    a = getLowestCredit().lendingRateInPercent,
                    b = getHighestCredit().lendingRateInPercent,
                    tolerance = 0.0,
                )
            }
        }

        @Test
        fun `should return credit with 0 and 10 percent when ratingLevel = lowercase`() = runBlocking<Unit> {
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()
            checkAll(
                genA = Arb.nonNegativeInt(),
                genB = Arb.nonNegativeInt(),
                genC = Exhaustive.enum<RatingLevel>().map { "$it".lowercase() },
            ) { amountInEuros, termInMonths, ratingLevel ->

                val result: Credit = classUnderTest.calculateCredit(
                    amountInEuros = amountInEuros,
                    termInMonths = termInMonths,
                    ratingLevel = ratingLevel,
                    delayInMilliseconds = null,
                )

                result.lendingRateInPercent.shouldBeBetween(
                    a = getLowestCredit().lendingRateInPercent,
                    b = getHighestCredit().lendingRateInPercent,
                    tolerance = 0.0,
                )
            }
        }

        @Test
        fun `should delay at most 15ms when delayInMilliseconds = null`() = runBlocking<Unit> {
            val (amountInEuros, termInMonths, ratingLevel) = getMinValidCreditResource()
            val delayInMilliseconds = null
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()

            val time = measureTimeMillis {
                classUnderTest.calculateCredit(
                    amountInEuros = amountInEuros!!,
                    termInMonths = termInMonths!!,
                    ratingLevel = ratingLevel!!,
                    delayInMilliseconds = delayInMilliseconds,
                )
            }

            time shouldBeLessThanOrEqual 15L
        }

        @Test
        fun `should delay at most 1ms when delayInMilliseconds is negative`() = runBlocking<Unit> {
            val (amountInEuros, termInMonths, ratingLevel) = getMinValidCreditResource()
            val delayInMilliseconds = Long.MIN_VALUE
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()

            val time = measureTimeMillis {
                classUnderTest.calculateCredit(
                    amountInEuros = amountInEuros!!,
                    termInMonths = termInMonths!!,
                    ratingLevel = ratingLevel!!,
                    delayInMilliseconds = delayInMilliseconds,
                )
            }

            time shouldBeLessThanOrEqual 1L
        }

        @Test
        fun `should delay at least 250ms when delayInMilliseconds = 250`() = runBlocking<Unit> {
            val (amountInEuros, termInMonths, ratingLevel) = getMinValidCreditResource()
            val delayInMilliseconds = 250L
            coEvery { bankCall.getCreditConfigByBankId() } returns getCreditConfigurationWithNoEmptyFields()

            val time = measureTimeMillis {
                classUnderTest.calculateCredit(
                    amountInEuros = amountInEuros!!,
                    termInMonths = termInMonths!!,
                    ratingLevel = ratingLevel!!,
                    delayInMilliseconds = delayInMilliseconds,
                )
            }

            time shouldBeGreaterThanOrEqual delayInMilliseconds
        }
    }
}
