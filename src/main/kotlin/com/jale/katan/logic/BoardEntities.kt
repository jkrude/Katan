package com.jale.katan.logic

open class Tile(val id: Int, val nodes: List<Node>) {
    init {
        require(nodes.size == 6)
        require(id > 0)
    }
}

class ResourceTile(id: Int, val resource: Resource, val diceNumber: Int, nodes: List<Node>) : Tile(id, nodes) {
    init {
        require(diceNumber in 2..12)
    }
}

abstract class Node(val id: Int, val withHarbour: Harbour? = null) {

    abstract val edges: List<Edge>
    var building: Building? = null

    fun hasBuilding() = building != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Node
        return (id == other.id)
    }

    override fun hashCode(): Int {
        return id
    }
}

abstract class Edge(var road: Road? = null) {
    abstract val end1: Node
    abstract val end2: Node

    fun otherEndTo(node: Node) =
        when (node) {
            end1 -> end2
            end2 -> end1
            else -> throw IllegalArgumentException("Node $node is at neither end of the edge.")
        }

    fun hasRoad() = road != null
}

sealed class Harbour() {

    object GenericHarbour : Harbour()
    class SpecificHarbour(val type: Resource) : Harbour()
}