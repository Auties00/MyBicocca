package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * What the JSON responses of an API look like, as a JSON document the live test runtimes check
 * real responses against, so that a server sending fields the spec doesn't know, values of
 * another type or enum values it doesn't list is noticed, though the lenient clients decode it:
 *
 * ```json
 * {
 *   "records": { "Esse3Career": [["stuId", {"k": "int"}, false], ["dataIscr", {"k": "date", "p": "dd/MM/yyyy", "blank": true}, true]] },
 *   "enums": { "Esse3Status": { "values": ["A", "C"], "int": false, "wrapped": true } }
 * }
 * ```
 *
 * A record lists `[wireName, shape, optional]` per field: an optional field may be missing or
 * null. Shapes are objects with a kind `k`:
 *
 * | `k`        | Values                                                                    |
 * |------------|---------------------------------------------------------------------------|
 * | `string`   | strings; `blank` / `null` list those meaning no value                      |
 * | `int`, `number`, `bool` | numbers, booleans, and the same as strings (`"1"`, `"true"`), booleans also as `0`/`1` |
 * | `date`, `datetime`, `time` | strings in pattern `p` (Unicode date syntax), blank ones too when `blank` |
 * | `epoch`    | seconds since the epoch, as a number or string                             |
 * | `enum`     | the values of enum `n`                                                     |
 * | `record`   | an object (or a delimited string) of record `n`; `folded` / `embedded` when it may arrive as an array of single-key objects or JSON in a string |
 * | `list`     | arrays of `e`; also objects when `objectValues`, and joined strings when `joined` |
 * | `map`      | objects of `v`, also `[]`                                                  |
 * | `json`, `any`, `html` | anything (an object, any value, an HTML fragment)               |
 */
object Shapes {
    /** The shapes of [roots] and the records and enums they reach. */
    fun json(spec: ApiSpec, roots: Collection<TypeRef>): JsonObject {
        val records = sortedMapOf<String, RecordType>()
        val enums = sortedMapOf<String, EnumType>()
        fun visit(type: TypeRef) {
            when (type) {
                is TypeRef.Named -> when (val declaration = spec.declarations[type.name]) {
                    is RecordType -> if (records.put(declaration.name, declaration) == null) declaration.fields.forEach { visit(it.type) }
                    is EnumType -> enums[declaration.name] = declaration
                    null -> Unit
                }
                is TypeRef.ListOf -> visit(type.element)
                is TypeRef.MapOf -> {
                    visit(type.key)
                    visit(type.value)
                }
                is TypeRef.Primitive -> Unit
            }
        }
        roots.forEach(::visit)
        return buildJsonObject {
            putJsonObject("records") {
                // Delimited records are strings, whose shape is a string's.
                for ((name, record) in records.filterValues { it.encoding !is RecordEncoding.Delimited }) {
                    putJsonArray(name) {
                        for (field in record.fields) {
                            add(buildJsonArray {
                                add(JsonPrimitive(field.wireName))
                                add(shape(spec, field.type, field.codec))
                                add(JsonPrimitive(isOptional(field)))
                            })
                        }
                    }
                }
            }
            putJsonObject("enums") {
                for ((name, enum) in enums) {
                    putJsonObject(name) {
                        put("values", JsonArray(enum.values.map { JsonPrimitive(it.wireValue) }))
                        put("int", enum.integer)
                        put("wrapped", enum.acceptsWrappedValue)
                    }
                }
            }
        }
    }

    /** The shape of a value of [type] written with [codec]. */
    fun shape(spec: ApiSpec, type: TypeRef, codec: Codec? = null): JsonElement = when (type) {
        is TypeRef.Primitive -> primitive(type.kind, codec)
        is TypeRef.Named -> when (val declaration = spec.declarations[type.name]) {
            is EnumType -> kind("enum") { put("n", declaration.name) }
            is RecordType -> when {
                codec is Codec.EmbeddedHtml -> kind("html")
                declaration.encoding is RecordEncoding.Delimited -> kind("string")
                else -> kind("record") {
                    put("n", declaration.name)
                    if (codec == Codec.FoldedObject) put("folded", true)
                    if (codec == Codec.EmbeddedJson) put("embedded", true)
                }
            }
            null -> kind("any")
        }
        is TypeRef.ListOf -> when (codec) {
            is Codec.EmbeddedHtml -> kind("html")
            else -> kind("list") {
                put("e", shape(spec, type.element))
                if (codec == Codec.ObjectValues) put("objectValues", true)
                if (codec is Codec.Joined) put("joined", true)
            }
        }
        is TypeRef.MapOf -> if (codec is Codec.EmbeddedHtml) kind("html") else kind("map") { put("v", shape(spec, type.value)) }
    }

    private fun primitive(kind: PrimitiveKind, codec: Codec?): JsonElement {
        if (codec is Codec.EmbeddedHtml) return kind("html")
        if (codec == Codec.EpochSeconds) return kind("epoch")
        return when (kind) {
            PrimitiveKind.STRING -> kind("string") {
                (codec as? Codec.BlankAsNull)?.let { blank ->
                    put("blank", true)
                    if (blank.alsoNull.isNotEmpty()) put("null", JsonArray(blank.alsoNull.map(::JsonPrimitive)))
                }
            }
            PrimitiveKind.INT32, PrimitiveKind.INT64 -> kind("int")
            PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> kind("number")
            PrimitiveKind.BOOLEAN -> kind("bool")
            PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME -> {
                val temporal = codec as? Codec.Temporal ?: Codec.Temporal.iso(kind)
                kind(
                    when (kind) {
                        PrimitiveKind.LOCAL_DATE -> "date"
                        PrimitiveKind.LOCAL_DATE_TIME -> "datetime"
                        else -> "time"
                    }
                ) {
                    put("p", temporal.pattern)
                    if (temporal.blankAsNull) put("blank", true)
                }
            }
            PrimitiveKind.JSON_OBJECT -> kind("json")
            PrimitiveKind.JSON_VALUE, PrimitiveKind.BINARY, PrimitiveKind.FILE -> kind("any")
        }
    }

    private fun kind(kind: String, body: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit = {}): JsonObject =
        buildJsonObject {
            put("k", kind)
            body()
        }

    /** Whether [field] may be missing from its record's object. */
    fun isOptional(field: Field): Boolean = field.nullable || field.default != null
}
