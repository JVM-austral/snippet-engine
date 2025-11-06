package engine.redis.format

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import engine.inputs.AnalyzeCodeInput
import engine.redis.RedisStreamConsumer
import engine.service.SnippetBucketService
import engine.service.SnippetEngineService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class FormatProductConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, Any>,
        @Value("\${stream.formatter}") streamKey: String,
        @Value("\${groups.formatter}") groupId: String,
        val engineService: SnippetEngineService,
        val bucketService: SnippetBucketService,
    ) : RedisStreamConsumer<Map<String, String>>(streamKey, groupId, redis) {
        private val objectMapper = jacksonObjectMapper()

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, Map<String, String>>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(1000 * 3))
                .targetType(Map::class.java as Class<Map<String, String>>)
                .build()

        override fun onMessage(record: ObjectRecord<String, Map<String, String>>) {
            try {
                val jsonString =
                    record.value["value"]
                        ?: throw IllegalArgumentException("No 'value' field found in record")
                val formatProductCreated = objectMapper.readValue<FormatProductCreated>(jsonString)

                val formatInput = fromFormatProductCreatedToAnalyzeCodeInput(formatProductCreated)
                val snippetPath = formatInput.assetPath
                val code = bucketService.getAsset(snippetPath)
                val output = engineService.formatWithOptions(formatInput = formatInput, code)
                bucketService.formatAsset(path = snippetPath, formattedCode = output)
            } catch (e: Exception) {
                println("Error processing message: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun fromFormatProductCreatedToAnalyzeCodeInput(formatProductCreated: FormatProductCreated): AnalyzeCodeInput =
            AnalyzeCodeInput(
                language = formatProductCreated.language,
                version = formatProductCreated.version,
                assetPath = formatProductCreated.assetPath,
                config = formatProductCreated.config,
            )
    }
