package cz.petrchatrny.kreator.annotations

/**
 * Annotation for defining a single DTO class.
 * The specific form of the resulting DTO class (its properties) is determined using
 * the parameters `pick` and `omit`.
 * Both parameters cannot be used at the same time — you must choose exactly one.
 *
 * [name] the name of the generated DTO class
 * [pick] defines the set of properties that will appear in the resulting DTO class
 * [omit] defines the properties that will *not* appear in the resulting DTO class
 *        (all remaining properties from the full set will be included)
 * [classType] the Kotlin class type — how the new DTO class will be implemented
 * [mapping] specifies which mapping methods will be generated
 *              (mapping from the domain class to the DTO, from the DTO to the domain class or none)
 *
 * @see Kreator
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dto(
    val name: String,
    val pick: Array<String> = [],
    val omit: Array<String> = [],
    val classType: ClassType = ClassType.DATA_CLASS,
    val mapping: Mapping = Mapping.NONE
)
