package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.web.TsSyntax.doc

/**
 * Emits a record as an interface plus `decode`/`encode` functions, which map wire names to
 * property names and apply the fields' defaults and codecs.
 */
internal class TsRecordEmitter(private val layout: TsLayout, private val codecs: TsCodecs) {

    fun emit(record: RecordType, file: TsFile) {
        val out = file.body
        out.doc(record.description)
        out.block("export interface ${record.name} {") {
            for (field in record.fields) {
                doc(field.description)
                line("${TsSyntax.propertyKey(field.name)}: ${type(field, file)};")
            }
        }
        out.line()
        layout.importRuntime(file, "asJsonObject")
        out.line("/** Decodes a {@link ${record.name}}; `undefined` when `json` isn't an object. */")
        out.block("export function ${layout.decoderName(record.name)}(json: unknown): ${record.name} | undefined {") {
            line("const object = asJsonObject(json);")
            line("if (object === undefined) return undefined;")
            block("return {", "};") {
                for (field in record.fields) {
                    line("${TsSyntax.propertyKey(field.name)}: ${decode(record, field, file)},")
                }
            }
        }
        out.line()
        layout.importRuntime(file, "JsonObject")
        val parameter = if (record.fields.isEmpty()) "_value" else "value"
        out.block("export function ${layout.encoderName(record.name)}($parameter: ${record.name}): JsonObject {") {
            block("return {", "};") {
                for (field in record.fields) {
                    val value = "value.${field.name}".takeIf { TsSyntax.propertyKey(field.name) == field.name } ?: "value[${TsSyntax.string(field.name)}]"
                    line("${TsSyntax.string(field.wireName)}: ${codecs.encode(value, field.type, field.codec, field.nullable, file)},")
                }
            }
        }
    }

    private fun type(field: Field, file: TsFile): String =
        layout.render(field.type, file) + if (field.nullable) " | null" else ""

    private fun decode(record: RecordType, field: Field, file: TsFile): String {
        val decoded = codecs.decode("object[${TsSyntax.string(field.wireName)}]", field.type, field.codec, file)
        return when (val default = field.default) {
            null -> if (field.nullable) {
                "$decoded ?? null"
            } else {
                layout.importRuntime(file, "required")
                "required($decoded, ${TsSyntax.string("${record.name}.${field.name}")})"
            }
            else -> "$decoded ?? ${literal(default, field)}"
        }
    }

    private fun literal(default: DefaultValue, field: Field): String = when (default) {
        DefaultValue.Null -> "null"
        is DefaultValue.Literal -> layout.literal(field.type, default)
        DefaultValue.EmptyList -> "[]"
        DefaultValue.Zero -> when ((field.type as TypeRef.Primitive).kind) {
            PrimitiveKind.STRING -> "\"\""
            PrimitiveKind.BOOLEAN -> "false"
            else -> "0"
        }
    }
}
