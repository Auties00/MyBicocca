package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.Codec
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
        is TypeRef.Primitive -> runtime(
            file,
            when (type.kind) {
                PrimitiveKind.STRING -> "decodeString"
                PrimitiveKind.INT32, PrimitiveKind.INT64 -> "decodeInteger"
                PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "decodeNumber"
                PrimitiveKind.BOOLEAN -> "decodeBoolean"
                PrimitiveKind.LOCAL_DATE -> "decodeLocalDate"
                PrimitiveKind.LOCAL_DATE_TIME -> "decodeLocalDateTime"
                PrimitiveKind.JSON_OBJECT -> "decodeJsonObject"
                PrimitiveKind.BINARY -> error("Binary values can't be decoded from JSON")
            },
        )
    }

    /** An expression decoding [json] as [type] with [codec], evaluating to `T | undefined`. */
    fun decode(json: String, type: TypeRef, codec: Codec?, file: TsFile): String = when (codec) {
        is Codec.Temporal -> {
            val kind = (type as TypeRef.Primitive).kind
            val temporal = if (kind == PrimitiveKind.LOCAL_DATE) "LocalDate" else "LocalDateTime"
            "${runtime(file, "decodeTemporal")}($json, ${runtime(file, temporal)}, ${TsSyntax.string(codec.pattern)}, ${codec.blankAsNull})"
        }
        null -> when (type) {
            is TypeRef.ListOf -> "${runtime(file, "decodeList")}($json, ${decoder(type.element, file)})"
            else -> "${decoder(type, file)}($json)"
        }
    }

    /**
     * An expression encoding [value] (of [type], possibly null when [nullable]) to its JSON
     * form, or [value] itself when the JSON form is the same.
     */
    fun encode(value: String, type: TypeRef, codec: Codec?, nullable: Boolean, file: TsFile): String {
        if (codec is Codec.Temporal) return "${runtime(file, "encodeTemporal")}($value, ${TsSyntax.string(codec.pattern)})"
        val encoded = encodeNonNull(value, type, file) ?: return value
        return if (nullable) "$value === null ? null : $encoded" else encoded
    }

    /** An expression encoding the non-null [value], or null when it already is its JSON form. */
    private fun encodeNonNull(value: String, type: TypeRef, file: TsFile): String? = when (type) {
        is TypeRef.Named -> if (layout.isRecord(type)) {
            "${layout.encoderName(type.name).also { layout.importDeclaration(file, type.name, it) }}($value)"
        } else {
            null
        }
        is TypeRef.ListOf -> encodeNonNull("item", type.element, file)?.let { "$value.map((item) => $it)" }
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME -> "${runtime(file, "encodeIsoTemporal")}($value)"
            else -> null
        }
    }

    private fun runtime(file: TsFile, name: String): String {
        layout.importRuntime(file, name)
        return name
    }
}
