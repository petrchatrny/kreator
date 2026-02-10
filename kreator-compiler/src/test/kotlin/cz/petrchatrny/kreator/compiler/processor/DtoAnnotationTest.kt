package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.KreatorProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import util.configureCompilation
import util.getKotlinSourceFile
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCompilerApi::class)
class DtoAnnotationTest {

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
            setOf("MtgCardRefDto.kt","MtgCardCreateDto.kt", "MtgCardUpdateDto.kt", "MtgCardListDto.kt"),
            generatedFiles.map { it.name }.toSet()
        )
    }

    @Test
    fun `check class has correct name`() {
        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.isFile && it.name == "MtgCardCreateDto.kt" }
            .readText()

        // then
        assertContains(generatedCode, "class MtgCardCreateDto")
    }

    @Test
    fun `check class has correct type`() {
        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val files = compilation.kspSourcesDir
            .walkTopDown()
            .filter { it.isFile }
            .toCollection(mutableSetOf())

        val createDto = files.first { it.name == "MtgCardCreateDto.kt" }.readText()
        val updateDto = files.first { it.name == "MtgCardUpdateDto.kt" }.readText()

        // then
        assertContains(createDto, "public data class MtgCardCreateDto")
        assertContains(updateDto, "public class MtgCardUpdateDto")
    }

    @Test
    fun `check only pick properties are included`() {
        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.isFile && it.name == "MtgCardListDto.kt" }
            .readText()

        // then
        assertContains(generatedCode, "val id: UUID")
        assertContains(generatedCode, "val name: String")
        assertContains(generatedCode, "val type: String?")
        assertContains(generatedCode, "val manaCost: Map<MtgCard.ManaColor, Int>")

        assertFalse (generatedCode.contains("val rarity: Int"))
        assertFalse (generatedCode.contains("val isFoil: Boolean"))
        assertFalse (generatedCode.contains("val createdByUser: String"))
        assertFalse (generatedCode.contains("var description: String?"))
        assertFalse (generatedCode.contains("var updatedByUser: String?"))
    }

    @Test
    fun `check only omit properties are excluded`() {
        // TODO check default expression of properties

        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.isFile && it.name == "MtgCardUpdateDto.kt" }
            .readText()

        // then
        assertContains(generatedCode, "val name: String")
        assertContains(generatedCode, "val type: String?")
        assertContains(generatedCode, "val rarity: Int")
        assertContains(generatedCode, "val isFoil: Boolean")
        assertContains(generatedCode, "val manaCost: Map<MtgCard.ManaColor, Int>")
        assertContains(generatedCode, "val description: String?")
        assertContains(generatedCode, "val updatedByUser: String?")

        assertFalse (generatedCode.contains("val id: UUID"))
        assertFalse (generatedCode.contains("val createdByUser: String"))
    }

    @Test
    fun `check class contains no conversion method`() {
        // TODO
    }

    @Test
    fun `check class contains from-conversion method`() {
        // given
        val source = getKotlinSourceFile("MtgExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(KreatorProcessorProvider())
        )
        val result = compilation.compile()
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val generatedCode = compilation.kspSourcesDir
            .walkTopDown()
            .first { it.isFile && it.name == "MtgCardCreateDto.kt" }
            .readText()

        // then
        assertContains(generatedCode, "fun toDomain(): MtgCard")
    }

    @Test
    fun `check class contains to-conversion method`() {
        // TODO
    }
}
