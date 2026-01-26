package cz.petrchatrny.kreator.annotations

import kotlin.reflect.KClass

/**
 * Anotace slouží pro předefinování vlastnosti v DTO.
 * Používá se přímo nad vlastností doménové třídy, kterou chceme v DTO změnit.
 * Označíme ve kterém DTO se má vlastnost změnit a nastavíme její nový název nebo datový typ.
 * Zároveň můžeme nadefinovat, jakým způsobem se má vlastnost z/do DTO převádět, pokud má v obou případech různý datový typ.
 * Anotaci je možné použít opakovaně, je tedy dovoleno z jednoho atributu vytvořit více jiných.
 *
 * [classNames] označuje na které DTO třídy se má změna vlastnosti aplikovat
 * [name] nové jméno výsledné vlastnosti v DTO, pokud prázdné tak zůstane výchozí
 * [type] nový datový typ výsledné vlastnosti v DTO, pokud bude Any:class tak zůstane výchozí
 * [expression] Kotlin výraz definující, jakým způsobem se bude převádět
 */
@Repeatable
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DtoField(
    vararg val classNames: String,
    val name: String = "",
    val type: KClass<*> = Any::class,
    val expression: String = "",
    val conversion: Conversion = Conversion.NONE
)
