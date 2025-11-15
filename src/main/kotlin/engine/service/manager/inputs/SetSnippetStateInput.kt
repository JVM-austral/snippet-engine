package engine.service.manager.inputs

data class SetSnippetStateInput(
    val state: CompilantState,
    val snippetId: String,
)
