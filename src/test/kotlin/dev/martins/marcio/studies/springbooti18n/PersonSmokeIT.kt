package dev.martins.marcio.studies.springbooti18n

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import java.util.UUID


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonSmokeIT {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun createAndRetrievePerson() {
        // given
        val personId = UUID.randomUUID().toString()
        val person = TestPersonDTO(
            name = "John Doe",
            age = 18,
        )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val jsonstring = person.toJson()
        val request = HttpEntity(jsonstring, headers)

        // when
        val createdResponseEntity = testRestTemplate.exchange<Any>("/people/$personId", HttpMethod.PUT, request)
        val retrievedResponseEntity = testRestTemplate.exchange<TestPersonDTO>("/people/$personId", HttpMethod.GET)

        // then
        assertThat(createdResponseEntity.statusCode).isEqualTo(HttpStatusCode.valueOf(204))
        assertAll(
            { assertThat(retrievedResponseEntity.statusCode).isEqualTo(HttpStatusCode.valueOf(200)) },
            { assertThat(retrievedResponseEntity.body).usingRecursiveComparison().isEqualTo(person) },
        )
    }

    private fun Any.toJson(): String = objectMapper.writeValueAsString(this)

    data class TestPersonDTO(val name: String? = null, val age: Int? = null)
}
