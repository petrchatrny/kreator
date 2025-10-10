package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.DtoFields

class DtoFieldsProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(DtoFields::class.qualifiedName!!)
        symbols.filterIsInstance<KSClassDeclaration>().forEach { generateFieldsObject(it) }
        return emptyList()
    }

    private fun generateFieldsObject(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val originalClassName = classDeclaration.simpleName.asString()
        val newClassName = "${originalClassName}Fields"

        val fileBuilder = FileSpec.builder(packageName, newClassName)
        val classBuilder = TypeSpec.objectBuilder(newClassName)

        classDeclaration.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()

            val propertySpec = PropertySpec
                .builder(propertyName, String::class) // name and data type of property
                .addModifiers(KModifier.CONST) // constant modifier
                .initializer("%S", propertyName) // default value
                .build()

            classBuilder.addProperty(propertySpec)
        }

        fileBuilder.addType(classBuilder.build())

        val fileSpec = fileBuilder.build()
        fileSpec.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }
}
