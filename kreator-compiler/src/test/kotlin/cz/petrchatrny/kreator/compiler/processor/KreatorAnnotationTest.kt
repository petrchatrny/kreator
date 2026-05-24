package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.FieldConstantsProcessorProvider
import cz.petrchatrny.kreator.compiler.provider.KreatorProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import util.configureCompilation
import util.getKotlinSourceFile
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCompilerApi::class)
class KreatorAnnotationTest {

    @Test
    fun `check all class files are generated`() {
        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile }
            .toCollection(mutableSetOf())

        // then
        assertEquals(4, generatedFiles.size)
        assertEquals(
            setOf("MtgCardRefDto.kt", "MtgCardCreateDto.kt", "MtgCardUpdateDto.kt", "MtgCardListDto.kt"),
            generatedFiles.map { it.name }.toSet()
        )
    }

    @Test
    fun `check classes are sealed`() {
        // given
        val source = getKotlinSourceFile("StudentExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider(), FieldConstantsProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.isFile && it.name == "StudentDto.kt" }
            .readText()

        // then
        assertContains(generatedCode, "sealed class StudentDto")
        assertContains(generatedCode, "data class Create")
        assertContains(generatedCode, "data class Update")
    }

    @Test
    fun `check classes are not sealed`() {
        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedFiles = compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile }
            .toCollection(mutableSetOf())

        // then
        assertEquals(4, generatedFiles.size)
        assertEquals(
            setOf("MtgCardRefDto.kt", "MtgCardCreateDto.kt", "MtgCardUpdateDto.kt", "MtgCardListDto.kt"),
            generatedFiles.map { it.name }.toSet()
        )

        val createDtoCode = generatedFiles.first { it.name == "MtgCardCreateDto.kt" }.readText()
        assertContains(createDtoCode, "class MtgCardCreateDto")
        assertFalse(createDtoCode.contains("sealed class"))
    }

}
