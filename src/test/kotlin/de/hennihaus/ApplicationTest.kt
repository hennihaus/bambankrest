package de.hennihaus

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test

class ApplicationTest {
    @Test
    fun `should return 200 and example message when test endpoint is called`() = testApplication {
        application {
            module()
        }

        val response = client.get(urlString = TEST_ENDPOINT_PATH)

        response shouldHaveStatus HttpStatusCode.OK
        response.bodyAsText() shouldBe TEST_ENDPOINT_MESSAGE
    }

    companion object {
        private const val TEST_ENDPOINT_PATH = "/test"
        private const val TEST_ENDPOINT_MESSAGE = "Hello Ktor"
    }
}
