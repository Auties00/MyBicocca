package it.attendance100.mybicocca.codegen.model

/** A reference to a type, as used by fields, parameters and responses. */
sealed interface TypeRef {
    /** A built-in type. */
    data class Primitive(val kind: PrimitiveKind) : TypeRef

    /** A [TypeDeclaration] of the same [ApiSpec]. */
    data class Named(val name: String) : TypeRef

    /** An ordered list of [element]s. */
    data class ListOf(val element: TypeRef) : TypeRef

    /**
     * A JSON object read as a map from [key]s to [value]s. Keys are strings on the wire, so
     * [key] is a type with a string form (see [ApiSpec.isMapKey]).
     *
     * PHP writes an empty map as `[]`, and some servers send a map as an array of single-key
     * objects; maps accept both, merging the objects of an array.
     */
    data class MapOf(val key: TypeRef, val value: TypeRef) : TypeRef

    companion object {
        val STRING = Primitive(PrimitiveKind.STRING)
    }
}

/** The built-in types every target maps to a native one. */
enum class PrimitiveKind {
    STRING,
    INT32,
    INT64,
    FLOAT32,
    FLOAT64,
    BOOLEAN,

    /** A calendar date without a time zone. */
    LOCAL_DATE,

    /** A date and time without a time zone. */
    LOCAL_DATE_TIME,

    /** A time of day without a time zone. */
    LOCAL_TIME,

    /** An arbitrary JSON object, kept undecoded. */
    JSON_OBJECT,

    /** Any JSON value, kept undecoded. */
    JSON_VALUE,

    /** Raw bytes, streamed when the target supports it. */
    BINARY;

    /** Whether values of this kind are dates or times, whose wire format a [Codec.Temporal] can set. */
    val isTemporal: Boolean get() = this == LOCAL_DATE || this == LOCAL_DATE_TIME || this == LOCAL_TIME

    /** Whether values of this kind are JSON strings, numbers or booleans. */
    val isScalar: Boolean get() = this in setOf(STRING, INT32, INT64, FLOAT32, FLOAT64, BOOLEAN)
}

/** A named type declared by an API. */
sealed interface TypeDeclaration {
    val name: String
    val description: String?
}

/**
 * A structured value with named fields, encoded as a JSON object unless [encoding] says
 * otherwise.
 *
 * @property fields The fields, in declaration order.
 * @property scrape How the record is read out of an HTML element, when it is.
 */
data class RecordType(
    override val name: String,
    val fields: List<Field>,
    override val description: String? = null,
    val encoding: RecordEncoding = RecordEncoding.Object,
    val scrape: RecordScrape? = null,
) : TypeDeclaration

/** How a [RecordType] is written on the wire. */
sealed interface RecordEncoding {
    /** A JSON object with a key per field. */
    data object Object : RecordEncoding

    /**
     * A JSON string joining the fields' values with [separator], in order (e.g. `09:00-09:30`
     * for a range of two times). Every field is a non-null scalar or temporal primitive,
     * written in its own codec's format.
     */
    data class Delimited(val separator: String) : RecordEncoding
}

/**
 * A field of a [RecordType].
 *
 * @property name camelCase name of the field.
 * @property wireName Key of the field in the JSON object.
 * @property nullable Whether the field may hold no value.
 * @property default Value the field takes when the key is missing or holds an unusable value.
 *   Null means the key is mandatory.
 * @property codec Non-default wire format of the value, if any. Dates and times without one
 *   are written in ISO 8601 (see [Codec.Temporal.iso]).
 * @property scrape How the field is read out of its record's HTML element, when the record is
 *   scraped (see [RecordType.scrape]).
 */
data class Field(
    val name: String,
    val wireName: String,
    val type: TypeRef,
    val nullable: Boolean,
    val default: DefaultValue?,
    val codec: Codec? = null,
    val description: String? = null,
    val scrape: List<ScrapeStep>? = null,
)

/** The value a field takes when its key is missing or unusable. */
sealed interface DefaultValue {
    /** No value; only valid on nullable fields. */
    data object Null : DefaultValue

    /** An empty list; only valid on list fields. */
    data object EmptyList : DefaultValue

    /** An empty map; only valid on map fields. */
    data object EmptyMap : DefaultValue

    /** The zero value of a scalar primitive: an empty string, `0` or `false`. */
    data object Zero : DefaultValue

    /**
     * A specific value of a scalar primitive or an enum: the text of a string or the wire
     * value of an enum (unquoted), or a number or boolean as written in JSON.
     */
    data class Literal(val value: String) : DefaultValue
}

/**
 * An enumeration of string or integer values.
 *
 * @property open Whether values outside [values] are kept as an "unknown" case instead of
 *   failing to decode. Servers add values over time, so APIs are normally open.
 * @property acceptsWrappedValue Whether a value may also arrive wrapped as `{"value": "…"}`.
 * @property integer Whether values are written as JSON integers rather than strings. Their
 *   [EnumValue.wireValue]s are still the decimal text, and decoding accepts either form.
 */
data class EnumType(
    override val name: String,
    val values: List<EnumValue>,
    val open: Boolean = true,
    val acceptsWrappedValue: Boolean = false,
    override val description: String? = null,
    val integer: Boolean = false,
) : TypeDeclaration

/**
 * A value of an [EnumType].
 *
 * @property name PascalCase name of the value.
 * @property wireValue The string written on the wire.
 */
data class EnumValue(
    val name: String,
    val wireValue: String,
    val description: String? = null,
)

/**
 * A non-default wire format, implemented once per language by the target's runtime.
 *
 * The set is closed on purpose: every target has to handle every codec, so adding one is a
 * compile error in each target until it is implemented there.
 */
sealed interface Codec {
    /**
     * A date or time written with [pattern], whose letters follow the Unicode date pattern
     * syntax (`yyyy`, `MM`, `dd`, `HH`, `mm`, `ss`).
     *
     * @property blankAsNull Whether blank strings (and the literal `null`) decode to no value.
     */
    data class Temporal(val pattern: String, val blankAsNull: Boolean) : Codec {
        companion object {
            /** The ISO 8601 format, which dates and times use when they have no codec. */
            fun iso(kind: PrimitiveKind): Temporal = when (kind) {
                PrimitiveKind.LOCAL_DATE -> Temporal("yyyy-MM-dd", blankAsNull = false)
                PrimitiveKind.LOCAL_DATE_TIME -> Temporal("yyyy-MM-dd'T'HH:mm:ss", blankAsNull = false)
                PrimitiveKind.LOCAL_TIME -> Temporal("HH:mm:ss", blankAsNull = false)
                else -> throw IllegalArgumentException("$kind isn't a date or time")
            }
        }
    }

    /**
     * A string where blank strings, and any of [alsoNull] (such as `"0"`), mean no value.
     */
    data class BlankAsNull(val alsoNull: List<String> = emptyList()) : Codec

    /**
     * A record that may also arrive as an array of single-key objects, merged into one object
     * (`[{"a": 1}, {"b": 2}]` for `{"a": 1, "b": 2}`), as Laravel writes some settings.
     */
    data object FoldedObject : Codec

    /**
     * A record that may also arrive as a JSON document inside a string. A blank string means
     * no value.
     */
    data object EmbeddedJson : Codec
}
