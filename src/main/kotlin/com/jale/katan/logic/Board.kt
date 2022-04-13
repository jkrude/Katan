package com.jale.katan.logic


class Board(
    tileStructure: List<BoardBuilder.TempTile>,
    harbours: Map<Int, Harbour>,
    initialRobberPosition: Int
) {

    val emptyTiles: Map<Int, Tile>
    val resourceTiles: Map<Int, ResourceTile>

    private var _robberPosition: Tile
    val robberPosition: Tile get() = _robberPosition

    val nodes: Map<Int, Node>

    val edges: Map<Pair<Int, Int>, Edge>

    // validation
    init {
        require(tileStructure.map { it.id }.toSet().size == tileStructure.size) {
            "All Tile IDs should be unique."
        }
        require(tileStructure.find { it.id == initialRobberPosition } != null) {
            "Initial position for robber was not on a valid tile id."
        }
        val nodes = tileStructure.flatMap { it.nodes }
        //TODO NodeID can only be the same if tiles are adjacent

        val edges = tileStructure.flatMap { it.edges }
        require(edges.all { it.first in nodes && it.second in nodes }) {
            "At least one edge pointed to an invalid node id."
        }
    }

    // instantiation
    init {
        this.edges = tileStructure
            .flatMap { it.edges }
            .map { setOf(it.first, it.second) }
            .toSet()
            .map { BoardEdge(it.first(), it.last()) }
            .associateBy { it.end1Id to it.end2Id }

        this.nodes = tileStructure
            .flatMap { it.nodes }
            .toSet()
            .associateWith {
                BoardNode(it, harbours[it])
            }

        resourceTiles = tileStructure
            .filter { it.resource != null }
            .map { tempTile ->
                ResourceTile(
                    tempTile.id,
                    tempTile.resource!!,
                    tempTile.diceNumber,
                    tempTile.nodes.map { this.nodes[it]!! }
                )
            }.associateBy { it.id }
        emptyTiles = tileStructure
            .filter { it.resource == null }
            .map { tempTile ->
                Tile(tempTile.id,
                    tempTile.nodes.map { this.nodes[it]!! })
            }.associateBy { it.id }

        this._robberPosition =
            emptyTiles[initialRobberPosition] ?: resourceTiles[initialRobberPosition]!!

    }

    fun getEdgeOrNull(fromTo: Pair<Int, Int>): Edge? {
        val (id1, id2) = fromTo
        return (edges[id1 to id2] ?: edges[id2 to id1])
    }

    fun getEdge(fromTo: Pair<Int, Int>): Edge = getEdgeOrNull(fromTo)!!

    fun placeRobber(tile: Tile) {
        require(tile in this)
        _robberPosition = tile
    }

    fun hasTile(tileID: Int) = tileID in this.emptyTiles || tileID in this.resourceTiles

    fun getTileOrNull(tileID: Int) = emptyTiles[tileID] ?: resourceTiles[tileID]

    operator fun contains(tile: Tile): Boolean = hasTile(tile.id)


    protected inner class BoardEdge(
        internal val end1Id: Int,
        internal val end2Id: Int
    ) : Edge() {

        override val end1: Node get() = nodes[this.end1Id]!!
        override val end2: Node get() = nodes[this.end2Id]!!

        override fun toString(): String =
            if (hasRoad()) "Edge($end1Id <- $road -> $end2Id)"
            else "Edge($end1Id <-> $end2Id)"
    }

    protected inner class BoardNode(id: Int, harbour: Harbour? = null) : Node(id, harbour) {

        override val edges = this@Board.edges
            .filter { (key, _) -> key.first == this.id || key.second == this.id }
            .map { it.value }

        override fun toString(): String {
            return "BoardNode(" +
                    "id=$id," +
                    if (withHarbour != null) "harbour=$withHarbour" else "" +
                            if (hasBuilding()) "building=$building" else "" +
                                    " edges=$edges)"
        }


    }

}


class BoardBuilder() {

    enum class PositionInRow {
        First,
        Mid,
        Last
    }

    class TempTile(
        val id: Int,
        val diceNumber: Int,
        val resource: Resource?,
        val nodes: List<Int>
    ) {
        val edges: List<Pair<Int, Int>> = nodes.windowed(2).map { (from, to) -> from to to }
    }
}


fun defaultBoard(): Board {
    // top left to bottom right
    val tiles = listOf(
        BoardBuilder.TempTile(
            id = 1,
            diceNumber = 10,
            resource = Resource.Ore,
            nodes = (11..16).toList()
        ),
        BoardBuilder.TempTile(
            id = 2,
            diceNumber = 2,
            resource = Resource.Wool,
            nodes = (21..24).toList() + 13 + 12,
        ),
        BoardBuilder.TempTile(
            id = 3,
            diceNumber = 9,
            resource = Resource.Lumber,
            nodes = (31..34).toList() + 23 + 22,
        ),

        BoardBuilder.TempTile(
            id = 4,
            diceNumber = 12,
            resource = Resource.Grain,
            nodes = listOf(15, 14, 43, 44, 45, 46)
        ),
        BoardBuilder.TempTile(
            id = 5,
            diceNumber = 6,
            resource = Resource.Clay,
            nodes = listOf(13, 24, 53, 54, 43, 14)
        ),
        BoardBuilder.TempTile(
            id = 6,
            diceNumber = 4,
            resource = Resource.Wool,
            nodes = listOf(23, 34, 63, 64, 54, 24)
        ),
        BoardBuilder.TempTile(
            id = 7,
            diceNumber = 10,
            resource = Resource.Clay,
            nodes = listOf(33, 72, 73, 74, 63, 34)
        ),
        // Second row
        BoardBuilder.TempTile(
            id = 8,
            diceNumber = 9,
            resource = Resource.Grain,
            nodes = listOf(45, 44, 83, 84, 85, 86)
        ),
        BoardBuilder.TempTile(
            id = 9,
            diceNumber = 11,
            resource = Resource.Lumber,
            nodes = listOf(43, 54, 93, 94, 83, 44)
        ),
        BoardBuilder.TempTile(
            id = 10,
            diceNumber = -1,
            resource = null,
            nodes = listOf(53, 64, 103, 104, 93, 54)
        ),
        BoardBuilder.TempTile(
            id = 11,
            diceNumber = 3,
            resource = Resource.Lumber,
            nodes = listOf(63, 74, 113, 114, 103, 64)
        ),
        BoardBuilder.TempTile(
            id = 12,
            diceNumber = 12,
            resource = Resource.Ore,
            nodes = listOf(73, 122, 123, 124, 113, 74)
        ),
        // Third row
        BoardBuilder.TempTile(
            id = 13,
            diceNumber = 8,
            resource = Resource.Lumber,
            nodes = listOf(83, 94, 133, 134, 135, 84)
        ),
        BoardBuilder.TempTile(
            id = 14,
            diceNumber = 3,
            resource = Resource.Ore,
            nodes = listOf(93, 104, 143, 144, 133, 94)
        ),
        BoardBuilder.TempTile(
            id = 15,
            diceNumber = 4,
            resource = Resource.Grain,
            nodes = listOf(103, 114, 153, 154, 143, 104)
        ),
        BoardBuilder.TempTile(
            id = 16,
            diceNumber = 5,
            resource = Resource.Wool,
            nodes = listOf(113, 124, 163, 164, 165, 113)
        ),
        // FourthRow
        BoardBuilder.TempTile(
            id = 17,
            diceNumber = 5,
            resource = Resource.Clay,
            nodes = listOf(133, 144, 173, 174, 175, 134)
        ),
        BoardBuilder.TempTile(
            id = 18,
            diceNumber = 6,
            resource = Resource.Grain,
            nodes = listOf(143, 154, 183, 184, 173, 144)
        ),
        BoardBuilder.TempTile(
            id = 19,
            diceNumber = 11,
            resource = Resource.Wool,
            nodes = listOf(153, 164, 193, 194, 184, 154)
        ),

        )


    val harbours: Map<Int, Harbour> = mapOf(
        101 to Harbour.GenericHarbour,
        106 to Harbour.GenericHarbour,
        21 to Harbour.SpecificHarbour(Resource.Grain),
        22 to Harbour.SpecificHarbour(Resource.Grain),
        33 to Harbour.SpecificHarbour(Resource.Ore),
        102 to Harbour.SpecificHarbour(Resource.Ore),
        122 to Harbour.GenericHarbour,
        123 to Harbour.GenericHarbour,
        163 to Harbour.SpecificHarbour(Resource.Wool),
        164 to Harbour.SpecificHarbour(Resource.Wool),
        183 to Harbour.GenericHarbour,
        184 to Harbour.GenericHarbour,
        174 to Harbour.GenericHarbour,
        175 to Harbour.GenericHarbour,
        135 to Harbour.SpecificHarbour(Resource.Clay),
        84 to Harbour.SpecificHarbour(Resource.Clay),
        45 to Harbour.SpecificHarbour(Resource.Lumber),
        46 to Harbour.SpecificHarbour(Resource.Lumber)
    )

    return Board(tiles, harbours, 10)
}
