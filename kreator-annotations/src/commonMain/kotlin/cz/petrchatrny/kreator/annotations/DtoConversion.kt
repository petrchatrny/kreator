package cz.petrchatrny.kreator.annotations

annotation class DtoConversion(
    vararg val classNames: String,
    val type: Conversion,
    val expression: String
)
