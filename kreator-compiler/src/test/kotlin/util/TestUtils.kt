package util

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspWithCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.jvm.javaClass

@OptIn(ExperimentalCompilerApi::class)
fun configureCompilation(
    sources: List<SourceFile>,
    providers: MutableList<SymbolProcessorProvider>,
    kspArgs: MutableMap<String, String> = mutableMapOf(),
): KotlinCompilation =
    KotlinCompilation().apply {
        kspWithCompilation = true
        this.sources = sources
        inheritClassPath = true
        kspIncremental = true

        configureKsp(true) {
            kspProcessorOptions = kspArgs
            symbolProcessorProviders +=
                buildList {
                    addAll(providers)
                }
        }
    }


fun getKotlinSourceFile(name: String): SourceFile {
    return SourceFile.kotlin(
        name = name,
        contents = object {}.javaClass.classLoader.getResource(name)!!.readText()
    )
}
