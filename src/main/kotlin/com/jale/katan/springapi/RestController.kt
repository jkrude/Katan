package com.jale.katan.springapi

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController {

    @PostMapping(value = ["/games"])
    fun createGame() : String {
        val id = "1234"
        println("Create game with id $id")
        return id
    }

    @DeleteMapping(value = ["/game/{id}"])
    fun removeGame(@PathVariable id: String) {
        println("Remove game $id")
    }
}