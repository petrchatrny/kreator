package cz.petrchatrny.kreator.annotations

import kotlin.reflect.KClass

/**
 * Annotation used to override a property in a DTO.
 * It is placed directly on the property of the domain class that you want to modify in the DTO.
 * You specify in which DTO the property should be changed and set its new name or data type.
 * You can also define how the property should be mapped to/from the DTO if the data types differ.
 * The annotation can be used repeatedly, so it is possible to generate multiple different properties from one attribute.
 *
 * [classNames] specifies which DTO classes the property modification should apply to
 * [name] the new name of the resulting DTO property; if empty, the default name is used
 * [type] the new data type of the resulting DTO property; if set to Any::class, the default type from domain class is used
 * [expression] a Kotlin expression defining how the value should be mapped
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DtoField(
    vararg val classNames: String,
    val name: String = "",
    val type: KClass<*> = Any::class,
    val expression: String = "",
)
