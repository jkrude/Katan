package com.jale.katan.logic

class Road(override val owner: Player) : Possessable, Purchasable {

    companion object {
        val cost: ResourceMap = resourceMapOf(Resource.Clay to 1, Resource.Lumber to 1)
    }

    override val cost = Road.cost

    override fun toString(): String {
        return "Road(owner=$owner)"
    }

}