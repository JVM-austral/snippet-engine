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
                code = "let incomplete: string = ",
                language = "austral",
                version = Version.V1,
            )
        val result: ParseDto = service.parseSnippet(input)
        assertTrue(result.parseErrors.isNotEmpty())
    }

    @Test
    fun `executeSnippet returns output for valid code`() {
        val input =
            ExecutionInput(
                code = "println(\"Hello World\");",
                language = "austral",
                version = Version.V2,
            )
        val output = service.executeSnippet(input)
        assertTrue(output.any { it.contains("Hello World") })
    }

    @Test
    fun `executeSnippet with varInputs returns correct output`() {
        val input =
            ExecutionInput(
                code = "let x: string = readInput(\"First Input\"); println(x);",
                language = "austral",
                version = Version.V2,
                varInputs = listOf("TestValue"),
            )
        val output = service.executeSnippet(input)
        assertTrue(output.any { it.contains("TestValue") })
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
                code = code,
            )
        val formatted = service.formatWithOptions(input)
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
                code = code,
            )
        val lintResult: LintDto = service.lintWithOptions(input)
        assertNotNull(lintResult)
        assertTrue(lintResult.lintErrors.isEmpty())
    }

    @Test
    fun `testSnippet returns passed true for correct output`() {
        val input =
            TestSnippetInput(
                code = "println(\"expected output\");",
                language = "austral",
                version = Version.V2,
                varInputs = listOf(),
                expectedOutputs = listOf("expected output"),
            )
        val result: TestSnippetDto = service.testSnippet(input)
        assertTrue(result.passed)
        assertEquals(null, result.failedAt)
    }

    @Test
    fun `testSnippet returns passed false for incorrect output`() {
        val input =
            TestSnippetInput(
                code = "println(\"wrong output\");",
                language = "austral",
                version = Version.V2,
                varInputs = listOf(),
                expectedOutputs = listOf("expected output"),
            )
        val result: TestSnippetDto = service.testSnippet(input)
        assertFalse(result.passed)
        assertNotNull(result.failedAt)
    }
}
