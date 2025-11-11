package engine.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class SnippetBucketClient : SnippetClient {
    private val log = org.slf4j.LoggerFactory.getLogger(SnippetBucketClient::class.java)

    private val restClient =
        RestClient
            .builder()
            .baseUrl("http://asset-service:8080")
            .build()

    override fun getAsset(
        path: String,
    ): String {
        log.info("Fetching asset from path: $path")
        try {
            val response =
                restClient
                    .get()
                    .uri(path)
                    .retrieve()
                    .toEntity(String::class.java)

            val body = response.body ?: throw RuntimeException("Asset no encontrado")
            log.info("Asset fetched successfully from path: $path")
            return body
        } catch (ex: Exception) {
            log.warn("Failed to fetch asset from path: $path - ${ex.message}")
            throw RuntimeException("Asset no encontrado")
        }
    }

    override fun formatAsset(
        path: String,
        formattedCode: String,
    ): String {
        log.info("Formatting asset at path: $path")
        try {
            val response =
                restClient
                    .put()
                    .uri(path)
                    .body(formattedCode)
                    .retrieve()
                    .toEntity(String::class.java)

            val result = when (response.statusCode.value()) {
                201 -> {
                    log.info("Asset created successfully at path: $path")
                    "Asset creado correctamente en $path"
                }
                200 -> {
                    log.info("Asset updated successfully at path: $path")
                    "Asset actualizado correctamente en $path"
                }
                else -> {
                    log.warn("Unexpected response when formatting asset at path: $path - Status: ${response.statusCode}")
                    throw RuntimeException("Respuesta inesperada: ${response.statusCode}")
                }
            }
            return result
        } catch (ex: RuntimeException) {
            throw ex
        } catch (ex: Exception) {
            log.warn("Failed to format asset at path: $path - ${ex.message}")
            throw RuntimeException("Error al formatear asset: ${ex.message}")
        }
    }
}
