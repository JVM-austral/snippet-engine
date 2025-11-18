package engine.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean
    fun managerRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("http://manager-service:8080")
            .build()

    @Bean
    fun bucketRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl("http://asset-service:8080")
            .build()
}
