package it.attendance100.mybicocca.data.remote.esse3.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Serializer for ESSE3 dates in the format `dd/MM/yyyy`.
 */
object Esse3LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }
}

/**
 * Serializer for ESSE3 date-times in the format `dd/MM/yyyy HH:mm:ss`.
 */
object Esse3LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime = LocalDateTime.parse(decoder.decodeString(), formatter)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }
}

/**
 * Serializer for nullable ESSE3 dates. Handles null, empty, and blank strings as null.
 */
object Esse3NullableLocalDateSerializer : KSerializer<LocalDate?> {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Esse3NullableLocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate? {
        val value = decoder.decodeString()
        return if (value.isBlank() || value == "null") null else LocalDate.parse(value, formatter)
    }

    override fun serialize(encoder: Encoder, value: LocalDate?) {
        if (value == null) encoder.encodeString("") else encoder.encodeString(value.format(formatter))
    }
}

/**
 * Serializer for nullable ESSE3 date-times. Handles null, empty, and blank strings as null.
 */
object Esse3NullableLocalDateTimeSerializer : KSerializer<LocalDateTime?> {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Esse3NullableLocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime? {
        val value = decoder.decodeString()
        return if (value.isBlank() || value == "null") null else LocalDateTime.parse(value, formatter)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime?) {
        if (value == null) encoder.encodeString("") else encoder.encodeString(value.format(formatter))
    }
}

/**
 * Serializer for ESSE3 integer booleans (0 = false, 1 = true).
 */
object Esse3IntBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3IntBoolean", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() == 1

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value) 1 else 0)
    }
}

/**
 * Serializer that unwraps ESSE3 enum-style objects `{"value":"X"}` into a plain `String`.
 *
 * The ESSE3 API wraps certain enum fields as JSON objects with a single `value` property
 * (e.g. `{"value":"BASIC"}`), even though the OpenAPI spec declares them as plain strings.
 * This serializer transparently extracts the inner value so the Kotlin field can be typed
 * as `String` rather than a wrapper object.
 */
object Esse3EnumValueSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Esse3EnumValue", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as JsonDecoder
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content
            is JsonObject -> element["value"]?.jsonPrimitive?.content ?: ""
            else -> ""
        }
    }

    override fun serialize(encoder: Encoder, value: String) = throw UnsupportedOperationException()
}

/**
 * Nullable variant of [Esse3EnumValueSerializer].
 */
object Esse3NullableEnumValueSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Esse3NullableEnumValue", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as JsonDecoder
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content
            is JsonObject -> element["value"]?.jsonPrimitive?.content
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: String?) = throw UnsupportedOperationException()
}
