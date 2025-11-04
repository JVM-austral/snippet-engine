package engine.inputs

import factory.Version
import jakarta.validation.constraints.NotBlank

data class ParseInput(
    @field:NotBlank val language: String,
    val version: Version,
    @field:NotBlank val code: String,
)
