package com.example.mtg

import cz.petrchatrny.kreator.annotations.ClassType
import cz.petrchatrny.kreator.annotations.Conversion
import cz.petrchatrny.kreator.annotations.Dto

import cz.petrchatrny.kreator.annotations.Kreator
import java.util.UUID

@Kreator(
    Dto(name = "MtgCardRefDto", pick = ["id"], conversion = Conversion.BOTH),
    Dto(name = "MtgCardCreateDto", omit = ["id"], conversion = Conversion.FROM),
    Dto(name = "MtgCardUpdateDto", omit = ["id", "createdByUser"], classType = ClassType.CLASS),
    Dto(name = "MtgCardListDto", pick = ["id", "name", "type", "manaCost"], conversion = Conversion.TO),
)
class MtgCard(
    val name: String,

    val type: String?,

    val rarity: Int,

    val isFoil: Boolean,

    val createdByUser: String,

    val manaCost: Map<ManaColor, Int> = emptyMap(),
) : Card() {
    var description: String? = null

    private var updatedByUser: String? = null

    enum class ManaColor {
        WHITE,
        BLUE,
        BLACK,
        RED,
        GREEN,
        WILD
    }
}

open class Card {
    val id: UUID = UUID.randomUUID()
}
