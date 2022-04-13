package com.jale.katan.util

import kotlin.random.Random

class Dice(seed: Long? = null) {

    private val random: Random? = if (seed != null) Random(seed) else null

    fun roll() = (random ?: Random).nextInt(1, 6)

}
