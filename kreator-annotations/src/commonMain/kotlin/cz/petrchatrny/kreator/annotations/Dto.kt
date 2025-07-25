package cz.petrchatrny.kreator.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dto(
    val name: String,
    val pick: Array<String> = [],
    val omit: Array<String> = [],
    val conversion: Conversion = Conversion.NONE
)
