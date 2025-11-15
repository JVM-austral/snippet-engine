package engine.service.manager.inputs

import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID

data class SetSnippetStateInput (
    val state: CompilantState,
    val snippetId: String,
)