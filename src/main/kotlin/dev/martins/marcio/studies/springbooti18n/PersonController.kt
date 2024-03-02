package dev.martins.marcio.studies.springbooti18n

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/people")
class PersonController(
) {
    private val people: MutableMap<String, Person> = mutableMapOf()

    @PutMapping("/{id}")
    fun createOrReplacePerson(@PathVariable id: String, @RequestBody person: Person?): ResponseEntity<Any> {
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
        val name: String,
        val age: Int,
    )
}