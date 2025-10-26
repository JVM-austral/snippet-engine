package engine

import com.fasterxml.jackson.databind.ObjectMapper
import engine.dto.LintDto
import engine.dto.ParseDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import evaluator.input.MockInputProvider
import factory.Version
import formatterconfig.ConfigurableFormatterOptionsV1
import formatterconfig.ConfigurableFormatterOptionsV2
import linterconfig.ConfigurableAnalyzerOptionsV1
import linterconfig.ConfigurableAnalyzerOptionsV2
import org.springframework.stereotype.Service
import runner.RunnerImplementation

@Service
class SnippetEngineService(
    private val objectMapper: ObjectMapper,
) {
    fun parseSnippet(
        parseInput: ParseInput,
    ): ParseDto {
        // Language have to be implemented in the future
        val runner = RunnerImplementation(parseInput.version.toString())
        val ran = runner.run(parseInput.code)
        return ParseDto(ran.errors)
    }

    fun executeSnippet(
        input: ExecutionInput,
    ): List<String> {
        if (input.varInput == null) {
            val runner = RunnerImplementation(input.version.toString())
            val ran = runner.run(input.code)
            return ran.output
        }

        val inputProvider = MockInputProvider(input.varInput[0])
        val runner = RunnerImplementation(input.version.toString(), inputProvider = inputProvider)
        val ran = runner.run(input.code)
        return ran.output
    }

    fun formatWithOptions(formatInput: AnalyzeCodeInput): String {
        val config =
            when (formatInput.version) {
                Version.V1 -> objectMapper.treeToValue(formatInput.config, ConfigurableFormatterOptionsV1::class.java)
                Version.V2 -> objectMapper.treeToValue(formatInput.config, ConfigurableFormatterOptionsV2::class.java)
            }

        val runner = RunnerImplementation(formatInput.version.toString())

        val formattedCode = runner.format(formatInput.code, config)

        return formattedCode
    }

    fun lintWithOptions(lintInput: AnalyzeCodeInput): LintDto {
        val config =
            when (lintInput.version) {
                Version.V1 -> objectMapper.treeToValue(lintInput.config, ConfigurableAnalyzerOptionsV1::class.java)
                Version.V2 -> objectMapper.treeToValue(lintInput.config, ConfigurableAnalyzerOptionsV2::class.java)
            }

        val runner = RunnerImplementation(lintInput.version.toString())

        val lintErrors = runner.lint(lintInput.code, config)

        return LintDto(lintErrors)
    }
}
