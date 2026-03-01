package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.Dictionary
import it.attendance100.mybicocca.codegen.esse3.EnumValueFields
import it.attendance100.mybicocca.codegen.esse3.RequiredOverrides
import it.attendance100.mybicocca.codegen.esse3.escapeKotlinKeyword
import it.attendance100.mybicocca.codegen.esse3.renderTranslated
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedDefinition
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import it.attendance100.mybicocca.codegen.esse3.spec.ResolvedType
import it.attendance100.mybicocca.codegen.esse3.spec.TypeMapping
import java.io.File

object DtoGenerator {

    private const val DTO_PACKAGE = "it.attendance100.mybicocca.data.dto.esse3"
    private const val PREFIX = "Esse3"

    fun generate(spec: ParsedSpec, outputDir: File, dictionary: Dictionary, enumValueFields: EnumValueFields, requiredOverrides: RequiredOverrides) {
        if (spec.definitions.isEmpty()) return

        val originalFileName = "${PREFIX}${spec.specName}Types.kt"
        val fileName = dictionary.translate(originalFileName)
        val file = File(outputDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("package $DTO_PACKAGE")
        sb.appendLine()

        val imports = collectImports(spec.definitions, dictionary)
        for (imp in imports.sorted()) {
            sb.appendLine("import $imp")
        }
        if (imports.isNotEmpty()) sb.appendLine()

        for ((index, definition) in spec.definitions.withIndex()) {
            if (index > 0) sb.appendLine()
            generateDataClass(sb, definition, dictionary, enumValueFields, requiredOverrides)
        }

        file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }

    private fun collectImports(definitions: List<ParsedDefinition>, dictionary: Dictionary): Set<String> {
        val imports = mutableSetOf<String>()
        imports.add("kotlinx.serialization.SerialName")
        imports.add("kotlinx.serialization.Serializable")

        for (def in definitions) {
            for (prop in def.properties) {
                val resolved = TypeMapping.resolvePropertyType(prop)
                val rendered = resolved.renderTranslated(dictionary, PREFIX)

                if ("LocalDate?" in rendered && "LocalDateTime?" !in rendered) {
                    imports.add("java.time.LocalDate")
                }
                if ("LocalDateTime?" in rendered) {
                    imports.add("java.time.LocalDateTime")
                }
            }
        }

        return imports
    }

    private fun generateDataClass(sb: StringBuilder, definition: ParsedDefinition, dictionary: Dictionary, enumValueFields: EnumValueFields, requiredOverrides: RequiredOverrides) {
        val className = dictionary.translate("$PREFIX${definition.name}")

        sb.appendLine("@Serializable")
        sb.appendLine("data class $className(")

        for ((index, prop) in definition.properties.withIndex()) {
            val isRequired = requiredOverrides.isRequired(definition.name, prop.wireName)
                ?: (prop.wireName in definition.requiredFields)
            val resolved = TypeMapping.resolvePropertyType(prop)
            val kotlinType = resolved.renderTranslated(dictionary, PREFIX)
            val serializer = when {
                enumValueFields.needsSerializer(definition.name, prop.wireName) -> {
                    if (isRequired) "Esse3EnumValueSerializer" else "Esse3NullableEnumValueSerializer"
                }
                else -> TypeMapping.needsSerializer(prop.type, prop.format)
            }

            if (serializer != null) {
                sb.appendLine("    @Serializable(with = ${serializer}::class)")
            }

            val rawFieldName = dictionary.translate(prop.wireName)
            val fieldName = if (rawFieldName != prop.wireName) rawFieldName.replaceFirstChar { it.lowercaseChar() } else rawFieldName
            sb.appendLine("    @SerialName(\"${prop.wireName}\")")

            val defaultValue = computeDefault(resolved, kotlinType, isRequired)
            val effectiveType = computeEffectiveType(kotlinType, isRequired, resolved)

            sb.append("    val ${escapeKotlinKeyword(fieldName)}: $effectiveType")
            if (defaultValue != null) {
                sb.append(" = $defaultValue")
            }

            if (index < definition.properties.size - 1) {
                sb.appendLine(",")
                sb.appendLine()
            } else {
                sb.appendLine()
            }
        }

        sb.appendLine(")")
    }

    private fun computeEffectiveType(kotlinType: String, isRequired: Boolean, resolved: ResolvedType): String {
        if (TypeMapping.isDateType(kotlinType)) return kotlinType
        if (resolved is ResolvedType.ListOf) return kotlinType
        if (!isRequired) {
            return if (kotlinType.endsWith("?")) kotlinType else "$kotlinType?"
        }
        return kotlinType
    }

    private fun computeDefault(resolved: ResolvedType, kotlinType: String, isRequired: Boolean): String? {
        if (TypeMapping.isDateType(kotlinType)) return "null"
        if (resolved is ResolvedType.ListOf) return "emptyList()"
        if (!isRequired) return "null"
        return null
    }
}
