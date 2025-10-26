package engine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import engine.inputs.AnalyzeCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import factory.Version
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [SnippetEngineController::class])
class SnippetEngineControllerMvcTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var engineService: SnippetEngineService

    private fun anyValidVersion(): Version = Version::class.java.enumConstants!!.first()

    @Test
    fun `POST engine-parse returns 202 and parse errors`() {
        val version = anyValidVersion()
        val input = ParseInput(code = "print(1)", language = "austral", version = version)
        val expectedErrors = listOf("syntax error: unexpected token")
        whenever(engineService.parseSnippet(input.code, input.language, version)).thenReturn(expectedErrors)

        mockMvc.perform(
            post("/engine/parse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.parseErrors[0]").value("syntax error: unexpected token"))

        verify(engineService).parseSnippet(input.code, input.language, version)
    }

    @Test
    fun `POST engine-execute returns 202 and outputs`() {
        val version = anyValidVersion()
        val input = ExecutionInput(code = "print(42)", language = "austral", version = version, varInput = null)
        val expectedOutputs = listOf("42")
        whenever(engineService.executeSnippet(input.code, input.language, version, null)).thenReturn(expectedOutputs)

        mockMvc.perform(
            post("/engine/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
        )
            .andExpect(status().isAccepted)
            .andExpect(content().json(objectMapper.writeValueAsString(expectedOutputs)))

        verify(engineService).executeSnippet(input.code, input.language, version, null)
    }

    @Test
    fun `POST engine-format returns 202 and formatted code`() {
        val version = anyValidVersion()
        val config: ObjectNode = objectMapper.createObjectNode() // empty config
        val input = AnalyzeCodeInput(language = "austral", version = version, config = config, code = "print(1)")
        val formatted = "print(1)" // identity for test
        whenever(engineService.formatWithOptions(input.language, version, input.config, input.code)).thenReturn(formatted)

        mockMvc.perform(
            post("/engine/format")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
        )
            .andExpect(status().isAccepted)
            .andExpect(content().string(formatted))

        verify(engineService).formatWithOptions(input.language, version, input.config, input.code)
    }

    @Test
    fun `POST engine-analyze returns 202 and lint errors dto`() {
        val version = anyValidVersion()
        val config: ObjectNode = objectMapper.createObjectNode()
        val input = AnalyzeCodeInput(language = "austral", version = version, config = config, code = "print(1)")
        // Return empty list to avoid constructing external error types
        whenever(engineService.lintWithOptions(input.language, version, input.config, input.code)).thenReturn(emptyList())

        mockMvc.perform(
            post("/engine/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.lintErrors").isArray)
            .andExpect(jsonPath("$.lintErrors.length()").value(0))

        verify(engineService).lintWithOptions(input.language, version, input.config, input.code)
    }

    @Test
    fun `POST engine-parse returns 400 when language is blank`() {
        val version = anyValidVersion()
        val input = ParseInput(code = "print(1)", language = "", version = version)

        mockMvc.perform(
            post("/engine/parse")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
        )
            .andExpect(status().isBadRequest)
    }
}
