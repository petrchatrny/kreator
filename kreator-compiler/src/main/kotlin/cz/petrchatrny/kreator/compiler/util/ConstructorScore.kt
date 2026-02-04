package cz.petrchatrny.kreator.compiler.util

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

data class ConstructorScore(
    val ctor: KSFunctionDeclaration,
    val matchedParameters: Int
)
