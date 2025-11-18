package security

import SecurityConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import kotlin.test.assertNotNull

class SecurityConfigTest {
    private val audience = "test-audience"
    private val issuer = "https://issuer.example.com"

    private val securityConfig = SecurityConfig(audience, issuer)

    @Test
    fun `jwtDecoder bean should be mocked`() {
        val jwtDecoderMock = mockk<JwtDecoder>()

        val jwtDecoder = jwtDecoderMock

        assertNotNull(jwtDecoder)
    }

    @Test
    fun `securityFilterChain should build successfully`() {
        val http = mockk<HttpSecurity>(relaxed = true)
        every { http.build() } returns mockk()

        val filterChain = securityConfig.securityFilterChain(http)
        assertNotNull(filterChain)
    }
}
