package engine.dto

data class ExecutionDto(
    val output: List<String>,
    val errors: List<String>,
)
