package engine.redis.lint

import com.fasterxml.jackson.databind.ObjectMapper
import engine.inputs.AnalyzeCodeInput
import engine.service.SnippetBucketClient
import engine.service.SnippetEngineService
import engine.service.manager.SnippetManagerService
import engine.service.manager.inputs.CompilantState
import engine.service.manager.inputs.SetSnippetStateInput
import factory.Version
import org.springframework.stereotype.Component

@Component
class LintEngine(
    private val engineService: SnippetEngineService,
    private val bucketService: SnippetBucketClient,
    private val objectMapper: ObjectMapper,
    private val managerService: SnippetManagerService,
) {
    fun applyLintingEvent(eventContent: String) {
        val response: LintProductCreated = objectMapper.readValue(eventContent, LintProductCreated::class.java)

        val lintInput = adaptEventResponseToService(response)
        val code = bucketService.getAsset(lintInput.assetPath)
        val output = engineService.lintWithOptions(lintInput, code)
        if (output.lintErrors.isNotEmpty()) {
            managerService.setSnippetState(
                SetSnippetStateInput(
                    snippetId = response.snippetId,
                    state = CompilantState.NON_COMPILANT,
                ),
            )
        } else {
            managerService.setSnippetState(
                SetSnippetStateInput(
                    snippetId = response.snippetId,
                    state = CompilantState.COMPILANT,
                ),
            )
        }
    }

    private fun adaptEventResponseToService(input: LintProductCreated): AnalyzeCodeInput {
        val version =
            when (input.version) {
                "V1" -> Version.V1
                "V2" -> Version.V2
                else -> throw IllegalArgumentException("Unsupported version: ${input.version}")
            }

        val lintInput =
            AnalyzeCodeInput(
                language = input.language,
                version = version,
                assetPath = input.assetPath,
                config = input.config,
            )
        return lintInput
    }
}
