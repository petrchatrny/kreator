package cz.petrchatrny.kreator.compiler.util

import com.google.devtools.ksp.symbol.KSType
import cz.petrchatrny.kreator.annotations.Mapping
import java.util.Collections

/**
 * TODO KDoc
 */
data class DtoFieldStruct(
    val classNames: Set<String> = Collections.emptySet(),
    val name: String,
    val type: KSType?,
    val expression: String,
)
