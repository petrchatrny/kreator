package cz.petrchatrny.kreator.compiler.util

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

/**
 * TODO KDoc
 */
data class ConstructorScore(
    val ctor: KSFunctionDeclaration,
    val matchedParameters: Int
)
