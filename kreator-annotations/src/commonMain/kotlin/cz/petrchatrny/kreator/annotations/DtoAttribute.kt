package cz.petrchatrny.kreator.annotations

import kotlin.reflect.KClass

/**
 * Anotace slouží pro předefinování vlastnosti v DTO.
 * Používá se přímo nad vlastností doménové třídy, kterou chceme v DTO změnit.
 * Označíme ve kterém DTO se má vlastnost změnit a nastavíme její nový název nebo datový typ.
 *
 * [classNames] označuje na které DTO třídy se má změna vlastnosti aplikovat
 * [name] nové jméno výsledné vlastnosti v DTO
 * [type] nový datový typ výsledné vlastnosti v DTO
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DtoAttribute(
    vararg val classNames: String,
    val name: String = "",
    val type: KClass<*> = Any::class
)
