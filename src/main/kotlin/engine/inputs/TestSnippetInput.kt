package engine.inputs

import factory.Version
import jakarta.validation.constraints.NotBlank

data class TestSnippetInput(
    @field:NotBlank val language: String,
    val version: Version,
    @field:NotBlank val assetPath: String,
    val varInputs: List<String> = emptyList(),
    val expectedOutputs: List<String> = emptyList(),
)
