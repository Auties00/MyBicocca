package it.attendance100.mybicocca.codegen.reader.esse3

import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawSpec
import java.nio.file.Path

/**
 * Keeps the overlay files in line with the specs: names and enum values seen for the first
 * time are added with an empty value (to be filled in, e.g. by an LLM for the glossary), and
 * entries the specs no longer use are dropped. Existing values are never changed.
 */
internal object OverlaySync {

    /** @return The number of entries in each file, by file. */
    fun sync(
        specs: List<RawSpec>,
        glossaryFile: Path,
        enumNamesFile: Path,
        requiredOverridesFile: Path,
    ): Map<Path, Int> {
        val glossary = syncGlossary(specs, Glossary.load(glossaryFile))
        val enumNames = syncEnumNames(specs, EnumNames.load(enumNamesFile))
        val requiredOverrides = syncRequiredOverrides(specs, RequiredOverrides.load(requiredOverridesFile))
        writeJsonObject(glossaryFile, glossary)
        writeJsonObject(enumNamesFile, enumNames)
        writeJsonObject(requiredOverridesFile, requiredOverrides)
        return mapOf(glossaryFile to glossary.size, enumNamesFile to enumNames.size, requiredOverridesFile to requiredOverrides.size)
    }

    private fun syncGlossary(specs: List<RawSpec>, glossary: Glossary): Map<String, String> {
        val names = mutableSetOf<String>()
        for (spec in specs) {
            if (spec.definitions.isNotEmpty()) names += "${Esse3ModelBuilder.PREFIX}${spec.name}Types.kt"
            if (spec.operations.isNotEmpty()) {
                names += "${Esse3ModelBuilder.PREFIX}${spec.name}Api.kt"
                names += "${Esse3ModelBuilder.PREFIX}${spec.name}Api"
            }
            for (definition in spec.definitions) {
                names += "${Esse3ModelBuilder.PREFIX}${definition.name}"
                definition.properties.mapTo(names) { it.wireName }
            }
            for (operation in spec.operations) {
                names += operation.operationId.decapitalized()
                (operation.pathParams + operation.queryParams + operation.formParams).mapTo(names) { it.name }
                operation.bodyParam?.let { names += it.name }
                for (parameter in operation.queryParams + operation.formParams) {
                    if (!parameter.enumValues.isNullOrEmpty()) names += operation.operationId.capitalized() + parameter.name.capitalized()
                }
            }
        }
        val kept = glossary.keys.filter { it in names }.associateWith { glossary[it].orEmpty() }
        val added = (names - glossary.keys).sorted().associateWith { "" }
        return kept + added
    }

    private fun syncEnumNames(specs: List<RawSpec>, enumNames: EnumNames): Map<String, Map<String, String>> {
        val entries = mutableMapOf<String, Map<String, String>>()
        fun put(key: String, wireValues: List<String>) {
            val existing = enumNames[key]
            entries[key] = wireValues.associateWith { existing?.get(it) ?: "" }
        }
        for (spec in specs) {
            for (definition in spec.definitions) {
                for (property in definition.properties) {
                    property.enumValues?.takeIf { it.isNotEmpty() }?.let { put("${definition.name}.${property.wireName}", it) }
                }
            }
            for (operation in spec.operations) {
                for (parameter in operation.queryParams + operation.formParams) {
                    parameter.enumValues?.takeIf { it.isNotEmpty() }?.let { put("${operation.operationId}.${parameter.name}", it) }
                }
            }
        }
        return entries.toSortedMap()
    }

    private fun syncRequiredOverrides(specs: List<RawSpec>, overrides: RequiredOverrides): Map<String, Boolean> {
        val keys = specs.flatMap { spec -> spec.definitions.flatMap { definition -> definition.requiredFields.map { "${definition.name}.$it" } } }.toSet()
        val kept = overrides.keys.filter { it in keys }.associateWith { overrides[it] ?: true }
        val added = (keys - kept.keys).sorted().associateWith { true }
        return kept + added
    }
}
