package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.kotlin.KotlinSyntax.doc

/** Emits records as `@Serializable` data classes. */
internal class RecordEmitter(
    private val spec: ApiSpec,
    private val layout: KotlinLayout,
    private val serializers: TemporalSerializers,
) {
    fun emit(record: RecordType, file: KotlinFile) {
        file.import("kotlinx.serialization.Serializable")
        val body = file.body
        body.doc(record.description)
        body.line("@Serializable")
        if (record.fields.isEmpty()) {
            body.line("class ${record.name}")
            return
        }
        file.import("kotlinx.serialization.SerialName")
        body.line("data class ${record.name}(")
        body.indent {
            record.fields.forEachIndexed { index, field ->
                if (index > 0) line()
                doc(field.description)
                serializerFor(field, file)?.let { line("@Serializable(with = $it::class)") }
                line("@SerialName(${KotlinSyntax.string(field.wireName)})")
                val separator = if (index < record.fields.lastIndex) "," else ""
                line("val ${KotlinSyntax.identifier(field.name)}: ${type(field, file)}${default(field)}$separator")
            }
        }
        body.line(")")
    }

    private fun type(field: Field, file: KotlinFile): String =
        layout.render(field.type, file) + if (field.nullable) "?" else ""

    private fun default(field: Field): String = when (val default = field.default) {
        null -> ""
        DefaultValue.Null -> " = null"
        DefaultValue.EmptyList -> " = emptyList()"
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
        is TypeRef.ListOf -> error("Lists can't have a literal default")
    }

    /**
     * kotlinx.serialization has no serializer for `java.time` types, so every date or time
     * field names one of the runtime's, ISO 8601 ones included.
     */
    private fun serializerFor(field: Field, file: KotlinFile): String? {
        val type = field.type
        if (type is TypeRef.ListOf && (type.element as? TypeRef.Primitive)?.kind?.isTemporal == true) {
            error("Field '${field.name}': the kotlin target doesn't support lists of dates or times")
        }
        val kind = (type as? TypeRef.Primitive)?.kind?.takeIf { it.isTemporal } ?: return null
        val codec = when (val codec = field.codec) {
            is Codec.Temporal -> codec
            null -> Codec.Temporal.iso(kind)
        }
        val serializer = serializers.find(kind, codec, field.nullable)
        if ('.' !in serializer) return serializer
        file.import(serializer)
        return serializer.substringAfterLast('.')
    }
}
