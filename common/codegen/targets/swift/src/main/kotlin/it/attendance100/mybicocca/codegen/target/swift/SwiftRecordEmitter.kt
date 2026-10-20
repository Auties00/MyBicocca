package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.swift.SwiftSyntax.doc

/**
 * Emits records as structs with explicit `Codable` conformance, so that decoding follows the
 * model rather than Swift's strict synthesis: a field with a default takes it when its key is
 * missing or unusable, numbers may arrive as strings, and codecs set custom wire formats.
 *
 * A delimited record is a `WireMapKey` too: its string form is its `wireKey`, so it can key
 * a `WireMap`.
 */
internal class SwiftRecordEmitter(private val layout: SwiftLayout) {

    fun emit(record: RecordType, out: CodeWriter) {
        out.doc(record.description)
        when (val encoding = record.encoding) {
            RecordEncoding.Object -> objectRecord(record, out)
            is RecordEncoding.Delimited -> delimitedRecord(record, encoding, out)
        }
    }

    private fun objectRecord(record: RecordType, out: CodeWriter) {
        out.block("public struct ${record.name}: Codable, Hashable, Sendable {") {
            if (record.fields.isEmpty()) {
                line("public init() {}")
                return@block
            }
            properties(record)
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

    private fun delimitedRecord(record: RecordType, encoding: RecordEncoding.Delimited, out: CodeWriter) {
        val separator = SwiftSyntax.string(encoding.separator)
        val fields = record.fields
        out.block("public struct ${record.name}: Codable, Hashable, Sendable, WireMapKey {") {
            properties(record)
            line()
            memberwiseInit(record)
            line()
            line("/// Reads the form `${fields.joinToString(SwiftSyntax.docText(encoding.separator)) { it.name }}`.")
            block("public init?(wireKey: String) {") {
                line("let parts = wireKey.components(separatedBy: $separator)")
                // Like a split with a limit: the last field keeps any further separators.
                line("guard parts.count >= ${fields.size} else { return nil }")
                fields.forEachIndexed { index, field ->
                    val text = if (index == fields.lastIndex) {
                        "parts[$index...].joined(separator: $separator).trimmingCharacters(in: .whitespaces)"
                    } else {
                        "parts[$index].trimmingCharacters(in: .whitespaces)"
                    }
                    line("guard let ${name(field)} = ${parse(field, text)} else { return nil }")
                }
                line("self.init(${fields.joinToString(", ") { "${name(it)}: ${name(it)}" }})")
            }
            line()
            block("public var wireKey: String {") {
                line("[${fields.joinToString(", ") { format(it) }}].joined(separator: $separator)")
            }
            line()
            block("public init(from decoder: Decoder) throws {") {
                line("let container = try decoder.singleValueContainer()")
                line("let text = try container.decode(String.self)")
                block("guard let value = ${record.name}(wireKey: text) else {") {
                    line("throw DecodingError.dataCorruptedError(in: container, debugDescription: \"Invalid ${record.name} '\\(text)'\")")
                }
                line("self = value")
            }
            line()
            block("public func encode(to encoder: Encoder) throws {") {
                line("var container = encoder.singleValueContainer()")
                line("try container.encode(wireKey)")
            }
        }
    }

    private fun CodeWriter.properties(record: RecordType) {
        for (field in record.fields) {
            doc(field.description)
            line("public var ${name(field)}: ${type(field)}")
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

    private fun pattern(field: Field): String =
        (field.codec as? Codec.Temporal ?: Codec.Temporal.iso((field.type as TypeRef.Primitive).kind)).pattern

    /** An expression parsing a delimited record's field from [text], nil when invalid. */
    private fun parse(field: Field, text: String): String = when ((field.type as TypeRef.Primitive).kind) {
        PrimitiveKind.STRING -> "Optional($text)"
        PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME ->
            "${layout.render(field.type)}.parse($text, pattern: TemporalPattern(${SwiftSyntax.string(pattern(field))}))"
        else -> "${layout.render(field.type)}($text)"
    }

    private fun format(field: Field): String = when ((field.type as TypeRef.Primitive).kind) {
        PrimitiveKind.STRING -> name(field)
        PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME ->
            "${name(field)}.format(pattern: TemporalPattern(${SwiftSyntax.string(pattern(field))}))"
        else -> "String(${name(field)})"
    }

    private fun decode(field: Field): String {
        val type = layout.render(field.type)
        val lenient = when (val codec = field.codec) {
            is Codec.Temporal -> {
                val arguments = "$type.self, forKey: ${key(field)}, pattern: ${SwiftSyntax.string(codec.pattern)}, blankAsNull: ${codec.blankAsNull}"
                return if (field.nullable) "try container.decodeTemporal($arguments)" else "try container.decodeTemporalRequired($arguments)"
            }
            is Codec.BlankAsNull -> {
                val also = if (codec.alsoNull.isEmpty()) "" else ", alsoNull: ${codec.alsoNull.joinToString(", ", "[", "]") { SwiftSyntax.string(it) }}"
                "container.decodeBlankAsNull(forKey: ${key(field)}$also)"
            }
            Codec.FoldedObject -> "container.decodeFolded($type.self, forKey: ${key(field)})"
            Codec.EmbeddedJson -> "container.decodeEmbedded($type.self, forKey: ${key(field)})"
            null -> {
                val default = defaultLiteral(field)
                if (default == null && !field.nullable) return "try container.decodeRequired($type.self, forKey: ${key(field)})"
                "container.decodeLenient($type.self, forKey: ${key(field)})"
            }
        }
        val default = defaultLiteral(field)
        return when {
            default != null && default != NIL -> "$lenient ?? $default"
            field.nullable -> lenient
            else -> "try container.required($lenient, forKey: ${key(field)})"
        }
    }

    private fun encode(field: Field): String = when (val codec = field.codec) {
        is Codec.Temporal -> "try container.encodeTemporal(${name(field)}, forKey: ${key(field)}, pattern: ${SwiftSyntax.string(codec.pattern)})"
        is Codec.BlankAsNull, Codec.FoldedObject, Codec.EmbeddedJson, null -> "try container.encode(${name(field)}, forKey: ${key(field)})"
    }

    private fun defaultLiteral(field: Field): String? = when (val default = field.default) {
        null -> null
        DefaultValue.Null -> NIL
        is DefaultValue.Literal -> layout.literal(field.type, default)
        DefaultValue.EmptyList -> "[]"
        DefaultValue.EmptyMap -> "[:]"
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
