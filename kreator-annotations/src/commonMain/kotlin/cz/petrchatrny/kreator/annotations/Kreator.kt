package cz.petrchatrny.kreator.annotations

/**
 * Hlavní a nejdůležitější anotace celé knihovny.
 * Používá se nad třídou, ze které se budou generovat nové DTO třídy.
 *
 * [dtos] atribut definuje podoby jednotlivých DTO tříd
 * [classType] udává, jaké ty třídy budou druhu (class, data class, sealed class, sealed interface)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Kreator(
    vararg val dtos: Dto,
    val classType: ClassType = ClassType.CLASS
)
