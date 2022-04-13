package com.jale.katan

import com.jale.katan.logic.BaseGame
import com.jale.katan.logic.Player
import com.jale.katan.logic.defaultBoard

object GameManager {

    private val idSequence = generateSequence(0) { it + 1 }.iterator()

    private val games: MutableMap<Int, BaseGame> = HashMap()

    fun createNewGame(): Int {
        val newId = idSequence.next()
        // TODO configure game before instantiation
        games[newId] = BaseGame(defaultBoard(), (1..4).map { Player(it) })
        return newId
    }

    fun removeGame(id: Int) {
        if (id !in games.keys) throw IllegalArgumentException("Id is not a valid game.")
        games.remove(id)
    }

}