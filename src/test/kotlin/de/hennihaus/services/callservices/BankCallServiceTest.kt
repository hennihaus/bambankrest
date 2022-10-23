package de.hennihaus.services.callservices

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.hennihaus.bamdatamodel.Bank
import de.hennihaus.bamdatamodel.CreditConfiguration
import de.hennihaus.bamdatamodel.objectmothers.BankObjectMother.getSyncBank
import de.hennihaus.objectmothers.ConfigurationObjectMother.getConfigBackendConfiguration
import de.hennihaus.services.callservices.BankCallService.Companion.CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
import de.hennihaus.services.callservices.paths.ConfigBackendPaths.BANKS_PATH
import de.hennihaus.testutils.MockEngineBuilder.getMockEngine
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BankCallServiceTest {

    private val config = getConfigBackendConfiguration()
    private val defaultBankId = getSyncBank().uuid

    private lateinit var engine: MockEngine
    private lateinit var classUnderTest: BankCallService

    @Nested
    inner class GetBankById {
        @Test
        fun `should return a bank by id and build call correctly`() = runBlocking {
            engine = getMockEngine(
                content = jacksonObjectMapper().writeValueAsString(getSyncBank()),
                assertions = {
                    it.method shouldBe HttpMethod.Get
                    it.url shouldBe Url(
                        urlString = buildString {
                            append(config.protocol)
                            append("://")
                            append(config.host)
                            append(":")
                            append(config.port)
                            append("/")
                            append(config.apiVersion)
                            append("/")
                            append(BANKS_PATH)
                            append("/")
                            append(defaultBankId)
                        },
                    )
                    it.headers[HttpHeaders.Accept] shouldBe "${ContentType.Application.Json}"
                },
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config,
            )

            val result: Bank = classUnderTest.getBankById()

            result shouldBe getSyncBank()
        }

        @Test
        fun `should throw an exception and request one time when bad request error occurs`() = runBlocking {
            var counter = 0
            engine = getMockEngine(
                status = HttpStatusCode.BadRequest,
                assertions = { counter++ },
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config,
            )

            val result = shouldThrowExactly<ClientRequestException> {
                classUnderTest.getBankById()
            }

            result.response shouldHaveStatus HttpStatusCode.BadRequest
            counter shouldBe 1
        }

        @Test
        fun `should throw an exception and request three times when internal server error occurs`() = runBlocking {
            var counter = 0
            engine = getMockEngine(
                status = HttpStatusCode.InternalServerError,
                assertions = { counter++ },
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config.copy(
                    maxRetries = 2,
                ),
            )

            val result = shouldThrowExactly<ServerResponseException> {
                classUnderTest.getBankById()
            }

            result.response shouldHaveStatus HttpStatusCode.InternalServerError
            counter shouldBe 3
        }
    }

    @Nested
    inner class GetCreditConfigByBankId {
        @Test
        fun `should return a credit configuration by bank id`() = runBlocking {
            engine = getMockEngine(
                content = jacksonObjectMapper().writeValueAsString(getSyncBank()),
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config,
            )

            val result: CreditConfiguration = classUnderTest.getCreditConfigByBankId()

            result shouldBe getSyncBank().creditConfiguration
        }

        @Test
        fun `should throw an exception when bank has no credit configuration`() = runBlocking {
            engine = getMockEngine(
                content = jacksonObjectMapper().writeValueAsString(getSyncBank(creditConfiguration = null)),
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config,
            )

            val result = shouldThrowExactly<NotFoundException> {
                classUnderTest.getCreditConfigByBankId()
            }

            result shouldHaveMessage CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
        }

        @Test
        fun `should throw an exception and request one time when bad request error occurs`() = runBlocking {
            var counter = 0
            engine = getMockEngine(
                status = HttpStatusCode.BadRequest,
                assertions = { counter++ },
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config,
            )

            val result = shouldThrowExactly<ClientRequestException> {
                classUnderTest.getCreditConfigByBankId()
            }

            result.response shouldHaveStatus HttpStatusCode.BadRequest
            counter shouldBe 1
        }

        @Test
        fun `should throw an exception and request three times when internal server error occurs`() = runBlocking {
            var counter = 0
            engine = getMockEngine(
                status = HttpStatusCode.InternalServerError,
                assertions = { counter++ },
            )
            classUnderTest = BankCallService(
                defaultBankId = "$defaultBankId",
                engine = engine,
                config = config.copy(
                    maxRetries = 2,
                ),
            )

            val result = shouldThrowExactly<ServerResponseException> {
                classUnderTest.getCreditConfigByBankId()
            }

            result.response shouldHaveStatus HttpStatusCode.InternalServerError
            counter shouldBe 3
        }
    }
}
