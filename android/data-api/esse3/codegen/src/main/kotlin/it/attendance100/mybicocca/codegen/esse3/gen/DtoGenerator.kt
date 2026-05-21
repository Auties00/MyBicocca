package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.Glossary
import it.attendance100.mybicocca.codegen.esse3.RequiredOverrides
import it.attendance100.mybicocca.codegen.esse3.escapeKotlinKeyword
import it.attendance100.mybicocca.codegen.esse3.renderTranslated
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedDefinition
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import it.attendance100.mybicocca.codegen.esse3.spec.ResolvedType
import it.attendance100.mybicocca.codegen.esse3.spec.TypeMapping
import java.io.File

object DtoGenerator {

    private const val PREFIX = "Esse3"

    fun generate(spec: ParsedSpec, outputDir: File, glossary: Glossary, enumTypeMap: Map<String, String>, requiredOverrides: RequiredOverrides, basePackage: String) {
        if (spec.definitions.isEmpty()) return

        val dtoPackage = "$basePackage.esse3.dto"
        val originalFileName = "${PREFIX}${spec.specName}Types.kt"
        val fileName = glossary.translate(originalFileName)
        val file = File(outputDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("package $dtoPackage")
        sb.appendLine()

        val imports = collectImports(spec.definitions, glossary)
        for (imp in imports.sorted()) {
            sb.appendLine("import $imp")
        }
        if (imports.isNotEmpty()) sb.appendLine()

        for ((index, definition) in spec.definitions.withIndex()) {
            if (index > 0) sb.appendLine()
            generateDataClass(sb, definition, glossary, enumTypeMap, requiredOverrides)
        }

        file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }

    private fun collectImports(definitions: List<ParsedDefinition>, glossary: Glossary): Set<String> {
        val imports = mutableSetOf<String>()
        imports.add("kotlinx.serialization.SerialName")
        imports.add("kotlinx.serialization.Serializable")

        for (def in definitions) {
            for (prop in def.properties) {
                val resolved = TypeMapping.resolvePropertyType(prop)
                val rendered = resolved.renderTranslated(glossary, PREFIX)

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

    private fun generateDataClass(sb: StringBuilder, definition: ParsedDefinition, glossary: Glossary, enumTypeMap: Map<String, String>, requiredOverrides: RequiredOverrides) {
        val className = glossary.translate("$PREFIX${definition.name}")

        sb.appendLine("@Serializable")
        sb.appendLine("data class $className(")

        for ((index, prop) in definition.properties.withIndex()) {
            val isRequired = requiredOverrides.isRequired(definition.name, prop.wireName)
                ?: (prop.wireName in definition.requiredFields)
            val resolved = TypeMapping.resolvePropertyType(prop)

            val enumClassName = enumTypeMap["${definition.name}.${prop.wireName}"]
            val kotlinType = enumClassName ?: resolved.renderTranslated(glossary, PREFIX)

            val serializer = if (enumClassName != null) {
                null
            } else {
                TypeMapping.needsSerializer(prop.type, prop.format)
            }

            if (prop.description != null) {
                sb.appendLine("    /** ${singleLine(prop.description)} */")
            }

            if (serializer != null) {
                sb.appendLine("    @Serializable(with = ${serializer}::class)")
            }

            val rawFieldName = glossary.translate(prop.wireName)
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
        return when (kotlinType) {
            "String" -> "\"\""
            "Int" -> "0"
            "Long" -> "0L"
            "Float" -> "0f"
            "Double" -> "0.0"
            "Boolean" -> "false"
            else -> null
        }
    }

    private fun singleLine(text: String): String {
        return text.replace(Regex("\\s*\\n\\s*"), " ").trim()
    }
}