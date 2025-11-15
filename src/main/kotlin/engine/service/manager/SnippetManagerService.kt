package engine.service.manager

import engine.service.manager.inputs.SetSnippetStateInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class SnippetManagerService {
    private val log = LoggerFactory.getLogger(SnippetManagerService::class.java)
    private val restClient =
        RestClient
            .builder()
            .baseUrl("http://manager-service:8080")
            .build()

    fun setSnippetState(input: SetSnippetStateInput): String {
        log.info("Setting state of snippet ${input.snippetId} to $input.state")
        val response =
            restClient
                .put()
                .uri("/snippets/compiling-state")
                .body(input)
                .retrieve()
                .toEntity(String::class.java)

        return when (response.statusCode.value()) {
            201 -> "State changed successfully for snippet ${input.snippetId}"
            200 -> "State changed successfully for snippet ${input.snippetId}"
            else -> throw RuntimeException("Unexpected answer: ${response.statusCode}")
        }
    }
}
