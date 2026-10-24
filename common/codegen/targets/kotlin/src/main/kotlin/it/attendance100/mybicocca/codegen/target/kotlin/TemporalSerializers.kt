package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import kotlinx.serialization.Serializable

/**
 * kotlinx.serialization needs a serializer object per date format, so the API's runtime
 * declares one for each [Codec.Temporal] it uses and lists it here.
 *
 * @property serializer Name of the serializer object: a simple name for one in the `dto`
 *   package, or a qualified name.
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
    fun find(kind: PrimitiveKind, codec: Codec.Temporal, nullable: Boolean): String =
        serializers.firstOrNull {
            it.kind == kind && it.pattern == codec.pattern && it.blankAsNull == codec.blankAsNull && it.nullable == nullable
        }?.serializer ?: error(
            "No Kotlin serializer configured for $kind '${codec.pattern}' (blankAsNull=${codec.blankAsNull}, nullable=$nullable). " +
                "Add one to the kotlin target's temporalSerializers option."
        )
}
