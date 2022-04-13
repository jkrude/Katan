package com.jale.katan.logic

class Player(val id: Int) {

    private val _resources: MutableResourceMap = MutableResourceMap()
    val resources: ResourceMap get() = _resources
    var availableSettlements = 5
    var availableCities = 4
    var availableRoads = 15

    var winningPoints = 0

    val playedDevelopmentCards: MutableList<OwnedDevelopmentCard> = ArrayList()
    val hiddenDevelopmentCards: MutableList<OwnedDevelopmentCard> = ArrayList()

    fun hasEnoughResources(resources: ResourceMap) =
        resources.entries.all { (r, amount) ->
            this._resources.getValue(r) >= amount
        }

    fun addResources(resources: ResourceMap) {
        this._resources += resources
    }

    fun addResource(pair: Pair<Resource, Int>) {
        this._resources += pair
    }

    fun removeResources(resources: ResourceMap) {
        this._resources -= resources
    }

    fun removeResource(pair: Pair<Resource, Int>) {
        this._resources -= pair
    }

    override fun toString(): String {
        return "Player(" +
                "id=$id," +
                " resources=$resources," +
                " winningPoints=$winningPoints," +
                " playedDevelopmentCards=$playedDevelopmentCards," +
                " hiddenDevelopmentCards=$hiddenDevelopmentCards" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Player
        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}
