package de.hennihaus.routes

import de.hennihaus.models.generated.Credit
import de.hennihaus.models.generated.Errors
import de.hennihaus.objectmothers.CreditObjectMother.getLowestCredit
import de.hennihaus.objectmothers.CreditObjectMother.getMinValidCreditResource
import de.hennihaus.objectmothers.ErrorsObjectMother.getInternalServerErrors
import de.hennihaus.objectmothers.ErrorsObjectMother.getInvalidRequestErrors
import de.hennihaus.objectmothers.ErrorsObjectMother.getMissingParameterErrors
import de.hennihaus.objectmothers.ErrorsObjectMother.getNotFoundErrors
import de.hennihaus.objectmothers.ReasonObjectMother.DEFAULT_INVALID_REQUEST_MESSAGE
import de.hennihaus.plugins.RequestValidationException
import de.hennihaus.services.CreditService
import de.hennihaus.services.TrackingService
import de.hennihaus.services.TrackingService.Companion.TEAM_NOT_FOUND_MESSAGE
import de.hennihaus.services.validationservices.CreditValidationService
import de.hennihaus.testutils.KtorTestBuilder.testApplicationWith
import de.hennihaus.testutils.testClient
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreditRoutesTest {

    private val creditValidation = mockk<CreditValidationService>()
    private val credit = mockk<CreditService>()
    private val tracking = mockk<TrackingService>()

    private val mockModule = module {
        single { creditValidation }
        single { credit }
        single { tracking }
    }

    @BeforeEach
    fun init() = clearAllMocks()

    @AfterEach
    fun tearDown() = stopKoin()

    @Nested
    inner class GetCredit {

        @BeforeEach
        fun init() {
            // default behaviour
            coEvery { creditValidation.validateUrl(resource = any()) } returns Unit
            coEvery { tracking.trackRequest(username = any(), password = any()) } returns Unit
            coEvery {
                credit.calculateCredit(
                    amountInEuros = any(),
                    termInMonths = any(),
                    ratingLevel = any(),
                    delayInMilliseconds = any(),
                )
            } returns getLowestCredit()
        }

        @Test
        fun `should return 200 and a credit`() = testApplicationWith(mockModule) {
            val (
                amountInEuros,
                termInMonths,
                ratingLevel,
                delayInMilliseconds,
                username,
                password,
            ) = getMinValidCreditResource()

            val response = testClient.get(
                urlString = """
                    /v1
                    /credit
                    ?amountInEuros=$amountInEuros
                    &termInMonths=$termInMonths
                    &ratingLevel=$ratingLevel
                    &delayInMilliseconds=$delayInMilliseconds
                    &username=$username
                    &password=$password
                """.trimIndent().replace("\n", "")
            )

            response shouldHaveStatus HttpStatusCode.OK
            response.body<Credit>() shouldBe getLowestCredit()
            coVerifySequence {
                creditValidation.validateUrl(
                    resource = getMinValidCreditResource(),
                )
                credit.calculateCredit(
                    amountInEuros = amountInEuros!!.toInt(),
                    termInMonths = termInMonths!!.toInt(),
                    ratingLevel = ratingLevel!!,
                    delayInMilliseconds = delayInMilliseconds?.toLongOrNull(),
                )
                tracking.trackRequest(
                    username = username!!,
                    password = password!!,
                )
            }
        }

        @Test
        fun `should return 400 and error when password is missing`() = testApplicationWith(mockModule) {
            val (amountInEuros, termInMonths, ratingLevel, delayInMilliseconds, username) = getMinValidCreditResource()
            coEvery { creditValidation.validateUrl(resource = any()) } throws RequestValidationException(
                reasons = listOf(
                    DEFAULT_INVALID_REQUEST_MESSAGE,
                ),
            )

            val response = testClient.get(
                urlString = """
                    /v1
                    /credit
                    ?amountInEuros=$amountInEuros
                    &termInMonths=$termInMonths
                    &ratingLevel=$ratingLevel
                    &delayInMilliseconds=$delayInMilliseconds
                    &username=$username
                """.trimIndent().replace("\n", "")
            )

            response shouldHaveStatus HttpStatusCode.BadRequest
            response.body<Errors>() shouldBe getInvalidRequestErrors()
            coVerifySequence {
                creditValidation.validateUrl(
                    resource = getMinValidCreditResource(
                        password = null,
                    ),
                )
            }
            coVerify(exactly = 0) {
                credit.calculateCredit(
                    amountInEuros = any(),
                    termInMonths = any(),
                    ratingLevel = any(),
                    delayInMilliseconds = any(),
                )
            }
            coVerify(exactly = 0) {
                tracking.trackRequest(
                    username = any(),
                    password = any(),
                )
            }
        }

        @Test
        fun `should return 400 and error when password and validation are missing`() = testApplicationWith(mockModule) {
            val (amountInEuros, termInMonths, ratingLevel, delayInMilliseconds, username) = getMinValidCreditResource()

            val response = testClient.get(
                urlString = """
                    /v1
                    /credit
                    ?amountInEuros=$amountInEuros
                    &termInMonths=$termInMonths
                    &ratingLevel=$ratingLevel
                    &delayInMilliseconds=$delayInMilliseconds
                    &username=$username
                """.trimIndent().replace("\n", "")
            )

            response shouldHaveStatus HttpStatusCode.BadRequest
            response.body<Errors>() shouldBe getMissingParameterErrors()
        }

        @Test
        fun `should return 404 and error when NotFoundException is thrown`() = testApplicationWith(mockModule) {
            val (
                amountInEuros,
                termInMonths,
                ratingLevel,
                delayInMilliseconds,
                username,
                password,
            ) = getMinValidCreditResource()
            coEvery { tracking.trackRequest(username = any(), password = any()) } throws NotFoundException(
                message = TEAM_NOT_FOUND_MESSAGE,
            )

            val response = testClient.get(
                urlString = """
                    /v1
                    /credit
                    ?amountInEuros=$amountInEuros
                    &termInMonths=$termInMonths
                    &ratingLevel=$ratingLevel
                    &delayInMilliseconds=$delayInMilliseconds
                    &username=$username
                    &password=$password
                """.trimIndent().replace("\n", "")
            )

            response shouldHaveStatus HttpStatusCode.NotFound
            response.body<Errors>() shouldBe getNotFoundErrors()
        }

        @Test
        fun `should return 500 and error when IllegalStateException is thrown`() = testApplicationWith(mockModule) {
            val (
                amountInEuros,
                termInMonths,
                ratingLevel,
                delayInMilliseconds,
                username,
                password,
            ) = getMinValidCreditResource()
            coEvery { tracking.trackRequest(username = any(), password = any()) } throws IllegalStateException()

            val response = testClient.get(
                urlString = """
                    /v1
                    /credit
                    ?amountInEuros=$amountInEuros
                    &termInMonths=$termInMonths
                    &ratingLevel=$ratingLevel
                    &delayInMilliseconds=$delayInMilliseconds
                    &username=$username
                    &password=$password
                """.trimIndent().replace("\n", "")
            )

            response shouldHaveStatus HttpStatusCode.InternalServerError
            response.body<Errors>() shouldBe getInternalServerErrors()
        }
    }
}
