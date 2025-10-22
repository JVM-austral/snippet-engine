package engine

import engine.dto.ParseDto
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
        @Valid @RequestBody req: ParseInput,
    ): ResponseEntity<ParseDto> {
        val parseErrors = engineService.parseSnippet(req.code, req.language, req.version)
        val parseDto = ParseDto(parseErrors)
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
        @Valid @RequestBody req: ExecutionInput,
    ): ResponseEntity<List<String>> {
        val outputs = engineService.executeSnippet(req.code, req.language, req.version, req.varInput)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(outputs)
    }
}
