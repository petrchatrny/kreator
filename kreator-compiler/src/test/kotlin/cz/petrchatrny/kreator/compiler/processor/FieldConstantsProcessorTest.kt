package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.FieldConstantsProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import util.configureCompilation
import kotlin.test.assertEquals
import org.junit.Test
import util.getKotlinSourceFile
import kotlin.test.assertContains

@OptIn(ExperimentalCompilerApi::class)
class FieldConstantsProcessorTest {

    @Test
    fun `check constants objects are generated for all classes`() {
        // given
        val source = getKotlinSourceFile("SocialSiteExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(FieldConstantsProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile }
            .toCollection(mutableSetOf())

        // then
        assertEquals(3, generatedFiles.size)
        assertEquals(
            setOf("PostFields.kt", "UserFields.kt", "ReplyFields.kt"),
            generatedFiles.map { it.name }.toSet()
        )
    }

    @Test
    fun `check constants objects contain all public fields`() {
        // given
        val source = getKotlinSourceFile("SocialSiteExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(FieldConstantsProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.name == "PostFields.kt" }
            .readText()

        // then
        assertContains(generatedCode, "object PostFields")
        assertContains(generatedCode, "const val author: String = \"author\"")
        assertContains(generatedCode, "const val title: String = \"title\"")
        assertContains(generatedCode, "const val text: String = \"text\"")
        assertContains(generatedCode, "const val created_at: String = \"created_at\"")
        assertContains(generatedCode, "const val comměnts: String = \"comměnts\"")
        assertContains(generatedCode, "const val `set`: String = \"set\"")
    }

    @Test
    fun `check constants objects contains inherited fields`() {
        // given
        val source = getKotlinSourceFile("SocialSiteExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(FieldConstantsProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.name == "ReplyFields.kt" }
            .readText()

        // then
        assertContains(generatedCode, "object ReplyFields")
        assertContains(generatedCode, "const val author: String = \"author\"")
        assertContains(generatedCode, "const val text: String = \"text\"")
        assertContains(generatedCode, "const val repliesTo: String = \"repliesTo\"")
        assertContains(generatedCode, "const val createdAt: String = \"createdAt\"")
        assertContains(generatedCode, "const val updatedAt: String = \"updatedAt\"")
    }

    @Test
    fun `check constants objects do not contain private fields`() { // TODO think about this
//        // given
//        val source = getKotlinSourceFile("SocialSiteExample.kt")
//
//        // when
//        val compilation = configureCompilation(
//            sources = listOf(source),
//            providers = mutableListOf(DtoFieldsProcessorProvider())
//        )
//        val result = compilation.compile()
//        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
//
//        val generatedCode = compilation.kspSourcesDir
//            .walkTopDown()
//            .first { it.name == "UserFields.kt" }
//            .readText()
//
//        // then
//        assertFalse(generatedCode.contains("const val password: String = \"password\""))
    }
}
