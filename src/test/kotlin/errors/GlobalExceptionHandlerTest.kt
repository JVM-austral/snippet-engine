package errors

import com.fasterxml.jackson.databind.ObjectMapper
import engine.config.GlobalExceptionHandler
import engine.controller.SnippetEngineController
import engine.inputs.ParseInput
import engine.service.SnippetClient
import engine.service.SnippetEngineService
import factory.Version
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.server.ResponseStatusException

class GlobalExceptionHandlerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var engineService: SnippetEngineService
    private lateinit var snippetClient: SnippetClient
    private lateinit var controller: SnippetEngineController
    private lateinit var exceptionHandler: GlobalExceptionHandler
    private lateinit var objectMapper: ObjectMapper

    private val testAssetPath = "bucket/snippet123.ps"

    @BeforeEach
    fun setup() {
        engineService = mockk()
        snippetClient = mockk()
        controller = SnippetEngineController(engineService, snippetClient)
        exceptionHandler = GlobalExceptionHandler()
        objectMapper = ObjectMapper()

        mockMvc =
            MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build()
    }

    @Test
    fun `should handle ResponseStatusException with NOT_FOUND status`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found")

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("404 NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Asset not found"))
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should handle ResponseStatusException with BAD_REQUEST status`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input")

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("400 BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("Invalid input"))
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should handle ResponseStatusException without reason message`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("500 INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("No message available"))
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should handle generic Exception as Internal Server Error`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            RuntimeException("Unexpected database error")

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("Unexpected database error"))
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should handle generic Exception without message`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            NullPointerException()

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should handle IllegalArgumentException`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            IllegalArgumentException("Invalid argument provided")

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("Invalid argument provided"))
            .andExpect(jsonPath("$.path").exists())
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `should include correct path in error response`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.path").value("/engine/parse"))
    }

    @Test
    fun `should return timestamp in ISO format`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } throws
            ResponseStatusException(HttpStatus.NOT_FOUND, "Not found")

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.timestamp").isNotEmpty)
            .andExpect(jsonPath("$.timestamp").isString)
    }
}
