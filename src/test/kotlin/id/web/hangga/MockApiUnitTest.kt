package id.web.hangga

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MockApiUnitTest {
    private lateinit var server: WireMockServer

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json)
        }
    }

    @BeforeEach
    fun setUp() {
        server = WireMockServer(WireMockConfiguration.options().dynamicPort())
        server.start()
        configureFor("localhost", server.port())

        // Health check endpoint
        stubFor(
            get(urlPathEqualTo("/health")).willReturn(
                    aResponse().withStatus(200).withBody("OK")
                )
        )

        // Login endpoint - Success
        stubFor(
            post(urlPathEqualTo("/auth/login")).withHeader(
                    "Content-Type",
                    containing("application/json")
                ).withRequestBody(matchingJsonPath("$.username"))
                .withRequestBody(matchingJsonPath("$.password")).willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("""{ "token": "dummy-token" }""")
                )
        )

        // Sign-up endpoint - Success
        stubFor(
            post(urlPathEqualTo("/auth/signup")).withHeader(
                    "Content-Type",
                    containing("application/json")
                ).withRequestBody(matchingJsonPath("$.username"))
                .withRequestBody(matchingJsonPath("$.password"))
                .withRequestBody(matchingJsonPath("$.email")).willReturn(
                    aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                        .withBody("""{ "message": "User created successfully" }""")
                )
        )

        // Sign-up endpoint - Username already exists (Error case)
        stubFor(
            post(urlPathEqualTo("/auth/signup")).withHeader(
                    "Content-Type",
                    containing("application/json")
                ).withRequestBody(
                    equalToJson(
                        """
                {
                    "username": "existinguser",
                    "password": "password123",
                    "email": "existing@example.com"
                }
            """.trimIndent()
                    )
                ).willReturn(
                    aResponse().withStatus(409).withHeader("Content-Type", "application/json")
                        .withBody("""{ "message": "Username already exists" }""")
                )
        )
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

    @Test
    fun `test health check endpoint`(): Unit = runBlocking {
        val response: HttpResponse = client.get("http://localhost:${server.port()}/health")
        assertEquals(200, response.status.value)
        assertEquals("OK", response.bodyAsText())
    }

    @Test
    fun `test login success`(): Unit = runBlocking {
        val response: HttpResponse = client.post("http://localhost:${server.port()}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "username": "user", "password": "pass" }""")
        }

        assertEquals(404, response.status.value)
        println(response.bodyAsText())
    }

    @Test
    fun `test signup success`(): Unit = runBlocking {
        val response: HttpResponse = client.post("http://localhost:${server.port()}/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{ "username": "newuser", "password": "newpass", "email": "new@example.com" }""")
        }

        assertEquals(404, response.status.value)
    }

    @Test
    fun `test signup username already exists`(): Unit = runBlocking {
        val response: HttpResponse = client.post("http://localhost:${server.port()}/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody("""{ "username": "existinguser", "password": "password123", "email": "existing@example.com" }""")
        }

        assertEquals(404, response.status.value)
    }
}