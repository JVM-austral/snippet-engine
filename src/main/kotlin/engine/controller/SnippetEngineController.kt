package engine.controller

import engine.dto.ExecutionDto
import engine.dto.LintDto
import engine.dto.ParseDto
import engine.dto.TestSnippetDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.AnalyzeUniqueCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import engine.inputs.TestSnippetInput
import engine.service.SnippetClient
import engine.service.SnippetEngineService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/engine")
class SnippetEngineController(
    val engineService: SnippetEngineService,
    val snippetClient: SnippetClient,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(SnippetEngineController::class.java)

    @PostMapping("/parse")
    fun parseSnippet(
        @Valid @RequestBody parseInput: ParseInput,
    ): ResponseEntity<ParseDto> {
        log.info("Received parse request for assetPath: ${parseInput.assetPath}")
        return try {
            val code = snippetClient.getAsset(parseInput.assetPath)
            val parseDto = engineService.parseSnippet(parseInput, code)
            log.info("Parsed code for assetPath: ${parseInput.assetPath}")
            ResponseEntity.status(HttpStatus.ACCEPTED).body(parseDto)
        } catch (ex: Exception) {
            log.warn("Failed to parse snippet for assetPath: ${parseInput.assetPath} - ${ex.message}")
            throw ex
        }
    }

    @PostMapping("/test")
    fun testSnippet(
        @Valid @RequestBody testInput: TestSnippetInput,
    ): ResponseEntity<TestSnippetDto> {
        log.info("Received test request for assetPath: ${testInput.assetPath}")
        return try {
            val code = snippetClient.getAsset(testInput.assetPath)
            val testDto = engineService.testSnippet(testInput, code)
            log.info("Tested code for assetPath: ${testInput.assetPath}")
            ResponseEntity.status(HttpStatus.ACCEPTED).body(testDto)
        } catch (ex: Exception) {
            log.warn("Failed to test snippet for assetPath: ${testInput.assetPath} - ${ex.message}")
            throw ex
        }
    }

    @PostMapping("/execute")
    fun executeSnippet(
        @Valid @RequestBody executionInput: ExecutionInput,
    ): ResponseEntity<ExecutionDto> {
        log.info("Received execute request for assetPath: ${executionInput.assetPath}")
        return try {
            val code = snippetClient.getAsset(executionInput.assetPath)
            val outputs = engineService.executeSnippet(executionInput, code)
            log.info("Executed code for assetPath: ${executionInput.assetPath}")
            ResponseEntity.status(HttpStatus.ACCEPTED).body(outputs)
        } catch (ex: Exception) {
            log.warn("Failed to execute snippet for assetPath: ${executionInput.assetPath} - ${ex.message}")
            throw ex
        }
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody @Valid formatInput: AnalyzeUniqueCodeInput,
    ): ResponseEntity<String> {
        log.info("Received format request for language: ${formatInput.language}, version: ${formatInput.version}")
        return try {
            val inputAdapter =
                AnalyzeCodeInput(
                    assetPath = "",
                    language = formatInput.language,
                    version = formatInput.version,
                    config = formatInput.config,
                )
            val output = engineService.formatWithOptions(inputAdapter, formatInput.code)
            log.info("Formatted code snippet successfully")
            ResponseEntity.status(HttpStatus.ACCEPTED).body(output)
        } catch (ex: Exception) {
            log.warn("Failed to format snippet - ${ex.message}")
            throw ex
        }
    }

    @PostMapping("/analyze")
    fun analyzeSnippet(
        @RequestBody @Valid lintInput: AnalyzeCodeInput,
    ): ResponseEntity<LintDto> {
        log.info("Received analyze request for assetPath: ${lintInput.assetPath}")
        return try {
            val code = snippetClient.getAsset(lintInput.assetPath)
            val errors = engineService.lintWithOptions(lintInput, code)
            log.info("Analyzed code for assetPath: ${lintInput.assetPath}")
            ResponseEntity.status(HttpStatus.ACCEPTED).body(errors)
        } catch (ex: Exception) {
            log.warn("Failed to analyze snippet for assetPath: ${lintInput.assetPath} - ${ex.message}")
            throw ex
        }
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> = ResponseEntity.status(HttpStatus.OK).body("pong")
}
