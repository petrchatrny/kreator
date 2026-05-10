package cz.petrchatrny.kreator.annotations

/**
 * Types of mapping methods
 */
enum class Mapping {
    /** No method will be generated. */
    NONE,

    /** A method mapping a DTO to a domain class will be generated. */
    TO_DOMAIN,

    /** A method mapping a domain class to a DTO will be generated. */
    FROM_DOMAIN,
}
