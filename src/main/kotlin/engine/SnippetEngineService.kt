package engine

import org.springframework.stereotype.Service
import runner.RunnerImplementation

@Service
class SnippetEngineService {
    fun parseSnippet(
        code: String,
        language: String,
        version: String,
    ): List<String> {
        // Language have to be implemented in the future
        val runner = RunnerImplementation(version)
        runner.run(code.byteInputStream())
        val errorOutput = runner.getErrorHandler()
        val capturedErrors = errorOutput.getCapturedErrors()
        return capturedErrors
    }
}
