package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.EnumMappings
import it.attendance100.mybicocca.codegen.esse3.Glossary
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import java.io.File

object EnumTypeGenerator {

    private const val PREFIX = "Esse3"

    data class Index(
        val byProperty: Map<String, String>,
        val byParameter: Map<String, String>,
    )

    fun generate(
        specs: List<ParsedSpec>,
        outputDir: File,
        glossary: Glossary,
        enumMappings: EnumMappings,
        basePackage: String
    ): Index {
        val dtoPackage = "$basePackage.esse3.dto"

        val fields = mutableListOf<EnumField>()
        for (spec in specs) {
            for (def in spec.definitions) {
                for (prop in def.properties) {
                    if (prop.enumValues.isNullOrEmpty()) continue
                    val key = "prop:${def.name}.${prop.wireName}"
                    val userMappings = enumMappings.getValueMappings(def.name, prop.wireName)
                    val values = linkedMapOf<String, EnumValue>()
                    for (raw in prop.enumValues) {
                        val name = userMappings?.get(raw)?.ifEmpty { null } ?: sanitizeEnumConstant(raw)
                        values[raw] = EnumValue(name, null)
                    }
                    fields.add(EnumField(key, prop.wireName, FieldKind.Property, values))
                }
            }
            for (op in spec.operations) {
                val candidates = op.queryParams + op.formParams
                for (param in candidates) {
                    if (param.enumValues.isNullOrEmpty()) continue
                    val key = "param:${op.operationId}.${param.name}"
                    val userMappings = enumMappings.getValueMappings(op.operationId, param.name)
                    val values = linkedMapOf<String, EnumValue>()
                    for (raw in param.enumValues) {
                        val name = userMappings?.get(raw)?.ifEmpty { null } ?: sanitizeEnumConstant(raw)
                        val doc = param.enumValueDocs?.get(raw)
                        values[raw] = EnumValue(name, doc)
                    }
                    val basis = "${op.operationId.replaceFirstChar { it.uppercaseChar() }}${param.name.replaceFirstChar { it.uppercaseChar() }}"
                    fields.add(EnumField(key, basis, FieldKind.Parameter, values))
                }
            }
        }

        val groups = fields.groupBy { it.values.entries.map { (k, v) -> k to v.kotlinName }.sortedBy { it.first } }

        val keyToClassName = mutableMapOf<String, String>()
        val types = mutableListOf<EnumTypeInfo>()
        val usedClassNames = mutableSetOf<String>()

        for ((_, group) in groups) {
            val preferred = group.firstOrNull { it.kind == FieldKind.Property } ?: group.first()
            val translated = glossary.translate(preferred.basisName)
            var className = "$PREFIX${translated.replaceFirstChar { it.uppercaseChar() }}"

            if (className in usedClassNames) {
                var i = 2
                while ("${className}$i" in usedClassNames) i++
                className = "${className}$i"
            }
            usedClassNames.add(className)

            types.add(EnumTypeInfo(className, group.first().values))
            for (field in group) {
                keyToClassName[field.key] = className
            }
        }

        if (types.isNotEmpty()) {
            generateFile(outputDir, types.sortedBy { it.className }, dtoPackage)
        }

        val byProperty = mutableMapOf<String, String>()
        val byParameter = mutableMapOf<String, String>()
        for ((key, className) in keyToClassName) {
            when {
                key.startsWith("prop:") -> byProperty[key.removePrefix("prop:")] = className
                key.startsWith("param:") -> byParameter[key.removePrefix("param:")] = className
            }
        }
        return Index(byProperty, byParameter)
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
        val hasUnknownConstant = type.values.values.any { it.kotlinName == "Unknown" }

        sb.appendLine("@Serializable(with = ${type.className}.Serializer::class)")
        sb.appendLine("sealed interface ${type.className} {")
        sb.appendLine("    val value: String")
        sb.appendLine()
        for ((raw, info) in type.values) {
            if (info.doc != null) {
                sb.appendLine("    /** ${singleLine(info.doc)} */")
            }
            sb.appendLine("    data object ${info.kotlinName} : ${type.className} { override val value = \"$raw\" }")
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
        for ((raw, info) in type.values) {
            sb.appendLine("                \"$raw\" -> ${info.kotlinName}")
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

    private fun singleLine(text: String): String {
        return text.replace(Regex("\\s*\\n\\s*"), " ").trim()
    }

    private data class EnumValue(val kotlinName: String, val doc: String?)

    private enum class FieldKind { Property, Parameter }

    private data class EnumField(
        val key: String,
        val basisName: String,
        val kind: FieldKind,
        val values: LinkedHashMap<String, EnumValue>
    )

    private data class EnumTypeInfo(
        val className: String,
        val values: LinkedHashMap<String, EnumValue>
    )
}
