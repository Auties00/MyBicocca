package it.attendance100.mybicocca.codegen.esse3

import it.attendance100.mybicocca.codegen.esse3.gen.ApiGenerator
import it.attendance100.mybicocca.codegen.esse3.gen.DtoGenerator
import it.attendance100.mybicocca.codegen.esse3.gen.EnumTypeGenerator
import it.attendance100.mybicocca.codegen.esse3.gen.FacadeGenerator
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import it.attendance100.mybicocca.codegen.esse3.spec.SpecParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

fun main(args: Array<String>) {
    val specDir = File(args.getOrElse(0) { "./openapi" }).canonicalFile
    val outputDir = File(args.getOrElse(1) { "../src/main/kotlin/it/attendance100/mybicocca/data" }).canonicalFile
    val basePackage = args.getOrElse(2) { "it.attendance100.mybicocca.data" }

    val dtoOutputDir = File(outputDir, "dto/esse3")
    val apiOutputDir = File(outputDir, "api/esse3")
    dtoOutputDir.mkdirs()
    apiOutputDir.mkdirs()

    val glossaryFile = File("glossary.json")
    val glossary = Glossary.load(glossaryFile)

    val enumMappingsFile = File("enum-mappings.json")
    val enumMappings = EnumMappings.load(enumMappingsFile)

    val requiredOverridesFile = File("required-overrides.json")
    val requiredOverrides = RequiredOverrides.load(requiredOverridesFile)

    println("ESSE3 Codegen")
    println("  specDir:   $specDir")
    println("  outputDir: $outputDir")
    println()

    val yamlFiles = specDir.listFiles { f -> f.extension == "yaml" }
        ?.sortedBy { it.name }
        ?: error("No YAML files found in $specDir")

    println("Found ${yamlFiles.size} spec files")
    println()

    val specs = mutableListOf<ParsedSpec>()
    for (yamlFile in yamlFiles) {
        print("Parsing ${yamlFile.name}...")
        try {
            val spec = SpecParser.parse(yamlFile)
            specs.add(spec)
            println(" OK (${spec.specName}: ${spec.operations.size} operations, ${spec.definitions.size} definitions)")
        } catch (e: Exception) {
            println(" FAILED: ${e.message}")
            e.printStackTrace()
        }
    }

    val seenDefinitions = mutableSetOf<String>()
    var dedupCount = 0
    val dedupedSpecs = specs.map { spec ->
        val unique = spec.definitions.filter { seenDefinitions.add(it.name) }
        val removed = spec.definitions.size - unique.size
        dedupCount += removed
        if (removed > 0) {
            spec.copy(definitions = unique)
        } else {
            spec
        }
    }

    println()
    println("Deduplicated $dedupCount duplicate definitions across specs")
    println()

    val preservedFiles = setOf(
        "Esse3AbstractApi.kt",
        "Esse3ErrorTypes.kt", "Esse3PermissionTypes.kt", "Esse3TypeConverters.kt"
    )
    for (dir in listOf(dtoOutputDir, apiOutputDir)) {
        dir.listFiles { f -> f.name.startsWith("Esse3") && f.extension == "kt" && f.name !in preservedFiles }
            ?.forEach { it.delete() }
    }

    val enumTypeMap = EnumTypeGenerator.generate(dedupedSpecs, dtoOutputDir, glossary, enumMappings, basePackage)
    println("Generated ${enumTypeMap.values.toSet().size} sealed enum types")

    val generatedFiles = mutableListOf("dto/esse3/Esse3EnumTypes.kt")
    for (spec in dedupedSpecs) {
        if (spec.definitions.isNotEmpty()) {
            DtoGenerator.generate(spec, dtoOutputDir, glossary, enumTypeMap, requiredOverrides, basePackage)
            val fileName = glossary.translate("Esse3${spec.specName}Types.kt")
            generatedFiles.add("dto/esse3/$fileName")
        }

        if (spec.operations.isNotEmpty()) {
            ApiGenerator.generate(spec, apiOutputDir, glossary, basePackage)
            val className = glossary.translate("Esse3${spec.specName}Api")
            generatedFiles.add("api/esse3/$className.kt")
        }
    }

    FacadeGenerator.generate(dedupedSpecs, apiOutputDir, glossary, basePackage)
    generatedFiles.add("api/esse3/Esse3Api.kt")

    println("Summary: ${generatedFiles.size} files generated")
    for (f in generatedFiles.sorted()) {
        println("  $f")
    }

    val names = mutableSetOf<String>()
    for (spec in dedupedSpecs) {
        if (spec.definitions.isNotEmpty()) {
            names.add("Esse3${spec.specName}Types.kt")
        }
        if (spec.operations.isNotEmpty()) {
            names.add("Esse3${spec.specName}Api.kt")
        }

        if (spec.operations.isNotEmpty()) {
            names.add("Esse3${spec.specName}Api")
        }

        for (definition in spec.definitions) {
            names.add("Esse3${definition.name}")
        }

        for (op in spec.operations) {
            names.add(op.operationId.replaceFirstChar { it.lowercaseChar() })
            for (param in op.pathParams) {
                names.add(param.name)
            }
            for (param in op.queryParams) {
                names.add(param.name)
            }
            op.bodyParam?.let { names.add(it.name) }
        }

        for (definition in spec.definitions) {
            for (prop in definition.properties) {
                names.add(prop.wireName)
            }
        }
    }

    val glossaryMap = linkedMapOf<String, String>()
    for (key in glossary.keys) {
        if (key in names) {
            glossaryMap[key] = glossary.existingValueOrEmpty(key)
        }
    }
    val newKeys = names.filter { it !in glossary.keys }.sorted()
    for (key in newKeys) {
        glossaryMap[key] = ""
    }
    val json = Json { prettyPrint = true }
    val jsonObject = JsonObject(glossaryMap.mapValues { JsonPrimitive(it.value) })
    glossaryFile.writeText(json.encodeToString(JsonObject.serializer(), jsonObject))
    println()
    println("Updated ${glossaryFile.absolutePath} with ${glossaryMap.size} entries")

    val enumMappingsMap = linkedMapOf<String, LinkedHashMap<String, String>>()
    for (spec in dedupedSpecs) {
        for (definition in spec.definitions) {
            for (prop in definition.properties) {
                if (!prop.enumValues.isNullOrEmpty()) {
                    val key = "${definition.name}.${prop.wireName}"
                    val existing = enumMappings.existingMappingOrNull(key)
                    val valueMap = linkedMapOf<String, String>()
                    for (enumValue in prop.enumValues) {
                        valueMap[enumValue] = existing?.get(enumValue) ?: ""
                    }
                    enumMappingsMap[key] = valueMap
                }
            }
        }
    }
    val sortedEnumMappingsMap = enumMappingsMap.entries.sortedBy { it.key }
        .associateTo(linkedMapOf()) { it.key to it.value }
    val enumJsonObject = JsonObject(sortedEnumMappingsMap.mapValues { (_, inner) ->
        JsonObject(inner.mapValues { JsonPrimitive(it.value) })
    })
    enumMappingsFile.writeText(json.encodeToString(JsonObject.serializer(), enumJsonObject))
    println("Updated ${enumMappingsFile.absolutePath} with ${enumMappingsMap.size} entries")

    val requiredFieldKeys = mutableSetOf<String>()
    for (spec in dedupedSpecs) {
        for (definition in spec.definitions) {
            for (wireName in definition.requiredFields) {
                requiredFieldKeys.add("${definition.name}.$wireName")
            }
        }
    }

    val requiredOverridesMap = linkedMapOf<String, Boolean>()
    for (key in requiredOverrides.keys) {
        if (key in requiredFieldKeys) {
            requiredOverridesMap[key] = requiredOverrides.existingValueOrNull(key) ?: true
        }
    }
    for (key in requiredFieldKeys.sorted()) {
        if (key !in requiredOverridesMap) {
            requiredOverridesMap[key] = true
        }
    }
    val requiredJsonObject = JsonObject(requiredOverridesMap.mapValues { JsonPrimitive(it.value) })
    requiredOverridesFile.writeText(json.encodeToString(JsonObject.serializer(), requiredJsonObject))
    val overriddenCount = requiredOverridesMap.count { !it.value }
    println("Updated ${requiredOverridesFile.absolutePath} with ${requiredOverridesMap.size} entries ($overriddenCount overridden to optional)")
}