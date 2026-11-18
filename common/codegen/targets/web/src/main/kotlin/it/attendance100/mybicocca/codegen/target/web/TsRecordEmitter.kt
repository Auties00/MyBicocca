package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.web.TsSyntax.doc

/**
 * Emits a record as an interface plus `decode`/`encode` functions, which map wire names to
 * property names and apply the fields' defaults and codecs. A delimited record's functions
 * read and write its string form instead.
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
        when (val encoding = record.encoding) {
            RecordEncoding.Object -> objectCodec(record, file)
            is RecordEncoding.Delimited -> delimitedCodec(record, encoding, file)
        }
    }

    private fun objectCodec(record: RecordType, file: TsFile) {
        val out = file.body
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
                    line("${TsSyntax.string(field.wireName)}: ${codecs.encode(property(field), field.type, field.codec, field.nullable, file)},")
                }
            }
        }
    }

    private fun delimitedCodec(record: RecordType, encoding: RecordEncoding.Delimited, file: TsFile) {
        val out = file.body
        val separator = TsSyntax.string(encoding.separator)
        val fields = record.fields
        val form = fields.joinToString(TsSyntax.docText(encoding.separator)) { it.name }
        out.line("/** Decodes a {@link ${record.name}} from the form `$form`; `undefined` when invalid. */")
        out.block("export function ${layout.decoderName(record.name)}(json: unknown): ${record.name} | undefined {") {
            line("const text = ${codecs.runtime(file, "decodeString")}(json);")
            line("if (text === undefined) return undefined;")
            line("const parts = text.split($separator);")
            line("if (parts.length < ${fields.size}) return undefined;")
            fields.forEachIndexed { index, field ->
                // Like a split with a limit: the last field keeps any further separators.
                val part = if (index == fields.lastIndex) "parts.slice($index).join($separator).trim()" else "parts[$index].trim()"
                line("const ${TsSyntax.variable(field.name)} = ${parse(field, part, file)};")
            }
            line("if (${fields.joinToString(" || ") { "${TsSyntax.variable(it.name)} === undefined" }}) return undefined;")
            line("return { ${fields.joinToString(", ") { "${TsSyntax.propertyKey(it.name)}: ${TsSyntax.variable(it.name)}" }} };")
        }
        out.line()
        out.line("/** Encodes a {@link ${record.name}} as the form `$form`. */")
        out.block("export function ${layout.encoderName(record.name)}(value: ${record.name}): string {") {
            line("return [${fields.joinToString(", ") { format(it, file) }}].join($separator);")
        }
    }

    private fun property(field: Field): String =
        "value.${field.name}".takeIf { TsSyntax.propertyKey(field.name) == field.name } ?: "value[${TsSyntax.string(field.name)}]"

    private fun pattern(field: Field): String =
        (field.codec as? Codec.Temporal ?: Codec.Temporal.iso((field.type as TypeRef.Primitive).kind)).pattern

    private fun parse(field: Field, text: String, file: TsFile): String = when (val kind = (field.type as TypeRef.Primitive).kind) {
        PrimitiveKind.STRING -> text
        PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME ->
            "${codecs.runtime(file, "decodeTemporal")}($text, ${codecs.runtime(file, codecs.temporalClass(kind))}, ${TsSyntax.string(pattern(field))}, false)"
        else -> "${codecs.decoder(field.type, file)}($text)"
    }

    private fun format(field: Field, file: TsFile): String = when ((field.type as TypeRef.Primitive).kind) {
        PrimitiveKind.STRING -> property(field)
        PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME ->
            "${codecs.runtime(file, "encodeTemporal")}(${property(field)}, ${TsSyntax.string(pattern(field))})"
        else -> "String(${property(field)})"
    }

    private fun type(field: Field, file: TsFile): String =
        layout.render(field.type, file) + if (field.nullable) " | null" else ""

    private fun decode(record: RecordType, field: Field, file: TsFile): String {
        val json = "object[${TsSyntax.string(field.wireName)}]"
        if (field.codec is Codec.EmbeddedHtml) {
            // Lists read as empty and other values as null from a blank fragment, so neither needs a default.
            val read = layout.htmlFunction(record, field)
            file.importFile(read, layout.scrapersFile)
            val decoded = "${codecs.runtime(file, "decodeEmbeddedHtml")}($json, $read)"
            return if (field.type is TypeRef.ListOf) "$decoded ?? []" else "$decoded ?? null"
        }
        val decoded = codecs.decode(json, field.type, field.codec, file)
        return when (val default = field.default) {
            null -> if (field.nullable) {
                "$decoded ?? null"
            } else {
                layout.importRuntime(file, "required")
                "required($decoded, ${TsSyntax.string("${record.name}.${field.name}")})"
            }
            else -> "$decoded ?? ${literal(default, field, file)}"
        }
    }

    private fun literal(default: DefaultValue, field: Field, file: TsFile): String = when (default) {
        DefaultValue.Null -> "null"
        is DefaultValue.Literal -> layout.literal(field.type, default)
        DefaultValue.EmptyList -> "[]"
        DefaultValue.EmptyMap -> {
            val key = (field.type as TypeRef.MapOf).key
            if (key == TypeRef.STRING) "{}" else "new ${codecs.runtime(file, "WireMap")}(${codecs.keyText(key, file)})"
        }
        DefaultValue.Zero -> when ((field.type as TypeRef.Primitive).kind) {
            PrimitiveKind.STRING -> "\"\""
            PrimitiveKind.BOOLEAN -> "false"
            else -> "0"
        }
    }
}
