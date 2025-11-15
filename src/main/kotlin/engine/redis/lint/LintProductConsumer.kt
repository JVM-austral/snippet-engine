package engine.redis.lint

import engine.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class LintProductConsumer(
    redis: RedisTemplate<String, Any>,
    @Value("\${stream.linter}") streamKey: String,
    @Value("\${groups.linter}") groupId: String,
    private val lintEngine: LintEngine,
) : RedisStreamConsumer<String>(streamKey, groupId, redis) {
    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofSeconds(3))
            .targetType(String::class.java)
            .build()

    override fun onMessage(record: ObjectRecord<String, String>) {
        try {
            lintEngine.applyLintingEvent(record.value)
        } catch (e: Exception) {
            println("Error processing record: ${e.message}")
            println("Record: ${record.value}")
            e.printStackTrace()
        }
    }
}
