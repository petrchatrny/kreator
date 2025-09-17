package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.DtoFieldsProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import util.getCompilation
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class DtoFieldsProcessorTest {

    @Test
    fun generateFields() {

        // zdrojový kód, který procesor zpracuje
        val source = SourceFile.kotlin(
            "Person.kt", """
            package com.example.api
            import cz.petrchatrny.kreator.annotations.DtoFields

            @DtoFields
            class Person(val name: String)
        """
        )

        val compilation = getCompilation(
            sources = listOf(source),
            providers = mutableListOf(DtoFieldsProcessorProvider())
        )
        compilation.compile()

        val generatedSourcesDir = compilation.kspSourcesDir.path
        val generatedFile = File(generatedSourcesDir, "/kotlin/com/example/api/PersonFields.kt")
        val actualSourceCode = generatedFile.readText()

        assertTrue(actualSourceCode.contains("object PersonFields"), "Expected PersonFields object")
        assertTrue(actualSourceCode.contains("const val name: String = \"name\""), "Expected property 'name'")

        val expectedSourceCode = """
            package com.example.api

            import kotlin.String

            public object PersonFields {
              public const val name: String = "name"
            }
        """
        assertEquals(expectedSourceCode.trimIndent(), actualSourceCode.trimIndent())
    }

}


//package cz.petrchatrny.kreator.compiler.processor
//
//import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
//import com.google.devtools.ksp.processing.SymbolProcessorProvider
//import com.google.devtools.ksp.processing.impl.KSPCompilation
//import com.google.devtools.ksp.symbol.KSClassDeclaration
//import com.google.devtools.ksp.symbol.KSFile
//import com.google.devtools.ksp.test.KotlinSymbolProcessingTest
//import org.junit.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class DtoFieldsProcessorTest {
//
//    @Test
//    fun generateFields() {
//        // zdrojový kód, který procesor zpracuje
//        val sources = listOf(
//            KotlinSymbolProcessingTest.SourceFile(
//                "Person.kt",
//                """
//                @DtoFields
//                class Person(val name: String)
//            """
//            )
//        )
//
//        val result = KotlinSymbolProcessingTest.runTest(
//            sources = sources,
//            symbolProcessorProvider = SymbolProcessorProvider { env: SymbolProcessorEnvironment ->
//                DtoFieldsProcessor(
//                    codeGenerator = env.codeGenerator,
//                    logger = env.logger
//                )
//            }
//        )
//
//        // kontrola, že kompilace proběhla OK
//        assertEquals(0, result.diagnostics.count { it.severity == KotlinSymbolProcessingTest.Diagnostic.Severity.ERROR })
//
//        // kontrola obsahu vygenerovaných souborů
//        val generated = result.generatedFiles.joinToString("\n") { it.readText() }
//        println("Generated files:\n$generated")
//
//        assertTrue(generated.contains("object PersonFields"), "Expected PersonFields object")
//        assertTrue(generated.contains("const val name = \"name\""), "Expected property 'name'")
//        assertTrue(generated.contains("const val age = \"age\""), "Expected property 'age'")
//    }
//}
