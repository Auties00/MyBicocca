package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import java.time.*
import java.time.format.DateTimeFormatter

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val instant = value.atZone(ZoneId.systemDefault()).toInstant()
        encoder.encodeLong(instant.epochSecond)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val epochSeconds = decoder.decodeLong()
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(epochSeconds),
            ZoneId.systemDefault()
        )
    }
}

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)
}

object ItalianLocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    override val descriptor = PrimitiveSerialDescriptor("ItalianLocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)
}

object StringBooleanSerializer : KSerializer<Boolean> {
    override val descriptor = PrimitiveSerialDescriptor("StringBoolean", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeString(if (value) "1" else "0")
    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeString() == "1"
}

object IntBooleanSerializer : KSerializer<Boolean> {
    override val descriptor = PrimitiveSerialDescriptor("IntBoolean", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeInt(if (value) 1 else 0)
    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() == 1
}

object LocalTimeSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")
    override val descriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalTime) = encoder.encodeString(value.format(formatter))
    override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString(), formatter)
}

object EmptyStringAsNullSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("EmptyStringAsNull", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }

    override fun deserialize(decoder: Decoder): String? {
        val value = decoder.decodeString()
        return value.ifBlank { null }
    }
}

object ResponseResultSerializer : KSerializer<Boolean> {
    private const val SUCCESS = "ok"
    private const val FAILURE = "failure"

    override val descriptor: SerialDescriptor
        get() =  PrimitiveSerialDescriptor("ResultSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeString() == SUCCESS
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeString(if (value) SUCCESS else FAILURE)
    }
}

object EasyStaffExamTypeSerializer : KSerializer<EasyStaffExamType> {
    private const val LBL_WRITTEN = "esame scritto"
    private const val LBL_ORAL = "esame orale"
    private const val LBL_ORAL_WRITTEN = "esame scritto-orale"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EasyStaffExamType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EasyStaffExamType) {
        val stringValue = when (value) {
            is EasyStaffExamType.Written -> LBL_WRITTEN
            is EasyStaffExamType.Oral -> LBL_ORAL
            is EasyStaffExamType.OralAndWritten -> LBL_ORAL_WRITTEN
            is EasyStaffExamType.Other -> value.value
        }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): EasyStaffExamType {
        return when (val stringValue = decoder.decodeString()) {
            LBL_WRITTEN -> EasyStaffExamType.Written
            LBL_ORAL -> EasyStaffExamType.Oral
            LBL_ORAL_WRITTEN -> EasyStaffExamType.OralAndWritten
            else -> EasyStaffExamType.Other(stringValue)
        }
    }
}

object ExamSubjectsSerializer : KSerializer<List<EasyStaffExamSubject>> {
    private val listSerializer = ListSerializer(EasyStaffExamSubject.serializer())
    private val mapSerializer = MapSerializer(String.serializer(), EasyStaffExamSubject.serializer())

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<EasyStaffExamSubject> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return listSerializer.deserialize(decoder)

        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> jsonDecoder.json.decodeFromJsonElement(listSerializer, element)
            is JsonObject -> jsonDecoder.json.decodeFromJsonElement(mapSerializer, element).values.toList()
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<EasyStaffExamSubject>) {
        listSerializer.serialize(encoder, value)
    }
}

object MapsIframeUrlSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("MapsIframeUrl", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        encoder.encodeString(value ?: "")
    }

    override fun deserialize(decoder: Decoder): String? {
        val html = decoder.decodeString()
        if (html.isBlank()) return null
        return Jsoup.parse(html)
            .selectFirst("iframe")
            ?.attr("src")
            ?.ifBlank { null }
    }
}

object CommaSeparatedListSerializer : KSerializer<List<String>> {
    override val descriptor = PrimitiveSerialDescriptor("CommaSeparatedList", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeString(value.joinToString(", "))
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val raw = decoder.decodeString()
        if (raw.isBlank()) return emptyList()
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}

object ScheduleCellSerializer : KSerializer<EasyStaffScheduleCell> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EasyStaffScheduleCell")

    override fun serialize(encoder: Encoder, value: EasyStaffScheduleCell) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw IllegalStateException("EasyStaffScheduleCell can only be serialized to JSON")

        val jsonElement = when (value) {
            is EasyStaffScheduleCell.Lesson -> jsonEncoder.json.encodeToJsonElement(
                EasyStaffScheduleCell.Lesson.serializer(),
                value
            )
            is EasyStaffScheduleCell.Closure -> jsonEncoder.json.encodeToJsonElement(
                EasyStaffScheduleCell.Closure.serializer(),
                value
            )
            is EasyStaffScheduleCell.Other -> JsonObject(
                value.rawFields + ("tipo" to JsonPrimitive(value.type))
            )
        }
        jsonEncoder.encodeJsonElement(jsonElement)
    }

    override fun deserialize(decoder: Decoder): EasyStaffScheduleCell {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("EasyStaffScheduleCell can only be deserialized from JSON")

        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        val type = jsonObject["tipo"]?.jsonPrimitive?.contentOrNull ?: ""

        return when (type) {
            EasyStaffScheduleCell.Closure.TYPE -> {
                jsonDecoder.json.decodeFromJsonElement(
                    EasyStaffScheduleCell.Closure.serializer(),
                    jsonObject
                )
            }
            EasyStaffScheduleCell.Lesson.TYPE -> {
                jsonDecoder.json.decodeFromJsonElement(
                    EasyStaffScheduleCell.Lesson.serializer(),
                    jsonObject
                )
            }
            else -> {
                EasyStaffScheduleCell.Other(type, jsonObject)
            }
        }
    }
}