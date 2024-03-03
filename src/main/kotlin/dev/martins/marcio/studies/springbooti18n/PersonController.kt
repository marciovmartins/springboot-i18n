package dev.martins.marcio.studies.springbooti18n

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/people")
class PersonController {
    private val people: MutableMap<String, Person> = mutableMapOf()

    @PutMapping("/{id}")
    fun createOrReplacePerson(@PathVariable id: String, @RequestBody @Valid person: Person?): ResponseEntity<Any> {
        this.people[id] = person!!
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    fun retrievePerson(@PathVariable id: String): ResponseEntity<Person> {
        val person = people[id]
        if (person === null) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(person)
    }

    data class Person(
        @field:Size(min = 1, max = 250)
        val name: String,
        @field:Min(0)
        val age: Int,
    )
}