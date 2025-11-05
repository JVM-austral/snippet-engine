package engine.service

import com.fasterxml.jackson.databind.ObjectMapper
import engine.dto.ExecutionDto
import engine.dto.LintDto
import engine.dto.ParseDto
import engine.dto.TestSnippetDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import engine.inputs.TestSnippetInput
import evaluator.input.ProvideQueueOfInputs
import factory.Version
import formatterconfig.ConfigurableFormatterOptions
import formatterconfig.ConfigurableFormatterOptionsV1
import formatterconfig.ConfigurableFormatterOptionsV2
import linterconfig.ConfigurableAnalyzerOptionsV1
import linterconfig.ConfigurableAnalyzerOptionsV2
import linterconfig.ConfigurableAnalyzersOptions
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import runner.RunnerImplementation

@Service
class SnippetEngineService(
    private val objectMapper: ObjectMapper,
) {
    fun parseSnippet(
        parseInput: ParseInput,
        code: String,
    ): ParseDto {
        val runner = RunnerImplementation(parseInput.version.toString())
        val ran = runCatchingToHttp("Failed to parse snippet") { runner.run(code) }
        return ParseDto(ran.errors)
    }

    fun executeSnippet(
        input: ExecutionInput,
        code: String,
    ): ExecutionDto {
        if (input.varInputs.isNullOrEmpty()) {
            val runner = RunnerImplementation(input.version.toString())
            val ran = runCatchingToHttp("Failed to execute snippet") { runner.run(code) }
            return ExecutionDto(output = ran.output, errors = ran.errors)
        }

        val inputProvider = ProvideQueueOfInputs(input.varInputs)
        val runner = RunnerImplementation(input.version.toString(), inputProvider = inputProvider)
        val ran = runCatchingToHttp("Failed to execute snippet") { runner.run(code) }
        return ExecutionDto(output = ran.output, errors = ran.errors)
    }

    fun formatWithOptions(
        formatInput: AnalyzeCodeInput,
        code: String,
    ): String {
        val config =
            setFormatVersionConfig(formatInput)

        val runner = RunnerImplementation(formatInput.version.toString())

        val formattedCode = runCatchingToHttp("Failed to format snippet") { runner.format(code, config) }

        return formattedCode
    }

    fun lintWithOptions(
        lintInput: AnalyzeCodeInput,
        code: String,
    ): LintDto {
        val config: ConfigurableAnalyzersOptions =
            setLintVersionOptions(lintInput)

        val runner = RunnerImplementation(lintInput.version.toString())

        val lintErrors = runCatchingToHttp("Failed to lint snippet") { runner.lint(code, config) }

        return LintDto(lintErrors)
    }

    fun testSnippet(
        input: TestSnippetInput,
        code: String,
    ): TestSnippetDto {
        val runner = createRunnerForTest(input)
        val ran = runCatchingToHttp("Failed to run snippet tests") { runner.run(code) }

        handleRunErrors(ran.errors, code)?.let { return it }
        handleOutputVerification(input, ran.output, code)?.let { return it }

        return TestSnippetDto(
            passed = true,
            failedAt = null,
        )
    }

    private fun createRunnerForTest(input: TestSnippetInput) = RunnerImplementation(input.version.toString(), inputProvider = ProvideQueueOfInputs(input.varInputs))

    private fun handleRunErrors(
        errors: List<String>,
        code: String,
    ): TestSnippetDto? {
        if (errors.isNotEmpty()) {
            val errorLine = findTestErrorLine(code, errors[0])
            return TestSnippetDto(
                passed = false,
                failedAt = errorLine,
            )
        }
        return null
    }

    private fun setLintVersionOptions(lintInput: AnalyzeCodeInput): ConfigurableAnalyzersOptions =
        try {
            when (lintInput.version) {
                Version.V1 -> objectMapper.treeToValue(lintInput.config, ConfigurableAnalyzerOptionsV1::class.java)
                Version.V2 -> objectMapper.treeToValue(lintInput.config, ConfigurableAnalyzerOptionsV2::class.java)
            }
        } catch (ex: ResponseStatusException) {
            throw ex
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid linter options: ${ex.message}")
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare linter options: ${ex.message}")
        }

    private fun setFormatVersionConfig(formatInput: AnalyzeCodeInput): ConfigurableFormatterOptions =
        try {
            when (formatInput.version) {
                Version.V1 -> objectMapper.treeToValue(formatInput.config, ConfigurableFormatterOptionsV1::class.java)
                Version.V2 -> objectMapper.treeToValue(formatInput.config, ConfigurableFormatterOptionsV2::class.java)
            }
        } catch (ex: ResponseStatusException) {
            throw ex
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid formatter options: ${ex.message}")
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare formatter options: ${ex.message}")
        }

    private fun handleOutputVerification(
        input: TestSnippetInput,
        output: List<String>,
        code: String,
    ): TestSnippetDto? {
        val (isSuccess, errorOutput) = assertPrintScriptOutputs(input.expectedOutputs, output)
        if (!isSuccess) {
            val errorLine = findTestErrorLine(code, errorOutput)
            return TestSnippetDto(
                passed = false,
                failedAt = errorLine,
            )
        }
        return null
    }

    private fun assertPrintScriptOutputs(
        expectedOutputs: List<String>,
        actualOutputs: List<String>,
    ): Pair<Boolean, String> {
        if (expectedOutputs.size > actualOutputs.size) {
            return Pair(false, "Missing outputs")
        }

        if (expectedOutputs.size < actualOutputs.size) {
            val totalOfOutputsIgnored = actualOutputs.size - expectedOutputs.size
            val isPrefix = expectedOutputs == actualOutputs.subList(0, expectedOutputs.size)
            return Pair(isPrefix, "Outputs ignored $totalOfOutputsIgnored")
        }
        val ok = actualOutputs == expectedOutputs
        return Pair(ok, if (ok) "ok" else "Mismatch")
    }

    private fun findTestErrorLine(
        code: String,
        match: String,
    ): Int {
        val codeLines = code.lines()
        for ((index, line) in codeLines.withIndex()) {
            if (line.contains("readInput($match)")) {
                return index + 1
            }
        }
        return -1
    }

    private inline fun <T> runCatchingToHttp(
        prefix: String,
        block: () -> T,
    ): T {
        try {
            return block()
        } catch (ex: ResponseStatusException) {
            throw ex
        } catch (ex: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$prefix: ${ex.message}")
        } catch (ex: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "$prefix: ${ex.message}")
        }
    }
}
