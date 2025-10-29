package engine.service

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.stereotype.Service
import runner.RunnerImplementation

@Service
class SnippetEngineService(
    private val objectMapper: ObjectMapper,
) {
    fun parseSnippet(
        parseInput: ParseInput,
    ): ParseDto {
        val runner = RunnerImplementation(parseInput.version.toString())
        val ran = runner.run(parseInput.code)
        return ParseDto(ran.errors)
    }

    fun executeSnippet(
        input: ExecutionInput,
    ): List<String> {
        if (input.varInputs.isNullOrEmpty() ) {
            val runner = RunnerImplementation(input.version.toString())
            val ran = runner.run(input.code)
            return ran.output
        }

        val inputProvider = ProvideQueueOfInputs(input.varInputs)
        val runner = RunnerImplementation(input.version.toString(), inputProvider = inputProvider)
        val ran = runner.run(input.code)
        return ran.output
    }

    fun formatWithOptions(formatInput: AnalyzeCodeInput): String {
        val config =
            setFormatVersionConfig(formatInput)

        val runner = RunnerImplementation(formatInput.version.toString())

        val formattedCode = runner.format(formatInput.code, config)

        return formattedCode
    }

    fun lintWithOptions(lintInput: AnalyzeCodeInput): LintDto {
        val config: ConfigurableAnalyzersOptions =
            setLintVersionOptions(lintInput)

        val runner = RunnerImplementation(lintInput.version.toString())

        val lintErrors = runner.lint(lintInput.code, config)

        return LintDto(lintErrors)
    }

    fun testSnippet(
        input: TestSnippetInput,
    ): TestSnippetDto {
        val runner = createRunnerForTest(input)
        val ran = runner.run(input.code)

        handleRunErrors(input, ran.errors)?.let { return it }
        handleOutputVerification(input, ran.output)?.let { return it }

        return TestSnippetDto(
            passed = true,
            failedAt = null,
        )
    }

    private fun createRunnerForTest(input: TestSnippetInput) = RunnerImplementation(input.version.toString(), inputProvider = ProvideQueueOfInputs(input.varInputs))

    private fun handleRunErrors(
        input: TestSnippetInput,
        errors: List<String>,
    ): TestSnippetDto? {
        if (errors.isNotEmpty()) {
            val errorLine = findTestErrorLine(input.code, errors[0])
            return TestSnippetDto(
                passed = false,
                failedAt = errorLine,
            )
        }
        return null
    }

    private fun setLintVersionOptions(lintInput: AnalyzeCodeInput): ConfigurableAnalyzersOptions =
        when (lintInput.version) {
            Version.V1 -> objectMapper.treeToValue(lintInput.config, ConfigurableAnalyzerOptionsV1::class.java)
            Version.V2 -> objectMapper.treeToValue(lintInput.config, ConfigurableAnalyzerOptionsV2::class.java)
        }

    private fun setFormatVersionConfig(formatInput: AnalyzeCodeInput): ConfigurableFormatterOptions =
        when (formatInput.version) {
            Version.V1 -> objectMapper.treeToValue(formatInput.config, ConfigurableFormatterOptionsV1::class.java)
            Version.V2 -> objectMapper.treeToValue(formatInput.config, ConfigurableFormatterOptionsV2::class.java)
        }

    private fun handleOutputVerification(
        input: TestSnippetInput,
        output: List<String>,
    ): TestSnippetDto? {
        val (isSuccess, errorOutput) = assertPrintScriptOutputs(input.expectedOutputs, output)
        if (!isSuccess) {
            val errorLine = findTestErrorLine(input.code, errorOutput)
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
}
