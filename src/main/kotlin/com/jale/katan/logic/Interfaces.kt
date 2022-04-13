package com.jale.katan.logic


class InvalidPlay(message: String) : Throwable(message)

interface Purchasable {
    val cost: ResourceMap
}

interface Possessable {
    val owner: Player
}
