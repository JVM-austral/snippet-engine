package engine.redis.format

import com.fasterxml.jackson.databind.ObjectMapper
import engine.inputs.AnalyzeCodeInput
import engine.redis.RedisStreamConsumer
import engine.service.SnippetBucketClient
import engine.service.SnippetEngineService
import factory.Version
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
        private val engineService: SnippetEngineService,
        private val bucketService: SnippetBucketClient,
        private val objectMapper: ObjectMapper,
    ) : RedisStreamConsumer<String>(streamKey, groupId, redis) {
        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(3))
                .targetType(String::class.java)
                .build()

        override fun onMessage(record: ObjectRecord<String, String>) {
            try {
                val response: FormatProductCreated = objectMapper.readValue(record.value, FormatProductCreated::class.java)

                val formatInput = adaptEventResponseToService(response)

                val snippetPath = formatInput.assetPath
                val code = bucketService.getAsset(snippetPath)
                val output = engineService.formatWithOptions(formatInput, code)
                bucketService.formatAsset(path = snippetPath, formattedCode = output)
            } catch (e: Exception) {
                println("Error processing record: ${e.message}")
                println("Record: ${record.value}")
                e.printStackTrace()
            }
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
