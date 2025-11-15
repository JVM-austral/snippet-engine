package engine.redis.lint

import com.fasterxml.jackson.databind.JsonNode

data class LintProductCreated(
    val snippetId: String,
    val config: JsonNode,
    val language: String,
    val version: String,
    val assetPath: String,
)
