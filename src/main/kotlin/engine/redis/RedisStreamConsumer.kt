package engine.redis

import jakarta.annotation.PostConstruct
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import reactor.core.publisher.Flux
import java.net.InetAddress

abstract class RedisStreamConsumer<Value : Any>(
    protected val streamKey: String,
    protected val groupId: String,
    private val redis: RedisTemplate<String, String>,
) {
    protected abstract fun onMessage(record: ObjectRecord<String, Value>)

    private lateinit var flow: Flux<ObjectRecord<String, Value>>

    protected abstract fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, Value>>

    @PostConstruct
    fun subscription() {
        val options = options()

        try {
            val consumerGroupExists = consumerGroupExists(streamKey, groupId)
            if (!consumerGroupExists) {
                println("Consumer group $groupId for stream $streamKey doesn't exist. Creating...")
                createConsumerGroup(streamKey, groupId)
            } else {
                println("Consumer group $groupId for stream $streamKey exists!")
            }
        } catch (e: Exception) {
            println("Exception: $e")
            println("Stream $streamKey doesn't exist. Creating stream $streamKey and group $groupId")
            redis.opsForStream<Any, Any>().createGroup(streamKey, groupId)
        }
        val factory = redis.connectionFactory as ReactiveRedisConnectionFactory
        val container = StreamReceiver.create(factory, options)
        flow =
            container.receiveAutoAck(
                Consumer.from(groupId, InetAddress.getLocalHost().hostName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            )
        flow.subscribe(this::onMessage)
    }

    private fun createConsumerGroup(
        streamKey: String,
        groupId: String,
    ): String = redis.opsForStream<Any, Any>().createGroup(streamKey, groupId)

    private fun consumerGroupExists(
        stream: String,
        group: String,
    ): Boolean {
        val groups = redis.opsForStream<Any, Any>().groups(stream)
        for (it in groups) {
            if (it.groupName() == group) return true
        }
        return false
    }
}
