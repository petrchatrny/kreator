package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.KreatorProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import util.configureCompilation
import util.getKotlinSourceFile
import kotlin.test.assertEquals

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
            setOf("MtgCardRefDto.kt","MtgCardCreateDto.kt", "MtgCardUpdateDto.kt", "MtgCardListDto.kt"),
            generatedFiles.map { it.name }.toSet()
        )
    }

    @Test
    fun `check classes are sealed`() {
        // TODO
    }

    @Test
    fun `check classes are not sealed`() {
        // TODO
    }

}
