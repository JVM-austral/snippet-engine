package security

import engine.security.AudienceValidator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.assertEquals

class AudienceValidatorTest {
    @Test
    fun `validate should succeed when audience matches`() {
        val validator = AudienceValidator("expected-aud")

        val jwt =
            Jwt(
                "token-value",
                null,
                null,
                mapOf("alg" to "none"),
                mapOf("aud" to listOf("expected-aud")),
            )

        val result = validator.validate(jwt)

        assertEquals(OAuth2TokenValidatorResult.success(), result)
    }

    @Test
    fun `validate should fail when audience does not match`() {
        val validator = AudienceValidator("expected-aud")

        val jwt =
            Jwt(
                "token-value",
                null,
                null,
                mapOf("alg" to "none"),
                mapOf("aud" to listOf("other-aud")),
            )

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
        assertEquals("invalid_token", result.errors.first().errorCode)
    }
}
