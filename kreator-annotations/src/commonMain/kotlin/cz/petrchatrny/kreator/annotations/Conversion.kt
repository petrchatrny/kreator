package cz.petrchatrny.kreator.annotations

/**
 * Druhy převodních mapovacích metod
 */
enum class Conversion {
    NONE, // nebude se generovat žádná
    FROM, // bude se generovat metoda převodu z doménové třídy na DTO
    TO, // bude se generovat metoda převodu DTO na doménovou třídu
}
