package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.Kreator

class KreatorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // najít všechny třídy anotované anotací @Kreator
        val symbols = resolver.getSymbolsWithAnnotation(Kreator::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        for (annotatedClass in symbols) {
            logger.info("Building DTOs for class ${annotatedClass.simpleName.asString()}", annotatedClass)
            val originalClassName = annotatedClass.simpleName.asString()
            val packageName = annotatedClass.packageName
            val properties = annotatedClass.getAllProperties().toList()

            // kontrola DtoFields anotace
//            val isAnnotatedWithDtoFields = annotatedClass.getAnnotationsByType(DtoFields::class)
//                .toList()
//                .isNotEmpty()
//
//            // pokud třída obsahuje DtoFields, je nutné zkontrolovat, že již byl vygenerován její Fields objekt
//            if (isAnnotatedWithDtoFields) {
//                // vygenerovaný objekt by se měla jmenovat stejně jako původní třída a měl by mít příponu "Fields"
//                val fieldsClassName = resolver.getKSNameFromString("${packageName}.${originalClassName}Fields")
//                val fieldsClassDeclaration = resolver.getClassDeclarationByName(fieldsClassName)
//
//                // pokud Fields objekt zatím není k dispozici, odložíme zpracování aktuální třídy do dalšího kola
//                if (fieldsClassDeclaration == null) {
//                    logger.error("Třída ${fieldsClassName.getShortName()} zatím neexistuje, odkládám zpracování.", annotatedClass)
//                    continue
//                }
//            }

            // získání anotace @Kreator a její parametry z anotované třídy
            val kreatorAnnotation: Kreator = annotatedClass.getAnnotationsByType(Kreator::class).first()

            // TODO tady rozhodnout, jestli se budou generovat SEALED třídy, DATA třídy neob obyč třídy

            // vygenerování DTO třídy pro každý záznam @Dto anotace
            kreatorAnnotation.dtos.forEach { dto ->
                logger.info("Creating dto ${dto.name}")

                val name = dto.name
                val pickList = dto.pick
                val omitList = dto.omit

                // TODO kontrola, jestli uživatel nezadal chybný název vlastnosti?
                val selectedProps = when {
                    pickList.isNotEmpty() -> properties.filter { it.simpleName.asString() in pickList }
                    omitList.isNotEmpty() -> properties.filter { it.simpleName.asString() !in omitList }
                    else -> throw IllegalArgumentException("DTO $name of class ${annotatedClass.simpleName.asString()} must define either 'pick' or 'omit' as non-empty array.")
                }

                generateDtoClass(annotatedClass, name, selectedProps)
            }
        }

        return emptyList()
    }

    private fun generateDtoClass(
        originClass: KSClassDeclaration,
        className: String,
        properties: List<KSPropertyDeclaration>
    ) {
        // package
        val packageName = originClass.packageName.asString()

        // soubor
        val fileSpecBuilder = FileSpec.builder(packageName, className)

        // třída
        val classSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(KModifier.DATA) // TODO Data vs Sealed vs Ordinary

        // konstruktor
        val constructorBuilder = FunSpec.constructorBuilder()
        for (prop in properties) {
            val name = prop.simpleName.asString()
            val type = prop.type.resolve()
            val typeName = type.toTypeName().copy(nullable = type.nullability == Nullability.NULLABLE)

            val propSpec = PropertySpec.builder(name, typeName)
                .initializer(name)
                .build()

            constructorBuilder.addParameter(name, typeName)
            classSpecBuilder.addProperty(propSpec)
        }

        // nastavení konstruktoru
        classSpecBuilder.primaryConstructor(constructorBuilder.build())

        // přidání třídy do souboru
        fileSpecBuilder.addType(classSpecBuilder.build())

        // zápis souboru na disk
        fileSpecBuilder.build().writeTo(codeGenerator, Dependencies(false, originClass.containingFile!!))

        logger.info("DTO class with name $className was generated")
    }
}
