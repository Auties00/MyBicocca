package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.kotlin.KotlinSyntax.doc

/**
 * Emits records as `@Serializable` data classes. A delimited record gets a nested `Serializer`
 * reading and writing its string form.
 */
internal class RecordEmitter(
    private val spec: ApiSpec,
    private val layout: KotlinLayout,
    private val serializers: KotlinSerializers,
) {
    fun emit(record: RecordType, file: KotlinFile) {
        file.import("kotlinx.serialization.Serializable")
        val body = file.body
        body.doc(record.description)
        val encoding = record.encoding
        body.line(if (encoding is RecordEncoding.Delimited) "@Serializable(with = ${record.name}.Serializer::class)" else "@Serializable")
        if (record.fields.isEmpty()) {
            body.line("class ${record.name}")
            return
        }
        if (encoding is RecordEncoding.Object) file.import("kotlinx.serialization.SerialName")
        body.line("data class ${record.name}(")
        body.indent {
            record.fields.forEachIndexed { index, field ->
                if (index > 0 && encoding is RecordEncoding.Object) line()
                doc(field.description)
                if (encoding is RecordEncoding.Object) {
                    serializers.of(field, file)?.let { line("@Serializable(with = $it::class)") }
                    line("@SerialName(${KotlinSyntax.string(field.wireName)})")
                }
                val separator = if (index < record.fields.lastIndex) "," else ""
                line("val ${KotlinSyntax.identifier(field.name)}: ${type(field, file)}${default(field)}$separator")
            }
        }
        when (encoding) {
            RecordEncoding.Object -> body.line(")")
            is RecordEncoding.Delimited -> {
                body.line(") {")
                body.indent { delimitedSerializer(record, encoding, file) }
                body.line("}")
            }
        }
    }

    /** The `Serializer` of a delimited record, which splits and joins its fields' text forms. */
    private fun CodeWriter.delimitedSerializer(record: RecordType, encoding: RecordEncoding.Delimited, file: KotlinFile) {
        listOf(
            "kotlinx.serialization.KSerializer",
            "kotlinx.serialization.SerializationException",
            "kotlinx.serialization.descriptors.PrimitiveKind",
            "kotlinx.serialization.descriptors.PrimitiveSerialDescriptor",
            "kotlinx.serialization.descriptors.SerialDescriptor",
            "kotlinx.serialization.encoding.Decoder",
            "kotlinx.serialization.encoding.Encoder",
        ).forEach(file::import)
        val separator = KotlinSyntax.string(encoding.separator)
        val fields = record.fields
        line("/** Reads and writes the form `${fields.joinToString(KotlinSyntax.docText(encoding.separator)) { it.name }}`. */")
        block("object Serializer : KSerializer<${record.name}> {") {
            val temporal = fields.filter { (it.type as TypeRef.Primitive).kind.isTemporal }
            if (temporal.isNotEmpty()) {
                file.import("java.time.format.DateTimeFormatter")
                for (field in temporal) line("private val ${formatter(field)} = DateTimeFormatter.ofPattern(${KotlinSyntax.string(pattern(field))})")
                line()
            }
            line("override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(${KotlinSyntax.string(record.name)}, PrimitiveKind.STRING)")
            line()
            block("override fun deserialize(decoder: Decoder): ${record.name} {") {
                line("val text = decoder.decodeString()")
                line("val parts = text.split($separator, limit = ${fields.size})")
                line("if (parts.size != ${fields.size}) throw SerializationException(\"Invalid ${record.name}: \$text\")")
                block("return ${record.name}(", ")") {
                    fields.forEachIndexed { index, field ->
                        val comma = if (index < fields.lastIndex) "," else ""
                        line("${KotlinSyntax.identifier(field.name)} = ${parse(field, "parts[$index]", file)}$comma")
                    }
                }
            }
            line()
            block("override fun serialize(encoder: Encoder, value: ${record.name}) {") {
                val parts = fields.joinToString(", ") { format(it, "value.${KotlinSyntax.identifier(it.name)}") }
                line("encoder.encodeString(listOf($parts).joinToString($separator))")
            }
        }
    }

    private fun formatter(field: Field): String = "${field.name}Format"

    private fun pattern(field: Field): String =
        (field.codec as? Codec.Temporal ?: Codec.Temporal.iso((field.type as TypeRef.Primitive).kind)).pattern

    private fun parse(field: Field, text: String, file: KotlinFile): String = when ((field.type as TypeRef.Primitive).kind) {
        PrimitiveKind.STRING -> text
        PrimitiveKind.INT32 -> "$text.trim().toInt()"
        PrimitiveKind.INT64 -> "$text.trim().toLong()"
        PrimitiveKind.FLOAT32 -> "$text.trim().toFloat()"
        PrimitiveKind.FLOAT64 -> "$text.trim().toDouble()"
        PrimitiveKind.BOOLEAN -> "$text.trim().toBooleanStrict()"
        else -> "${layout.render(field.type, file)}.parse($text.trim(), ${formatter(field)})"
    }

    private fun format(field: Field, value: String): String = when ((field.type as TypeRef.Primitive).kind) {
        PrimitiveKind.STRING -> value
        PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME -> "$value.format(${formatter(field)})"
        else -> "$value.toString()"
    }

    private fun type(field: Field, file: KotlinFile): String =
        serializers.render(field.type, file, field.name) + if (field.nullable) "?" else ""

    private fun default(field: Field): String = when (val default = field.default) {
        null -> ""
        DefaultValue.Null -> " = null"
        DefaultValue.EmptyList -> " = emptyList()"
        DefaultValue.EmptyMap -> " = emptyMap()"
        is DefaultValue.Literal -> " = " + literal(field.type, default)
        DefaultValue.Zero -> " = " + when ((field.type as TypeRef.Primitive).kind) {
            PrimitiveKind.STRING -> "\"\""
            PrimitiveKind.INT32 -> "0"
            PrimitiveKind.INT64 -> "0L"
            PrimitiveKind.FLOAT32 -> "0f"
            PrimitiveKind.FLOAT64 -> "0.0"
            PrimitiveKind.BOOLEAN -> "false"
            else -> error("Field '${field.name}' can't default to zero")
        }
    }

    private fun literal(type: TypeRef, literal: DefaultValue.Literal): String = when (type) {
        is TypeRef.Named -> "${type.name}.${(spec.resolve(type) as EnumType).values.first { it.wireValue == literal.value }.name}"
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> KotlinSyntax.string(literal.value)
            PrimitiveKind.INT64 -> "${literal.value}L"
            PrimitiveKind.FLOAT32 -> "${literal.value}f"
            else -> literal.value
        }
        is TypeRef.ListOf, is TypeRef.MapOf -> error("Collections can't have a literal default")
    }
}
