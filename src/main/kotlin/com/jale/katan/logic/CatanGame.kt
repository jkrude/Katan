package com.jale.katan.logic

interface CatanGame {

    /**
     * Used implicitly by most  methods that use the currentPlayer as the one performing the action.
     */
    val currentPlayer: Player

    /**
     * Tests if any player has won and executes end turn specific logic.
     */
    fun endTurn()

    /**
     * @throws IllegalArgumentException if player is not playing
     * @return if pointsToWin is reached or more specific criteria was met
     */
    fun hasWon(player: Player): Boolean

    /**
     * @param robbResourcesFrom if not null => the player from witch resources should be stolen.
     * @throws IllegalStateException if last turn was not ended
     * @throws InvalidPlay if
     * seven is thrown AND (
     *   tile does not exist
     *  OR player is has no building at this tile).
     * onThrow: [InvalidPlay] Robber stays, game progresses with new turn
     * onThrow: [IllegalStateException] Nothing changes
     */
    fun nextTurn(putRobberOn: Tile, robbResourcesFrom: Player?)

    /**
     * @throws InvalidPlay if
     *  player does not have enough resources
     *  OR has no roads left
     *  OR edge is not empty
     *  OR no (building or road) on at least one end
     * onThrow: Game state is not affected.
     */
    fun buildRoad(location: Edge)

    /**
     * @throws InvalidPlay if
     *  player does not have enough resources
     *  OR has no settlements left
     *  OR has no road leading to location
     *  OR node is not empty
     *  OR distance rule is not fulfilled
     * onThrow: game state is not affected.
     */
    fun buildSettlement(location: Node)

    /**
     * @throws InvalidPlay
     *  player does not have enough resources
     *  OR has no cities left
     *  OR node has no settlement owned by the player
     *  Implicitly has the same conditions as a settlement.
     * onThrow: game state is not affected.
     */
    fun buildCity(location: Node)

    /**
     * @throws InvalidPlay if
     *  player has not enough resources
     *  OR no development-card is left
     * onThrow: game state is not affected
     */
    fun buyDevelopmentCard()

    /**
     * Steal all resources of this type and give them to the current player
     * @throws InvalidPlay
     *  player does not own this card
     *  OR the card was bought this turn
     *  OR the player already played a development-card this turn
     * onThrow: game state is not affected
     */
    fun playDevelopmentMonopoly(monopolyOn: Resource)

    /**
     * Give the current player one resource of both types
     * @throws InvalidPlay if
     *  player does  not own this card
     *  OR the card was bought this turn
     *  OR the player already played a development-card this turn
     * onThrow: game state is not affected
     */
    fun playDevelopmentYearOfPlenty(r1: Resource, r2: Resource)

    /**
     * The current player gets two roads for free at the given locations.
     * @throws InvalidPlay if
     *  player does not own this card
     *  OR the card was bought this turn
     *  OR the player already played a development-card this turn
     *  OR edges are not on the board
     *  OR any edge is not empty
     * onThrow: game state is not affected
     */
    fun playDevelopmentRoadBuilding(location1: Edge, location2: Edge)

    /**
     * Move the robber to a different field.
     * If a player is provided a random resource will be stolen from this player.
     * @throws InvalidPlay if
     *  player does not own this card
     *  OR the card was bought this turn
     *  OR the player already played a development-card this turn
     *  OR tile not on the board
     *  OR tile is the same position the robber is on
     *  OR robbResourceFrom != null AND robbResourceFrom has no building at given tile
     * onThrow: game state is not affected
     */
    fun playDevelopmentKnight(tile: Tile, robbResourceFrom: Player?)

    /**
     * The player can trade three resources for one (with the bank).
     * @throws InvalidPlay if
     *  player has no building at a generic port
     *  OR player has not enough resources
     * onThrow: game state is not affected
     */
    fun tradeGenericHarbour(r1: Resource, r2: Resource, r3: Resource, desired: Resource)

    /**
     * The player can trade two of the same resources for one (with the bank).
     * @throws InvalidPlay if
     *  Player has no building at a port of this type
     *  OR player has not enough resources
     * onThrow: game state is not affected
     */
    fun tradeSpecialHarbour(r1: Resource, desired: Resource)

    /**
     * The player can always trade four to one (with the bank) .
     * requires: Player has enough resources
     * @throws InvalidPlay
     * onThrow: game state is not affected
     */
    fun tradeBasic(r1: Resource, r2: Resource, r3: Resource, r4: Resource, desired: Resource)
}