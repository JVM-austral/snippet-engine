package engine.redis.format

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
    ) : RedisStreamConsumer<FormatProductCreated>(streamKey, groupId, redis) {
        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, FormatProductCreated>> =
            StreamReceiver.StreamReceiverOptions
                .builder()
                .pollTimeout(Duration.ofMillis(1000 * 3)) // Set poll rate
                .targetType(FormatProductCreated::class.java) // Set type to de-serialize record
                .build()

        override fun onMessage(record: ObjectRecord<String, FormatProductCreated>) {
            val formatProduct =
                when (val raw = record.value) {
                    else -> raw
                }

            println("📨 Received FormatProductCreated: $formatProduct")

            val formatInput = fromRedisReqToFormatInput(formatProduct)

            val snippetPath = formatInput.assetPath
            val code = bucketService.getAsset(snippetPath)
            val output = engineService.formatWithOptions(formatInput, code)
            bucketService.formatAsset(path = snippetPath, formattedCode = output)
        }

        private fun fromRedisReqToFormatInput(formatProduct: FormatProductCreated): AnalyzeCodeInput {
            val formatInput =
                AnalyzeCodeInput(
                    language = formatProduct.language,
                    version = formatProduct.version,
                    assetPath = formatProduct.assetPath,
                    config = formatProduct.config,
                )
            return formatInput
        }
    }
