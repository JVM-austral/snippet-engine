package engine.redis.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        val objectMapper: ObjectMapper =
            jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                .findAndRegisterModules()
        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)

        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer
        template.afterPropertiesSet()
        return template
    }
}
