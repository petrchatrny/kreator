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
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import cz.petrchatrny.kreator.annotations.ClassType
import cz.petrchatrny.kreator.annotations.Conversion
import cz.petrchatrny.kreator.annotations.Dto
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

        // seznam tříd, které budou zadrženy pro kompilaci i v dalším kole
        val deferred = mutableListOf<KSClassDeclaration>()

        for (annotatedClass in symbols) {
            try {
                val properties = annotatedClass.getAllProperties().toList()

                // získání anotace @Kreator a její parametry z anotované třídy
                val kreatorAnnotation: Kreator = annotatedClass.getAnnotationsByType(Kreator::class).first()

                // TODO vybrat z Kreator anotace jestli bude výsledek Sealed nebo ne
                // kreatorAnnotation.isSealed

                // vygenerování DTO třídy pro každou použitou @Dto anotaci
                val dtoFiles = mutableListOf<FileSpec>()
                kreatorAnnotation.dtos.forEach { dto ->
                    logger.info("Creating dto ${dto.name}")
                    logger.info("Class: ${annotatedClass.annotations.toList()}")
                    dtoFiles.add(generateDtoFile(originClass = annotatedClass, originProperties = properties, dto = dto))
                }

                // zápis vygenerovaných tříd na disk
                for (file in dtoFiles) {
                    file.writeTo(codeGenerator, Dependencies(false, annotatedClass.containingFile!!))
                }
            }
            catch (_: IllegalStateException) {
                logger.info("Invalid symbol added: $annotatedClass")
                deferred.add(annotatedClass)
            }
        }

        return deferred
    }

    private fun generateDtoFile(
        originClass: KSClassDeclaration,
        originProperties: List<KSPropertyDeclaration>,
        dto: Dto
    ) : FileSpec {
        // package
        val packageName = originClass.packageName.asString()

        // soubor
        val fileSpecBuilder = FileSpec.builder(packageName, dto.name)

        // třída
        val classSpecBuilder = TypeSpec.classBuilder(dto.name)
        when (dto.classType) {
            ClassType.CLASS -> {}
            ClassType.DATA_CLASS -> {
                classSpecBuilder.addModifiers(KModifier.DATA)
            }
        }

        // vlastnosti (výběr správných vlastností pro výslednou třídu na základě argumentů pick/omit)
        // TODO kontrola, jestli uživatel nezadal chybný název vlastnosti, takovém případě použít warning
        val properties = when {
            dto.pick.isNotEmpty() -> originProperties.filter { it.simpleName.asString() in dto.pick }
            dto.omit.isNotEmpty() -> originProperties.filter { it.simpleName.asString() !in dto.omit }
            else -> throw IllegalArgumentException("DTO ${dto.name} of class ${originClass.simpleName.asString()} must define either 'pick' or 'omit' as non-empty array.")
        }

        // konstruktor
        val constructorBuilder = FunSpec.constructorBuilder()
        for (prop in properties) {
            processDtoAttribute(prop, dto.name, constructorBuilder, classSpecBuilder)
        }

        // nastavení konstruktoru
        classSpecBuilder.primaryConstructor(constructorBuilder.build())

        // převodní metody
        when (dto.conversion) {
            Conversion.NONE -> {}
            Conversion.FROM -> {
                addDtoToDomainConversion(classSpecBuilder, originClass)
            }

            Conversion.TO -> {
                addDomainToDtoConversion(originClass, dto.name)
            }

            Conversion.BOTH -> {
                addDtoToDomainConversion(classSpecBuilder, originClass)
                addDomainToDtoConversion(originClass, dto.name)
            }
        }

        // přidání třídy do souboru
        fileSpecBuilder.addType(classSpecBuilder.build())

        // zápis souboru na disk
        return fileSpecBuilder.build()
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
    private fun addDtoToDomainConversion(
        classSpecBuilder: TypeSpec.Builder,
        originClass: KSClassDeclaration
    ) {
        // DTO -> DOMAIN
        // TODO tady se bude muset pracovat i s DtoAnnotation, který atribut se převádí na jaký a jak
        val fn = FunSpec.builder("toDomain")
            .returns(originClass.toClassName())
            .addStatement("""return TODO("NotImplemented")""")
            .build()

        classSpecBuilder.addFunction(fn)
    }

    /**
     * Metoda vytvoří extension funkci pro Domain třídu. Tato extension funkce bude sloužit
     * pro převod DOMAIN třídy na DTO třídu. Implementačně bude funkce obsahovat pouze return a volání
     * konstruktoru DTO třídy. Pokud nebude možné napasovat argumenty, vyhodí exception.
     */
    private fun addDomainToDtoConversion(originClass: KSClassDeclaration, dtoClassName: String) {
        // Domain -> DTO

        // TODO Tady by to šlo ještě vyřešit bez extension funkce na DOMAIN třídu a to tak, že by se vytvořil
        // companion object v DTO a přidala by se statická funkce, která by byla jako argument DOMAIN třídu a vracela by DTO
    }
}
