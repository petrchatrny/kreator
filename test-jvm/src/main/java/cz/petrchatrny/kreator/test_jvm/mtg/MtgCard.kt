package cz.petrchatrny.kreator.test_jvm.mtg

import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.DtoAttribute
import cz.petrchatrny.kreator.annotations.DtoFields
import cz.petrchatrny.kreator.annotations.Kreator
import cz.petrchatrny.kreator.test_jvm.mtg.MtgCardFields.id
import cz.petrchatrny.kreator.test_jvm.mtg.MtgCardFields.name
import cz.petrchatrny.kreator.test_jvm.mtg.MtgCardFields.type
import java.math.BigDecimal
import java.util.UUID

//@Kreator(
//    Dto(name = "MtgCardCreateDto", omit = [id]),
//    Dto(name = "MtgCardListDto", pick = [id, name, type])
//)
@Kreator(
    Dto(name = "MtgCardCreateDto", omit = ["id"]),
    Dto(name = "MtgCardListDto", pick = ["id", "name", "type"])
)
@DtoFields
class MtgCard(
    val id: UUID,
    val name: String,
    val type: String,
    val rarity: Int?,

    @DtoAttribute("MtgCardCreateDto", name = "requiredMana", type = BigDecimal::class)
    @DtoAttribute("MtgCardListDto", name = "requiredMana", type = String::class)
    val manaCost: String,
    val rulesText: String?,
    val set: String // použití rezervovaného slova
)
