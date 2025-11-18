package engine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import engine.controller.SnippetEngineController
import engine.dto.ExecutionDto
import engine.dto.LintDto
import engine.dto.ParseDto
import engine.dto.TestSnippetDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.AnalyzeUniqueCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import engine.inputs.TestSnippetInput
import engine.service.SnippetClient
import engine.service.SnippetEngineService
import error.LinterError
import factory.Version
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.assertEquals

class SnippetEngineControllerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var engineService: SnippetEngineService
    private lateinit var snippetClient: SnippetClient
    private lateinit var controller: SnippetEngineController
    private lateinit var objectMapper: ObjectMapper

    private val testAssetPath = "bucket/snippet123.ps"
    private val testCode = "let x: number = 5;"

    @BeforeEach
    fun setup() {
        engineService = mockk()
        snippetClient = mockk()
        controller = SnippetEngineController(engineService, snippetClient)
        objectMapper = ObjectMapper()

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `parseSnippet should return parse result with no errors`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        val parseDto = ParseDto(parseErrors = emptyList())

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.parseSnippet(parseInput, testCode) } returns parseDto

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.parseErrors").isArray)
            .andExpect(jsonPath("$.parseErrors").isEmpty)

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.parseSnippet(parseInput, testCode) }
    }

    @Test
    fun `parseSnippet should return parse result with errors`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        val parseDto = ParseDto(parseErrors = listOf("Syntax error at line 1", "Missing semicolon"))

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.parseSnippet(parseInput, testCode) } returns parseDto

        mockMvc
            .perform(
                post("/engine/parse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(parseInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.parseErrors").isArray)
            .andExpect(jsonPath("$.parseErrors[0]").value("Syntax error at line 1"))
            .andExpect(jsonPath("$.parseErrors[1]").value("Missing semicolon"))

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.parseSnippet(parseInput, testCode) }
    }

    @Test
    fun `testSnippet should return test result when passed`() {
        val testInput =
            TestSnippetInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
                varInputs = listOf("5", "10"),
                expectedOutputs = listOf("15"),
            )

        val testDto = TestSnippetDto(passed = true, failedAt = null)

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.testSnippet(testInput, testCode) } returns testDto

        mockMvc
            .perform(
                post("/engine/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.passed").value(true))
            .andExpect(jsonPath("$.failedAt").isEmpty)

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.testSnippet(testInput, testCode) }
    }

    @Test
    fun `testSnippet should return test result when failed`() {
        val testInput =
            TestSnippetInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
                varInputs = listOf("5", "10"),
                expectedOutputs = listOf("15", "20"),
            )

        val testDto = TestSnippetDto(passed = false, failedAt = 1)

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.testSnippet(testInput, testCode) } returns testDto

        mockMvc
            .perform(
                post("/engine/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.passed").value(false))
            .andExpect(jsonPath("$.failedAt").value(1))

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.testSnippet(testInput, testCode) }
    }

    @Test
    fun `executeSnippet should return execution result with output`() {
        val executionInput =
            ExecutionInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
                varInputs = listOf("5"),
            )

        val executionDto =
            ExecutionDto(
                output = listOf("Hello World", "Result: 5"),
                errors = emptyList(),
            )

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.executeSnippet(executionInput, testCode) } returns executionDto

        mockMvc
            .perform(
                post("/engine/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(executionInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.output").isArray)
            .andExpect(jsonPath("$.output[0]").value("Hello World"))
            .andExpect(jsonPath("$.output[1]").value("Result: 5"))
            .andExpect(jsonPath("$.errors").isEmpty)

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.executeSnippet(executionInput, testCode) }
    }

    @Test
    fun `executeSnippet should return execution result with errors`() {
        val executionInput =
            ExecutionInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        val executionDto =
            ExecutionDto(
                output = emptyList(),
                errors = listOf("Runtime error: Division by zero"),
            )

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.executeSnippet(executionInput, testCode) } returns executionDto

        mockMvc
            .perform(
                post("/engine/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(executionInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.output").isEmpty)
            .andExpect(jsonPath("$.errors").isArray)
            .andExpect(jsonPath("$.errors[0]").value("Runtime error: Division by zero"))

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.executeSnippet(executionInput, testCode) }
    }

    @Test
    fun `formatSnippet should format code and return result`() {
        val formatInput =
            AnalyzeUniqueCodeInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                config = JsonNodeFactory.instance.objectNode(),
                code = "let x:number=5;",
            )

        val formattedCode = "let x: number = 5;"

        every { engineService.formatWithOptions(any(), formatInput.code) } returns formattedCode

        mockMvc
            .perform(
                post("/engine/format")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(formatInput)),
            ).andExpect(status().isAccepted)
            .andExpect(content().string(formattedCode))

        verify(exactly = 1) { engineService.formatWithOptions(any(), formatInput.code) }
    }

    @Test
    fun `analyzeSnippet should return lint result with no errors`() {
        val analyzeInput =
            AnalyzeCodeInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                config = JsonNodeFactory.instance.objectNode(),
                assetPath = testAssetPath,
            )

        val lintDto = LintDto(lintErrors = emptyList())

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.lintWithOptions(analyzeInput, testCode) } returns lintDto

        mockMvc
            .perform(
                post("/engine/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(analyzeInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.lintErrors").isArray)
            .andExpect(jsonPath("$.lintErrors").isEmpty)

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.lintWithOptions(analyzeInput, testCode) }
    }

    @Test
    fun `analyzeSnippet should return lint result with errors`() {
        val analyzeInput =
            AnalyzeCodeInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                config = JsonNodeFactory.instance.objectNode(),
                assetPath = testAssetPath,
            )

        val lintErrors =
            listOf(
                LinterError("Variable name should be camelCase", 1, 5),
                LinterError("Missing space around operator", 1, 10),
            )

        val lintDto = LintDto(lintErrors = lintErrors)

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.lintWithOptions(analyzeInput, testCode) } returns lintDto

        mockMvc
            .perform(
                post("/engine/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(analyzeInput)),
            ).andExpect(status().isAccepted)
            .andExpect(jsonPath("$.lintErrors").isArray)
            .andExpect(jsonPath("$.lintErrors[0].message").value("Variable name should be camelCase"))
            .andExpect(jsonPath("$.lintErrors[1].message").value("Missing space around operator"))

        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
        verify(exactly = 1) { engineService.lintWithOptions(analyzeInput, testCode) }
    }

    @Test
    fun `ping should return pong`() {
        mockMvc
            .perform(get("/engine/ping"))
            .andExpect(status().isOk)
            .andExpect(content().string("pong"))
    }

    @Test
    fun `parseSnippet should propagate exception`() {
        val parseInput = ParseInput(language = "PRINTSCRIPT", version = Version.V1, assetPath = testAssetPath)

        every { snippetClient.getAsset(testAssetPath) } throws RuntimeException("Asset not found")

        val exception =
            assertThrows<jakarta.servlet.ServletException> {
                mockMvc
                    .perform(
                        post("/engine/parse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(parseInput)),
                    ).andReturn()
            }

        assertTrue(exception.cause is RuntimeException)
        assertEquals("Asset not found", exception.cause?.message)
        verify(exactly = 1) { snippetClient.getAsset(testAssetPath) }
    }

    @Test
    fun `testSnippet should propagate exception from engineService`() {
        val testInput =
            TestSnippetInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
                varInputs = emptyList(),
                expectedOutputs = listOf("ok"),
            )

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.testSnippet(testInput, testCode) } throws IllegalStateException("Execution failed")

        val exception =
            assertThrows<jakarta.servlet.ServletException> {
                mockMvc
                    .perform(
                        post("/engine/test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testInput)),
                    ).andReturn()
            }

        assertTrue(exception.cause is IllegalStateException)
        assertEquals("Execution failed", exception.cause?.message)
        verify(exactly = 1) { engineService.testSnippet(testInput, testCode) }
    }

    @Test
    fun `executeSnippet should propagate exception from engineService`() {
        val executionInput = ExecutionInput(language = "PRINTSCRIPT", version = Version.V1, assetPath = testAssetPath)

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.executeSnippet(executionInput, testCode) } throws RuntimeException("Runtime error")

        val exception =
            assertThrows<jakarta.servlet.ServletException> {
                mockMvc
                    .perform(
                        post("/engine/execute")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(executionInput)),
                    ).andReturn()
            }

        assertTrue(exception.cause is RuntimeException)
        assertEquals("Runtime error", exception.cause?.message)
    }

    @Test
    fun `formatSnippet should propagate exception from engineService`() {
        val formatInput =
            AnalyzeUniqueCodeInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                config = JsonNodeFactory.instance.objectNode(),
                code = "let x=5;",
            )

        every { engineService.formatWithOptions(any(), formatInput.code) } throws IllegalArgumentException("Invalid code")

        val exception =
            assertThrows<jakarta.servlet.ServletException> {
                mockMvc
                    .perform(
                        post("/engine/format")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(formatInput)),
                    ).andReturn()
            }

        assertTrue(exception.cause is IllegalArgumentException)
        assertEquals("Invalid code", exception.cause?.message)
    }

    @Test
    fun `analyzeSnippet should propagate exception from engineService`() {
        val analyzeInput =
            AnalyzeCodeInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                config = JsonNodeFactory.instance.objectNode(),
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.lintWithOptions(analyzeInput, testCode) } throws RuntimeException("Lint failed")

        val exception =
            assertThrows<jakarta.servlet.ServletException> {
                mockMvc
                    .perform(
                        post("/engine/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(analyzeInput)),
                    ).andReturn()
            }

        assertTrue(exception.cause is RuntimeException)
        assertEquals("Lint failed", exception.cause?.message)
    }

    @Test
    fun `parseSnippet should propagate exception from engineService`() {
        val parseInput =
            ParseInput(
                language = "PRINTSCRIPT",
                version = Version.V1,
                assetPath = testAssetPath,
            )

        every { snippetClient.getAsset(testAssetPath) } returns testCode
        every { engineService.parseSnippet(parseInput, testCode) } throws IllegalStateException("Parse failed")

        val exception =
            assertThrows<jakarta.servlet.ServletException> {
                mockMvc
                    .perform(
                        post("/engine/parse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(parseInput)),
                    ).andReturn()
            }

        assertTrue(exception.cause is IllegalStateException)
        assertEquals("Parse failed", exception.cause?.message)
    }
}
