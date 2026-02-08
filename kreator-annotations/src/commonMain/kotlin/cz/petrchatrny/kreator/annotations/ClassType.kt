package cz.petrchatrny.kreator.annotations

/**
 * Kotlin class type
 */
enum class ClassType {

    /** Standard Kotlin class **/
    CLASS,

    /** Data class with primary extra features like EQ and HS methods, primary constructor and so on *
     * See: https://kotlinlang.org/docs/data-classes.html
     */
    DATA_CLASS
}
