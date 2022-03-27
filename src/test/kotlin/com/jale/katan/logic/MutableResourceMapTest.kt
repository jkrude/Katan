package com.jale.katan.logic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MutableResourceMapTest {

    private fun generateTestMap(): MutableResourceMap = mutableResourceMapOf(
        Resource.Wool to 1,
        Resource.Ore to 3,
        Resource.Lumber to 0
    )

    @Test
    internal fun resourceMapOfTest() {
        // only accepts >= 0
        assertThrows<IllegalArgumentException> {
            resourceMapOf(Resource.Wool to -1)
        }
        // builder works
        val m1 = resourceMapOf(Resource.Wool to 1)
        assertEquals(1, m1[Resource.Wool])
        // 0 as default
        val m2 = resourceMapOf()
        assertEquals(0, m2[Resource.Wool])
        // multiple values get added
        val map = resourceMapOf(
            Resource.Wool to 1,
            Resource.Wool to 3,
        )
        assertEquals(4, map[Resource.Wool])
    }

    @Test
    fun testKeys() {
        val m = MutableResourceMap()
        Resource.values().forEach {
            assertTrue(it in m.keys)
        }
    }

    @Test
    fun testEntries() {
        val m = MutableResourceMap().entries
        Resource.values().forEach {
            assertTrue(m.find { (key, value) -> key == it && value == 0 } != null,
                "$it not in map or did not default to 0")
        }
    }

    @Test
    fun testMinusAssign() {
        val map: MutableResourceMap = generateTestMap()
        map -= (Resource.Wool to 1)
        assertEquals(0, map[Resource.Wool])
        // don't accept values which would result in negative results
        assertThrows<IllegalArgumentException> {
            generateTestMap() -= (Resource.Wool to 2)
        }
        // don't accept negative arguments
        assertThrows<IllegalArgumentException> {
            generateTestMap() -= (Resource.Wool to -1)
        }
    }

    @Test
    fun testMinus() {
        val map: ResourceMap = generateTestMap()
        // Minus with pair
        val otherMap: ResourceMap = map - (Resource.Wool to 1)

        assertFalse(map === otherMap)
        assertEquals(map[Resource.Wool] - 1, otherMap[Resource.Wool])

        val map1: ResourceMap = generateTestMap()
        // Minus with map
        val otherMap1: ResourceMap = map1 - generateTestMap()

        assertFalse(map1 === otherMap1)
        otherMap1.values.forEach { amt ->
            assertEquals(amt, 0)
        }
    }

    @Test
    fun testPlusAssign() {
        val map: MutableResourceMap = generateTestMap()
        map += (Resource.Wool to 1)
        assertEquals(2, map[Resource.Wool])
        // don't accept negative arguments
        assertThrows<IllegalArgumentException> {
            generateTestMap() += (Resource.Wool to -1)
        }
    }

    @Test
    fun testPlus() {
        val map: ResourceMap = generateTestMap()
        // Test with pair
        val otherMap = map + (Resource.Wool to 1)

        assertFalse(map === otherMap)
        assertEquals(map[Resource.Wool] + 1, otherMap[Resource.Wool])

        val map1: ResourceMap = generateTestMap()
        // Test with map
        val otherMap1: ResourceMap = map1 + generateTestMap()

        otherMap1.keys.forEach { res ->
            assertEquals(map1[res] * 2, otherMap1[res])
        }
    }

    @Test
    fun testEquals() {
        val m1 = generateTestMap()
        m1 += (Resource.Wool to 1)
        val m2 = generateTestMap()
        m2 += (Resource.Wool to 1)
        assertEquals(m1, m2)
        m2 += (Resource.Wool to 1)
        assertFalse(m1 == m2)
    }

    @Test
    fun testApply() {
        val map = MutableResourceMap().apply { this += (Resource.Clay to 1) }
        assertEquals(1, map[Resource.Clay])
    }

    @Test
    fun testConstructor() {
        val map: ResourceMap = generateTestMap()
        val m1 = MutableResourceMap(map)
        map.forEach { (res, amt) ->
            assertEquals(amt, m1[res], "New map was wrongly initialized.")
        }

    }

    @Test
    fun testClone() {
        val map = generateTestMap()
        val map1 = map.clone()
        assertEquals(map, map1)
        val valuePre = map1[Resource.Clay]
        map1 + (Resource.Clay to 1)
        assertEquals(valuePre, map[Resource.Clay])
    }

}

fun assertSameResources(
    expected: ResourceMap,
    actual: ResourceMap,
    except: Iterable<Resource>? = null,
    message: String? = null
) {
    for (res in Resource.values()) {
        if (except == null || res !in except) {
            assertEquals(
                expected[res], actual[res],
                "$message. Resource $res did not match"
            )
        }
    }
}