package cz.petrchatrny.kreator.compiler.util

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * TODO KDoc
 */
fun PropertySpec.toParameterSpec(): ParameterSpec = ParameterSpec.builder(name, type).build()
