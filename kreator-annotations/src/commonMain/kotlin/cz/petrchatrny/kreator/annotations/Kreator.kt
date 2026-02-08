package cz.petrchatrny.kreator.annotations

/**
 * The main and most important annotation of the entire library.
 * It is applied to a class from which new DTO classes will be generated.
 *
 * [dtos] defines the configurations of the individual DTO classes
 * [isSealed] specifies whether the generated classes should be placed together
 *            in a single file as a sealed class hierarchy
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Kreator(
    vararg val dtos: Dto,
    val isSealed: Boolean = false
)
