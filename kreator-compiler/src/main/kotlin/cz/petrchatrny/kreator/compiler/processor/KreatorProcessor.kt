package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.ClassType
import cz.petrchatrny.kreator.annotations.Conversion
import cz.petrchatrny.kreator.annotations.DtoField
import cz.petrchatrny.kreator.annotations.Kreator
import java.util.ArrayList

@OptIn(KspExperimental::class)
class KreatorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private var resolver: Resolver? = null
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver

        // najít všechny třídy anotované anotací @Kreator
        val symbols = resolver.getSymbolsWithAnnotation(Kreator::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        for (annotatedClass in symbols) {
            val properties = annotatedClass.getAllProperties().toList()

            // získání anotace @Kreator a její parametry z anotované třídy
            val kreatorAnnotation: Kreator = annotatedClass.getAnnotationsByType(Kreator::class).first()

            // TODO vybrat z Kreator anotace jestli bude výsledek Sealed nebo ne

            // vygenerování DTO třídy pro každý záznam @Dto anotace
            kreatorAnnotation.dtos.forEach { dto ->
                logger.info("Creating dto ${dto.name}")

                // TODO všechno co je tady pod tímto komentářem přemístit do generateDtoClass funkce

                val name = dto.name
                val pickList = dto.pick
                val omitList = dto.omit

                // TODO kontrola, jestli uživatel nezadal chybný název vlastnosti, takovém případě použít warning
                // výběr správných vlastností pro výslednou třídu na základě argumentů pick/omit
                val selectedProps = when {
                    pickList.isNotEmpty() -> properties.filter { it.simpleName.asString() in pickList }
                    omitList.isNotEmpty() -> properties.filter { it.simpleName.asString() !in omitList }
                    else -> throw IllegalArgumentException("DTO $name of class ${annotatedClass.simpleName.asString()} must define either 'pick' or 'omit' as non-empty array.")
                }

                generateDtoClass(
                    originClass = annotatedClass,
                    dtoClassName = name,
                    dtoClassType = kreatorAnnotation.classType,
                    properties = selectedProps,
                    conversion = dto.conversion
                )
            }
        }

        return emptyList()
    }

    private fun generateDtoClass(
        originClass: KSClassDeclaration,
        dtoClassName: String,
        dtoClassType: ClassType,
        properties: List<KSPropertyDeclaration>,
        conversion: Conversion
    ) {
        // package
        val packageName = originClass.packageName.asString()

        // soubor
        val fileSpecBuilder = FileSpec.builder(packageName, dtoClassName)

        // třída
        val classSpecBuilder = TypeSpec.classBuilder(dtoClassName)
        when (dtoClassType) {
            ClassType.CLASS -> {}
            ClassType.DATA_CLASS -> {
                classSpecBuilder.addModifiers(KModifier.DATA)
            }
        }

        // konstruktor
        val constructorBuilder = FunSpec.constructorBuilder()
        for (prop in properties) {
            processDtoAttribute(prop, dtoClassName, constructorBuilder, classSpecBuilder)
        }

        // nastavení konstruktoru
        classSpecBuilder.primaryConstructor(constructorBuilder.build())

        // přidání třídy do souboru
        fileSpecBuilder.addType(classSpecBuilder.build())

        // zápis souboru na disk
        fileSpecBuilder.build().writeTo(codeGenerator, Dependencies(false, originClass.containingFile!!))
    }

    // TODO k tomuto se zkusit vrátit, protože použití resolve() metody a výběr argumentů na základě indexů není ideální
    private fun processDtoAttribute(
        prop: KSPropertyDeclaration,
        dtoClassName: String,
        constructorBuilder: FunSpec.Builder,
        classSpecBuilder: TypeSpec.Builder,
    ) {
        // získání použitých DtoAttribute anotací
        val attributeAnnotation = prop.annotations.toList()
            .filter { ann -> ann.annotationType.resolve().declaration.qualifiedName?.asString() == DtoField::class.qualifiedName }
            .firstOrNull { ann -> (ann.arguments[0].value as ArrayList<*>).contains(dtoClassName) }

        var name: String
        var type: KSType
        if (attributeAnnotation != null) {
            name = attributeAnnotation.arguments[1].value as String
            type = attributeAnnotation.arguments[2].value as KSType

            // kontrola výchozího typu Any, pokud uživatel nespecifikoval vlastní typ, použít výchozí typ property
            if (type == this.resolver?.builtIns?.anyType) {
                type = prop.type.resolve()
            }
        } else {
            name = prop.simpleName.asString()
            type = prop.type.resolve()
        }

        val typeName = type.toTypeName().copy(nullable = type.nullability == Nullability.NULLABLE)

        val propSpec = PropertySpec.builder(name, typeName)
            .initializer(name)
            .build()

        constructorBuilder.addParameter(name, typeName)
        classSpecBuilder.addProperty(propSpec)
    }

    /**
     * Metoda přidá do vznikající DTO třídy novou funkci. Tato funkce bude sloužit jako konverze mezi
     * novou DTO třídou a existující DOMAIN třídou. Implementace třídy bude jednoduše return a
     * za ním volání konstruktoru dané třídy.
     */
//    private fun addDtoToDomainConversion(
//        properties: List<KSPropertyDeclaration>,
//        dtoClassName: String,
//        classSpecBuilder: TypeSpec.Builder
//    ) {
//        // sestavíme argumenty pro konstruktor
//        val args = properties.joinToString(", ") { prop ->
//            val dtoName = getDtoPropertyName(prop, dtoClassName) // funkce níže
//            "${prop.simpleName.asString()} = this.$dtoName"
//        }
//
//        val fn = FunSpec.builder("toDomain")
//            .returns(originType)
//            .addStatement("return %T($args)", originType)
//            .build()
//
//        classSpecBuilder.addFunction(fn)
//    }

    /**
     * Metoda vytvoří extension funkci pro Domain třídu. Tato extension funkce bude sloužit
     * pro převod DOMAIN třídy na DTO třídu. Implementačně bude funkce obsahovat pouze return a volání
     * konstruktoru DTO třídy. Pokud nebude možné napasovat argumenty, vyhodí exception.
     */
    private fun addDomainToDtoConversion(originClass: KSClassDeclaration, dtoClassName: String) {
//        // Domain -> DTO
//        val packageName = originClass.packageName.asString()
//        val originType = originClass.toClassName()
//        val dtoType = ClassName(packageName, dtoClassName)
//
//        // argumenty konstruktoru DTO
//        val args = properties.joinToString(", ") { prop ->
//            val dtoProp = getDtoPropertyName(prop, dtoClassName)
//            "$dtoProp = this.${prop.simpleName.asString()}"
//        }
//
//        val funSpec = FunSpec.builder("to${dtoClassName}")
//            .receiver(originType)
//            .returns(dtoType)
//            .addStatement("return %T($args)", dtoType)
//            .build()
//
//        val file = FileSpec.builder(packageName, "DomainTo${dtoClassName}Extensions")
//            .addFunction(funSpec)
//            .build()
//
//        file.writeTo(codeGenerator, Dependencies(false))
    }
}
