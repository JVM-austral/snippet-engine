package engine.redis.format

import com.fasterxml.jackson.databind.ObjectMapper
import engine.inputs.AnalyzeCodeInput
import engine.service.SnippetBucketClient
import engine.service.SnippetEngineService
import factory.Version
import org.springframework.stereotype.Component

@Component
class FormatEngine(
    private val engineService: SnippetEngineService,
    private val bucketService: SnippetBucketClient,
    private val objectMapper: ObjectMapper,
) {
    fun applyFormattingEvent(eventContent: String) {
        val response: FormatProductCreated = objectMapper.readValue(eventContent, FormatProductCreated::class.java)

        val formatInput = adaptEventResponseToService(response)

        val snippetPath = formatInput.assetPath
        val code = bucketService.getAsset(snippetPath)
        val output = engineService.formatWithOptions(formatInput, code)
        bucketService.formatAsset(path = snippetPath, formattedCode = output)
    }

    private fun adaptEventResponseToService(input: FormatProductCreated): AnalyzeCodeInput {
        val version =
            when (input.version) {
                "V1" -> Version.V1
                "V2" -> Version.V2
                else -> throw IllegalArgumentException("Unsupported version: ${input.version}")
            }

        val formatInput =
            AnalyzeCodeInput(
                language = input.language,
                version = version,
                assetPath = input.assetPath,
                config = input.config,
            )
        return formatInput
    }
}
