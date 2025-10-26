package engine.inputs

import com.fasterxml.jackson.databind.JsonNode
import factory.Version
import jakarta.validation.constraints.NotBlank

data class TestSnippetInput(
    @field:NotBlank val language: String,
    val version: Version,
    val inputs: List<String> = emptyList(),
    val expectedOutputs: List<String> = emptyList(),
)
