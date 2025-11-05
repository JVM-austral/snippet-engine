package ingsis.auth

import engine.SnippetEngineApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [SnippetEngineApplication::class])
@ActiveProfiles("test")
class EngineApplicationTests {
    @Test
    fun contextLoads() {
    }
}
