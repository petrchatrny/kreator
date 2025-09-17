package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.Kreator

class KreatorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Kreator::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        for (annotatedClass in symbols) {
            val originalClassName = annotatedClass.simpleName.asString()
            val pkgName = annotatedClass.packageName
            val allProperties = annotatedClass.getAllProperties().toList()
            val propertyNames = allProperties.map { it.simpleName.asString() }

// TODO kontrola DtoFields trida existuje

//            val deferred = mutableListOf<KSAnnotated>()
//            val hasDtoFields = annotatedClass.annotations.any {
//                it.shortName.asString() == "DtoFields"
//                        && it.annotationType.resolve().declaration.qualifiedName?.asString() == "cz.petrchatrny.kreator.annotations.DtoFields"
//            }
//            if (hasDtoFields) {
//                val expectedFieldsClassName = "$pkgName.${originalClassName}Fields"
//                val fieldsClassDecl = resolver.getClassDeclarationByName(
//                    resolver.getKSNameFromString(expectedFieldsClassName)
//                )
//
//                if (fieldsClassDecl == null) {
//                    // fields třída zatím neexistuje
//                    deferred += annotatedClass
//                    continue
//                }
//            }

            // obtain @Kreator annotation from annotated class
            val kreatorAnnotation = annotatedClass.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == Kreator::class.qualifiedName
            }

            val dtoArgs = kreatorAnnotation.arguments.first { it.name?.asString() == "dtos" }.value as List<*>

            for (dtoAnno in dtoArgs) {
                val dto = dtoAnno as KSAnnotation
                val dtoName = dto.arguments.first { it.name?.asString() == "name" }.value as String
                val pickList = dto.arguments.first { it.name?.asString() == "pick" }.value as List<String>
                val omitList = dto.arguments.first { it.name?.asString() == "omit" }.value as List<String>

                val selectedProps = when {
                    pickList.isNotEmpty() -> allProperties.filter { it.simpleName.asString() in pickList }
                    omitList.isNotEmpty() -> allProperties.filter { it.simpleName.asString() !in omitList }
                    else ->  throw IllegalArgumentException("@Dto annotation on class ${annotatedClass.simpleName.asString()} must define either 'pick' or 'omit' as non-empty array.")
                }

                generateDtoClass(annotatedClass, dtoName, selectedProps)
            }

//            val dtoAnnotations = kreatorAnnotation.arguments
//                .first { it.name?.asString() == "dtos" }
//                .value as List<*>
//
//            for (dto in dtoAnnotations) {
//                val dtoAnnotation = dto as KSAnnotation
//                val name = dtoAnnotation.arguments.firstOrNull { it.name?.asString() == "name" }?.value
//                val omit = dtoAnnotation.arguments.firstOrNull { it.name?.asString() == "omit" }?.value as? List<*> ?: emptyList<Any>()
//
//                logger.warn("Dto name: $name, omit: $omit")
//            }
        }

        return emptyList()
    }

    private fun generateDtoClass(
        originClass: KSClassDeclaration,
        className: String,
        properties: List<KSPropertyDeclaration>
    ) {
        val packageName = originClass.packageName.asString()

        val fileSpecBuilder = FileSpec.builder(packageName, className)

        val classSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA)

        val ctorBuilder = FunSpec.constructorBuilder()

        for (prop in properties) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            val typeName = type.toTypeName().copy(nullable = type.nullability == Nullability.NULLABLE)

            val propSpec = PropertySpec.builder(name, typeName)
                .initializer(name)
                .build()

            ctorBuilder.addParameter(name, typeName)
            classSpecBuilder.addProperty(propSpec)
        }

        classSpecBuilder.primaryConstructor(ctorBuilder.build())
        fileSpecBuilder.addType(classSpecBuilder.build())

        fileSpecBuilder.build().writeTo(codeGenerator, Dependencies(false))
    }
}