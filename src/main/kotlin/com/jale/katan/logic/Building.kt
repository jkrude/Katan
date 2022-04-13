package com.jale.katan.logic

interface Building : Purchasable, Possessable {

    fun resourceYield(tileResource: Resource): ResourceMap
    fun yieldsWinningPoints(): Int
}

class City(override val owner: Player) : Building {

    companion object {
        val cost: ResourceMap = resourceMapOf(
            Resource.Grain to 2,
            Resource.Ore to 3
        )
        const val winningPoints = 2
    }

    override val cost = City.cost

    override fun resourceYield(tileResource: Resource): ResourceMap =
        resourceMapOf(tileResource to 2)

    override fun yieldsWinningPoints() = winningPoints

    override fun toString(): String {
        return "City(owner=$owner)"
    }
}

class Settlement(override val owner: Player) : Building {

    companion object {
        val cost: ResourceMap = resourceMapOf(
            Resource.Clay to 1,
            Resource.Lumber to 1,
            Resource.Grain to 1,
            Resource.Wool to 1
        )
        const val winningPoints = 1
    }

    override val cost = Settlement.cost

    override fun resourceYield(tileResource: Resource): ResourceMap =
        resourceMapOf(tileResource to 1)

    override fun yieldsWinningPoints(): Int = winningPoints

    override fun toString(): String {
        return "Settlement(owner=$owner)"
    }
}
