package it.attendance100.mybicocca.codegen.esse3

import it.attendance100.mybicocca.codegen.esse3.gen.ApiGenerator
import it.attendance100.mybicocca.codegen.esse3.gen.DtoGenerator
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

    val dtoOutputDir = File(outputDir, "dto/esse3")
    val apiOutputDir = File(outputDir, "api/esse3")
    dtoOutputDir.mkdirs()
    apiOutputDir.mkdirs()

    val dictionaryFile = File("dictionary.json")
    val dictionary = Dictionary.load(dictionaryFile)

    val enumValuesFile = File("enum-values.json")
    val enumValueFields = EnumValueFields.load(enumValuesFile)

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
        "Esse3CommonTypes.kt", "Esse3TypeConverters.kt"
    )
    for (dir in listOf(dtoOutputDir, apiOutputDir)) {
        dir.listFiles { f -> f.name.startsWith("Esse3") && f.extension == "kt" && f.name !in preservedFiles }
            ?.forEach { it.delete() }
    }

    val generatedFiles = mutableListOf<String>()
    for (spec in dedupedSpecs) {
        if (spec.definitions.isNotEmpty()) {
            DtoGenerator.generate(spec, dtoOutputDir, dictionary, enumValueFields, requiredOverrides)
            val fileName = dictionary.translate("Esse3${spec.specName}Types.kt")
            generatedFiles.add("dto/esse3/$fileName")
        }

        if (spec.operations.isNotEmpty()) {
            ApiGenerator.generate(spec, apiOutputDir, dictionary)
            val className = dictionary.translate("Esse3${spec.specName}Api")
            generatedFiles.add("api/esse3/$className.kt")
        }
    }

    FacadeGenerator.generate(dedupedSpecs, apiOutputDir, dictionary)
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

    val dictionaryMap = linkedMapOf<String, String>()
    for (key in dictionary.keys) {
        if (key in names) {
            dictionaryMap[key] = dictionary.existingValueOrEmpty(key)
        }
    }
    val newKeys = names.filter { it !in dictionary.keys }.sorted()
    for (key in newKeys) {
        dictionaryMap[key] = ""
    }
    val json = Json { prettyPrint = true }
    val jsonObject = JsonObject(dictionaryMap.mapValues { JsonPrimitive(it.value) })
    dictionaryFile.writeText(json.encodeToString(JsonObject.serializer(), jsonObject))
    println()
    println("Updated ${dictionaryFile.absolutePath} with ${dictionaryMap.size} entries")

    val enumFieldKeys = mutableSetOf<String>()
    for (spec in dedupedSpecs) {
        for (definition in spec.definitions) {
            for (prop in definition.properties) {
                if (!prop.enumValues.isNullOrEmpty()) {
                    enumFieldKeys.add("${definition.name}.${prop.wireName}")
                }
            }
        }
    }

    val enumValuesMap = linkedMapOf<String, Boolean>()
    for (key in enumValueFields.keys) {
        if (key in enumFieldKeys) {
            enumValuesMap[key] = enumValueFields.existingValueOrNull(key) ?: false
        }
    }
    for (key in enumFieldKeys.sorted()) {
        if (key !in enumValuesMap) {
            enumValuesMap[key] = false
        }
    }
    val enumJsonObject = JsonObject(enumValuesMap.mapValues { JsonPrimitive(it.value) })
    enumValuesFile.writeText(json.encodeToString(JsonObject.serializer(), enumJsonObject))
    println("Updated ${enumValuesFile.absolutePath} with ${enumValuesMap.size} entries (${enumValuesMap.count { it.value }} enabled)")

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
