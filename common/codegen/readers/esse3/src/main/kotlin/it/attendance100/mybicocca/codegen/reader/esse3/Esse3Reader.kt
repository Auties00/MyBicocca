package it.attendance100.mybicocca.codegen.reader.esse3

import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.core.SpecReader
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawSpec
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.SwaggerParser
import kotlinx.serialization.Serializable
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Reads the Esse3 REST API from the Swagger 2.0 specs scraped from Esse3's SwaggerUI, plus
 * three overlay files maintained next to them: the glossary, enum value names and
 * required-field overrides (see [Glossary], [EnumNames] and [RequiredOverrides]).
 */
class Esse3Reader : SpecReader {
    override val id: String = "esse3"

    /** Locations of the inputs, relative to the spec directory. */
    @Serializable
    data class Options(
        val specs: String = "openapi",
        val glossary: String = "glossary.json",
        val enumNames: String = "enum-mappings.json",
        val requiredOverrides: String = "required-overrides.json",
    )

    override fun read(context: ReaderContext): ApiSpec {
        val options = context.options.decodeOptions<Options>()
        val specFiles = context.specDir.resolve(options.specs)
            .listDirectoryEntries()
            .filter { it.isRegularFile() && it.extension == "yaml" }
            .sortedBy { it.name }
        require(specFiles.isNotEmpty()) { "No Esse3 spec files in ${context.specDir.resolve(options.specs)}" }

        val specs = deduplicateDefinitions(specFiles.map(SwaggerParser::parse))
        val glossaryFile = context.specDir.resolve(options.glossary)
        val enumNamesFile = context.specDir.resolve(options.enumNames)
        val requiredOverridesFile = context.specDir.resolve(options.requiredOverrides)

        val spec = Esse3ModelBuilder(
            glossary = Glossary.load(glossaryFile),
            enumNames = EnumNames.load(enumNamesFile),
            requiredOverrides = RequiredOverrides.load(requiredOverridesFile),
        ).build(specs)

        if (context.mayUpdateSpecFiles) {
            val counts = OverlaySync.sync(specs, glossaryFile, enumNamesFile, requiredOverridesFile)
            counts.forEach { (file, count) -> context.log("Synced ${file.name}: $count entries") }
        }
        return spec
    }

    /**
     * The spec files repeat shared definitions; each is kept only in the first file (by file
     * name) that declares it.
     */
    private fun deduplicateDefinitions(specs: List<RawSpec>): List<RawSpec> {
        val seen = mutableSetOf<String>()
        return specs.map { spec -> spec.copy(definitions = spec.definitions.filter { seen.add(it.name) }) }
    }
}
