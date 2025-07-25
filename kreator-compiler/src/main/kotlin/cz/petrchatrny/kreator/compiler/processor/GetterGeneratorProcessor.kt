package cz.petrchatrny.kreator.compiler.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import cz.petrchatrny.kreator.annotations.GenerateGetters

class GetterGeneratorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenerateGetters::class.qualifiedName!!)
        symbols.filterIsInstance<KSClassDeclaration>().forEach { generateGetters(it) }
        return emptyList()
    }

    private fun generateGetters(classDeclaration: KSClassDeclaration) {
        // jméno třídy
        val className = classDeclaration.simpleName.asString()
        logger.info("Zpracovávání třídy: $className")

        // jméno balíčku
        val packageName = classDeclaration.packageName.asString()

        // buildery pro vytvoření souboru s extension funkcemi
        val fileBuilder = FileSpec.builder(packageName, "${className}Getters")
        val classBuilder =
            TypeSpec.objectBuilder("${className}Getters") // v souboru vytvoříme třídu typu object, aby bylo možné extesnion metody naimportovat

        // iterování po všech atributech třídy
        classDeclaration.getAllProperties().forEach { property ->
            // název atributu
            val propertyName = property.simpleName.asString()

            // datový typ
            val resolvedType = property.type.resolve()
            val kotlinType = resolveTypeIncludingGenerics(resolvedType)

            // vytvoření getteru pomocí KotlinPoet
            val funSpec = FunSpec.builder("get${propertyName.capitalize()}")
                .returns(kotlinType)
                .addStatement("return this.$propertyName")
                .receiver(ClassName(packageName, className))
                .build()

            classBuilder.addFunction(funSpec)
        }

        fileBuilder.addType(classBuilder.build())

        val fileSpec = fileBuilder.build()
        fileSpec.writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }

    // metoda pro analýzu datových typů včetně jejich genericity
    private fun resolveTypeIncludingGenerics(resolvedType: KSType): TypeName {
        val declaration = resolvedType.declaration
        val qualifiedName = declaration.qualifiedName?.asString() ?: return Any::class.asClassName()
        val isNullable = resolvedType.isMarkedNullable // zapamatování nullable

        // pokud typ obsahuje generické argumenty (parametry), vytvoříme ParameterizedTypeName
        val typeArguments = resolvedType.arguments.map { arg ->
            arg.type?.resolve()?.let { resolveTypeIncludingGenerics(it) }
                ?: Any::class.asClassName()
        }

        // přidání generických argumentů
        var kotlinType = if (typeArguments.isNotEmpty()) {
            ClassName.bestGuess(qualifiedName).parameterizedBy(*typeArguments.toTypedArray())
        } else {
            ClassName.bestGuess(qualifiedName)
        }

        // přidání nullability
        if (isNullable) {
            kotlinType = kotlinType.copy(nullable = true)
        }

        return kotlinType
    }
}