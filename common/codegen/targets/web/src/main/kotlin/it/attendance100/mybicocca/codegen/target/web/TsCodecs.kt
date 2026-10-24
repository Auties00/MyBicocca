package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Builds the expressions that convert values between their JSON and TypeScript forms.
 *
 * Decoders follow the runtime convention `(json: unknown) => T | undefined`: `undefined`
 * means the value is missing or unusable, and the caller applies the field's default.
 */
internal class TsCodecs(private val layout: TsLayout) {

    /** A decoder function for [type], as an expression usable as a callback. */
    fun decoder(type: TypeRef, file: TsFile): String = when (type) {
        is TypeRef.Named -> layout.decoderName(type.name).also { layout.importDeclaration(file, type.name, it) }
        is TypeRef.ListOf -> {
            layout.importRuntime(file, "decodeList")
            "(json: unknown) => decodeList(json, ${decoder(type.element, file)})"
        }
        is TypeRef.MapOf -> "(json: unknown) => ${decodeMap("json", type, file)}"
        is TypeRef.Primitive -> runtime(
            file,
            when (type.kind) {
                PrimitiveKind.STRING -> "decodeString"
                PrimitiveKind.INT32, PrimitiveKind.INT64 -> "decodeInteger"
                PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "decodeNumber"
                PrimitiveKind.BOOLEAN -> "decodeBoolean"
                PrimitiveKind.LOCAL_DATE -> "decodeLocalDate"
                PrimitiveKind.LOCAL_DATE_TIME -> "decodeLocalDateTime"
                PrimitiveKind.LOCAL_TIME -> "decodeLocalTime"
                PrimitiveKind.JSON_OBJECT -> "decodeJsonObject"
                PrimitiveKind.JSON_VALUE -> "decodeJsonValue"
                PrimitiveKind.BINARY, PrimitiveKind.FILE -> error("Bytes and files can't be decoded from JSON")
            },
        )
    }

    /** An expression decoding [json] as [type] with [codec], evaluating to `T | undefined`. */
    fun decode(json: String, type: TypeRef, codec: Codec?, file: TsFile): String = when (codec) {
        is Codec.Temporal -> {
            val kind = (type as TypeRef.Primitive).kind
            "${runtime(file, "decodeTemporal")}($json, ${runtime(file, temporalClass(kind))}, ${TsSyntax.string(codec.pattern)}, ${codec.blankAsNull})"
        }
        is Codec.BlankAsNull -> {
            val also = if (codec.alsoNull.isEmpty()) "" else ", ${codec.alsoNull.joinToString(", ", "[", "]") { TsSyntax.string(it) }}"
            "${runtime(file, "decodeBlankAsNull")}($json$also)"
        }
        Codec.FoldedObject -> "${runtime(file, "decodeFolded")}($json, ${decoder(type, file)})"
        Codec.EmbeddedJson -> "${runtime(file, "decodeEmbedded")}($json, ${decoder(type, file)})"
        Codec.EpochSeconds -> "${runtime(file, "decodeEpochSeconds")}($json)"
        is Codec.Joined -> "${runtime(file, "decodeJoined")}($json, ${TsSyntax.string(codec.separator)})"
        Codec.ObjectValues -> "${runtime(file, "decodeObjectValues")}($json, ${decoder((type as TypeRef.ListOf).element, file)})"
        is Codec.EmbeddedHtml -> error("Fields read from HTML are decoded by their record's scraper function")
        null -> when (type) {
            is TypeRef.ListOf -> "${runtime(file, "decodeList")}($json, ${decoder(type.element, file)})"
            is TypeRef.MapOf -> decodeMap(json, type, file)
            else -> "${decoder(type, file)}($json)"
        }
    }

    /** String-keyed maps are plain records; others are `WireMap`s, keyed by their string form. */
    private fun decodeMap(json: String, type: TypeRef.MapOf, file: TsFile): String {
        if (type.key == TypeRef.STRING) return "${runtime(file, "decodeRecordMap")}($json, ${decoder(type.value, file)})"
        return "${runtime(file, "decodeWireMap")}($json, ${decoder(type.key, file)}, ${keyText(type.key, file)}, ${decoder(type.value, file)})"
    }

    /**
     * The function writing a map key of [type] as its string form: a delimited record's
     * encoder, or `String` for numbers, dates and enums.
     */
    fun keyText(type: TypeRef, file: TsFile): String =
        if (type is TypeRef.Named && layout.isRecord(type)) encoder(type, file) else "String"

    /**
     * An expression encoding [value] (of [type], possibly null when [nullable]) to its JSON
     * form, or [value] itself when the JSON form is the same.
     */
    fun encode(value: String, type: TypeRef, codec: Codec?, nullable: Boolean, file: TsFile): String {
        when (codec) {
            is Codec.Temporal -> return "${runtime(file, "encodeTemporal")}($value, ${TsSyntax.string(codec.pattern)})"
            Codec.EpochSeconds -> return "${runtime(file, "encodeEpochSeconds")}($value)"
            is Codec.Joined -> return "$value.join(${TsSyntax.string(codec.separator)})"
            // A value read out of HTML can't be written back; an empty fragment reads as no value.
            is Codec.EmbeddedHtml -> return "\"\""
            is Codec.BlankAsNull, Codec.FoldedObject, Codec.EmbeddedJson, Codec.ObjectValues, null -> Unit
        }
        val encoded = encodeNonNull(value, type, file) ?: return value
        return if (nullable) "$value === null ? null : $encoded" else encoded
    }

    /** An expression encoding the non-null [value], or null when it already is its JSON form. */
    private fun encodeNonNull(value: String, type: TypeRef, file: TsFile): String? = when (type) {
        is TypeRef.Named -> when {
            layout.isRecord(type) -> "${encoder(type, file)}($value)"
            (layout.declaration(type) as EnumType).integer -> "Number($value)"
            else -> null
        }
        is TypeRef.ListOf -> encodeNonNull("item", type.element, file)?.let { "$value.map((item) => $it)" }
        is TypeRef.MapOf -> {
            val item = encodeNonNull("item", type.value, file)
            when {
                type.key != TypeRef.STRING -> "${runtime(file, "encodeWireMap")}($value, (item) => ${item ?: "item"})"
                item != null -> "Object.fromEntries(Object.entries($value).map(([key, item]) => [key, $item]))"
                else -> null
            }
        }
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME -> "${runtime(file, "encodeIsoTemporal")}($value)"
            else -> null
        }
    }

    private fun encoder(type: TypeRef.Named, file: TsFile): String =
        layout.encoderName(type.name).also { layout.importDeclaration(file, type.name, it) }

    fun temporalClass(kind: PrimitiveKind): String = when (kind) {
        PrimitiveKind.LOCAL_DATE -> "LocalDate"
        PrimitiveKind.LOCAL_TIME -> "LocalTime"
        else -> "LocalDateTime"
    }

    fun runtime(file: TsFile, name: String): String {
        layout.importRuntime(file, name)
        return name
    }
}
