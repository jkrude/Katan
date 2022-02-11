package com.jale.katan.springapi

import com.jale.katan.GameManager
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController {

    @PostMapping(value = ["/games"])
    fun createGame(): Int {
        return GameManager.createNewGame()
    }

    @DeleteMapping(value = ["/game/{id}"])
    fun removeGame(@PathVariable id: Int) {
        GameManager.removeGame(id)
    }
}