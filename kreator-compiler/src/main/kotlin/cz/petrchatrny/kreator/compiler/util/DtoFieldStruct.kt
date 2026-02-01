package cz.petrchatrny.kreator.compiler.util

import com.google.devtools.ksp.symbol.KSType
import cz.petrchatrny.kreator.annotations.Conversion
import java.util.Collections

data class DtoFieldStruct(
    var classNames: Set<String> = Collections.emptySet(),
    var name: String,
    var type: KSType?,
    var expression: String,
    var conversion: Conversion
)
