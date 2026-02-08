package cz.petrchatrny.kreator.annotations

/**
 * Types of conversion mapping methods
 */
enum class Conversion {
    /** No method will be generated. */
    NONE,

    /** A method converting a DTO to a domain class will be generated. */
    TO_DOMAIN,

    /** A method converting a domain class to a DTO will be generated. */
    FROM_DOMAIN,
}
