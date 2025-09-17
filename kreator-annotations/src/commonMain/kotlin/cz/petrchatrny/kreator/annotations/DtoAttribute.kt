package cz.petrchatrny.kreator.annotations

import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DtoAttribute(
    vararg val classNames: String,
    val name: String = "",
    val type: KClass<*> = Any::class
)
