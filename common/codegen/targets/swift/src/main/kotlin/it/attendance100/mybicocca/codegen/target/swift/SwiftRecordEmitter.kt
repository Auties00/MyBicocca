package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.swift.SwiftSyntax.doc

/**
 * Emits records as structs with explicit `Codable` conformance, so that decoding follows the
 * model rather than Swift's strict synthesis: a field with a default takes it when its key is
 * missing or unusable, numbers may arrive as strings, and codecs set custom wire formats.
 */
internal class SwiftRecordEmitter(private val layout: SwiftLayout) {

    fun emit(record: RecordType, out: CodeWriter) {
        out.doc(record.description)
        out.block("public struct ${record.name}: Codable, Hashable, Sendable {") {
            if (record.fields.isEmpty()) {
                line("public init() {}")
                return@block
            }
            for (field in record.fields) {
                doc(field.description)
                line("public var ${name(field)}: ${type(field)}")
            }
            line()
            memberwiseInit(record)
            line()
            block("private enum CodingKeys: String, CodingKey {") {
                for (field in record.fields) {
                    line("case ${name(field)} = ${SwiftSyntax.string(field.wireName)}")
                }
            }
            line()
            block("public init(from decoder: Decoder) throws {") {
                line("let container = try decoder.container(keyedBy: CodingKeys.self)")
                for (field in record.fields) {
                    line("self.${name(field)} = ${decode(field)}")
                }
            }
            line()
            block("public func encode(to encoder: Encoder) throws {") {
                line("var container = encoder.container(keyedBy: CodingKeys.self)")
                for (field in record.fields) {
                    line(encode(field))
                }
            }
        }
    }

    private fun CodeWriter.memberwiseInit(record: RecordType) {
        val parameters = record.fields.map { field ->
            "${name(field)}: ${type(field)}" + (defaultLiteral(field)?.let { " = $it" } ?: "")
        }
        commaList("public init(", parameters, ") {")
        indent {
            for (field in record.fields) {
                line("self.${name(field)} = ${name(field)}")
            }
        }
        line("}")
    }

    private fun name(field: Field): String = SwiftSyntax.identifier(field.name)

    private fun type(field: Field): String = layout.render(field.type) + if (field.nullable) "?" else ""

    private fun key(field: Field): String = ".${name(field)}"

    private fun decode(field: Field): String {
        val type = layout.render(field.type)
        return when (val codec = field.codec) {
            is Codec.Temporal -> {
                val arguments = "$type.self, forKey: ${key(field)}, pattern: ${SwiftSyntax.string(codec.pattern)}, blankAsNull: ${codec.blankAsNull}"
                if (field.nullable) "try container.decodeTemporal($arguments)" else "try container.decodeTemporalRequired($arguments)"
            }
            null -> {
                val lenient = "container.decodeLenient($type.self, forKey: ${key(field)})"
                val default = defaultLiteral(field)
                when {
                    default != null && default != NIL -> "$lenient ?? $default"
                    field.nullable -> lenient
                    else -> "try container.decodeRequired($type.self, forKey: ${key(field)})"
                }
            }
        }
    }

    private fun encode(field: Field): String = when (val codec = field.codec) {
        is Codec.Temporal -> "try container.encodeTemporal(${name(field)}, forKey: ${key(field)}, pattern: ${SwiftSyntax.string(codec.pattern)})"
        null -> "try container.encode(${name(field)}, forKey: ${key(field)})"
    }

    private fun defaultLiteral(field: Field): String? = when (field.default) {
        null -> null
        DefaultValue.Null -> NIL
        DefaultValue.EmptyList -> "[]"
        DefaultValue.Zero -> when ((field.type as TypeRef.Primitive).kind) {
            PrimitiveKind.STRING -> "\"\""
            PrimitiveKind.BOOLEAN -> "false"
            else -> "0"
        }
    }

    private companion object {
        const val NIL = "nil"
    }
}
