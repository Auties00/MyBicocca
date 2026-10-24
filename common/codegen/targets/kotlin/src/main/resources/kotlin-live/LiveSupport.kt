package __PACKAGE__

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Decodes the JSON values `tests.yaml` gives records and lists, as servers send them. */
val LiveJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

/**
 * The report of an API's live tests: one JSON object per line, in `<platform>-<api>.jsonl` under
 * [LiveSettings.reportDir]. It holds outcomes, findings and fingerprints, never values or
 * credentials, so that it can be shared.
 */
class LiveReport(private val api: String) {
    private val file = File(LiveSettings.reportDir, "${LiveSettings.PLATFORM}-$api.jsonl")
    private val findings = mutableSetOf<String>()
    private val seen = mutableSetOf<String>()

    fun start(skips: Map<String, String>, declared: List<String>) {
        file.parentFile.mkdirs()
        file.writeText("")
        write("run") { put("seed", LiveSettings.seed); put("writes", LiveSettings.writes) }
        for ((operation, reason) in skips) write("skip") { put("id", operation); put("reason", reason) }
        write("declared") { put("fields", JsonArray(declared.map(::JsonPrimitive))) }
    }

    fun case(id: String, status: String, reason: String?, fingerprint: String?, millis: Long) = write("case") {
        put("id", id)
        put("status", status)
        reason?.let { put("reason", it) }
        fingerprint?.let { put("fingerprint", it) }
        put("ms", millis)
    }

    /** Something a response did that the spec doesn't say, reported once per kind and place. */
    fun finding(kind: String, type: String, field: String, detail: String) {
        if (!findings.add("$kind|$type|$field")) return
        write("finding") { put("kind", kind); put("record", type); put("field", field); put("detail", detail) }
    }

    /** A field of a record that held a value. */
    fun seen(type: String, field: String) {
        if (!seen.add("$type|$field")) return
        write("seen") { put("record", type); put("field", field) }
    }

    fun event(kind: String, id: String, detail: String) = write(kind) { put("id", id); put("detail", detail) }

    @Synchronized
    private fun write(type: String, body: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit) {
        val line = buildJsonObject {
            put("type", type)
            put("platform", LiveSettings.PLATFORM)
            put("api", api)
            body()
        }
        file.appendText(line.toString() + "\n")
    }
}

/**
 * The changes write cases made and haven't undone yet, in [LiveSettings.journal], so that a run
 * interrupted between a change and its undo is cleaned up by the next one.
 */
object LiveJournal {
    class Entry(val api: String, val case: String, val values: Map<String, String>)

    private val file get() = File(LiveSettings.journal)

    @Synchronized
    fun entries(api: String): List<Entry> = read().filter { it.api == api }

    @Synchronized
    fun add(entry: Entry) = write(read().filterNot { it.api == entry.api && it.case == entry.case } + entry)

    @Synchronized
    fun remove(entry: Entry) = remove(entry.api, entry.case)

    @Synchronized
    fun remove(api: String, case: String) = write(read().filterNot { it.api == api && it.case == case })

    private fun read(): List<Entry> {
        if (!file.exists()) return emptyList()
        return Json.parseToJsonElement(file.readText().ifBlank { "[]" }).jsonArray.map { item ->
            val entry = item.jsonObject
            Entry(
                entry.getValue("api").jsonPrimitive.content,
                entry.getValue("case").jsonPrimitive.content,
                entry.getValue("values").jsonObject.mapValues { it.value.jsonPrimitive.content },
            )
        }
    }

    private fun write(entries: List<Entry>) {
        file.parentFile?.mkdirs()
        file.writeText(JsonArray(entries.map { entry ->
            buildJsonObject {
                put("api", entry.api)
                put("case", entry.case)
                put("platform", LiveSettings.PLATFORM)
                put("values", JsonObject(entry.values.mapValues { JsonPrimitive(it.value) }))
            }
        }).toString())
    }
}

/**
 * Checks responses against what the spec says they look like (the generator's `Shapes`),
 * reporting fields the spec doesn't know, values of another type than declared, required fields
 * that are missing and enum values it doesn't list, and noting the fields that held values.
 */
class LiveShapes(catalog: String, private val report: LiveReport) {
    private val root = Json.parseToJsonElement(catalog).jsonObject
    private val records = root.getValue("records").jsonObject
    private val enums = root.getValue("enums").jsonObject

    /** Every field of the records, as `Record.wireName`. */
    val fields: List<String> = records.flatMap { (name, fields) -> fields.jsonArray.map { "$name.${it.jsonArray[0].jsonPrimitive.content}" } }

    fun check(shape: String, response: Observed, envelope: List<String>, variable: String?) {
        val body = response.body ?: return
        val text = if (variable != null) scriptVariable(body, variable) ?: return else body
        var value = runCatching { Json.parseToJsonElement(text) }.getOrNull() ?: return
        for (key in envelope) value = (value as? JsonObject)?.get(key) ?: return
        visit(value, Json.parseToJsonElement(shape).jsonObject, "", "")
    }

    private fun scriptVariable(script: String, variable: String): String? = script.lineSequence()
        .map { line -> listOf("var ", "let ", "const ").fold(line.trim()) { statement, keyword -> statement.removePrefix(keyword) } }
        .firstOrNull { it.startsWith("$variable =") }
        ?.removePrefix("$variable =")?.trim()?.removeSuffix(";")

    private fun visit(value: JsonElement, shape: JsonObject, type: String, field: String) {
        if (value == JsonNull) return
        fun mismatch(expected: String) = report.finding("type-mismatch", type, field, "expected $expected, got ${kindOf(value)}")
        val primitive = value as? JsonPrimitive
        when (val kind = shape.getValue("k").jsonPrimitive.content) {
            "string" -> if (primitive == null) mismatch("a string")
            "int" -> if (primitive == null || (primitive.longOrNull == null && primitive.content.trim().toLongOrNull() == null && primitive.content.isNotBlank())) mismatch("an integer")
            "number", "epoch" -> if (primitive == null || (primitive.doubleOrNull == null && primitive.content.isNotBlank())) mismatch("a number")
            "bool" -> if (primitive == null || primitive.content.lowercase() !in BOOLEANS) mismatch("a boolean")
            "date", "datetime", "time" -> {
                val pattern = shape.getValue("p").jsonPrimitive.content
                val blank = shape["blank"]?.jsonPrimitive?.booleanOrNull == true
                val text = primitive?.takeIf { it.isString }?.content
                when {
                    text == null -> mismatch("a $kind in $pattern")
                    text.isBlank() || text == "null" -> if (!blank) mismatch("a $kind in $pattern")
                    !temporal(pattern).containsMatchIn(text) -> report.finding("type-mismatch", type, field, "expected a $kind in $pattern")
                }
            }
            "enum" -> {
                val name = shape.getValue("n").jsonPrimitive.content
                val enum = enums.getValue(name).jsonObject
                val raw = if (enum["wrapped"]?.jsonPrimitive?.booleanOrNull == true && value is JsonObject) value["value"] else value
                val content = (raw as? JsonPrimitive)?.content
                val values = enum.getValue("values").jsonArray.map { it.jsonPrimitive.content }
                if (content == null) mismatch("a value of $name") else if (content !in values) report.finding("unknown-enum", name, field, "'$content' isn't one of its values")
            }
            "record" -> record(value, shape, type, field)
            "list" -> when {
                value is JsonArray -> value.take(MAX_ELEMENTS).forEach { visit(it, shape.getValue("e").jsonObject, type, field) }
                value is JsonObject && shape["objectValues"] != null -> value.values.take(MAX_ELEMENTS).forEach { visit(it, shape.getValue("e").jsonObject, type, field) }
                primitive != null && shape["joined"] != null -> Unit
                else -> mismatch("a list")
            }
            "map" -> when (value) {
                is JsonObject -> value.values.take(MAX_ELEMENTS).forEach { visit(it, shape.getValue("v").jsonObject, type, field) }
                is JsonArray -> value.filterIsInstance<JsonObject>().flatMap { it.values }.take(MAX_ELEMENTS).forEach { visit(it, shape.getValue("v").jsonObject, type, field) }
                else -> mismatch("an object")
            }
            "json" -> if (value !is JsonObject) mismatch("an object")
        }
    }

    private fun record(value: JsonElement, shape: JsonObject, type: String, field: String) {
        val name = shape.getValue("n").jsonPrimitive.content
        val objectValue: JsonObject = when {
            value is JsonObject -> value
            value is JsonArray && shape["folded"] != null -> JsonObject(value.filterIsInstance<JsonObject>().flatMap { it.entries }.associate { it.key to it.value })
            value is JsonPrimitive && shape["embedded"] != null -> {
                if (value.content.isBlank()) return
                runCatching { Json.parseToJsonElement(value.content) }.getOrNull() as? JsonObject
                    ?: return report.finding("type-mismatch", type, field, "expected JSON of $name in a string")
            }
            value is JsonArray && value.isEmpty() -> return
            else -> return report.finding("type-mismatch", type, field, "expected an object of $name, got ${kindOf(value)}")
        }
        val fields = records[name]?.jsonArray ?: return
        val declared = fields.associate { it.jsonArray[0].jsonPrimitive.content to it.jsonArray }
        for (key in objectValue.keys - declared.keys) report.finding("unknown-key", name, key, "the spec doesn't declare it")
        for ((wire, definition) in declared) {
            val item = objectValue[wire]
            val optional = definition[2].jsonPrimitive.booleanOrNull == true
            when {
                item == null -> if (!optional) report.finding("missing-key", name, wire, "a required field is missing")
                item == JsonNull -> if (!optional) report.finding("missing-key", name, wire, "a required field is null")
                else -> {
                    if ((item as? JsonPrimitive)?.content?.isBlank() != true) report.seen(name, wire)
                    visit(item, definition[1].jsonObject, name, wire)
                }
            }
        }
    }

    private val temporals = mutableMapOf<String, Regex>()

    /** A regex matching the start of a date or time written in [pattern] (Unicode date syntax). */
    private fun temporal(pattern: String): Regex = temporals.getOrPut(pattern) {
        val regex = StringBuilder("^")
        var index = 0
        while (index < pattern.length) {
            val char = pattern[index]
            var end = index
            while (end < pattern.length && pattern[end] == char) end++
            val count = end - index
            when {
                char == '\'' -> {
                    val close = pattern.indexOf('\'', index + 1).let { if (it < 0) pattern.length else it }
                    regex.append(Regex.escape(pattern.substring(index + 1, close)))
                    end = close + 1
                }
                char == 'y' -> regex.append(if (count == 2) "\\d{2}" else "\\d{4}")
                char == 'S' -> regex.append("\\d+")
                char.isLetter() -> regex.append("\\d{1,2}")
                else -> regex.append(Regex.escape(pattern.substring(index, end)))
            }
            index = end
        }
        Regex(regex.toString())
    }

    private fun kindOf(value: JsonElement): String = when (value) {
        is JsonObject -> "an object"
        is JsonArray -> "a list"
        JsonNull -> "null"
        is JsonPrimitive -> when {
            value.isString -> "a string"
            value.booleanOrNull != null -> "a boolean"
            else -> "a number"
        }
    }

    private companion object {
        const val MAX_ELEMENTS = 50
        val BOOLEANS = setOf("true", "false", "0", "1")
    }
}

/**
 * The canonical form of a decoded result, the same on every platform, and its hash: atoms end
 * with `;` (`~` for no value, `"…"` text with `\` and `"` escaped, integers, numbers as
 * millionths rounded down after adding a half, `t`/`f`, dates in ISO 8601, `b<size>` for bytes,
 * JSON with sorted keys); lists are `[…]`, or `<…>` with their elements sorted when their order
 * isn't meaningful, records `(…)` with their fields in declaration order, maps `{key=value…}`
 * sorted by key. The hash is the 64-bit FNV-1a of the UTF-8 text, in hex.
 */
class LiveFingerprint {
    private val out = StringBuilder()

    fun nil() {
        out.append("~;")
    }

    fun str(value: String?) = atom(value) { "\"" + it.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" }

    fun int(value: Long?) = atom(value) { it.toString() }

    fun num(value: Double?) = atom(value) { number(it) }

    fun bool(value: Boolean?) = atom(value) { if (it) "t" else "f" }

    fun date(value: LocalDate?) = atom(value) { "%04d-%02d-%02d".format(it.year, it.monthValue, it.dayOfMonth) }

    fun dateTime(value: LocalDateTime?) =
        atom(value) { "%04d-%02d-%02dT%02d:%02d:%02d".format(it.year, it.monthValue, it.dayOfMonth, it.hour, it.minute, it.second) }

    fun time(value: LocalTime?) = atom(value) { "%02d:%02d:%02d".format(it.hour, it.minute, it.second) }

    fun bytes(size: Int) {
        out.append("b").append(size).append(';')
    }

    fun json(value: JsonElement?) = atom(value?.takeUnless { it == JsonNull }) { canonical(it) }

    fun <T> list(value: List<T>?, each: (T) -> Unit) {
        if (value == null) return nil()
        out.append('[')
        value.forEach(each)
        out.append(']')
    }

    /** A list whose order isn't meaningful, such as one sent as an object's values: its elements, sorted. */
    fun <T> unordered(value: List<T>?, each: LiveFingerprint.(T) -> Unit) {
        if (value == null) return nil()
        out.append('<')
        value.map { LiveFingerprint().apply { each(it) }.out.toString() }.sorted().forEach { out.append(it) }
        out.append('>')
    }

    fun obj(value: Any?, fields: () -> Unit) {
        if (value == null) return nil()
        out.append('(')
        fields()
        out.append(')')
    }

    fun <K, V> map(value: Map<K, V>?, key: LiveFingerprint.(K) -> Unit, each: (V) -> Unit) {
        if (value == null) return nil()
        val entries = value.entries.map { (k, v) -> LiveFingerprint().apply { key(k) }.out.toString() to v }.sortedBy { it.first }
        out.append('{')
        for ((k, v) in entries) {
            out.append(k).append('=')
            each(v)
        }
        out.append('}')
    }

    fun text(): String = out.toString()

    fun hash(): String {
        var hash = -0x340d631b7bdddcdbL
        for (byte in out.toString().toByteArray(Charsets.UTF_8)) {
            hash = hash xor (byte.toLong() and 0xff)
            hash *= 0x100000001b3L
        }
        return java.lang.Long.toHexString(hash).padStart(16, '0')
    }

    private fun <T : Any> atom(value: T?, render: (T) -> String) {
        if (value == null) return nil()
        out.append(render(value)).append(';')
    }

    private fun number(value: Double): String = Math.floor(value * 1_000_000 + 0.5).toLong().toString()

    private fun canonical(value: JsonElement): String = when (value) {
        JsonNull -> "null"
        is JsonObject -> value.entries.sortedBy { it.key }.joinToString(",", "{", "}") { (key, item) -> "\"" + escape(key) + "\":" + canonical(item) }
        is JsonArray -> value.joinToString(",", "[", "]") { canonical(it) }
        is JsonPrimitive -> when {
            value.isString -> "\"" + escape(value.content) + "\""
            value.booleanOrNull != null -> value.content
            else -> {
                val number = value.content.toDouble()
                if (number == Math.floor(number) && Math.abs(number) < 9.007199254740992E15) number.toLong().toString() else number(number)
            }
        }
    }

    private fun escape(text: String): String = text.replace("\\", "\\\\").replace("\"", "\\\"")
}
