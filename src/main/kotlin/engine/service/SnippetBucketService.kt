package engine.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class SnippetBucketService {
    private val restClient =
        RestClient
            .builder()
            .baseUrl("http://asset-service:8080")
            .build()

    fun getAsset(
        path: String,
    ): String {
        val response =
            restClient
                .get()
                .uri("path")
                .retrieve()
                .toEntity(String::class.java)

        return response.body ?: throw RuntimeException("Asset no encontrado")
    }
}
