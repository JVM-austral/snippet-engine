package engine

import com.fasterxml.jackson.databind.ObjectMapper
import engine.controller.SnippetEngineController
import engine.dto.LintDto
import engine.dto.ParseDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import engine.service.SnippetEngineService
import factory.Version
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class EngineMockControllerTest {
    private val engineService: SnippetEngineService = mock()
    private val controller = SnippetEngineController(engineService)
    private val objectMapper = ObjectMapper()

    private fun anyValidVersion(): Version = Version::class.java.enumConstants!!.first()

    @Test
    fun parseSnippet_returnsAcceptedAndErrorsFromService() {
        val code = "print(1)"
        val language = "austral"
        val version = anyValidVersion()
        val input = ParseInput(code = code, language = language, version = version)
        val expected = listOf("syntax error: unexpected token")
        whenever(engineService.parseSnippet(input)).thenReturn(ParseDto(expected))

        val response = controller.parseSnippet(input)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(expected, response.body!!.parseErrors)
        verify(engineService).parseSnippet(input)
    }

    @Test
    fun executeSnippet_returnsAcceptedAndOutputsFromService() {
        val code = "print(42)"
        val language = "austral"
        val version = anyValidVersion()
        val input = ExecutionInput(code = code, language = language, version = version)
        val expectedOutputs = listOf("42")
        whenever(engineService.executeSnippet(input)).thenReturn(expectedOutputs)

        val response = controller.executeSnippet(input)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(expectedOutputs, response.body!!)
        verify(engineService).executeSnippet(input)
    }

    @Test
    fun executeSnippet_withVarInput_returnsAcceptedAndOutputsFromService() {
        val code = "readInput(x); print(x)"
        val language = "austral"
        val version = anyValidVersion()
        val input = ExecutionInput(code = code, language = language, version = version, varInputs = listOf("7"))
        val expectedOutputs = listOf("7")
        whenever(engineService.executeSnippet(input)).thenReturn(expectedOutputs)

        val response = controller.executeSnippet(input)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(expectedOutputs, response.body!!)
        verify(engineService).executeSnippet(input)
    }

    @Test
    fun formatSnippet_returnsAcceptedAndFormattedFromService() {
        val language = "austral"
        val version = anyValidVersion()
        val code = "x=1"
        val config = ObjectMapper().readTree("{}")
        val input = AnalyzeCodeInput(language = language, version = version, config = config, code = code)
        val expectedFormatted = "x = 1"
        whenever(engineService.formatWithOptions(input)).thenReturn(expectedFormatted)

        val response = controller.formatSnippet(input)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(expectedFormatted, response.body!!)
        verify(engineService).formatWithOptions(input)
    }

    @Test
    fun analyzeSnippet_returnsAcceptedAndLintErrorsFromService() {
        val language = "austral"
        val version = anyValidVersion()
        val code = "print(1)"
        val config = ObjectMapper().readTree("{}")
        val input = AnalyzeCodeInput(language = language, version = version, config = config, code = code)
        val expected = LintDto(emptyList())
        whenever(engineService.lintWithOptions(input)).thenReturn(expected)

        val response = controller.analyzeSnippet(input)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(expected, response.body!!)
        verify(engineService).lintWithOptions(input)
    }

    @Test
    fun ping_returnsOkPong() {
        val response = controller.ping()
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("pong", response.body)
    }
}
