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
    val bucketService: SnippetClient,
) {
    @PostMapping("/parse")
    fun parseSnippet(
        @Valid @RequestBody parseInput: ParseInput,
    ): ResponseEntity<ParseDto> {
        val code = bucketService.getAsset(parseInput.assetPath)
        val parseDto = engineService.parseSnippet(parseInput, code)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(parseDto)
    }

    @PostMapping("/test")
    fun testSnippet(
        @Valid @RequestBody testInput: TestSnippetInput,
    ): ResponseEntity<TestSnippetDto> {
        val code = bucketService.getAsset(testInput.assetPath)
        val testDto = engineService.testSnippet(testInput, code)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(testDto)
    }

    @PostMapping("/execute")
    fun executeSnippet(
        @Valid @RequestBody executionInput: ExecutionInput,
    ): ResponseEntity<ExecutionDto> {
        val code = bucketService.getAsset(executionInput.assetPath)
        val outputs = engineService.executeSnippet(executionInput, code)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(outputs)
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody @Valid formatInput: AnalyzeUniqueCodeInput,
    ): ResponseEntity<String> {
        val inputAdapter =
            AnalyzeCodeInput(
                assetPath = "",
                language = formatInput.language,
                version = formatInput.version,
                config = formatInput.config,
            )
        val output = engineService.formatWithOptions(inputAdapter, formatInput.code)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(output)
    }

    @PostMapping("/analyze")
    fun analyzeSnippet(
        @RequestBody @Valid lintInput: AnalyzeCodeInput,
    ): ResponseEntity<LintDto> {
        val code = bucketService.getAsset(lintInput.assetPath)
        val errors = engineService.lintWithOptions(lintInput, code)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(errors)
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> = ResponseEntity.status(HttpStatus.OK).body("pong")
}
