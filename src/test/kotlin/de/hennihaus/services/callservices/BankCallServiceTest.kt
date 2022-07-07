package de.hennihaus.services.callservices

import de.hennihaus.models.CreditConfiguration
import de.hennihaus.objectmothers.BankObjectMother
import de.hennihaus.objectmothers.ConfigurationObjectMother.getConfigBackendConfiguration
import de.hennihaus.objectmothers.CreditConfigurationObjectMother.getCreditConfiguration
import de.hennihaus.services.callservices.BankCallService.Companion.CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
import de.hennihaus.services.callservices.resources.BankPaths.BANKS_PATH
import de.hennihaus.testutils.MockEngineBuilder
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BankCallServiceTest {

    private lateinit var classUnderTest: BankCallService

    @Nested
    inner class GetCreditConfigurationByJmsQueue {
        @Test
        fun `should return creditConfiguration and build call correctly`() = runBlocking {
            val config = getConfigBackendConfiguration()
            val engine = MockEngineBuilder.getMockEngine(
                content = Json.encodeToString(
                    value = BankObjectMother.getSyncBank(),
                ),
                assertions = {
                    it.method shouldBe HttpMethod.Get
                    it.url shouldBe Url(
                        urlString = """
                            ${config.protocol}://${config.host}:${config.port}$BANKS_PATH/${config.defaultJmsQueue}
                        """.trimIndent()
                    )
                    it.headers[HttpHeaders.Accept] shouldBe "${ContentType.Application.Json}"
                }
            )
            classUnderTest = BankCallService(
                engine = engine,
                config = config,
            )

            val result: CreditConfiguration = classUnderTest.getCreditConfigByJmsQueue(
                jmsQueue = config.defaultJmsQueue,
            )

            result shouldBe getCreditConfiguration()
        }

        @Test
        fun `should throw an exception when creditConfiguration = null`() = runBlocking {
            val config = getConfigBackendConfiguration()
            val engine = MockEngineBuilder.getMockEngine(
                content = Json.encodeToString(
                    value = BankObjectMother.getSchufaBank(
                        creditConfiguration = null,
                    ),
                ),
            )
            classUnderTest = BankCallService(
                engine = engine,
                config = config,
            )

            val result: IllegalStateException = shouldThrowExactly {
                classUnderTest.getCreditConfigByJmsQueue()
            }

            result shouldHaveMessage CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
        }

        @Test
        fun `should throw an exception and request three times when error occurs`() = runBlocking {
            var counter = 0
            val config = getConfigBackendConfiguration()
            val engine = MockEngineBuilder.getMockEngine(
                status = HttpStatusCode.InternalServerError,
                assertions = { counter++ },
            )
            classUnderTest = BankCallService(
                engine = engine,
                config = config,
            )

            shouldThrowExactly<ServerResponseException> {
                classUnderTest.getCreditConfigByJmsQueue()
            }

            counter shouldBe 3
        }
    }
}
