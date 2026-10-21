package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Chooses the serializer of every value kotlinx.serialization can't read with its defaults,
 * and emits the ones the API needs into `<Prefix>Serializers.kt`:
 *
 * - dates and times use the runtime's serializers (see [TemporalSerializers]);
 * - booleans use `<Prefix>BooleanSerializer`, which also reads `0`/`1` and strings;
 * - maps use `<Prefix>MapSerializer`, which also reads PHP's `[]` and arrays of objects;
 * - [Codec.BlankAsNull], [Codec.FoldedObject], [Codec.EmbeddedJson], [Codec.EpochSeconds],
 *   [Codec.Joined] and [Codec.ObjectValues] fields use a serializer per codec and type;
 * - [Codec.EmbeddedHtml] fields use a serializer per field, emitted with the scrapers (see
 *   [ScrapeEmitter]).
 *
 * The file also holds `appendBrackets`, which writes [ParameterEncoding.BRACKETS] parameters.
 *
 * A field names its serializer with `@Serializable(with = …)`; values nested in lists and maps
 * name theirs with the same annotation on the type argument.
 */
internal class KotlinSerializers(
    private val spec: ApiSpec,
    private val layout: KotlinLayout,
    private val temporal: TemporalSerializers,
) {
    private val prefix = spec.prefix
    private val boolean = "${prefix}BooleanSerializer"
    private val map = "${prefix}MapSerializer"
    private val epochSeconds = "${prefix}EpochSecondsSerializer"
    private val objectValues = "${prefix}ListSerializer"

    private val fields: List<Field> = spec.modules.flatMap { it.records }.flatMap { it.fields }

    /** Every distinct [Codec.BlankAsNull], in the order fields first use it. */
    private val blankAsNullCodecs: List<Codec.BlankAsNull> = fields.mapNotNull { it.codec as? Codec.BlankAsNull }.distinct()

    /** Every distinct [Codec.Joined], in the order fields first use it. */
    private val joinedCodecs: List<Codec.Joined> = fields.mapNotNull { it.codec as? Codec.Joined }.distinct()

    val fileName = "${prefix}Serializers.kt"

    private val usesBrackets: Boolean =
        spec.modules.any { module -> module.operations.any { operation -> operation.parameters.any { it.encoding == ParameterEncoding.BRACKETS } } }

    /** Whether the API needs any of the declarations this class emits. */
    val isNeeded: Boolean
        get() = usesBrackets ||
            fields.any { field -> field.codec.let { it != null && it !is Codec.Temporal && it !is Codec.EmbeddedHtml } || field.type.contains { isBoolean(it) || it is TypeRef.MapOf } }

    /**
     * The serializer of a field, or null when the type's own one does. [file] imports it.
     */
    fun of(record: RecordType, field: Field, file: KotlinFile): String? =
        if (field.codec is Codec.EmbeddedHtml) layout.htmlSerializer(record, field) else of(field.type, field.codec, field.nullable, field.name, file)

    /**
     * Renders [type] as the type of a field, annotating the type arguments whose values need
     * a serializer. [file] imports what it references.
     */
    fun render(type: TypeRef, file: KotlinFile, where: String): String = when (type) {
        is TypeRef.ListOf -> "List<${argument(type.element, file, where)}>"
        is TypeRef.MapOf -> "Map<${argument(type.key, file, where)}, ${argument(type.value, file, where)}>"
        else -> layout.render(type, file)
    }

    private fun argument(type: TypeRef, file: KotlinFile, where: String): String {
        val serializer = of(type, codec = null, nullable = false, where, file)
        val rendered = render(type, file, where)
        if (serializer == null) return rendered
        file.import("kotlinx.serialization.Serializable")
        return "@Serializable(with = $serializer::class) $rendered"
    }

    private fun of(type: TypeRef, codec: Codec?, nullable: Boolean, where: String, file: KotlinFile): String? {
        val name = when (codec) {
            is Codec.Temporal -> temporal(type, codec, nullable, where)
            is Codec.BlankAsNull -> blankAsNull(codec)
            Codec.FoldedObject -> folded((type as TypeRef.Named).name)
            Codec.EmbeddedJson -> embedded((type as TypeRef.Named).name)
            Codec.EpochSeconds -> epochSeconds
            is Codec.Joined -> joined(codec)
            Codec.ObjectValues -> objectValues
            is Codec.EmbeddedHtml -> error("'$where' is read from HTML by a serializer of the scrapers")
            null -> when {
                type is TypeRef.Primitive && type.kind.isTemporal -> temporal(type, Codec.Temporal.iso(type.kind), nullable, where)
                isBoolean(type) -> boolean
                type is TypeRef.MapOf -> map
                else -> null
            }
        } ?: return null
        return qualified(name, file)
    }

    private fun temporal(type: TypeRef, codec: Codec.Temporal, nullable: Boolean, where: String): String {
        val kind = (type as? TypeRef.Primitive)?.kind?.takeIf { it.isTemporal } ?: error("'$where' has a temporal codec but isn't a date or time")
        return temporal.find(kind, codec, nullable)
    }

    /** Imports [name] when it's qualified, and returns its simple name. */
    private fun qualified(name: String, file: KotlinFile): String {
        if ('.' !in name) {
            file.import("${layout.dtoPackage}.$name")
            return name
        }
        file.import(name)
        return name.substringAfterLast('.')
    }

    private fun blankAsNull(codec: Codec.BlankAsNull): String {
        if (codec.alsoNull.isEmpty()) return "${prefix}BlankAsNullSerializer"
        val readable = codec.alsoNull.all { value -> value.isNotEmpty() && value.all { it.isLetterOrDigit() } }
        val marker = if (readable) codec.alsoNull.joinToString("Or") { it.replaceFirstChar(Char::uppercaseChar) } else "${blankAsNullCodecs.indexOf(codec) + 1}"
        return "${prefix}BlankOr${marker}AsNullSerializer"
    }

    private fun joined(codec: Codec.Joined): String =
        if (joinedCodecs.size == 1) "${prefix}JoinedSerializer" else "${prefix}Joined${joinedCodecs.indexOf(codec) + 1}Serializer"

    private fun folded(record: String): String = "${record}FoldedSerializer"

    private fun embedded(record: String): String = "${record}EmbeddedSerializer"

    private fun isBoolean(type: TypeRef): Boolean = type is TypeRef.Primitive && type.kind == PrimitiveKind.BOOLEAN

    private fun TypeRef.contains(predicate: (TypeRef) -> Boolean): Boolean = predicate(this) || when (this) {
        is TypeRef.ListOf -> element.contains(predicate)
        is TypeRef.MapOf -> key.contains(predicate) || value.contains(predicate)
        is TypeRef.Named, is TypeRef.Primitive -> false
    }

    /** Emits the serializers the API's fields use. */
    fun emit(file: KotlinFile) {
        val out = file.body
        val sections = buildList<CodeWriter.() -> Unit> {
            if (fields.any { field -> field.type.contains(::isBoolean) }) add { booleanSerializer(file) }
            val folding = fields.any { field -> field.codec == Codec.FoldedObject || field.type.contains { it is TypeRef.MapOf } }
            if (fields.any { field -> field.type.contains { it is TypeRef.MapOf } }) add { mapSerializer(file) }
            for (codec in blankAsNullCodecs) add { blankAsNullSerializer(codec, file) }
            for (record in recordsWith(Codec.FoldedObject)) add { foldedSerializer(record, file) }
            for (record in recordsWith(Codec.EmbeddedJson)) add { embeddedSerializer(record, file) }
            if (fields.any { it.codec == Codec.EpochSeconds }) add { epochSecondsSerializer(file) }
            for (codec in joinedCodecs) add { joinedSerializer(codec, file) }
            if (fields.any { it.codec == Codec.ObjectValues }) add { objectValuesSerializer(file) }
            if (folding) add { foldObjects(file) }
            if (usesBrackets) add { appendBrackets(file) }
        }
        out.separated(sections) { it(this) }
    }

    private fun recordsWith(codec: Codec): List<RecordType> =
        fields.filter { it.codec == codec }.map { (it.type as TypeRef.Named).name }.distinct()
            .map { spec.resolve(TypeRef.Named(it)) as RecordType }

    private fun CodeWriter.booleanSerializer(file: KotlinFile) {
        importAll(file, "kotlinx.serialization.KSerializer", "kotlinx.serialization.SerializationException", PRIMITIVE_KIND, PRIMITIVE_DESCRIPTOR, DECODER, ENCODER, JSON_DECODER, JSON_PRIMITIVE, "kotlinx.serialization.json.JsonNull")
        line("/** Booleans, also sent as `0`/`1` or as strings (`\"true\"`, `\"1\"`), as lenient servers send them. */")
        block("object $boolean : KSerializer<Boolean> {") {
            line("override val descriptor = PrimitiveSerialDescriptor(${KotlinSyntax.string(boolean)}, PrimitiveKind.BOOLEAN)")
            line()
            line("override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)")
            line()
            block("override fun deserialize(decoder: Decoder): Boolean {") {
                line("val json = decoder as? JsonDecoder ?: return decoder.decodeBoolean()")
                line("val element = json.decodeJsonElement()")
                block("return when ((element as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content?.lowercase()) {") {
                    line("\"true\", \"1\" -> true")
                    line("\"false\", \"0\" -> false")
                    line("else -> throw SerializationException(\"Expected a boolean, found \$element\")")
                }
            }
        }
    }

    private fun CodeWriter.mapSerializer(file: KotlinFile) {
        importAll(file, "kotlinx.serialization.KSerializer", "kotlinx.serialization.builtins.MapSerializer", JSON_ELEMENT, TRANSFORMING)
        line("/** Maps, also sent as PHP's `[]` when empty, or as an array of single-key objects. */")
        line("class $map<K, V>(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>) :")
        line("    JsonTransformingSerializer<Map<K, V>>(MapSerializer(keySerializer, valueSerializer)) {")
        indent { line("override fun transformDeserialize(element: JsonElement): JsonElement = foldObjects(element)") }
        line("}")
    }

    private fun CodeWriter.blankAsNullSerializer(codec: Codec.BlankAsNull, file: KotlinFile) {
        importAll(
            file, "kotlinx.serialization.KSerializer", PRIMITIVE_KIND, PRIMITIVE_DESCRIPTOR, DECODER, ENCODER, JSON_DECODER, JSON_PRIMITIVE,
            "kotlinx.serialization.descriptors.nullable", "kotlinx.serialization.json.JsonNull",
        )
        val name = blankAsNull(codec)
        val also = codec.alsoNull.joinToString(", ") { "`\"${KotlinSyntax.docText(it)}\"`" }
        val meaning = if (codec.alsoNull.isEmpty()) "blank ones" else "blank ones, and $also,"
        line("/** Strings where $meaning mean no value; so do `null` and values that aren't strings, numbers or booleans. */")
        block("object $name : KSerializer<String?> {") {
            if (codec.alsoNull.isNotEmpty()) {
                line("private val nullValues = ${codec.alsoNull.joinToString(", ", "setOf(", ")") { KotlinSyntax.string(it) }}")
                line()
            }
            line("override val descriptor = PrimitiveSerialDescriptor(${KotlinSyntax.string(name)}, PrimitiveKind.STRING).nullable")
            line()
            line("override fun serialize(encoder: Encoder, value: String?) = encoder.encodeString(value ?: \"\")")
            line()
            val condition = if (codec.alsoNull.isEmpty()) "it.isBlank()" else "it.isBlank() || it in nullValues"
            block("override fun deserialize(decoder: Decoder): String? {") {
                line("val text = when (decoder) {")
                indent {
                    line("is JsonDecoder -> (decoder.decodeJsonElement() as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content")
                    line("else -> decoder.decodeString()")
                }
                line("}")
                line("return text?.takeUnless { $condition }")
            }
        }
    }

    private fun CodeWriter.foldedSerializer(record: RecordType, file: KotlinFile) {
        importAll(file, JSON_ELEMENT, TRANSFORMING)
        line("/** [${record.name}], also sent as an array of single-key objects. */")
        block("object ${folded(record.name)} : JsonTransformingSerializer<${record.name}>(${record.name}.serializer()) {") {
            line("override fun transformDeserialize(element: JsonElement): JsonElement = foldObjects(element)")
        }
    }

    private fun CodeWriter.embeddedSerializer(record: RecordType, file: KotlinFile) {
        importAll(
            file, "kotlinx.serialization.KSerializer", DECODER, ENCODER, JSON_DECODER, JSON_PRIMITIVE,
            "kotlinx.serialization.descriptors.nullable", "kotlinx.serialization.json.JsonObject", "kotlinx.serialization.json.contentOrNull",
            "kotlinx.serialization.builtins.nullable",
        )
        val name = record.name
        line("/** [$name], also sent as a JSON document inside a string; a blank string means no value. */")
        block("object ${embedded(name)} : KSerializer<$name?> {") {
            line("override val descriptor = $name.serializer().descriptor.nullable")
            line()
            line("override fun serialize(encoder: Encoder, value: $name?) = encoder.encodeSerializableValue($name.serializer().nullable, value)")
            line()
            block("override fun deserialize(decoder: Decoder): $name? {") {
                line("val json = decoder as? JsonDecoder ?: return decoder.decodeSerializableValue($name.serializer().nullable)")
                block("return when (val element = json.decodeJsonElement()) {") {
                    line("is JsonObject -> json.json.decodeFromJsonElement($name.serializer(), element)")
                    line("is JsonPrimitive -> element.contentOrNull?.takeUnless { it.isBlank() }?.let { json.json.decodeFromString($name.serializer(), it) }")
                    line("else -> null")
                }
            }
        }
    }

    private fun CodeWriter.epochSecondsSerializer(file: KotlinFile) {
        importAll(
            file, "kotlinx.serialization.KSerializer", "kotlinx.serialization.SerializationException", PRIMITIVE_KIND, PRIMITIVE_DESCRIPTOR,
            DECODER, ENCODER, JSON_DECODER, JSON_PRIMITIVE, "java.time.Instant", "java.time.LocalDateTime", "java.time.ZoneId",
        )
        line("/** Dates and times sent as seconds since the Unix epoch, read in the device's time zone; numbers may be sent as strings. */")
        block("object $epochSeconds : KSerializer<LocalDateTime> {") {
            line("override val descriptor = PrimitiveSerialDescriptor(${KotlinSyntax.string(epochSeconds)}, PrimitiveKind.LONG)")
            line()
            line("override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeLong(value.atZone(ZoneId.systemDefault()).toEpochSecond())")
            line()
            block("override fun deserialize(decoder: Decoder): LocalDateTime {") {
                line("val json = decoder as? JsonDecoder")
                line("val seconds = if (json == null) decoder.decodeLong() else {")
                indent {
                    line("val element = json.decodeJsonElement()")
                    line("(element as? JsonPrimitive)?.content?.toLongOrNull() ?: throw SerializationException(\"Expected epoch seconds, found \$element\")")
                }
                line("}")
                line("return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault())")
            }
        }
    }

    private fun CodeWriter.joinedSerializer(codec: Codec.Joined, file: KotlinFile) {
        importAll(file, "kotlinx.serialization.KSerializer", PRIMITIVE_KIND, PRIMITIVE_DESCRIPTOR, DECODER, ENCODER, JSON_DECODER, JSON_PRIMITIVE, "kotlinx.serialization.json.contentOrNull")
        val name = joined(codec)
        val separator = KotlinSyntax.string(codec.separator)
        line("/** Lists of strings sent as one string joining them with `${KotlinSyntax.docText(codec.separator)}`; blank elements are left out. */")
        block("object $name : KSerializer<List<String>> {") {
            line("override val descriptor = PrimitiveSerialDescriptor(${KotlinSyntax.string(name)}, PrimitiveKind.STRING)")
            line()
            line("override fun serialize(encoder: Encoder, value: List<String>) = encoder.encodeString(value.joinToString($separator))")
            line()
            block("override fun deserialize(decoder: Decoder): List<String> {") {
                line("val text = if (decoder is JsonDecoder) (decoder.decodeJsonElement() as? JsonPrimitive)?.contentOrNull else decoder.decodeString()")
                line("return text.orEmpty().split($separator).map { it.trim() }.filter { it.isNotEmpty() }")
            }
        }
    }

    private fun CodeWriter.objectValuesSerializer(file: KotlinFile) {
        importAll(file, "kotlinx.serialization.KSerializer", "kotlinx.serialization.builtins.ListSerializer", JSON_ELEMENT, TRANSFORMING, "kotlinx.serialization.json.JsonArray", "kotlinx.serialization.json.JsonObject")
        line("/** Lists, also sent as an object whose values are the elements. */")
        line("class $objectValues<T>(elementSerializer: KSerializer<T>) : JsonTransformingSerializer<List<T>>(ListSerializer(elementSerializer)) {")
        indent {
            line("override fun transformDeserialize(element: JsonElement): JsonElement = if (element is JsonObject) JsonArray(element.values.toList()) else element")
        }
        line("}")
    }

    private fun CodeWriter.foldObjects(file: KotlinFile) {
        importAll(file, JSON_ELEMENT, "kotlinx.serialization.json.JsonArray", "kotlinx.serialization.json.JsonObject")
        line("/** Merges an array of objects into one object, as some servers send objects and maps. */")
        block("private fun foldObjects(element: JsonElement): JsonElement = when (element) {") {
            line("is JsonArray -> JsonObject(element.filterIsInstance<JsonObject>().flatMap { it.entries }.associate { it.key to it.value })")
            line("else -> element")
        }
    }

    private fun CodeWriter.appendBrackets(file: KotlinFile) {
        importAll(
            file, "io.ktor.http.ParametersBuilder", JSON_ELEMENT, JSON_PRIMITIVE, "kotlinx.serialization.json.JsonArray",
            "kotlinx.serialization.json.JsonNull", "kotlinx.serialization.json.JsonObject", "kotlinx.serialization.json.booleanOrNull",
        )
        line("/**")
        line(" * Appends [value] as PHP-style bracketed fields named after [name] (`name[0][key]=value`), as")
        line(" * PHP servers read lists and records. Booleans are written `1` and `0`; nulls are left out.")
        line(" */")
        block("internal fun ParametersBuilder.appendBrackets(name: String, value: JsonElement) {") {
            block("when (value) {") {
                line("is JsonObject -> value.forEach { (key, item) -> appendBrackets(\"\$name[\$key]\", item) }")
                line("is JsonArray -> value.forEachIndexed { index, item -> appendBrackets(\"\$name[\$index]\", item) }")
                line("JsonNull -> Unit")
                line("is JsonPrimitive -> append(name, value.takeUnless { it.isString }?.booleanOrNull?.let { if (it) \"1\" else \"0\" } ?: value.content)")
            }
        }
    }

    private fun importAll(file: KotlinFile, vararg names: String) = names.forEach(file::import)

    private companion object {
        const val PRIMITIVE_KIND = "kotlinx.serialization.descriptors.PrimitiveKind"
        const val PRIMITIVE_DESCRIPTOR = "kotlinx.serialization.descriptors.PrimitiveSerialDescriptor"
        const val DECODER = "kotlinx.serialization.encoding.Decoder"
        const val ENCODER = "kotlinx.serialization.encoding.Encoder"
        const val JSON_DECODER = "kotlinx.serialization.json.JsonDecoder"
        const val JSON_PRIMITIVE = "kotlinx.serialization.json.JsonPrimitive"
        const val JSON_ELEMENT = "kotlinx.serialization.json.JsonElement"
        const val TRANSFORMING = "kotlinx.serialization.json.JsonTransformingSerializer"
    }
}
