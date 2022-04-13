package com.jale.katan.logic

import com.jale.katan.logic.Resource.*
import com.jale.katan.util.Dice
import kotlin.math.floor

typealias NodeID = Int
typealias EdgeByID = Pair<NodeID, NodeID>
typealias TileID = Int
typealias PlayerID = Int

fun defaultGame(seed1: Long? = null, seed2: Long? = null): BaseGame {
    val board = defaultBoard()
    val startingConditions = StartingConditions(
        settlements = listOf(
            0 to 13, // red
            0 to 83,
            1 to 34, // orange
            1 to 143,
            2 to 43, // white
            2 to 113,
            3 to 133,  // blue
            3 to 153,
        ),
        cities = emptyList(), // no cities
        roads = listOf(
            0 to (13 to 24),
            0 to (83 to 94),
            1 to (23 to 34),
            1 to (143 to 154),
            2 to (43 to 44),
            2 to (113 to 74),
            3 to (153 to 114),
            3 to (133 to 144)
        ),
        resources = mapOf(
            0 to resourceMapOf(Grain to 1, Lumber to 2),
            1 to resourceMapOf(Ore to 1, Grain to 2),
            2 to resourceMapOf(Grain to 1, Lumber to 1, Clay to 1),
            3 to resourceMapOf(Lumber to 1, Ore to 1, Clay to 1)
        )
    )
    return BaseGame(
        board,
        (0..3).map { Player(it) },
        startingConditions,
        seed1,
        seed2
    )
}

class StartingConditions(
    val settlements: List<Pair<PlayerID, NodeID>>,
    val cities: List<Pair<PlayerID, NodeID>>,
    val roads: List<Pair<PlayerID, EdgeByID>>,
    val resources: Map<PlayerID, ResourceMap>
)

open class BaseGame(
    val board: Board,
    val players: List<Player>,
    startingConditions: StartingConditions? = null,
    diceSeed1: Long? = null,
    diceSeed2: Long? = null
) : CatanGame {

    val turn get() = _turn
    private var _turn = 0
    private var lastTurnEnded = true
    private var playerIdx: Int = players.lastIndex
    override val currentPlayer: Player get() = players[playerIdx]
    private val playerIDs = players.map { it.id }.toSet()
    private val dice1 = Dice(diceSeed1)
    private val dice2 = Dice(diceSeed2)

    // Config
    open val robberResourceLimit = 7
    open val moveRobberOnRolledDice = 7
    open val requiredPointsToWin = 10

    init {
        startingConditions?.let { setStartPositions(it) }
    }

    // Get by ID
    fun getPlayer(id: PlayerID): Player {
        val p = players.find { it.id == id }
        require(p != null)
        return p
    }

    fun getTile(id: TileID): Tile {
        require(board.hasTile(id))
        return board.getTileOrNull(id)!!
    }

    fun getNode(id: NodeID): Node {
        val n = board.nodes[id]
        require(n != null)
        return n
    }

    fun getEdge(id1: NodeID, id2: NodeID): Edge {
        val e = board.getEdgeOrNull(id1 to id2)
        require(e != null)
        return e
    }

    // Turn based functions

    override fun endTurn() {
        val haveWon = players.filter(this::hasWon)
        if (haveWon.isNotEmpty()) {
            //TODO "End game"
            println("Player has/have won $haveWon")
        }
    }

    override fun hasWon(player: Player): Boolean {
        require(player in players)
        return player == this.currentPlayer && player.winningPoints >= requiredPointsToWin
    }

    override fun nextTurn(putRobberOn: Tile, robbResourcesFrom: Player?) {
        if (!lastTurnEnded) {
            throw IllegalStateException("Last turn has to be ended.")
        }
        this._turn++
        this.playerIdx = (playerIdx + 1) % players.size
        val result = this.dice1.roll() + this.dice2.roll()

        var possibleException: InvalidPlay? = null
        if (result == moveRobberOnRolledDice) {
            capResourcesOnRobber()
            try {
                requireValidTile(putRobberOn)
                if (robbResourcesFrom != null) requireBuildingAtTile(putRobberOn, robbResourcesFrom)
                moveRobber(putRobberOn, robbResourcesFrom)
            } catch (ex: InvalidPlay) {
                // delay exception
                possibleException = ex
            }
        }

        distributeNewResources(result)
        if (possibleException != null) throw possibleException
    }

    protected fun distributeNewResources(totalDiceResult: Int) {
        // Collect all tiles with thrown number as { resource : List of adjacent nodes }
        val relevantTiles: Map<Resource, List<Node>> = board.resourceTiles.values
            .filter { it.diceNumber == totalDiceResult }
            .associateWith { it.nodes }
            .mapKeys { (resourceTile, _) -> resourceTile.resource }
        // Every player gets the extra resource for every building for every tile
        relevantTiles
            .mapValues { (_, nodeList) -> nodeList.mapNotNull { it.building } }
            .forEach { (resource, listOfBuildings) ->
                listOfBuildings.forEach { building ->
                    building.owner.addResources(building.resourceYield(resource))
                }
            }
    }

    protected fun capResourcesOnRobber() {
        for (player in players) {
            val resources = player.resources
            if (resources.values.sum() > robberResourceLimit) {
                val toBeDiscarded = floor(resources.values.sum() / 2.0).toInt()
                repeat(toBeDiscarded) {
                    val discard = resources.entries.filter { it.value > 0 }.random()
                    player.removeResource(discard.key to 1)
                }
            }
        }
    }

    protected fun moveRobber(putRobberOn: Tile, robbResourcesFrom: Player?) {
        this.board.placeRobber(putRobberOn)
        if (robbResourcesFrom == null) return
        val possibleResources = robbResourcesFrom.resources.filter { it.value > 0 }
        if (possibleResources.isEmpty()) return
        val randomResource: Resource = possibleResources.keys.random()
        robbResourcesFrom.removeResource(randomResource to 1)
        this.currentPlayer.addResource(randomResource to 1)
    }

    private fun setStartPositions(conditions: StartingConditions) {
        require(_turn == 0)
        require(conditions.settlements.map { it.second }.all { it in board.nodes })
        require(conditions.cities.map { it.second }.all { it in board.nodes })
        require(conditions.roads.map { it.second }.all { board.getEdgeOrNull(it) != null })
        require(conditions.settlements.map { it.second }
            .none { settlement -> settlement in conditions.cities.map { it.second } })
        conditions.resources.keys.forEach { requireValidPlayer(it) }

        val cities = conditions.cities.map { (k, v) -> getPlayer(k) to getNode(v) }
        val settlements = conditions.settlements.map { (k, v) -> getPlayer(k) to getNode(v) }
        val roads = conditions.roads.map { (k, v) -> getPlayer(k) to getEdge(v.first, v.second) }
        settlements.forEach { (player, node) ->
            requireDistanceRule(node)
            requireNodeEmpty(node)
            placeBuilding(node, Settlement(player))
        }
        cities.forEach { (player, node) ->
            requireDistanceRule(node)
            requireNodeEmpty(node)
            placeBuilding(node, City(player))
        }
        roads.forEach { (player, edge) ->
            requireEdgeEmpty(edge)
            requireBuildingOrRoadAtEdgeEnd(edge, player)
            placeRoad(edge, Road(player))
        }
        conditions.resources.forEach { (playerID, resources) ->
            getPlayer(playerID).addResources(resources)
        }
    }


    // Buy and Build

    private fun buyOrThrow(purchasable: Purchasable, player: Player = currentPlayer) {
        requireResources(purchasable.cost, player)
        player.removeResources(purchasable.cost)
    }

    private fun placeBuilding(location: Node, building: Building) {
        val oldBuilding = location.building
        if (oldBuilding != null) oldBuilding.owner.winningPoints -= oldBuilding.yieldsWinningPoints()
        location.building = building
        building.owner.winningPoints += building.yieldsWinningPoints()
    }

    private fun buildBuilding(location: Node, building: Building, allowOld: Boolean = false) {
        requireDistanceRule(location)
        if (!allowOld) requireNodeEmpty(location)
        buyOrThrow(building)
        placeBuilding(location, building)
    }

    private fun placeRoad(location: Edge, road: Road) {
        location.road = road
        // TODO test for longest road
    }

    override fun buildRoad(location: Edge) {
        requireEnoughRoads()
        requireEdgeEmpty(location)
        requireBuildingOrRoadAtEdgeEnd(location)
        val road = Road(currentPlayer)
        buyOrThrow(road)
        placeRoad(location, road)
        currentPlayer.availableRoads--
    }

    override fun buildSettlement(location: Node) {
        requireEnoughSettlements()
        requireRoadToNode(location)
        buildBuilding(location, Settlement(currentPlayer))
        currentPlayer.availableSettlements--
    }

    override fun buildCity(location: Node) {
        requireEnoughCities()
        requireSettlementAtNode(location)
        buildBuilding(location, City(currentPlayer), allowOld = true)
        currentPlayer.availableSettlements++
        currentPlayer.availableCities--
    }

    // Development cards


    override fun buyDevelopmentCard() {
        //TODO generate random event card
        val eventCard = DevelopmentCard.Knight
        buyOrThrow(eventCard)
        currentPlayer.hiddenDevelopmentCards.add(
            OwnedDevelopmentCard(currentPlayer, _turn, eventCard)
        )
    }

    private fun playCardOrThrow(eventCard: DevelopmentCard) {
        val card =
            if (eventCard is DevelopmentCard.VictoryPoint) {
                requireOwnsCard(eventCard).also { currentPlayer.winningPoints += 1 }
            } else {
                requireOwnsAndBoughtBefore(eventCard)
            }
        currentPlayer.hiddenDevelopmentCards.remove(card)
        if (eventCard.staysAfterPlay) currentPlayer.playedDevelopmentCards.add(card)
    }

    override fun playDevelopmentMonopoly(monopolyOn: Resource) {
        playCardOrThrow(DevelopmentCard.ProgressMonopoly)
        //val snatchedResources: Int
        players
            .filter { it != this.currentPlayer }
            //.also { list -> snatchedResources = list.sumOf { p -> p.resources[monopolyOn] } }
            .forEach { player -> // snatch all resources of this type
                player.removeResource(monopolyOn to player.resources[monopolyOn])
            }
    }

    override fun playDevelopmentYearOfPlenty(r1: Resource, r2: Resource) {
        playCardOrThrow(DevelopmentCard.ProgressYearOfPlenty)
        currentPlayer.addResource(r1 to 1)
        currentPlayer.addResource(r2 to 1)
    }

    override fun playDevelopmentRoadBuilding(location1: Edge, location2: Edge) {
        playCardOrThrow(DevelopmentCard.ProgressRoadBuilding)
        requireEdgeEmpty(location1)
        requireEdgeEmpty(location2)
        location1.road = Road(currentPlayer)
        location2.road = Road(currentPlayer)
    }

    override fun playDevelopmentKnight(tile: Tile, robbResourceFrom: Player?) {
        requireValidTile(tile)
        if (robbResourceFrom != null) requireBuildingAtTile(tile, robbResourceFrom)
        playCardOrThrow(DevelopmentCard.Knight)
        moveRobber(tile, robbResourceFrom)
        // TODO test fo greatest army
    }

    // Tradings


    private fun tradeOrThrow(required: ResourceMap, desired: Resource) {
        requireResources(required)
        this.currentPlayer.removeResources(required)
        this.currentPlayer.addResource(desired to 1)
    }

    override fun tradeGenericHarbour(r1: Resource, r2: Resource, r3: Resource, desired: Resource) {
        requireGenericPort()
        val asMap = resourceMapOf(
            (r1 to 1),
            (r2 to 1),
            (r3 to 1),
        )
        tradeOrThrow(asMap, desired)
    }

    override fun tradeSpecialHarbour(r1: Resource, desired: Resource) {
        requireSpecialPort(r1)
        tradeOrThrow(resourceMapOf(r1 to 2), desired)
    }

    override fun tradeBasic(r1: Resource, r2: Resource, r3: Resource, r4: Resource, desired: Resource) {
        // type of r1...r4 are not necessarily disjoint
        val asMap = resourceMapOf(
            (r1 to 1),
            (r2 to 1),
            (r3 to 1),
            (r4 to 1),
        )
        tradeOrThrow(asMap, desired)
    }

    // Preconditions


    protected fun requireValidPlayer(player: PlayerID): Player {
        if (player !in playerIDs) {
            throw InvalidPlay("Player $player is not part of the game")
        }
        return getPlayer(player)
    }

    protected fun requireResources(resourceMap: ResourceMap, player: Player = currentPlayer) {
        if (!player.hasEnoughResources(resourceMap)) {
            throw InvalidPlay("Player has not enough resources.")
        }
    }

    protected fun requireDistanceRule(location: Node) {
        val conflictingNode: Node? = location.edges
            .map { it.otherEndTo(location) }
            .firstOrNull { it.hasBuilding() }
        if (conflictingNode != null) {
            throw InvalidPlay("Cant place building here. $conflictingNode violates distance rule.")
        }
    }

    private fun requireSettlementAtNode(location: Node, player: Player = currentPlayer): Settlement {
        if (location.building !is Settlement || location.building?.owner != player) {
            throw InvalidPlay("Action requires a settlement at $location")
        }
        return location.building!! as Settlement
    }

    protected fun requireEnoughSettlements(player: Player = currentPlayer) {
        if (player.availableSettlements == 0)
            throw InvalidPlay("Player has not enough settlements")
    }

    protected fun requireEnoughCities(player: Player = currentPlayer) {
        if (player.availableCities == 0)
            throw InvalidPlay("Player has not enough cities")
    }

    protected fun requireEnoughRoads(player: Player = currentPlayer) {
        if (player.availableRoads == 0)
            throw InvalidPlay("Player has not enough roads")
    }

    protected fun requireOwnsCard(
        developmentCard: DevelopmentCard,
        player: Player = currentPlayer
    ): OwnedDevelopmentCard {
        val card = player.hiddenDevelopmentCards.find { it.card == developmentCard }
        if (card == null) throw InvalidPlay("Player does not own this card: $developmentCard")
        else return card
    }

    protected fun requireOwnsAndBoughtBefore(
        developmentCard: DevelopmentCard,
        player: Player = currentPlayer
    ): OwnedDevelopmentCard {
        val card = player.hiddenDevelopmentCards.find { it.card == developmentCard && it.boughtInTurn < _turn }
        if (card == null) throw InvalidPlay("Player has no card of this type: $developmentCard which was bought before this round: $_turn")
        else return card

    }

    protected fun requireValidTile(tile: Tile) {
        if (tile !in this.board) {
            throw InvalidPlay("Tile is not on the board: $tile")
        }
    }

    protected fun requireBuildingAtTile(tile: Tile, player: Player) {
        if (tile.nodes.map { it.building?.owner }.firstOrNull { it == player } == null) {
            throw InvalidPlay("Player cant be targeted (no building at tile).")
        }
    }

    protected fun requireBuildingOrRoadAtEdgeEnd(location: Edge, player: Player = currentPlayer) {
        if (!(location.end2.building?.owner == player
                    || location.end1.building?.owner == player
                    || location.end2.edges.find { it.road?.owner == player } != null
                    || location.end1.edges.find { it.road?.owner == player } != null)
        ) {
            throw InvalidPlay("Player has neither building nor road connected: $location")
        }
    }

    protected fun requireRoadToNode(location: Node, player: Player = currentPlayer) {
        if (location.edges.find { it.road?.owner == player } == null)
            throw InvalidPlay("Player has no connected road: $location")
    }

    protected fun requireEdgeEmpty(edge: Edge) {
        if (edge.hasRoad()) {
            throw InvalidPlay("Edge $edge is not empty: ${edge.road}")
        }
    }

    protected fun requireNodeEmpty(node: Node) {
        if (node.hasBuilding()) {
            throw InvalidPlay("Node $node is not empty: ${node.building}")
        }
    }

    protected fun requireGenericPort(player: Player = currentPlayer): Node {
        val nodeWithHarbour = board.nodes.values.find {
            it.building?.owner == currentPlayer
                    && it.withHarbour is Harbour.GenericHarbour
        }
        if (nodeWithHarbour == null) {
            throw InvalidPlay("Player $player has no settlement at a generic harbours")
        }
        return nodeWithHarbour
    }

    protected fun requireSpecialPort(resource: Resource, player: Player = currentPlayer): Node {
        val nodeWithSpecialPort = board.nodes.values.find {
            it.building?.owner == currentPlayer
                    && it.withHarbour is Harbour.SpecificHarbour
                    && it.withHarbour.type == resource
        }
        if (nodeWithSpecialPort == null) {
            throw InvalidPlay("Player $player has no settlement at a special port of this resource")
        }
        return nodeWithSpecialPort

    }
}