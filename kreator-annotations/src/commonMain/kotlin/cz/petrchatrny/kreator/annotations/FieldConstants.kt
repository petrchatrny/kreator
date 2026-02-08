package cz.petrchatrny.kreator.annotations

/**
 * Annotation that can be applied to a class.
 * When used, the processor generates a new object with the suffix "Fields".
 * The newly generated object will contain, as constants, the names of all properties.
 * For example, applying it to the class "User" will produce a new object "UserFields".
 *
 * Covered are all properties of the class on which the annotation was applied.
 * Using this annotation makes it easier to work with the @Dto annotation
 * and to select properties for the `pick` or `omit` parameters.
 *
 * @see Dto
 */
@Target(AnnotationTarget.CLASS)
annotation class FieldConstants
