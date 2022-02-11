package com.jale.katan

import com.jale.katan.logic.Game

object GameManager {

    private val idSequence = generateSequence(0) { it + 1 }.iterator()

    private val games: MutableMap<Int, Game> = HashMap()

    fun createNewGame(): Int {
        val newId = idSequence.next()
        games[newId] = Game()
        return newId
    }

    fun removeGame(id: Int) {
        if (id !in games.keys) throw IllegalArgumentException("Id is not a valid game.")
        games.remove(id)
    }

}