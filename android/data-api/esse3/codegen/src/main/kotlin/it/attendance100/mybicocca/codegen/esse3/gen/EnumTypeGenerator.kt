package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.EnumMappings
import it.attendance100.mybicocca.codegen.esse3.Glossary
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import java.io.File

object EnumTypeGenerator {

    private const val PREFIX = "Esse3"

    fun generate(
        specs: List<ParsedSpec>,
        outputDir: File,
        glossary: Glossary,
        enumMappings: EnumMappings,
        basePackage: String
    ): Map<String, String> {
        val dtoPackage = "$basePackage.dto.esse3"

        val fields = mutableListOf<EnumField>()
        for (spec in specs) {
            for (def in spec.definitions) {
                for (prop in def.properties) {
                    if (prop.enumValues.isNullOrEmpty()) continue
                    val key = "${def.name}.${prop.wireName}"
                    val userMappings = enumMappings.getValueMappings(def.name, prop.wireName)
                    val values = linkedMapOf<String, String>()
                    for (raw in prop.enumValues) {
                        values[raw] = userMappings?.get(raw)?.ifEmpty { null }
                            ?: sanitizeEnumConstant(raw)
                    }
                    fields.add(EnumField(key, prop.wireName, values))
                }
            }
        }

        val groups = fields.groupBy { it.values.entries.map { (k, v) -> k to v } }

        val fieldToClassName = mutableMapOf<String, String>()
        val types = mutableListOf<EnumTypeInfo>()
        val usedClassNames = mutableSetOf<String>()

        for ((_, group) in groups) {
            val wireName = group.first().wireName
            val translated = glossary.translate(wireName)
            var className = "$PREFIX${translated.replaceFirstChar { it.uppercaseChar() }}"

            if (className in usedClassNames) {
                var i = 2
                while ("${className}$i" in usedClassNames) i++
                className = "${className}$i"
            }
            usedClassNames.add(className)

            types.add(EnumTypeInfo(className, group.first().values))
            for (field in group) {
                fieldToClassName[field.key] = className
            }
        }

        if (types.isNotEmpty()) {
            generateFile(outputDir, types.sortedBy { it.className }, dtoPackage)
        }

        return fieldToClassName
    }

    private fun generateFile(outputDir: File, types: List<EnumTypeInfo>, dtoPackage: String) {
        val sb = StringBuilder()
        sb.appendLine("package $dtoPackage")
        sb.appendLine()
        sb.appendLine("import kotlinx.serialization.KSerializer")
        sb.appendLine("import kotlinx.serialization.Serializable")
        sb.appendLine("import kotlinx.serialization.descriptors.PrimitiveKind")
        sb.appendLine("import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor")
        sb.appendLine("import kotlinx.serialization.descriptors.SerialDescriptor")
        sb.appendLine("import kotlinx.serialization.encoding.Decoder")
        sb.appendLine("import kotlinx.serialization.encoding.Encoder")
        sb.appendLine("import kotlinx.serialization.json.JsonDecoder")
        sb.appendLine("import kotlinx.serialization.json.JsonObject")
        sb.appendLine("import kotlinx.serialization.json.JsonPrimitive")
        sb.appendLine("import kotlinx.serialization.json.jsonPrimitive")
        sb.appendLine()

        for ((index, type) in types.withIndex()) {
            if (index > 0) sb.appendLine()
            generateSealedInterface(sb, type)
        }

        File(outputDir, "Esse3EnumTypes.kt").apply {
            parentFile.mkdirs()
            writeText(sb.toString())
        }
    }

    private fun generateSealedInterface(sb: StringBuilder, type: EnumTypeInfo) {
        val hasUnknownConstant = "Unknown" in type.values.values

        sb.appendLine("@Serializable(with = ${type.className}.Serializer::class)")
        sb.appendLine("sealed interface ${type.className} {")
        sb.appendLine("    val value: String")
        sb.appendLine()
        for ((raw, kotlinName) in type.values) {
            sb.appendLine("    data object $kotlinName : ${type.className} { override val value = \"$raw\" }")
        }
        if (!hasUnknownConstant) {
            sb.appendLine("    data class Unknown(override val value: String) : ${type.className}")
        }
        sb.appendLine()
        sb.appendLine("    object Serializer : KSerializer<${type.className}> {")
        sb.appendLine("        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(\"${type.className}\", PrimitiveKind.STRING)")
        sb.appendLine()
        sb.appendLine("        override fun deserialize(decoder: Decoder): ${type.className} {")
        sb.appendLine("            val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {")
        sb.appendLine("                is JsonPrimitive -> element.content")
        sb.appendLine("                is JsonObject -> element[\"value\"]?.jsonPrimitive?.content ?: \"\"")
        sb.appendLine("                else -> \"\"")
        sb.appendLine("            }")
        sb.appendLine("            return when (raw) {")
        for ((raw, kotlinName) in type.values) {
            sb.appendLine("                \"$raw\" -> $kotlinName")
        }
        if (hasUnknownConstant) {
            sb.appendLine("                else -> Unknown")
        } else {
            sb.appendLine("                else -> Unknown(raw)")
        }
        sb.appendLine("            }")
        sb.appendLine("        }")
        sb.appendLine()
        sb.appendLine("        override fun serialize(encoder: Encoder, value: ${type.className}) {")
        sb.appendLine("            encoder.encodeString(value.value)")
        sb.appendLine("        }")
        sb.appendLine("    }")
        sb.appendLine("}")
    }

    private fun sanitizeEnumConstant(raw: String): String {
        if (raw.isEmpty()) return "Empty"
        return raw.split("_")
            .joinToString("") { part ->
                part.lowercase().replaceFirstChar { it.uppercaseChar() }
            }
    }

    private data class EnumField(
        val key: String,
        val wireName: String,
        val values: LinkedHashMap<String, String>
    )

    private data class EnumTypeInfo(
        val className: String,
        val values: LinkedHashMap<String, String>
    )
}