package it.attendance100.mybicocca.data.dto.easystaff

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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