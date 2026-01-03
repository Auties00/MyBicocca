package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ElearningSortOrderSerializer::class)
enum class ElearningSortOrder(val id: Int) {
    LAST_POST_DESCENDING(1),
    LAST_POST_ASCENDING(2),
    CREATION_DATE_DESCENDING(3),
    CREATION_DATE_ASCENDING(4),
    REPLY_COUNT_DESCENDING(5),
    REPLY_COUNT_ASCENDING(6);

    companion object {
        fun fromId(id: Int): ElearningSortOrder? = entries.find { it.id == id }
    }
}

object ElearningSortOrderSerializer : KSerializer<ElearningSortOrder> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ElearningSortOrder", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ElearningSortOrder) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): ElearningSortOrder {
        val id = decoder.decodeInt()
        return ElearningSortOrder.fromId(id)
            ?: throw IllegalArgumentException("Unknown ElearningSortOrder id: $id")
    }
}