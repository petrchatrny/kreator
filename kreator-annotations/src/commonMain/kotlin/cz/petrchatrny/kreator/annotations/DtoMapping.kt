package cz.petrchatrny.kreator.annotations

@Repeatable
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DtoMapping(
    vararg val dtos: String,
    val mapTo: String
)
