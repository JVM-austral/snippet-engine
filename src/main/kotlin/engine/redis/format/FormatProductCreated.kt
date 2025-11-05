package engine.redis.format

import com.fasterxml.jackson.databind.JsonNode
import factory.Version

data class FormatProductCreated(
    val language: String,
    val version: Version,
    val config: JsonNode,
    val assetPath: String,
)
