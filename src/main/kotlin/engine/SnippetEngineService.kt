package engine

import evaluator.input.MockInputProvider
import factory.Version
import org.springframework.stereotype.Service
import runner.RunnerImplementation

@Service
class SnippetEngineService {
    fun parseSnippet(
        code: String,
        language: String,
        version: Version,
    ): List<String> {
        // Language have to be implemented in the future
        val runner = RunnerImplementation(version.toString())
        val ran = runner.run(code)
        return ran.errors
    }

    fun executeSnippet(
        code: String,
        language: String,
        version: Version,
        inputValue : String? = null,
    ): List<String> {
        // Language have to be implemented in the future

        if (inputValue == null) {
            val runner = RunnerImplementation(version.toString())
            val ran = runner.run(code)
            return ran.output
        }

        val inputProvider = MockInputProvider(inputValue)
        val runner = RunnerImplementation(version.toString(), inputProvider = inputProvider)
        val ran = runner.run(code)
        return ran.output
    }
}
