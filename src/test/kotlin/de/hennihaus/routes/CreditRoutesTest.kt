package de.hennihaus.routes

import de.hennihaus.models.generated.Credit
import de.hennihaus.models.generated.Error
import de.hennihaus.objectmothers.CreditObjectMother.getLowestCredit
import de.hennihaus.objectmothers.CreditObjectMother.getMinValidCreditResource
import de.hennihaus.objectmothers.ErrorObjectMother
import de.hennihaus.objectmothers.ErrorObjectMother.DEFAULT_INVALID_REQUEST_ERROR_MESSAGE
import de.hennihaus.objectmothers.ErrorObjectMother.DEFAULT_NOT_FOUND_ERROR_MESSAGE
import de.hennihaus.plugins.ValidationException
import de.hennihaus.services.CreditService
import de.hennihaus.services.TrackingService
import de.hennihaus.services.resourceservices.CreditResourceService
import de.hennihaus.testutils.KtorTestBuilder.testApplicationWith
import de.hennihaus.testutils.testClient
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.should
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
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreditRoutesTest {

    private val creditResource = mockk<CreditResourceService>()
    private val credit = mockk<CreditService>()
    private val tracking = mockk<TrackingService>()

    private val mockModule = module {
        single { creditResource }
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
            coEvery { creditResource.validate(resource = any()) } returns getMinValidCreditResource()
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
                creditResource.validate(
                    resource = getMinValidCreditResource(),
                )
                credit.calculateCredit(
                    amountInEuros = amountInEuros!!,
                    termInMonths = termInMonths!!,
                    ratingLevel = ratingLevel!!,
                    delayInMilliseconds = delayInMilliseconds!!,
                )
                tracking.trackRequest(
                    username = username!!,
                    password = password!!,
                )
            }
        }

        @Test
        fun `should return 400 and error when amountInEuros missing and invalid`() = testApplicationWith(mockModule) {
            val (_, termInMonths, ratingLevel, delayInMilliseconds, username, password) = getMinValidCreditResource()
            coEvery { creditResource.validate(resource = any()) } throws ValidationException(
                message = DEFAULT_INVALID_REQUEST_ERROR_MESSAGE,
            )

            val response = testClient.get(
                urlString = """
                    /credit
                    ?termInMonths=$termInMonths
                    &ratingLevel=$ratingLevel
                    &delayInMilliseconds=$delayInMilliseconds
                    &username=$username
                    &password=$password
                """.trimIndent().replace("\n", "")
            )

            response shouldHaveStatus HttpStatusCode.BadRequest
            response.body<Error>().shouldBeEqualToIgnoringFields(
                other = ErrorObjectMother.getInvalidRequestError(),
                property = Error::dateTime,
            )
            coVerifySequence {
                creditResource.validate(
                    resource = getMinValidCreditResource(
                        amountInEuros = null,
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
                message = DEFAULT_NOT_FOUND_ERROR_MESSAGE,
            )

            val response = testClient.get(
                urlString = """
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
            response.body<Error>().shouldBeEqualToIgnoringFields(
                other = ErrorObjectMother.getNotFoundError(),
                property = Error::dateTime,
            )
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
            response.body<Error>() should {
                it.shouldBeEqualToIgnoringFields(
                    other = ErrorObjectMother.getInternalServerError(),
                    property = Error::dateTime,
                )
                it.dateTime.shouldBeEqualToIgnoringFields(
                    other = ErrorObjectMother.getInternalServerError().dateTime,
                    property = LocalDateTime::second,
                )
            }
        }
    }
}
