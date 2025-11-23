package cz.petrchatrny.kreator.annotations

/**
 * Anotace pro definici jedné DTO třídy.
 * Konkrétní podoba výsledné DTO třídy (vlastnosti) se definuje pomocí parametrů pick a omit.
 * Není možné používat souběžně oba parametry, člověk si musí vybrat pouze jeden.
 *
 * [pick] definuje množinu vlastností, které se budou nacházet ve výsledné DTO třídě
 * [omit] říká, které vlastnosti ve výsledné DTO třídě nebudou (zbytek vlastností z množiny všech obsažen bude)
 * [conversion] říká, které převodní metody se budou vytvářet (převod z hlavní třídy na DTO, převod z DTO na hlavní třídu, oboustranný převod nebo žádný)
 * @see Kreator
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dto(
    val name: String,
    val pick: Array<String> = [],
    val omit: Array<String> = [],
    val conversion: Conversion = Conversion.NONE
)
