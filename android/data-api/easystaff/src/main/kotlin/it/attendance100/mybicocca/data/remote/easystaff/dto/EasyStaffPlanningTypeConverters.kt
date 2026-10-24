package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*
 * The date and time serializers the generated Portale Planning types use, as listed in
 * common/specs/easystaff-planning/codegen.json.
 */

/** Dates in ISO 8601 (`yyyy-MM-dd`), as schedules key their days. */
object EasyStaffPlanningLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningLocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

/** Times of day in ISO 8601 (`HH:mm:ss`), as the booking deadlines of a service. */
object EasyStaffPlanningLocalTimeSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningLocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString(), formatter)
}

/**
 * Dates and times written `yyyy-MM-dd HH:mm`, as the first availability of a month schedule.
 * Blank strings, and values that aren't strings, mean no value.
 */
object EasyStaffPlanningNullableLocalDateTimeSerializer : KSerializer<LocalDateTime?> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffPlanningNullableLocalDateTime", PrimitiveKind.STRING).nullable

    override fun serialize(encoder: Encoder, value: LocalDateTime?) = encoder.encodeString(value?.format(formatter).orEmpty())

    override fun deserialize(decoder: Decoder): LocalDateTime? {
        val text = when (decoder) {
            is JsonDecoder -> (decoder.decodeJsonElement() as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull
            else -> decoder.decodeString()
        }
        return text?.takeUnless { it.isBlank() }?.let { LocalDateTime.parse(it, formatter) }
    }
}
