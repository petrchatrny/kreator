package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.ClassType
import cz.petrchatrny.kreator.annotations.Conversion
import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.DtoField
import cz.petrchatrny.kreator.annotations.Kreator
import cz.petrchatrny.kreator.compiler.util.DtoFieldStruct
import cz.petrchatrny.kreator.compiler.util.toParameterSpec
import java.util.ArrayList
import java.util.Collections.emptySet
import kotlin.collections.contains

@OptIn(KspExperimental::class)
class KreatorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private var resolver: Resolver? = null
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver

        // find all classes with @Kreator annotation
        val symbols = resolver.getSymbolsWithAnnotation(Kreator::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        // list of classes which will be deferred to be compiled in next round
        val deferred = mutableListOf<KSClassDeclaration>()

        for (annotatedClass in symbols) {
            try {
                // get @Kreator with its parameters from annotated class
                val kreatorAnnotation: Kreator = annotatedClass.getAnnotationsByType(Kreator::class).first()

                // TODO vybrat z Kreator anotace jestli bude výsledek Sealed nebo ne
                // kreatorAnnotation.isSealed

                // build DTO class for every @Dto annotation used in @Kreator
                val dtoClasses = mutableListOf<TypeSpec>()
                kreatorAnnotation.dtos.forEach { dto ->
                    logger.info("Building dto ${dto.name}")
                    dtoClasses.add(buildDtoClass(annotatedClass, dto))
                }

                // build files
                val dtoFiles = mutableListOf<FileSpec>()
                if (kreatorAnnotation.isSealed) {
                    val sealedClass = TypeSpec.classBuilder("${annotatedClass.simpleName}Dto")
                        .addTypes(dtoClasses)
                        .build()

                    dtoFiles.add(buildDtoFile(annotatedClass.packageName.asString(), sealedClass))
                } else {
                    for (dtoClass in dtoClasses) {
                        dtoFiles.add(buildDtoFile(annotatedClass.packageName.asString(), dtoClass))
                    }
                }

                // generate and write files to disk
                for (file in dtoFiles) {
                    file.writeTo(codeGenerator, Dependencies(false, annotatedClass.containingFile!!))
                }
            } catch (_: IllegalStateException) {
                logger.info("Invalid symbol added: $annotatedClass")
                deferred.add(annotatedClass)
            }
        }

        return deferred
    }

    private fun buildDtoFile(packageName: String, dtoClass: TypeSpec): FileSpec {
        // file
        val fileSpecBuilder = FileSpec.builder(packageName, dtoClass.name!!)

        // add class to file
        fileSpecBuilder.addType(dtoClass)

        // build file
        return fileSpecBuilder.build()
    }

    private fun buildDtoClass(sourceClass: KSClassDeclaration, dto: Dto): TypeSpec {

        // class specification and name
        val classSpecBuilder = TypeSpec.classBuilder(dto.name)

        // choose type of class
        when (dto.classType) {
            ClassType.CLASS -> {}
            ClassType.DATA_CLASS -> {
                classSpecBuilder.addModifiers(KModifier.DATA)
            }
        }

        // select the right properties for the DTO class based on pick/omit arguments
        val originProperties = sourceClass.getAllProperties().toList()
        val selectedProperties = when {
            dto.pick.isNotEmpty() -> originProperties.filter { it.simpleName.asString() in dto.pick }
            dto.omit.isNotEmpty() -> originProperties.filter { it.simpleName.asString() !in dto.omit }
            else -> emptyList()
        }

        // TODO check if user entered invalid name of property
        // logger.warn()

        // check if data class is not empty
        if (dto.classType == ClassType.DATA_CLASS && selectedProperties.isEmpty()) {
            logger.error("No properties found for data class ${dto.name}. Data class must have at least 1 property.")
            throw IllegalArgumentException("No properties found for data class ${dto.name}. Data class must have at least 1 property.")
        } else if (selectedProperties.isEmpty()) {
            logger.warn("DTO ${dto.name} of class ${sourceClass.simpleName.asString()} should define either 'pick' or 'omit' as non-empty array.")
        }

        // build DTO properties
        val newProperties: MutableSet<PropertySpec> = mutableSetOf()
        for (prop in selectedProperties) {
            logger.info("Processing property ${prop.simpleName.getShortName()}")
            newProperties.addAll(buildDtoProperties(dto.name, prop))
        }

        // add constructor
        val constructorBuilder = FunSpec.constructorBuilder()
        constructorBuilder.addParameters(newProperties.map { it.toParameterSpec() })
        classSpecBuilder.primaryConstructor(constructorBuilder.build())

        // add properties to class
        classSpecBuilder.addProperties(newProperties)

        // TODO add mapping methods
//        when (dto.conversion) {
//            Conversion.NONE -> {}
//            Conversion.FROM -> {
//                addDtoToDomainConversion(classSpecBuilder, originClass)
//            }
//
//            Conversion.TO -> {
//                addDomainToDtoConversion(originClass, dto.name)
//            }
//
//            Conversion.BOTH -> {
//                addDtoToDomainConversion(classSpecBuilder, originClass)
//                addDomainToDtoConversion(originClass, dto.name)
//            }
//        }

        // build class
        return classSpecBuilder.build()
    }

    private fun buildDtoProperties(dtoName: String, sourceProperty: KSPropertyDeclaration, ): List<PropertySpec> {
        val dtoProperties = mutableListOf<PropertySpec>()

        // parse all DtoField annotations among this property
        // val dtoField = sourceProperty.getAnnotationsByType(DtoField::class).firstOrNull() // TODO this parsing doesn't work here due to usage of KClass<> argument in annotation: https://kotlinlang.org/docs/ksp-additional-details.html#type-and-resolution
        val annotations = sourceProperty.annotations.toList()
            // only @DtoField annotation is important
            .filter { ann -> ann.annotationType.resolve().declaration.qualifiedName?.asString() == DtoField::class.qualifiedName }
            // DTO which is being created
            .filter { ann -> (ann.arguments[0].value as ArrayList<*>).contains(dtoName) }
            .map { ann -> parseDtoFieldAnnotation(ann) }

        // if selected property has no @DtoAttribute annotation, just add it
        if (annotations.isEmpty()) {
            dtoProperties.add(
                PropertySpec.builder(
                    name = sourceProperty.simpleName.asString(),
                    type = typeReferenceToTypeName(sourceProperty.type)
                ).initializer(sourceProperty.simpleName.asString()).build()
            )
        }
        // otherwise apply changes from annotations and construct new property
        else {
            for (annotation in annotations) {
                // name
                var name = annotation.name
                if (name.isEmpty()) {
                    name = sourceProperty.simpleName.asString()
                }

                // type
                val type: TypeName = annotation.type?.toTypeName()
                    ?: run { typeReferenceToTypeName(sourceProperty.type) }

                dtoProperties.add(
                    PropertySpec.builder(name, type)
                        .initializer(name).build()
                )
            }
        }

        return dtoProperties
    }

    /**
     * Protože anotace @DtoField obsahuje jako jeden z argumentů KClass, je nutné přidat toto speciální parsování, jinak by to byl problém
     */
    private fun parseDtoFieldAnnotation(annotation: KSAnnotation): DtoFieldStruct {
        val classNames: MutableSet<String> = emptySet()
        var name = ""
        var type: KSType? = null
        var expression = ""
        var conversion: Conversion = Conversion.NONE

        for (argument in annotation.arguments) {
            when (argument.name?.getShortName()) {
                "name" -> {
                    name = argument.value as String
                }

                "type" -> {
                    type = argument.value as KSType
                }

                "expression" -> {
                    expression = argument.value as String
                }

                "conversion" -> {
                    // conversion = argument.value as Conversion
                }
            }
        }

        // if user didn't set any type and value is Any::class
        if (type != null && type == this.resolver?.builtIns?.anyType) {
            type = null
        }

        return DtoFieldStruct(classNames = classNames, name, type, expression, conversion)
    }

    private fun typeReferenceToTypeName(typeReference: KSTypeReference): TypeName {
        val resolvedType = typeReference.resolve()
        val nullable = resolvedType.nullability == Nullability.NULLABLE

        return resolvedType.toTypeName().copy(nullable = nullable)
    }
}
