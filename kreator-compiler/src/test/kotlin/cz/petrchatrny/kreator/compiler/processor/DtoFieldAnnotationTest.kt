package cz.petrchatrny.kreator.compiler.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import cz.petrchatrny.kreator.compiler.provider.FieldConstantsProcessorProvider
import cz.petrchatrny.kreator.compiler.provider.KreatorProcessorProvider
import junit.framework.TestCase.assertFalse
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import util.configureCompilation
import util.getKotlinSourceFile
import kotlin.test.assertContains
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class DtoFieldAnnotationTest {

    @Test
    fun `check field is renamed`() {
        // given
        val source = getKotlinSourceFile("InvoiceExample.kt")

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

        val createDto = generatedFiles.first { it.name == "InvoiceCreateDto.kt" }.readText()
        val listDto = generatedFiles.first { it.name == "InvoiceListDto.kt" }.readText()
        val internalDto = generatedFiles.first { it.name == "InvoiceInternalDto.kt" }.readText()

        // then
        assertContains(createDto, "val number")
        assertContains(createDto, "val total")
        assertContains(createDto, "val customer")
        assertFalse(createDto.contains("val customerName"))

        assertContains(listDto, "val totalFormatted")
        assertContains(listDto, "val total:")
        assertContains(listDto, "val customer")

        assertContains(internalDto, "val number")
        assertContains(internalDto, "val totalCents")
        assertFalse(internalDto.contains("val total:"))
    }

    @Test
    fun `check field has changed data type`() {
        // given
        val source = getKotlinSourceFile("InvoiceExample.kt")

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

        val createDto = generatedFiles.first { it.name == "InvoiceCreateDto.kt" }.readText()
        val listDto = generatedFiles.first { it.name == "InvoiceListDto.kt" }.readText()
        val internalDto = generatedFiles.first { it.name == "InvoiceInternalDto.kt" }.readText()

        // then
        assertContains(createDto, "val number: Long")
        assertContains(createDto, "val total: Long")
        assertContains(createDto, "val customer: String")
        assertContains(createDto, "val billingAddress: BillingAddressCreateDto")

        assertContains(listDto, "val totalFormatted: String")
        assertContains(listDto, "val total: BigDecimal")
        assertContains(listDto, "val customerName: String")

        assertContains(internalDto, "val number: Long")
        assertContains(internalDto, "val totalCents: Long")
    }

    @Test
    fun `check expression is used in fromDomain mapping method`() {
        // TODO
    }

    @Test
    fun `check expression is used in toDomain mapping method`() {
        // TODO
    }
}
