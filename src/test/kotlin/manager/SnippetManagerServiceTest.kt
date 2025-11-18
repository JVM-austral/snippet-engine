package manager

import engine.service.manager.SnippetManagerService
import engine.service.manager.inputs.CompilantState
import engine.service.manager.inputs.SetSnippetStateInput
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SnippetManagerServiceTest {
    @Mock
    lateinit var restClient: RestClient

    @Mock
    lateinit var putSpec: RestClient.RequestBodyUriSpec

    @Mock
    lateinit var bodySpec: RestClient.RequestBodySpec

    @Mock
    lateinit var retrieveSpec: RestClient.ResponseSpec

    private lateinit var service: SnippetManagerService

    @BeforeEach
    fun setup() {
        service = SnippetManagerService(restClient)
    }

    @Test
    fun `setSnippetState returns success message for 200`() {
        val input = SetSnippetStateInput(CompilantState.COMPILANT, "abc")

        `when`(restClient.put()).thenReturn(putSpec)
        `when`(putSpec.uri("/snippets/compiling-state")).thenReturn(bodySpec)
        `when`(bodySpec.body(input)).thenReturn(bodySpec)
        `when`(bodySpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("OK", HttpStatus.OK)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        val result = service.setSnippetState(input)

        assertEquals("State changed successfully for snippet abc", result)
    }

    @Test
    fun `setSnippetState returns success message for 201`() {
        val input = SetSnippetStateInput(CompilantState.COMPILANT, "xyz")

        `when`(restClient.put()).thenReturn(putSpec)
        `when`(putSpec.uri("/snippets/compiling-state")).thenReturn(bodySpec)
        `when`(bodySpec.body(input)).thenReturn(bodySpec)
        `when`(bodySpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("Created", HttpStatus.CREATED)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        val result = service.setSnippetState(input)

        assertEquals("State changed successfully for snippet xyz", result)
    }

    @Test
    fun `setSnippetState throws for unexpected status`() {
        val input = SetSnippetStateInput(CompilantState.COMPILANT, "PENDING")

        `when`(restClient.put()).thenReturn(putSpec)
        `when`(putSpec.uri("/snippets/compiling-state")).thenReturn(bodySpec)
        `when`(bodySpec.body(input)).thenReturn(bodySpec)
        `when`(bodySpec.retrieve()).thenReturn(retrieveSpec)

        val response = ResponseEntity("Bad Request", HttpStatus.BAD_REQUEST)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(response)

        assertThrows(RuntimeException::class.java) {
            service.setSnippetState(input)
        }
    }
}
