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
                .uri(path)
                .retrieve()
                .toEntity(String::class.java)

        return response.body ?: throw RuntimeException("Asset no encontrado")
    }

    fun formatAsset(
        path: String,
        formattedCode: String,
    ): String {
        val response =
            restClient
                .put()
                .uri(path)
                .body(formattedCode)
                .retrieve()
                .toEntity(String::class.java)

        return when (response.statusCode.value()) {
            201 -> "Asset creado correctamente en $path"
            200 -> "Asset actualizado correctamente en $path"
            else -> throw RuntimeException("Respuesta inesperada: ${response.statusCode}")
        }
    }
}
