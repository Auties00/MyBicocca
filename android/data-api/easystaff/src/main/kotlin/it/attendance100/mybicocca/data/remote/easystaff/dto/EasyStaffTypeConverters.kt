package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*
 * The date and time serializers the generated Agenda Web types use, as listed in
 * common/specs/easystaff-agenda/codegen.json.
 */

/** Dates written `dd-MM-yyyy`, as Agenda Web writes the days of lessons and exams. */
object EasyStaffItalianDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffItalianDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)
}

/** Dates in ISO 8601 (`yyyy-MM-dd`), as the days of room occupations and events. */
object EasyStaffIsoDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffIsoDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

/** Times of day written `HH:mm`. */
object EasyStaffTimeSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    override val descriptor = PrimitiveSerialDescriptor("EasyStaffTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString(), formatter)
}
