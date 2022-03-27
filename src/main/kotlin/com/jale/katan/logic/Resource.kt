package com.jale.katan.logic

// Resource should be extensibility and therefore can't be an enum
sealed class Resource {
    object Clay : Resource()
    object Lumber : Resource()
    object Ore : Resource()
    object Grain : Resource()
    object Wool : Resource()

    companion object {
        fun values(): Array<Resource> {
            return arrayOf(Clay, Lumber, Ore, Grain, Wool)
        }

        fun valueOf(value: String): Resource {
            return when (value) {
                "Clay" -> Clay
                "Lumber" -> Lumber
                "Ore" -> Ore
                "Grain" -> Grain
                "Wool" -> Wool
                else -> throw IllegalArgumentException("No object com.jale.katan.logic.Resource.$value")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return this.javaClass == other?.javaClass
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String = this.javaClass.simpleName

}