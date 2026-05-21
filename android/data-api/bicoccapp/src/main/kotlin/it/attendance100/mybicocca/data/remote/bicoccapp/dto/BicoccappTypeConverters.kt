package it.attendance100.mybicocca.data.remote.bicoccapp.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BioccappStringBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YesNoBoolean", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean {
        val value = decoder.decodeString()
        return value.equals("YES", ignoreCase = true) || value == "1"
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappIntBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntBinaryBoolean", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() == 1
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappLocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("dd[-][/]MM[-][/]yyyy")
    override val descriptor = PrimitiveSerialDescriptor("ItalianLocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString(), formatter)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappNullableLocalDateSerializer : KSerializer<LocalDate?> {
    private val formatter = DateTimeFormatter.ofPattern("dd[-][/]MM[-][/]yyyy")
    override val descriptor = PrimitiveSerialDescriptor("ItalianLocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate? {
        val date = decoder.decodeString()
        return if(date == "null") {
            null
        } else {
            LocalDate.parse(date, formatter)
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDate?) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private const val NIL = "data-non-registrata"

    private val formatter = DateTimeFormatter.ofPattern("dd[-][/]MM[-][/]yyyy HH:mm[:ss]")

    override val descriptor = PrimitiveSerialDescriptor("ItalianLocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val date = decoder.decodeString()
        return if(date == NIL) {
            LocalDateTime.MIN
        } else {
            LocalDateTime.parse(date, formatter)
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappEventCoordinatesSerializer : KSerializer<BicoccappEventCoordinates?> {
    @Serializable
    private data class Surrogate(
        @SerialName("latitude") val latitude: Double? = null,
        @SerialName("longitude") val longitude: Double? = null
    )

    override val descriptor = Surrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): BicoccappEventCoordinates? {
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        return if (surrogate.latitude != null && surrogate.longitude != null) {
            BicoccappEventCoordinates(surrogate.latitude, surrogate.longitude)
        } else {
            null
        }
    }

    override fun serialize(encoder: Encoder, value: BicoccappEventCoordinates?) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappExamBookingResultSerializer : KSerializer<BicoccappExamBookingResult> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BicoccappExamBookingResult")

    override fun deserialize(decoder: Decoder): BicoccappExamBookingResult {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JSON")
        val jsonObject = input.decodeJsonElement().jsonObject

        val status = jsonObject["status"]?.jsonPrimitive?.contentOrNull
        if(status == "ok") {
            return BicoccappExamBookingResult.Success
        } else {
            val code = jsonObject["code"]?.jsonPrimitive?.contentOrNull ?: ""
            val message = jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: ""
            val redirectUrl = jsonObject["redirect"]?.jsonPrimitive?.contentOrNull
            return BicoccappExamBookingResult.Error(
                status = status ?: "",
                code = code,
                message = message,
                redirectUrl = redirectUrl
            )
        }
    }

    override fun serialize(encoder: Encoder, value: BicoccappExamBookingResult) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappSetCalendarResultSerializer : KSerializer<BicoccappSetCalendarResult> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BicoccappSetCalendarResult")

    override fun deserialize(decoder: Decoder): BicoccappSetCalendarResult {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JSON")
        val jsonObject = input.decodeJsonElement().jsonObject

        val status = jsonObject["status"]?.jsonPrimitive?.intOrNull
        if(status == 200) {
            return BicoccappSetCalendarResult.Success
        } else {
            val message = jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: ""
            return BicoccappSetCalendarResult.Error(
                message = message
            )
        }
    }

    override fun serialize(encoder: Encoder, value: BicoccappSetCalendarResult) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappTeacherSerializer : KSerializer<BicoccappTeacher?> {
    override val descriptor = buildClassSerialDescriptor("BicoccappTeacher")

    override fun deserialize(decoder: Decoder): BicoccappTeacher? {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JSON")

        val json = input.decodeJsonElement().jsonObject

        val email = json["teacher_email"]?.jsonPrimitive?.contentOrNull
        if (email.isNullOrBlank() || email == "#NULL#") {
            return null
        }

        val key = json["teacher_key"]?.jsonPrimitive?.contentOrNull
            ?: return null
        val code = json["teacher_code"]?.jsonPrimitive?.contentOrNull
            ?: return null
        val fullName = json["teacher_fullname"]?.jsonPrimitive?.contentOrNull
            ?: parseAlternativeName(json)
            ?: return null

        return BicoccappTeacher(key, code, fullName, email)
    }

    private fun parseAlternativeName(json: JsonObject): String? {
        val name = json["teacher_name"]?.jsonPrimitive?.contentOrNull
        val surname = json["teacher_surname"]?.jsonPrimitive?.contentOrNull
        return if (name != null && surname != null) {
            "$name $surname"
        } else {
            null
        }
    }

    override fun serialize(encoder: Encoder, value: BicoccappTeacher?) {
        throw NotImplementedError("Serialization not supported")
    }
}

object BicoccappTeachersSerializer : KSerializer<List<BicoccappTeacher>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BicoccappTeachers")

    override fun deserialize(decoder: Decoder): List<BicoccappTeacher> {
        val jsonArray = (decoder as JsonDecoder).decodeJsonElement().jsonArray
        return jsonArray.mapNotNull { element ->
            decoder.json.decodeFromJsonElement(BicoccappTeacherSerializer, element)
        }
    }

    override fun serialize(encoder: Encoder, value: List<BicoccappTeacher>) {
        throw NotImplementedError("Serialization not supported")
    }
}