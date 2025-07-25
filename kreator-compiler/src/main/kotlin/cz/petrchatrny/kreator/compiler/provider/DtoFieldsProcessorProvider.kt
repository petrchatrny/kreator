package cz.petrchatrny.kreator.compiler.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import cz.petrchatrny.kreator.compiler.processor.DtoFieldsProcessor

class DtoFieldsProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DtoFieldsProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}