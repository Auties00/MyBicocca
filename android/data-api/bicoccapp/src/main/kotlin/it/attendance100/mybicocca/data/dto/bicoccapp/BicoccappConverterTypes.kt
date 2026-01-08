package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object YesNoBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YesNoBoolean", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeString().equals("YES", ignoreCase = true)
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeString(if (value) "YES" else "NO")
    }
}

object BinaryBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BinaryBoolean", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() == 1
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value) 1 else 0)
    }
}

object ExamBookingSerializer : KSerializer<BicoccappExamBookingResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ExamBookingResponse")

    override fun deserialize(decoder: Decoder): BicoccappExamBookingResponse {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JSON")
        val jsonObject = input.decodeJsonElement().jsonObject

        val status = jsonObject["status"]?.jsonPrimitive?.contentOrNull
        if(status == "ok") {
            return BicoccappExamBookingResponse.Success
        } else {
            val code = jsonObject["code"]?.jsonPrimitive?.contentOrNull ?: ""
            val message = jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: ""
            val redirectUrl = jsonObject["redirect"]?.jsonPrimitive?.contentOrNull
            return BicoccappExamBookingResponse.Error(
                status = status ?: "",
                code = code,
                message = message,
                redirectUrl = redirectUrl
            )
        }
    }

    override fun serialize(encoder: Encoder, value: BicoccappExamBookingResponse) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("Expected JSON")
        val json = buildJsonObject {
            when (value) {
                is BicoccappExamBookingResponse.Success -> {

                }

                is BicoccappExamBookingResponse.Error -> {
                    put("code", value.code)
                    put("message", value.message)
                    put("redirect", value.redirectUrl)
                }
            }
        }
        output.encodeJsonElement(json)
    }
}