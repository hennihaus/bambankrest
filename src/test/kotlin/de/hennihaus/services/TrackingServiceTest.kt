package de.hennihaus.services

import de.hennihaus.objectmothers.BankObjectMother
import de.hennihaus.objectmothers.GroupObjectMother
import de.hennihaus.services.callservices.GroupCallService
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.throwable.shouldHaveMessage
import io.ktor.server.plugins.NotFoundException
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrackingServiceTest {

    private lateinit var groupCall: GroupCallService
    private lateinit var bankName: String

    private lateinit var classUnderTest: TrackingService

    @BeforeEach
    fun init() = clearAllMocks()

    @Nested
    inner class TrackRequest {

        @BeforeEach
        fun init() {
            groupCall = mockk()
            bankName = BankObjectMother.SYNC_BANK_NAME
            classUnderTest = TrackingService(
                groupCall = groupCall,
                bankName = bankName,
            )

            // default behavior
            coEvery { groupCall.getAllGroups() } returns listOf(
                GroupObjectMother.getFirstGroup(),
                GroupObjectMother.getSecondGroup(),
                GroupObjectMother.getThirdGroup(),
            )
            coEvery { groupCall.updateGroup(id = any(), group = any()) } returns mockk()
        }

        @Test
        fun `should pass raised stats correctly when username, password and bank available`() = runBlocking {
            val (id, username, password, _, _, stats) = GroupObjectMother.getThirdGroup()

            classUnderTest.trackRequest(username = username, password = password)

            coVerifySequence {
                groupCall.getAllGroups()
                groupCall.updateGroup(
                    id = id,
                    group = GroupObjectMother.getThirdGroup().copy(
                        stats = stats + Pair(
                            first = bankName,
                            second = stats[bankName]!! + 1,
                        ),
                    ),
                )
            }
        }

        @Test
        fun `should throw an exception when zero groups are available`() = runBlocking {
            val (_, username, password) = GroupObjectMother.getThirdGroup()
            coEvery { groupCall.getAllGroups() } returns emptyList()

            val result: NotFoundException = shouldThrowExactly {
                classUnderTest.trackRequest(
                    username = username,
                    password = password,
                )
            }

            result shouldHaveMessage TrackingService.GROUP_NOT_FOUND_MESSAGE
            coVerify(exactly = 1) { groupCall.getAllGroups() }
            coVerify(exactly = 0) { groupCall.updateGroup(id = any(), group = any()) }
        }

        @Test
        fun `should throw an exception and not update stats when username is unknown`() = runBlocking {
            val username = "unknown"
            val password = GroupObjectMother.getThirdGroup().password

            val result: NotFoundException = shouldThrowExactly {
                classUnderTest.trackRequest(
                    username = username,
                    password = password,
                )
            }

            result shouldHaveMessage TrackingService.GROUP_NOT_FOUND_MESSAGE
            coVerify(exactly = 1) { groupCall.getAllGroups() }
            coVerify(exactly = 0) { groupCall.updateGroup(id = any(), group = any()) }
        }

        @Test
        fun `should throw an exception and not update stats when password is unknown`() = runBlocking {
            val username = GroupObjectMother.getThirdGroup().username
            val password = "unknown"

            val result: NotFoundException = shouldThrowExactly {
                classUnderTest.trackRequest(
                    username = username,
                    password = password,
                )
            }

            result shouldHaveMessage TrackingService.GROUP_NOT_FOUND_MESSAGE
            coVerify(exactly = 1) { groupCall.getAllGroups() }
            coVerify(exactly = 0) { groupCall.updateGroup(id = any(), group = any()) }
        }

        @Test
        fun `should throw an exception and not update stats when bankName is unknown`() = runBlocking {
            val (_, username, password) = GroupObjectMother.getThirdGroup()
            bankName = "unknown"
            classUnderTest = TrackingService(
                groupCall = groupCall,
                bankName = bankName,
            )

            val result: IllegalStateException = shouldThrowExactly {
                classUnderTest.trackRequest(
                    username = username,
                    password = password,
                )
            }

            result shouldHaveMessage TrackingService.BANK_NOT_FOUND_MESSAGE
            coVerify(exactly = 1) { groupCall.getAllGroups() }
            coVerify(exactly = 0) { groupCall.updateGroup(id = any(), group = any()) }
        }

        @Test
        fun `should throw an exception when error in groupCall occurs`() = runBlocking {
            val (_, username, password, _, _, _) = GroupObjectMother.getThirdGroup()
            coEvery { groupCall.updateGroup(id = any(), group = any()) } throws Exception("")

            val result: Exception = shouldThrowExactly {
                classUnderTest.trackRequest(
                    username = username,
                    password = password,
                )
            }

            result shouldHaveMessage ""
            coVerifySequence {
                groupCall.getAllGroups()
                groupCall.updateGroup(id = any(), group = any())
            }
        }
    }
}
