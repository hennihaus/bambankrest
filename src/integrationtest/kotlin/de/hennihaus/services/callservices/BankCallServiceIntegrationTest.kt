package de.hennihaus.services.callservices

import de.hennihaus.models.CreditConfiguration
import de.hennihaus.objectmothers.ConfigBackendObjectMother.BANK_JMS_QUEUE_WITHOUT_CREDIT_CONFIGURATION
import de.hennihaus.objectmothers.ConfigBackendObjectMother.BANK_JMS_QUEUE_WITH_CREDIT_CONFIGURATION
import de.hennihaus.plugins.initKoin
import de.hennihaus.services.callservices.BankCallService.Companion.CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension

@Disabled(value = "until dev cluster is available")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankCallServiceIntegrationTest : KoinTest {

    private val classUnderTest: BankCallService by inject()

    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestInstance = KoinTestExtension.create {
        initKoin()
    }

    @AfterAll
    fun cleanUp() = stopKoin()

    @Nested
    inner class GetCreditConfigurationByJmsQueue {
        @Test
        fun `should return a credit configuration when jmsQueue is available`() = runBlocking<Unit> {
            val jmsQueue = BANK_JMS_QUEUE_WITH_CREDIT_CONFIGURATION

            val result: CreditConfiguration = classUnderTest.getCreditConfigByJmsQueue(
                jmsQueue = jmsQueue,
            )

            result.shouldNotBeNull()
        }

        @Test
        fun `should throw exception when credit configuration is null`() = runBlocking {
            val jmsQueue = BANK_JMS_QUEUE_WITHOUT_CREDIT_CONFIGURATION

            val result: IllegalStateException = shouldThrowExactly {
                classUnderTest.getCreditConfigByJmsQueue(
                    jmsQueue = jmsQueue,
                )
            }

            result shouldHaveMessage CREDIT_CONFIGURATION_NOT_FOUND_MESSAGE
        }
    }
}
