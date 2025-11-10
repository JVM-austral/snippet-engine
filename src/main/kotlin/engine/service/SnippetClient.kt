package engine.service

interface SnippetClient {
    fun getAsset(
        path: String,
    ): String

    fun formatAsset(
        path: String,
        formattedCode: String,
    ): String
}
