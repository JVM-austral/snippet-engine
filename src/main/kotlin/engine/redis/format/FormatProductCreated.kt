package engine.redis.format

import com.fasterxml.jackson.databind.JsonNode

data class FormatProductCreated(
    val language: String,
    val version: String,
    val config: JsonNode,
    val assetPath: String,
)
