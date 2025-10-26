package engine.inputs

import com.fasterxml.jackson.databind.JsonNode
import factory.Version

import jakarta.validation.constraints.NotBlank

data class AnalyzeCodeInput(
    @field:NotBlank val language: String,
    val version: Version,
    val config: JsonNode,
    val code: String,
)
