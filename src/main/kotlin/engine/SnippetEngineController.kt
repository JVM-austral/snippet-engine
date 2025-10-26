package engine

import engine.dto.LintDto
import engine.dto.ParseDto
import engine.inputs.AnalyzeCodeInput
import engine.inputs.ExecutionInput
import engine.inputs.ParseInput
import engine.inputs.TestSnippetInput
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/engine")
class SnippetEngineController(
    val engineService: SnippetEngineService,
) {
    @PostMapping("/parse")
    fun parseSnippet(
        @Valid @RequestBody parseInput: ParseInput,
    ): ResponseEntity<ParseDto> {
        val parseDto = engineService.parseSnippet(parseInput)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(parseDto)
    }

    @PostMapping("/test")
    fun testSnippet(
        @Valid @RequestBody req: TestSnippetInput,
    ): ResponseEntity<ParseDto> {
        TODO()
    }

    @PostMapping("/execute")
    fun executeSnippet(
        @Valid @RequestBody executionInput: ExecutionInput,
    ): ResponseEntity<List<String>> {
        val outputs = engineService.executeSnippet(executionInput)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(outputs)
    }

    @PostMapping("/format")
    fun formatSnippet(
        @RequestBody @Valid formatInput: AnalyzeCodeInput,
    ): ResponseEntity<String> {
        val output = engineService.formatWithOptions(formatInput)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(output)
    }

    @PostMapping("/analyze")
    fun analyzeSnippet(
        @RequestBody @Valid lintInput: AnalyzeCodeInput,
    ): ResponseEntity<LintDto> {
        val errors = engineService.lintWithOptions(lintInput)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(errors)
    }
}
