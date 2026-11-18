package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import kotlinx.serialization.Serializable

/**
 * kotlinx.serialization needs a serializer object per date format, so the kotlin target's
 * options name one for each [Codec.Temporal] the API uses. Those with a simple name are
 * generated in the `dto` package; a qualified name refers to one declared elsewhere. One
 * reading blank strings as no value is nullable, so the nullable and non-null uses of a format
 * may share a serializer only when it doesn't.
 *
 * @property serializer Name of the serializer object.
 */
@Serializable
data class TemporalSerializer(
    val kind: PrimitiveKind,
    val pattern: String,
    val blankAsNull: Boolean,
    val nullable: Boolean,
    val serializer: String,
)

internal class TemporalSerializers(private val serializers: List<TemporalSerializer>) {
    fun find(kind: PrimitiveKind, codec: Codec.Temporal, nullable: Boolean): String = serializer(kind, codec, nullable).serializer

    fun serializer(kind: PrimitiveKind, codec: Codec.Temporal, nullable: Boolean): TemporalSerializer =
        serializers.firstOrNull {
            it.kind == kind && it.pattern == codec.pattern && it.blankAsNull == codec.blankAsNull && it.nullable == nullable
        } ?: error(
            "No Kotlin serializer configured for $kind '${codec.pattern}' (blankAsNull=${codec.blankAsNull}, nullable=$nullable). " +
                "Add one to the kotlin target's temporalSerializers option."
        )
}
