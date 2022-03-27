package com.jale.katan.logic

interface ResourceMap : Map<Resource, Int>, Cloneable {
    override fun get(key: Resource): Int
    public override fun clone(): ResourceMap
    infix operator fun plus(resource: Pair<Resource, Int>): ResourceMap
    infix operator fun plus(other: ResourceMap): ResourceMap
    infix operator fun minus(resource: Pair<Resource, Int>): ResourceMap
    infix operator fun minus(other: ResourceMap): ResourceMap
}


fun resourceMapOf(vararg pairs: Pair<Resource, Int>): ResourceMap {
    return mutableResourceMapOf(*pairs)
}

fun mutableResourceMapOf(vararg pairs: Pair<Resource, Int>): MutableResourceMap {
    val map = MutableResourceMap()
    pairs.forEach { map += it }
    return map
}

class MutableResourceMap(
    // EnumMap would be preferred but resource has to be extensible.
    private val mapImpl: MutableMap<Resource, Int> =
        Resource.values().associateWith { 0 }.toMutableMap()
) : MutableMap<Resource, Int> by mapImpl, ResourceMap {

    constructor(resourceMap: ResourceMap) : this() {
        this += resourceMap
    }

    override operator fun get(key: Resource): Int = mapImpl.getValue(key)

    // Minus and Plus
    override infix operator fun minus(resource: Pair<Resource, Int>): MutableResourceMap =
        clone().apply { this -= resource }

    infix operator fun minusAssign(resource: Pair<Resource, Int>) {
        require(resource.second >= 0 && (this[resource.first] - resource.second) >= 0)
        mapImpl[resource.first] = this[resource.first] - resource.second
    }

    override infix operator fun plus(resource: Pair<Resource, Int>): MutableResourceMap =
        clone().apply { this += resource }

    infix operator fun plusAssign(resource: Pair<Resource, Int>) {
        require(resource.second >= 0)
        mapImpl[resource.first] = this[resource.first] + resource.second
    }

    override infix operator fun minus(other: ResourceMap): MutableResourceMap =
        clone().apply { this -= other }

    infix operator fun minusAssign(otherMap: ResourceMap) {
        otherMap.forEach { (r, v) ->
            this -= r to v
        }
    }

    override infix operator fun plus(other: ResourceMap): MutableResourceMap =
        clone().apply { this += other }

    infix operator fun plusAssign(otherMap: ResourceMap) {
        otherMap.forEach { (r, v) ->
            this += (r to v)
        }
    }

    // Util functions
    override fun toString(): String {
        return "{" +
                mapImpl.entries.joinToString { (k, v) -> "$k : $v" } +
                "}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MutableResourceMap
        return Resource.values().all { this[it] == other[it] }
    }

    override fun hashCode(): Int {
        return mapImpl.hashCode()
    }

    override fun clone(): MutableResourceMap {
        return MutableResourceMap(this as ResourceMap)
    }
}