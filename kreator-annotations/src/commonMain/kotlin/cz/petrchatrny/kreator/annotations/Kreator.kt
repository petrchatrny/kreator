package cz.petrchatrny.kreator.annotations

/**
 * Hlavní a nejdůležitější anotace celé knihovny.
 * Používá se nad třídou, ze které se budou generovat nové DTO třídy.
 *
 * [dtos] atribut definuje podoby jednotlivých DTO tříd
 * [isSealed] udává, jestli vygenerované třídy budou v jednom souboru jako sealed třída
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Kreator(
    vararg val dtos: Dto,
    val isSealed: Boolean = false
)
