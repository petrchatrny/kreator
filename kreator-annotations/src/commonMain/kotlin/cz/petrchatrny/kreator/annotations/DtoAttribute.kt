package cz.petrchatrny.kreator.annotations

@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DtoAttribute(
    vararg val classNames: String,
    val name: String = "",
    val type: String = ""
)
