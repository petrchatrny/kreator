package cz.petrchatrny.kreator.compiler.provider

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import cz.petrchatrny.kreator.compiler.processor.GetterGeneratorProcessor

class GetterGeneratorProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return GetterGeneratorProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}