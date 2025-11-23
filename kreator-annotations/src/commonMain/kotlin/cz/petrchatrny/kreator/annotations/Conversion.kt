package cz.petrchatrny.kreator.annotations

/**
 * Druhy převodních mapovacích metod
 */
enum class Conversion {
    NONE, // nebude se generovat žádná
    FROM, // bude se generovat metoda převodu z DTO na doménovou třídu
    TO, // bude se generovat metoda převodu doménové třídy na DTO
    BOTH // budou se generovat obě metody
}
