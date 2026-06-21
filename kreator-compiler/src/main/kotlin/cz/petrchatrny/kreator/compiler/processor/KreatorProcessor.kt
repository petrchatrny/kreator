package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.ClassType
import cz.petrchatrny.kreator.annotations.Mapping
import cz.petrchatrny.kreator.annotations.Dto
import cz.petrchatrny.kreator.annotations.DtoField
import cz.petrchatrny.kreator.annotations.Kreator
import cz.petrchatrny.kreator.compiler.exception.TypeNotResolved
import cz.petrchatrny.kreator.compiler.util.ConstructorScore
import cz.petrchatrny.kreator.compiler.util.DtoFieldStruct
import cz.petrchatrny.kreator.compiler.util.Metadata
import cz.petrchatrny.kreator.compiler.util.toParameterSpec
import kotlin.collections.contains
import kotlin.jvm.Throws

/**
 * TODO KDoc
 */
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

                // build DTO class for every @Dto annotation used in @Kreator
                val dtoClasses = mutableListOf<TypeSpec>()
                kreatorAnnotation.dtos.forEach { dto ->
                    logger.info("Building DTO ${dto.name}")
                    dtoClasses.add(buildDtoClass(annotatedClass, dto))
                }

                // build files
                val dtoFiles = mutableListOf<FileSpec>()
                if (kreatorAnnotation.isSealed) {
                    val sealedClass = TypeSpec.classBuilder("${annotatedClass.simpleName.asString()}Dto")
                        .addTypes(dtoClasses)
                        .addModifiers(KModifier.SEALED)
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
            } catch (_: TypeNotResolved) {
                logger.info("Missing type")
                deferred.add(annotatedClass)
            } catch (_: FileAlreadyExistsException) {
                logger.warn("One file already existed")
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
            dto.pick.isNotEmpty() && dto.omit.isNotEmpty() -> throw IllegalArgumentException("Both pick and omit cannot be used at the same time.")
            dto.pick.isNotEmpty() -> originProperties.filter { it.simpleName.asString() in dto.pick }
            dto.omit.isNotEmpty() -> originProperties.filter { it.simpleName.asString() !in dto.omit }
            else -> originProperties // both are empty, take every property
        }

        // warn user about non-matched properties
        val notFound: Set<String> = if (dto.pick.isNotEmpty()) {
            (dto.pick.toSet() - originProperties.map { it.simpleName.asString() }.toSet())
        } else {
            (dto.omit.toSet() - originProperties.map { it.simpleName.asString() }.toSet())
        }
        if (notFound.isNotEmpty()) {
            logger.warn(
                "These properties defined for ${dto.name} could not " +
                        "be found in the source class (${sourceClass.simpleName.asString()}): " +
                        notFound.joinToString(", ")
            )
        }

        // check if data class is not empty
        if (dto.classType == ClassType.DATA_CLASS && selectedProperties.isEmpty()) {
            logger.error("No properties found for data class ${dto.name}. Data class must have at least 1 property.")
            throw IllegalArgumentException("No properties found for data class ${dto.name}. Data class must have at least 1 property.")
        } else if (selectedProperties.isEmpty()) {
            logger.warn("DTO ${dto.name} of class ${sourceClass.simpleName.asString()} should define either 'pick' or 'omit' as non-empty array.")
        }

        // build DTO properties and metadata
        val newProperties: MutableSet<PropertySpec> = mutableSetOf()
        val metadata: MutableSet<Metadata> = mutableSetOf()

        for (prop in selectedProperties) {
            logger.info("Processing property ${prop.simpleName.getShortName()}")
            val (properties, meta) = buildDtoProperties(dto, prop, sourceClass)

            newProperties.addAll(properties)
            metadata.addAll(meta)
        }

        // add constructor
        val constructorBuilder = FunSpec.constructorBuilder()
        constructorBuilder.addParameters(newProperties.map { it.toParameterSpec() })
        classSpecBuilder.primaryConstructor(constructorBuilder.build())

        // add properties to class
        classSpecBuilder.addProperties(newProperties)

        // add mapping functions
        val className = ClassName(packageName = sourceClass.packageName.asString(), simpleNames = listOf(dto.name))
        when (dto.mapping) {
            Mapping.NONE -> {}

            Mapping.TO_DOMAIN -> {
                val toDomainFunction = buildToDomainFunction(sourceClass, newProperties, metadata)
                if (toDomainFunction != null) {
                    classSpecBuilder.addFunction(toDomainFunction)
                }
            }

            Mapping.FROM_DOMAIN -> {
                val compObject = TypeSpec.companionObjectBuilder()
                val fromDomainFunction = buildFromDomainFunction(sourceClass, className, metadata)

                compObject.addFunction(fromDomainFunction)
                classSpecBuilder.addType(compObject.build())
            }
        }

        // build class
        return classSpecBuilder.build()
    }

    private fun buildDtoProperties(
        dto: Dto,
        sourceProperty: KSPropertyDeclaration,
        sourceClass: KSClassDeclaration
    ): Pair<MutableList<PropertySpec>, Set<Metadata>> {
        val dtoProperties = mutableListOf<PropertySpec>()
        val metadata: MutableSet<Metadata> = mutableSetOf()

        // parse all DtoField annotations among this property
        // val dtoField = sourceProperty.getAnnotationsByType(DtoField::class).firstOrNull() // TODO this parsing doesn't work here due to usage of KClass<> argument in annotation: https://kotlinlang.org/docs/ksp-additional-details.html#type-and-resolution
        val annotations = sourceProperty.annotations
            // only @DtoField annotation is important
            .filter { ann -> ann.annotationType.resolve().declaration.qualifiedName?.asString() == DtoField::class.qualifiedName }
            // DTO which is being created
            .filter { ann -> (ann.arguments[0].value as ArrayList<*>).contains(dto.name) }
            .map { ann -> parseDtoFieldAnnotation(ann) }
            .toSet()

        // mapping direction
        val (fromClass, toClass) =
            if (dto.mapping == Mapping.FROM_DOMAIN) {
                sourceClass.simpleName.asString() to dto.name
            } else {
                dto.name to sourceClass.simpleName.asString()
            }

        // if selected property has no @DtoField annotation, just add it as it is
        if (annotations.isEmpty()) {
            dtoProperties.add(
                PropertySpec.builder(
                    name = sourceProperty.simpleName.asString(),
                    type = typeReferenceToTypeName(sourceProperty.type)
                ).initializer(sourceProperty.simpleName.asString()).build()
            )

            // save metadata
            metadata.add(
                Metadata(
                    fromClass = fromClass,
                    toClass = toClass,
                    fromProperty = sourceProperty.simpleName.asString(),
                    toProperty = sourceProperty.simpleName.asString(),
                    expression = ""
                )
            )

            return Pair(dtoProperties, metadata)
        }
        // otherwise apply changes from annotations and construct new property
        else {
            for (annotation in annotations) {
                // name
                var dtoPropertyName = annotation.name
                if (dtoPropertyName.isEmpty()) {
                    // use default property's name if not it's not overridden by annotation
                    dtoPropertyName = sourceProperty.simpleName.asString()
                }

                // type
                val type: TypeName = annotation.type?.toTypeName()
                    ?: run { typeReferenceToTypeName(sourceProperty.type) }

                // save property with correct type
                dtoProperties.add(PropertySpec.builder(dtoPropertyName, type).initializer(dtoPropertyName).build())

                // mapping direction
                val (fromProperty, toProperty) =
                    if (dto.mapping == Mapping.FROM_DOMAIN) {
                        sourceProperty.simpleName.asString() to dtoPropertyName
                    } else {
                        dtoPropertyName to sourceProperty.simpleName.asString()
                    }

                // save metadata
                metadata.add(
                    Metadata(
                        fromClass = fromClass,
                        toClass = toClass,
                        fromProperty = fromProperty,
                        toProperty = toProperty,
                        expression = annotation.expression
                    )
                )
            }
        }

        return Pair(dtoProperties, metadata)
    }

    /**
     * Companion object function inside DTO
     */
    private fun buildFromDomainFunction(
        domainClass: KSClassDeclaration,
        dtoClass: ClassName,
        metadata: Set<Metadata>
    ): FunSpec {
        /* TODO when a class from different package is used in the expression
         ** compilation will fail on missing import */
        val parameters = mutableListOf<String>()
        val filteredMetadata = metadata
            .filter { it.fromClass == domainClass.toClassName().simpleName && it.toClass == dtoClass.simpleName }
            .toSet()

        for (metadata in filteredMetadata) {
            if (metadata.expression != "") {
                parameters.add("${metadata.toProperty}=${metadata.expression}")
            } else {
                parameters.add("${metadata.toProperty}=domain.${metadata.fromProperty}")
            }
        }

        val fromDomain = FunSpec.builder("fromDomain")
            .addParameter(name = "domain", type = domainClass.toClassName())
            .addStatement(format = "return %T(%L)", dtoClass, parameters.joinToString(","))
            .returns(dtoClass)

        return fromDomain.build()
    }

    /**
     * Member function inside DTO
     * Převádí DTO třídu na Doménovou třídu.
     * Musí vybrat správný konstruktor doménové třídy.
     * K tomu slouží funkce pro výběr nejlepšího konstruktoru.
     */
    private fun buildToDomainFunction(
        domainClass: KSClassDeclaration,
        dtoProperties: Set<PropertySpec>,
        metadata: Set<Metadata>
    ): FunSpec? {
        val dtoPropsByNames = dtoProperties.associateBy { it.name }
        val toProperties = metadata.map { it.toProperty }.toSet()

        val callableConstructors = domainClass.getConstructors()
            .filter { constructor -> isCallable(constructor, toProperties) }
            .filter { constructor -> checkTypes(constructor.parameters, metadata, dtoPropsByNames) }
            .toSet()

        val bestConstructor = callableConstructors
            .map { scoreConstructor(it, dtoPropsByNames) }
            .maxByOrNull { it.matchedParameters }
            ?.ctor

        if (bestConstructor == null) {
            logger.warn("No matching constructor for DTO")
            return null
        }

        val constructorParameters = bestConstructor.parameters
            .map { it.name?.asString() }
            .toSet()

        val otherSettableParameters = domainClass.getAllProperties()
            .filter { it.isMutable }
            .map { it.simpleName.asString() }
            .toSet() - constructorParameters

        val parameters = mutableListOf<String>()
        val filteredMetaData = metadata
            .filter { it.toClass == domainClass.toClassName().simpleName }
            .toSet()

        for (metadata in filteredMetaData) {
            if (metadata.fromProperty in constructorParameters) {
                if (metadata.expression != "") {
                    parameters.add("${metadata.toProperty}=${metadata.expression}")
                } else {
                    parameters.add("${metadata.toProperty}=${metadata.fromProperty}")
                }
            }
        }

        val toDomain = FunSpec.builder("toDomain")
            .addStatement(format = "return %T(%L)", domainClass.toClassName(), parameters.joinToString(","))
            .addStatement(".apply{}")
            .returns(domainClass.toClassName())

        return toDomain.build()
    }

    /**
     * Kontroluje, jestli každý parametr z metadat odpovídá svým datovým typem parametru z Konstruktoru.
     * Pokud je u DTO parametr použitá expression, bere se to tak, že datový typ sedět nemusí, protože
     * u expression to nejde ověřit a musíme důvěřovat uživateli knihovny, že to zadal dobře,
     * kdyžtak sám dotane compilation error.
     */
    private fun checkTypes(
        constructorParameters: List<KSValueParameter>,
        metadata: Set<Metadata>,
        dtoProperties: Map<String, PropertySpec>
    ): Boolean {
        for (md in metadata) {
            if (md.expression == "") {
                val ctorParameter = constructorParameters.find { it.name?.asString() == md.toProperty }
                val dtoProperty = dtoProperties[md.fromProperty]
                if (ctorParameter?.type?.toTypeName() != dtoProperty?.type) {
                    logger.warn(
                        "Parameter types are not matching: " +
                                "$ctorParameter (${ctorParameter?.type}) and " +
                                "${dtoProperty?.name}(${dtoProperty?.type})"
                    )
                    return false
                }
            }
        }
        return true
    }

    private fun isCallable(
        constructor: KSFunctionDeclaration,
        toProperties: Set<String>
    ): Boolean {
        val constructorProperties = constructor.parameters.map { it.name?.asString() }
        return toProperties.containsAll(constructorProperties)
    }

    /**
     * Protože anotace @DtoField obsahuje jako jeden z argumentů KClass, je nutné přidat toto speciální parsování, jinak by to byl problém
     */
    private fun parseDtoFieldAnnotation(annotation: KSAnnotation): DtoFieldStruct {
        val classNames: MutableSet<String> = mutableSetOf()
        var name = ""
        var type: KSType? = null
        var expression = ""

        for (argument in annotation.arguments) {
            when (argument.name?.getShortName()) {
                "classNames" -> {
                    (argument.value as Collection<String>?)?.let { classNames.addAll(it) }
                }

                "name" -> {
                    name = argument.value as String
                }

                "type" -> {
                    type = argument.value as KSType
                }

                "expression" -> {
                    expression = argument.value as String
                }
            }
        }

        // if user didn't set any type and value is Any::class
        if (type != null && type == this.resolver?.builtIns?.anyType) {
            type = null
        }

        return DtoFieldStruct(classNames = classNames, name, type, expression)
    }

    @Throws(TypeNotResolved::class)
    private fun typeReferenceToTypeName(typeReference: KSTypeReference): TypeName {
        try {
            val resolvedType = typeReference.resolve()
            val nullable = resolvedType.nullability == Nullability.NULLABLE

            return resolvedType.toTypeName().copy(nullable = nullable)
        } catch (e: IllegalArgumentException) {
            logger.warn(e.message ?: "")
            throw TypeNotResolved()
        }
    }

    /**
     * Spočítá, kolik maximálně argumentů je možné dosadit přímo do konstruktoru.
     */
    private fun scoreConstructor(
        constructor: KSFunctionDeclaration,
        dtoProps: Map<String, PropertySpec>
    ): ConstructorScore {
        var matchedParameters = 0

        for (param in constructor.parameters) {
            val name = param.name?.asString()
            if (name != null && name in dtoProps.keys) {
                matchedParameters += 1
            }
        }

        return ConstructorScore(constructor, matchedParameters)
    }
}
