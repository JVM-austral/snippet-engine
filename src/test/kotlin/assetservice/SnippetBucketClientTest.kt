package assetservice

import engine.service.SnippetBucketClient
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SnippetBucketClientTest {
    @Mock
    lateinit var restClient: RestClient

    @Mock
    lateinit var getSpec: RestClient.RequestHeadersUriSpec<*>

    @Mock
    lateinit var retrieveSpec: RestClient.ResponseSpec

    @Mock
    lateinit var putSpec: RestClient.RequestBodyUriSpec

    @Mock
    lateinit var putBodySpec: RestClient.RequestBodySpec

    private lateinit var client: SnippetBucketClient

    @BeforeEach
    fun setup() {
        client = SnippetBucketClient(restClient)
    }

    @Test
    fun `getAsset returns body when successful`() {
        val path = "/assets/test.txt"

        `when`(restClient.get()).thenReturn(getSpec)
        `when`(getSpec.uri(path)).thenReturn(getSpec)
        `when`(getSpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("contenido", HttpStatus.OK)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        val result = client.getAsset(path)

        assertEquals("contenido", result)
        verify(restClient).get()
    }

    @Test
    fun `getAsset throws when body is null`() {
        val path = "/assets/missing.txt"

        `when`(restClient.get()).thenReturn(getSpec)
        `when`(getSpec.uri(path)).thenReturn(getSpec)
        `when`(getSpec.retrieve()).thenReturn(retrieveSpec)

        val response: ResponseEntity<String?>? = ResponseEntity(null, HttpStatus.OK)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        assertThrows(RuntimeException::class.java) {
            client.getAsset(path)
        }
    }

    @Test
    fun `getAsset throws when restclient fails`() {
        val path = "/assets/fail.txt"

        `when`(restClient.get()).thenThrow(RuntimeException("boom"))

        assertThrows(RuntimeException::class.java) {
            client.getAsset(path)
        }
    }

    @Test
    fun `formatAsset returns creation message for 201`() {
        val path = "/assets/new.txt"
        val code = "formatted"

        `when`(restClient.put()).thenReturn(putSpec)
        `when`(putSpec.uri(path)).thenReturn(putBodySpec)
        `when`(putBodySpec.body(code)).thenReturn(putBodySpec)
        `when`(putBodySpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("created", HttpStatus.CREATED)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        val result = client.formatAsset(path, code)

        assertEquals("Asset creado correctamente en /assets/new.txt", result)
    }

    @Test
    fun `formatAsset returns update message for 200`() {
        val path = "/assets/existing.txt"
        val code = "formatted"

        `when`(restClient.put()).thenReturn(putSpec)
        `when`(putSpec.uri(path)).thenReturn(putBodySpec)
        `when`(putBodySpec.body(code)).thenReturn(putBodySpec)
        `when`(putBodySpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("updated", HttpStatus.OK)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        val result = client.formatAsset(path, code)

        assertEquals("Asset actualizado correctamente en /assets/existing.txt", result)
    }

    @Test
    fun `formatAsset throws for unexpected status`() {
        val path = "/assets/oops"
        val code = "formatted"

        `when`(restClient.put()).thenReturn(putSpec)
        `when`(putSpec.uri(path)).thenReturn(putBodySpec)
        `when`(putBodySpec.body(code)).thenReturn(putBodySpec)
        `when`(putBodySpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("weird", HttpStatus.BAD_REQUEST)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        assertThrows(RuntimeException::class.java) {
            client.formatAsset(path, code)
        }
    }

    @Test
    fun `formatAsset wraps unexpected errors`() {
        val path = "/assets/error"
        val code = "formatted"

        `when`(restClient.put()).thenThrow(RuntimeException("boom"))

        assertThrows(RuntimeException::class.java) {
            client.formatAsset(path, code)
        }
    }
}
