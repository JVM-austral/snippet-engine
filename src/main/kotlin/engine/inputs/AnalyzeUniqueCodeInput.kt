package engine.inputs

import com.fasterxml.jackson.databind.JsonNode
import factory.Version
import jakarta.validation.constraints.NotBlank

data class AnalyzeUniqueCodeInput(
    @field:NotBlank val language: String,
    val version: Version,
    val config: JsonNode,
    @field:NotBlank val code: String,
)
