package engine

import engine.dto.ParseDto
import engine.inputs.ParseInput
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
) {
    @PostMapping
    fun parseSnippet(
        @Valid @RequestBody req: ParseInput,
    ): ResponseEntity<ParseDto> {
        val parseErrors = engineService.parseSnippet(req.code, req.language, req.version)
        val parseDto = ParseDto(parseErrors)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(parseDto)
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> = ResponseEntity.status(HttpStatus.OK).body("pong")
}
