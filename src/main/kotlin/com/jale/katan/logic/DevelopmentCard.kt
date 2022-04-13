package com.jale.katan.logic


sealed class DevelopmentCard(val staysAfterPlay: Boolean) : Purchasable {

    companion object {
        val cost = resourceMapOf(
            Resource.Lumber to 1,
            Resource.Clay to 1,
            Resource.Wool to 1
        )
    }

    override val cost = DevelopmentCard.cost

    object Knight : DevelopmentCard(true)
    object ProgressMonopoly : DevelopmentCard(false)
    object ProgressYearOfPlenty : DevelopmentCard(false)
    object ProgressRoadBuilding : DevelopmentCard(false)
    object VictoryPoint : DevelopmentCard(true)

}

class OwnedDevelopmentCard(
    override val owner: Player,
    val boughtInTurn: Int,
    val card: DevelopmentCard
) : Possessable