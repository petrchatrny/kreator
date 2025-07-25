package cz.petrchatrny.kreator.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Kreator(
    vararg val dtos: Dto,
    val classType: ClassType = ClassType.CLASS
)
