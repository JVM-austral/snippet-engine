
package engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnippetEngineApplication

fun main(args: Array<String>) {
    runApplication<SnippetEngineApplication>(*args)
}
