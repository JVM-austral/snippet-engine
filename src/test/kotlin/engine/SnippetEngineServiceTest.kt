package engine

import com.fasterxml.jackson.databind.ObjectMapper
import engine.dto.LintDto
import engine.dto.ParseDto
import engine.dto.TestSnippetDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import engine.inputs.TestSnippetInput
import engine.service.SnippetEngineService
import factory.Version
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SnippetEngineServiceTest {
    private val objectMapper = ObjectMapper()
    private val service = SnippetEngineService(objectMapper)

    @Test
    fun `parseSnippet returns errors for invalid code`() {
        val input =
            ParseInput(
                assetPath = "",
                language = "austral",
                version = Version.V1,
            )
        val code = "let incomplete: string = "
        val result: ParseDto = service.parseSnippet(input, code)
        assertTrue(result.parseErrors.isNotEmpty())
    }

    @Test
    fun `executeSnippet returns output for valid code`() {
        val input =
            ExecutionInput(
                assetPath = "",
                language = "austral",
                version = Version.V2,
            )
        val code = "println(\"Hello World\");"
        val output =
            service.executeSnippet(
                input,
                code,
            )
        assertTrue(output.output.any { it.contains("Hello World") })
    }

    @Test
    fun `executeSnippet with varInputs returns correct output`() {
        val input =
            ExecutionInput(
                assetPath = "",
                language = "austral",
                version = Version.V2,
                varInputs = listOf("TestValue"),
            )
        val code = "let x: string = readInput(\"First Input\"); println(x);"
        val output = service.executeSnippet(input, code)
        assertTrue(output.output.any { it.contains("TestValue") })
    }

    @Test
    fun `formatWithOptions returns formatted code`() {
        val code = "let a:string=\"hello\";println(a);"
        val config = objectMapper.readTree("{}")
        val input =
            AnalyzeCodeInput(
                language = "austral",
                version = Version.V1,
                config = config,
                assetPath = "",
            )
        val formatted = service.formatWithOptions(input, code)
        assertTrue(formatted.isNotEmpty())
        assertNotEquals(formatted, code)
    }

    @Test
    fun `lintWithOptions returns no errors for clean code`() {
        val code = "let message: string = \"Hello\"; println(message);"
        val config = objectMapper.readTree("{}")
        val input =
            AnalyzeCodeInput(
                language = "austral",
                version = Version.V1,
                config = config,
                assetPath = "",
            )
        val lintResult: LintDto = service.lintWithOptions(input, code)
        assertNotNull(lintResult)
        assertTrue(lintResult.lintErrors.isEmpty())
    }

    @Test
    fun `testSnippet returns passed true for correct output`() {
        val input =
            TestSnippetInput(
                assetPath = "",
                language = "austral",
                version = Version.V2,
                varInputs = listOf(),
                expectedOutputs = listOf("expected output"),
            )
        val code = "println(\"expected output\");"
        val result: TestSnippetDto = service.testSnippet(input, code)
        assertTrue(result.passed)
        assertEquals(null, result.failedAt)
    }

    @Test
    fun `testSnippet returns passed false for incorrect output`() {
        val input =
            TestSnippetInput(
                assetPath = "",
                language = "austral",
                version = Version.V2,
                varInputs = listOf(),
                expectedOutputs = listOf("expected output"),
            )
        val code = "println(\"wrong output\");"
        val result: TestSnippetDto = service.testSnippet(input, code)
        assertFalse(result.passed)
        assertNotNull(result.failedAt)
    }
}
