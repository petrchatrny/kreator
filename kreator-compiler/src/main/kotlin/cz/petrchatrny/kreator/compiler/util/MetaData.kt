package cz.petrchatrny.kreator.compiler.util

/**
 * TODO KDoc
 */
data class MetaData(
    val fromClass: String,
    val toClass: String,
    val fromProperty: String,
    val toProperty: String,
    val expression: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetaData

        if (fromClass != other.fromClass) return false
        if (toClass != other.toClass) return false
        if (fromProperty != other.fromProperty) return false
        if (toProperty != other.toProperty) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromClass.hashCode()
        result = 31 * result + toClass.hashCode()
        result = 31 * result + fromProperty.hashCode()
        result = 31 * result + toProperty.hashCode()
        return result
    }
}
