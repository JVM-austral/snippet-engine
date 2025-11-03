package engine.inputs

import factory.Version
import jakarta.validation.constraints.NotBlank

data class ExecutionInput(
    @field:NotBlank val language: String,
    val version: Version,
    @field:NotBlank val assetPath: String,
    val varInputs: List<String>? = null,
)
