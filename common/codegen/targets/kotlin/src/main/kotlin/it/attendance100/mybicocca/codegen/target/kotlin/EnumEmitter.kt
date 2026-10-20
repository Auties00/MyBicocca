package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.target.kotlin.KotlinSyntax.doc

/**
 * Emits enums as sealed interfaces with a `data object` per value, rather than enum classes,
 * so that an open enum can also hold values the server added later (`Unknown(value)`).
 * Integer enums keep their values as decimal text, and read and write JSON numbers.
 */
internal object EnumEmitter {
    private const val UNKNOWN = "Unknown"

    fun emit(enum: EnumType, file: KotlinFile) {
        listOf(
            "kotlinx.serialization.KSerializer",
            "kotlinx.serialization.Serializable",
            "kotlinx.serialization.descriptors.PrimitiveKind",
            "kotlinx.serialization.descriptors.PrimitiveSerialDescriptor",
            "kotlinx.serialization.descriptors.SerialDescriptor",
            "kotlinx.serialization.encoding.Decoder",
            "kotlinx.serialization.encoding.Encoder",
        ).forEach(file::import)

        // A declared value named "Unknown" takes the place of the catch-all case.
        val declaresUnknown = enum.values.any { it.name == UNKNOWN }
        val name = enum.name
        file.body.apply {
            doc(enum.description)
            line("@Serializable(with = $name.Serializer::class)")
            block("sealed interface $name {") {
                line("val value: String")
                line()
                for (value in enum.values) {
                    doc(value.description)
                    line("data object ${value.name} : $name { override val value = ${KotlinSyntax.string(value.wireValue)} }")
                }
                if (enum.open && !declaresUnknown) {
                    line("data class $UNKNOWN(override val value: String) : $name")
                }
                line()
                block("object Serializer : KSerializer<$name> {") {
                    val kind = if (enum.integer) "LONG" else "STRING"
                    line("override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(${KotlinSyntax.string(name)}, PrimitiveKind.$kind)")
                    line()
                    block("override fun deserialize(decoder: Decoder): $name {") {
                        writeRawValue(enum, file)
                        block("return when (raw) {") {
                            for (value in enum.values) {
                                line("${KotlinSyntax.string(value.wireValue)} -> ${value.name}")
                            }
                            line(
                                when {
                                    !enum.open -> {
                                        file.import("kotlinx.serialization.SerializationException")
                                        "else -> throw SerializationException(\"Unknown $name value: \$raw\")"
                                    }
                                    declaresUnknown -> "else -> $UNKNOWN"
                                    else -> "else -> $UNKNOWN(raw)"
                                }
                            )
                        }
                    }
                    line()
                    block("override fun serialize(encoder: Encoder, value: $name) {") {
                        if (enum.integer) {
                            line("value.value.toLongOrNull()?.let(encoder::encodeLong) ?: encoder.encodeString(value.value)")
                        } else {
                            line("encoder.encodeString(value.value)")
                        }
                    }
                }
            }
        }
    }

    private fun writeRawValue(enum: EnumType, file: KotlinFile) {
        if (enum.integer && !enum.acceptsWrappedValue) {
            // A number or a string: either way, its content.
            file.import("kotlinx.serialization.json.JsonDecoder")
            file.import("kotlinx.serialization.json.jsonPrimitive")
            file.body.line("val raw = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content")
            return
        }
        if (!enum.acceptsWrappedValue) {
            file.body.line("val raw = decoder.decodeString()")
            return
        }
        listOf(
            "kotlinx.serialization.json.JsonDecoder",
            "kotlinx.serialization.json.JsonObject",
            "kotlinx.serialization.json.JsonPrimitive",
            "kotlinx.serialization.json.jsonPrimitive",
        ).forEach(file::import)
        file.body.block("val raw = when (val element = (decoder as JsonDecoder).decodeJsonElement()) {") {
            line("is JsonPrimitive -> element.content")
            line("is JsonObject -> element[\"value\"]?.jsonPrimitive?.content ?: \"\"")
            line("else -> \"\"")
        }
    }
}
