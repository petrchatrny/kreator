package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.FieldConstantsProcessorProvider
import cz.petrchatrny.kreator.compiler.provider.KreatorProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import util.configureCompilation
import util.getKotlinSourceFile
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class AnnotationCooperationTest {

    @Test
    fun `check annotations @FieldConstants and @Kreator cooperate`() {
        // given
        val source = getKotlinSourceFile("BookExample.kt")

        // when
        val compilation = configureCompilation(
            sources = listOf(source),
            providers = mutableListOf(FieldConstantsProcessorProvider(), KreatorProcessorProvider())
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
            setOf("BookFields.kt","BookCreateDto.kt", "BookListDto.kt"),
            generatedFiles.map { it.name }.toSet()
        )
    }

}
