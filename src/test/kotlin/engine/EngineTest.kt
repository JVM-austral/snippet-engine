package engine

import engine.dto.ParseDto
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import factory.Version
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class EngineTest {
    private val engineService: SnippetEngineService = mock()
    private val controller = SnippetEngineController(engineService)

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
}
