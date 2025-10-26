package engine.inputs

import factory.Version
import jakarta.validation.constraints.NotBlank

data class ExecutionInput(
    val code: String,
    @field:NotBlank val language: String,
    val version: Version,
    val varInput: List<String>? = null,
)
