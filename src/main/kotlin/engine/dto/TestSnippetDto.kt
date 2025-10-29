package engine.dto

data class TestSnippetDto(
    val passed: Boolean,
    val failedAt: Int?,
)
