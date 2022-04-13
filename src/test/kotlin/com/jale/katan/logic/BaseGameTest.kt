package com.jale.katan.logic

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BaseGameTest {

    lateinit var board: Board
    lateinit var game: BaseGame


    @BeforeEach
    internal fun setUp() {
        // dice0 rolls 5, 4, 3
        // dice1 rolls 1, 3, 1
        game = defaultGame(0, 16)
        board = game.board
        game.nextTurn(game.getTile(15), game.getPlayer(3))
        /*
         * Player 0 (red) Clay: 1, Lumber: 2; Grain: 1
         * Player 1 (orange) Grain: 3; Ore: 1
         * Player 2 (white) Clay: 2; Lumber: 1; Grain: 1;
         * Player 3 (blue) Clay: 1; Lumber 1; Ore 1
         * currentPlayer = Player 0
         * next dice rolls: 7, 4
         */
    }

    @Test
    fun hasWon() {
        assertThrows<IllegalArgumentException> {
            game.hasWon(Player(6))
        }

        game.players.forEach {
            assertFalse(game.hasWon(it))
        }
        // TODO should not be possible (?)
        game.currentPlayer.winningPoints = game.requiredPointsToWin

        assertTrue(
            game.hasWon(game.currentPlayer)
        )
    }

    @Test
    fun nextTurnNormal() {
        // Completely clean setup.
        game = defaultGame(0, 16)
        board = game.board

        val playerResPre = game.players.map { it.resources.clone() }
        val expected: List<ResourceMap> = playerResPre.mapIndexed { idx, res ->
            when (idx) {
                // player 0 (red) and 2 (white) get one clay
                0, 2 -> res + (Resource.Clay to 1)
                // player 1 (orange) gets one grain
                1 -> res + (Resource.Grain to 1)
                else -> res
            }
        }
        val previousPlayer = game.currentPlayer
        game.nextTurn(game.getTile(15), game.getPlayer(3))
        for ((playerID, expectedResources) in expected.withIndex()) {
            assertSameResources(
                expectedResources, game.getPlayer(playerID).resources,
                message = "Player ${game.getPlayer(playerID)} had wrong amount of resources."
            )
        }
        assertEquals(1, game.turn, "Next turn should increment current turn.")
        assertNotEquals(previousPlayer, game.currentPlayer, "Next turn -> next player.")
    }

    @Test
    fun nextTurnRobber() {
        // Completely clean setup.
        game = defaultGame(0, 16)
        board = game.board

        game.nextTurn(game.getTile(15), game.getPlayer(3))
        game.endTurn()
        val playerResPre = game.players.associate { it.id to it.resources.clone() }
        game.nextTurn(game.getTile(15), game.getPlayer(3))
        val currPlayer = game.currentPlayer
        // current player stole a resource
        val stolen: Map.Entry<Resource, Int>? = currPlayer.resources.entries.find { (res, amt) ->
            playerResPre[currPlayer.id]!![res] == amt - 1
        }
        assertNotNull(stolen)
        val stolenResource = stolen!!.key
        // There was nothing else stolen
        currPlayer.resources.forEach { (res, amt) ->
            if (res != stolenResource) assertEquals(playerResPre[currPlayer.id]!![res], amt)
            else assertEquals(playerResPre[currPlayer.id]!![res] + 1, amt)
        }
        // Resource was stolen from the right person
        assertEquals((playerResPre[3]!![stolenResource] - 1), game.getPlayer(3).resources[stolenResource])
        // robber was moved
        assertEquals(game.getTile(15), game.board.robberPosition)
        // Nothing else changed
        game.players.forEach { p ->
            p.resources.forEach { (res, amt) ->
                if (res != stolenResource
                    || (p != currPlayer && p != game.getPlayer(3))
                ) assertEquals(playerResPre[p.id]!![res], amt)
            }
        }
    }

    // BUILD ROAD

    @Test
    fun buildRoadValid() {
        val expectedResources: ResourceMap = game.currentPlayer.resources.clone()
        val expectedAvailableRoads: Int = game.currentPlayer.availableRoads - 1
        game.currentPlayer.addResources(Road.cost)
        game.buildRoad(game.getEdge(24, 53))
        // The costs of a road were subtracted.
        assertSameResources(expectedResources, game.currentPlayer.resources)
        assertTrue(game.getEdge(24, 53).hasRoad()) // The road was placed.
        assertEquals(game.currentPlayer, game.getEdge(24, 53).road?.owner) // Right owner.
        assertEquals(expectedAvailableRoads, game.currentPlayer.availableRoads)
    }

    @Test
    fun buildRoadInvalidEdge() {
        // Test unreachable edge.
        val expected = game.currentPlayer.resources
        val expectedRoads = game.currentPlayer.availableRoads
        assertThrows<InvalidPlay> {
            game.buildRoad(game.getEdge(11, 12)) // Edge not reachable for red.
        }
        assertSameResources(
            expected,
            game.currentPlayer.resources,
            message = "Resources should not be altered if method fails."
        )
        assertFalse(game.getEdge(22, 23).hasRoad())
        assertEquals(expectedRoads, game.currentPlayer.availableRoads)

        game.endTurn()
        game.nextTurn(game.getTile(15), game.getPlayer(3))
    }

    @Test
    fun buildRoadInvalidResources() {
        // Try to build without enough resources.
        game.endTurn()
        game.nextTurn(game.getTile(15), game.getPlayer(3))
        assert(!game.currentPlayer.hasEnoughResources(Road.cost))
        assertResourcesNotChanging {
            assertBuildRoadDoesntChange(location = 22 to 23) {
                assertThrows<InvalidPlay> {
                    game.buildRoad(game.getEdge(22, 23))
                }
            }
        }
    }

    @Test
    fun buildRoadInvalidNumberOfRoads() {
        // TODO This should not be possible
        assert(game.currentPlayer.hasEnoughResources(Road.cost))
        game.currentPlayer.availableRoads = 0
        val expectedResources = game.currentPlayer.resources
        assertThrows<InvalidPlay> {
            game.buildRoad(game.getEdge(24, 53))
        }
        assertSameResources(expectedResources, game.currentPlayer.resources)
        assertFalse(game.getEdge(22, 23).hasRoad())
        assertEquals(0, game.currentPlayer.availableRoads)
    }

    @Test
    fun buildRoadInvalidRoadExists() {
        game.buildRoad(game.getEdge(24, 53))
        game.currentPlayer.addResources(Road.cost)
        assert(game.getEdge(24, 53).hasRoad())
        val existingRoad = game.getEdge(24, 53).road!!
        val expectedResources = game.currentPlayer.resources
        val expectedRoads = game.currentPlayer.availableRoads
        assertThrows<InvalidPlay> {
            game.buildRoad(game.getEdge(24, 53))
        }
        assertSameResources(expectedResources, game.currentPlayer.resources)
        assertTrue(existingRoad === game.getEdge(24, 53).road)
        assertEquals(expectedRoads, game.currentPlayer.availableRoads)
    }

    // Build Settlement

    @Test
    fun buildSettlementValid() {
        game.currentPlayer.addResources(Road.cost)
        game.buildRoad(game.getEdge(24, 53))
        val expectedResources: ResourceMap = game.currentPlayer.resources.clone()
        val expectedAvailableSettlements: Int = game.currentPlayer.availableSettlements - 1

        game.currentPlayer.addResources(Settlement.cost)
        assertResourcesNotChanging(except = listOf(game.currentPlayer)) {
            game.buildSettlement(game.getNode(53))
        }

        assertSameResources(expectedResources, game.currentPlayer.resources)
        assertTrue(game.getNode(53).building is Settlement)
        assertSame(game.currentPlayer, game.getNode(53).building?.owner)
        assertEquals(expectedAvailableSettlements, game.currentPlayer.availableSettlements)
    }

    @Test
    fun buildSettlementInvalidResources() {

    }

    @Test
    fun buildSettlementInvalidSettlementsLeft() {

    }

    @Test
    fun buildSettlementInvalidLocation() {

    }

    @Test
    fun buildSettlementInvalidDistanceRule() {

    }

    @Test
    fun buildCity() {
        game.currentPlayer.addResources(Road.cost)
        game.currentPlayer.addResources(Settlement.cost)
        game.buildRoad(game.getEdge(24, 53))
        game.buildSettlement(game.getNode(53))

        val expectedResources: ResourceMap = game.currentPlayer.resources.clone()
        val expectedAvailableCities: Int = game.currentPlayer.availableCities - 1
        game.currentPlayer.addResources(City.cost)

        game.buildCity(game.getNode(53))

        assertSameResources(expectedResources, game.currentPlayer.resources)
        assertEquals(expectedAvailableCities, game.currentPlayer.availableCities)
        assertTrue(game.getNode(53).building is City)
        assertSame(game.currentPlayer, game.getNode(53).building?.owner)
    }

    @Test
    fun buyDevelopmentCard() {
    }

    @Test
    fun playDevelopmentMonopoly() {
    }

    @Test
    fun playDevelopmentYearOfPlenty() {
    }

    @Test
    fun playDevelopmentRoadBuilding() {
    }

    @Test
    fun playDevelopmentKnight() {
    }

    @Test
    fun tradeGenericHarbour() {
    }

    @Test
    fun tradeSpecialHarbour() {
    }

    @Test
    fun tradeBasic() {
    }

    private fun assertResourcesNotChanging(except: Iterable<Player>? = null, gameFunction: (() -> Any?)) {
        val resourcesPre = game.players.map { it.resources.clone() }
        gameFunction()
        for ((idx, player) in game.players.withIndex()) {
            if (except != null && player in except) continue
            assertSameResources(resourcesPre[idx], player.resources)
        }
    }

    private fun assertBuildRoadDoesntChange(location: Pair<NodeID, NodeID>, gameFunction: () -> Any?) {
        val expectedRoads = game.currentPlayer.availableRoads
        val existingRoad: Road? = game.getEdge(location.first, location.second).road
        gameFunction()
        assertEquals(expectedRoads, game.currentPlayer.availableRoads)
        assertEquals(existingRoad, game.getEdge(location.first, location.second).road)
    }
}