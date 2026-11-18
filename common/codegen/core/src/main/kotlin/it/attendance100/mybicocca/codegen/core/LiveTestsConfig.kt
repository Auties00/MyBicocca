package it.attendance100.mybicocca.codegen.core

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * How the live tests of an API run, as its `tests.yaml` describes them: the account they call the
 * servers with, how hard they may push them, and how to test the operations the generator can't
 * work out on its own. See [TestPlanner] for what is generated from it.
 *
 * `defaults` gives the value of every input of that name, unless an operation's `inputs` gives
 * another; optional inputs are also left out, in other combinations.
 *
 * ```yaml
 * account:
 *   env: [ESSE3_USERNAME, ESSE3_PASSWORD]
 *   roles: [STUDENT]
 *   headers: { Authorization: "Basic {base64:{env:ESSE3_USERNAME}:{env:ESSE3_PASSWORD}}" }
 * limits: { requestsPerSecond: 2, maxRequests: 1500 }
 * defaults:
 *   userId: { from: Users.getMe, pick: userId }
 * operations:
 *   Exams.getExamSessions:
 *     inputs: { activityId: { from: Transcript.getRows, pick: "[*].activityId" } }
 * ```
 */
data class LiveTestsConfig(
    val account: Account = Account(),
    val limits: Limits = Limits(),
    val defaults: Map<String, ValueSpec> = emptyMap(),
    val operations: Map<String, OperationTests> = emptyMap(),
) {
    /**
     * The account the tests call the servers with.
     *
     * @property env Environment variables the account needs; without them the tests are skipped.
     * @property roles The roles of the API (as operations name them) the account has; operations
     *   requiring others are skipped.
     * @property headers Headers every request carries, as [ValueSpec.Text] templates.
     * @property parameters Values of the API parameters.
     * @property auth Credentials of each auth scheme, by scheme and credential name.
     * @property settings Values of the client settings, by name.
     * @property anonymous Operations to call without the account's headers too, expecting the
     *   server to refuse: the check that its headers matter.
     */
    data class Account(
        val env: List<String> = emptyList(),
        val roles: List<String> = emptyList(),
        val headers: Map<String, String> = emptyMap(),
        val parameters: Map<String, ValueSpec> = emptyMap(),
        val auth: Map<String, Map<String, ValueSpec>> = emptyMap(),
        val settings: Map<String, String> = emptyMap(),
        val anonymous: List<String> = emptyList(),
    )

    /**
     * @property requestsPerSecond How many requests a second a run may send to the API.
     * @property maxRequests How many requests a run may send to the API in all; later cases are
     *   skipped.
     * @property casesPerOperation How many parameter combinations an operation is called with at
     *   most.
     * @property negative Whether to generate the cases expecting errors: unknown ids and invalid
     *   credentials.
     */
    data class Limits(
        val requestsPerSecond: Double = 2.0,
        val maxRequests: Int = 1000,
        val casesPerOperation: Int = 6,
        val negative: Boolean = true,
    )

    /**
     * How to test one operation, keyed in [operations] by `Module.operation`.
     *
     * @property skip Why the operation isn't tested, if it isn't.
     * @property inputs Values of its parameters (by name, `body` for the body), replacing those
     *   the generator would pick.
     * @property expect What every successful result must satisfy.
     * @property errors Statuses and error codes the operation may answer with in a passing case,
     *   such as the 404 of a service the account doesn't use.
     * @property volatile Whether results change from minute to minute, so that platforms aren't
     *   compared on them.
     * @property negative Whether to generate the cases expecting errors for this operation.
     * @property verify A read showing the change a reversible operation makes.
     * @property undo How to undo a reversible operation.
     */
    data class OperationTests(
        val skip: String? = null,
        val inputs: Map<String, ValueSpec> = emptyMap(),
        val expect: List<ExpectationSpec> = emptyList(),
        val errors: List<String> = emptyList(),
        val volatile: Boolean = false,
        val negative: Boolean = true,
        val verify: VerifySpec? = null,
        val undo: UndoSpec? = null,
    )

    companion object {
        const val FILE_NAME = "tests.yaml"

        /** The config in [specDir], or null when it has none. */
        fun load(specDir: Path): LiveTestsConfig? {
            val file = specDir.resolve(FILE_NAME)
            if (!file.exists()) return null
            return try {
                parse(file.readText())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("$file: ${e.message}", e)
            }
        }

        fun parse(text: String): LiveTestsConfig {
            val root = toJson(Yaml(SafeConstructor(LoaderOptions())).load<Any?>(text)) as? JsonObject ?: JsonObject(emptyMap())
            return root.read("tests.yaml", setOf("account", "limits", "defaults", "operations")) {
                LiveTestsConfig(
                    account = obj("account")?.let(::account) ?: Account(),
                    limits = obj("limits")?.let(::limits) ?: Limits(),
                    defaults = obj("defaults")?.mapValues { ValueSpec.parse(it.value) }.orEmpty(),
                    operations = obj("operations")?.mapValues { (key, value) -> operation(key, value as? JsonObject ?: fail("$key must be an object")) }.orEmpty(),
                )
            }
        }

        private fun account(json: JsonObject): Account = json.read("account", setOf("env", "roles", "headers", "parameters", "auth", "settings", "anonymous")) {
            Account(
                env = strings("env"),
                roles = strings("roles"),
                headers = stringMap("headers"),
                parameters = obj("parameters")?.mapValues { ValueSpec.parse(it.value) }.orEmpty(),
                auth = obj("auth")?.mapValues { (scheme, credentials) ->
                    (credentials as? JsonObject ?: fail("auth.$scheme must be an object")).mapValues { ValueSpec.parse(it.value) }
                }.orEmpty(),
                settings = stringMap("settings"),
                anonymous = strings("anonymous"),
            )
        }

        private fun limits(json: JsonObject): Limits = json.read("limits", setOf("requestsPerSecond", "maxRequests", "casesPerOperation", "negative")) {
            val defaults = Limits()
            Limits(
                requestsPerSecond = primitive("requestsPerSecond")?.doubleOrNull ?: defaults.requestsPerSecond,
                maxRequests = primitive("maxRequests")?.intOrNull ?: defaults.maxRequests,
                casesPerOperation = primitive("casesPerOperation")?.intOrNull ?: defaults.casesPerOperation,
                negative = primitive("negative")?.booleanOrNull ?: defaults.negative,
            )
        }

        private fun operation(key: String, json: JsonObject): OperationTests =
            json.read(key, setOf("skip", "inputs", "expect", "errors", "volatile", "negative", "verify", "undo")) {
                OperationTests(
                    skip = primitive("skip")?.content,
                    inputs = obj("inputs")?.mapValues { ValueSpec.parse(it.value) }.orEmpty(),
                    expect = (get("expect") as? JsonArray).orEmpty().map { ExpectationSpec.parse(it, "$key.expect") },
                    errors = (get("errors") as? JsonArray).orEmpty().map { it.jsonPrimitive.content },
                    volatile = primitive("volatile")?.booleanOrNull ?: false,
                    negative = primitive("negative")?.booleanOrNull ?: true,
                    verify = obj("verify")?.let { VerifySpec.parse(it, "$key.verify") },
                    undo = obj("undo")?.let { UndoSpec.parse(it, "$key.undo") },
                )
            }

        /** A YAML document, as loaded by SnakeYAML, as JSON. */
        private fun toJson(value: Any?): JsonElement = when (value) {
            null -> JsonNull
            is Map<*, *> -> JsonObject(value.entries.associate { (key, item) -> key.toString() to toJson(item) })
            is List<*> -> JsonArray(value.map(::toJson))
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            else -> JsonPrimitive(value.toString())
        }
    }
}

/**
 * A value in `tests.yaml`: of a parameter, credential, or expectation.
 *
 * - `null` leaves an optional parameter out;
 * - a number or boolean is that value;
 * - a string is text, where `{env:NAME}`, `{today}`, `{today+7}` and `{base64:…}` are replaced
 *   when the tests run; dates are written as ISO 8601 text;
 * - a string starting with `$` refers to the call being undone or verified: `$args.<param>`,
 *   `$result.<path>` or `$before` (the value a restore saved);
 * - `{from: Module.operation, with: {…}, pick: "<path>", where: {field: value}}` is a value
 *   read from another operation's result: every value at `pick`, of the elements `where` keeps;
 * - `{oneOf: [...]}` tries each value in turn, as the generator does with an enum;
 * - any other object or list is JSON, decoded into the parameter's type.
 *
 * Paths walk fields by name: `[*]` is every element of a list (or value of a map), `[2]` one.
 */
sealed interface ValueSpec {
    data object Omit : ValueSpec

    data class Scalar(val value: JsonPrimitive) : ValueSpec

    data class Text(val template: String) : ValueSpec

    data class Json(val value: JsonElement) : ValueSpec

    data class Pick(val from: String, val with: Map<String, ValueSpec>, val pick: String, val where: Map<String, JsonPrimitive>) : ValueSpec

    data class OneOf(val values: List<ValueSpec>) : ValueSpec

    /** @property root `args`, `result` or `before`. */
    data class Ref(val root: String, val path: String) : ValueSpec

    companion object {
        private val REF = Regex("""^\$(args|result|before)(?:\.(.*))?$""")

        fun parse(json: JsonElement): ValueSpec = when (json) {
            JsonNull -> Omit
            is JsonPrimitive -> if (json.isString) text(json.content) else Scalar(json)
            is JsonObject -> when {
                "from" in json -> json.read("from", setOf("from", "with", "pick", "where")) {
                    Pick(
                        from = primitive("from")!!.content,
                        with = obj("with")?.mapValues { parse(it.value) }.orEmpty(),
                        pick = primitive("pick")?.content ?: "",
                        where = obj("where")?.mapValues { it.value as? JsonPrimitive ?: fail("where values must be scalars") }.orEmpty(),
                    )
                }
                "oneOf" in json -> OneOf((json["oneOf"] as? JsonArray ?: fail("oneOf must be a list")).map(::parse))
                else -> Json(json)
            }
            is JsonArray -> Json(json)
        }

        private fun text(text: String): ValueSpec {
            val ref = REF.matchEntire(text) ?: return Text(text).also { Templates.check(text) }
            return Ref(ref.groupValues[1], ref.groupValues[2])
        }
    }
}

/**
 * Something every successful result of an operation satisfies, about the values at a path of
 * it (see [ValueSpec] for paths):
 *
 * - `nonEmpty: <path>`: there is at least one;
 * - `contains: {path, value}`: one of them is `value`;
 * - `equals: {path, value}`: there is at least one, and all are `value`;
 * - `matches: {path, regex}`: there is at least one, and all are text matching `regex`;
 * - `count: {path, min, max}`: there are between `min` and `max`.
 */
sealed interface ExpectationSpec {
    val path: String

    data class NonEmpty(override val path: String) : ExpectationSpec

    data class Contains(override val path: String, val value: ValueSpec) : ExpectationSpec

    data class Equals(override val path: String, val value: ValueSpec) : ExpectationSpec

    data class Matches(override val path: String, val regex: String) : ExpectationSpec

    data class Count(override val path: String, val min: Int?, val max: Int?) : ExpectationSpec

    companion object {
        fun parse(json: JsonElement, where: String): ExpectationSpec {
            val entry = (json as? JsonObject)?.entries?.singleOrNull() ?: fail("Each of $where must be an object with one key")
            val (kind, value) = entry
            return when (kind) {
                "nonEmpty" -> NonEmpty((value as? JsonPrimitive)?.content ?: "")
                "contains", "equals", "matches", "count" -> (value as? JsonObject ?: fail("$where.$kind must be an object")).read(kind, setOf("path", "value", "regex", "min", "max")) {
                    val path = primitive("path")?.content ?: ""
                    when (kind) {
                        "contains" -> Contains(path, ValueSpec.parse(get("value") ?: fail("$where.contains needs a value")))
                        "equals" -> Equals(path, ValueSpec.parse(get("value") ?: fail("$where.equals needs a value")))
                        "matches" -> Matches(path, primitive("regex")?.content?.also { Regex(it) } ?: fail("$where.matches needs a regex"))
                        else -> Count(path, primitive("min")?.intOrNull, primitive("max")?.intOrNull)
                    }
                }
                else -> fail("$where: unknown expectation '$kind'")
            }
        }
    }
}

/**
 * A read showing the change a reversible operation makes: after the call, `contains` holds of
 * its result; after the undo it no longer does.
 */
data class VerifySpec(val call: String, val with: Map<String, ValueSpec>, val path: String, val value: ValueSpec) {
    companion object {
        fun parse(json: JsonObject, where: String): VerifySpec = json.read(where, setOf("call", "with", "contains")) {
            val contains = obj("contains") ?: fail("$where needs contains: {path, value}")
            VerifySpec(
                call = primitive("call")?.content ?: fail("$where needs a call"),
                with = obj("with")?.mapValues { ValueSpec.parse(it.value) }.orEmpty(),
                path = contains["path"]?.jsonPrimitive?.content ?: "",
                value = ValueSpec.parse(contains["value"] ?: fail("$where.contains needs a value")),
            )
        }
    }
}

/**
 * How to undo a reversible operation: by [Call]ing another operation, or by [Restore]ing the
 * value the operation sets, saved beforehand.
 */
sealed interface UndoSpec {
    data class Call(val call: String, val with: Map<String, ValueSpec>) : UndoSpec

    /**
     * Before the call, saves the value at [pick] of [read]'s result; afterwards calls the
     * operation again with the same arguments but [into] (a parameter, or `parameter.field`)
     * set to it.
     */
    data class Restore(val read: String, val with: Map<String, ValueSpec>, val pick: String, val into: String) : UndoSpec

    companion object {
        fun parse(json: JsonObject, where: String): UndoSpec = when {
            "call" in json -> json.read(where, setOf("call", "with")) {
                Call(primitive("call")!!.content, obj("with")?.mapValues { ValueSpec.parse(it.value) }.orEmpty())
            }
            "restore" in json -> (json["restore"] as? JsonObject ?: fail("$where.restore must be an object")).read("$where.restore", setOf("read", "with", "pick", "into")) {
                Restore(
                    read = primitive("read")?.content ?: fail("$where.restore needs read"),
                    with = obj("with")?.mapValues { ValueSpec.parse(it.value) }.orEmpty(),
                    pick = primitive("pick")?.content ?: fail("$where.restore needs pick"),
                    into = primitive("into")?.content ?: fail("$where.restore needs into"),
                )
            }
            else -> fail("$where needs call or restore")
        }
    }
}

/**
 * The placeholders of text values, which the live test runtimes replace when the tests run:
 * `{env:NAME}`, `{today}`, `{today+N}`, `{today-N}` (ISO dates, or in a pattern of `yyyy`, `MM`
 * and `dd` after a colon: `{today:yyyy}`) and `{base64:…}`, whose content may hold placeholders
 * too.
 */
object Templates {
    sealed interface Part {
        data class Literal(val text: String) : Part

        data class Env(val name: String) : Part

        /** Today's date, [offsetDays] later, in [pattern] (`yyyy`, `MM` and `dd`), ISO 8601 when null. */
        data class Today(val offsetDays: Int, val pattern: String? = null) : Part

        data class Base64(val parts: List<Part>) : Part
    }

    private val ENV_NAME = Regex("""[A-Za-z_][A-Za-z0-9_]*""")
    private val TODAY = Regex("""today(?:([+-])(\d+))?(?::([A-Za-z\-/.]+))?""")

    /** The parts of [text]; fails when a placeholder is malformed or unknown. */
    fun parse(text: String): List<Part> = parts(text, 0, nested = false).first

    /** Fails when [text] has a malformed or unknown placeholder. */
    fun check(text: String) {
        parse(text)
    }

    /** The environment variables [text] reads. */
    fun envVariables(text: String): List<String> = envVariables(parse(text))

    private fun envVariables(parts: List<Part>): List<String> = parts.flatMap { part ->
        when (part) {
            is Part.Env -> listOf(part.name)
            is Part.Base64 -> envVariables(part.parts)
            is Part.Literal, is Part.Today -> emptyList()
        }
    }

    private fun parts(text: String, start: Int, nested: Boolean): Pair<List<Part>, Int> {
        val parts = mutableListOf<Part>()
        val literal = StringBuilder()
        fun flush() {
            if (literal.isNotEmpty()) parts += Part.Literal(literal.toString())
            literal.clear()
        }
        var index = start
        while (index < text.length) {
            when (val char = text[index]) {
                '{' -> {
                    flush()
                    val (part, next) = placeholder(text, index + 1)
                    parts += part
                    index = next
                }
                '}' -> {
                    require(nested) { "Unbalanced '}' in '$text'" }
                    flush()
                    return parts to index + 1
                }
                else -> {
                    literal.append(char)
                    index++
                }
            }
        }
        require(!nested) { "Unclosed '{' in '$text'" }
        flush()
        return parts to index
    }

    private fun placeholder(text: String, start: Int): Pair<Part, Int> {
        if (text.startsWith("base64:", start)) {
            val (parts, next) = parts(text, start + "base64:".length, nested = true)
            return Part.Base64(parts) to next
        }
        val end = text.indexOf('}', start)
        require(end >= 0) { "Unclosed '{' in '$text'" }
        val content = text.substring(start, end)
        val part = when {
            content.startsWith("env:") -> Part.Env(content.removePrefix("env:").also {
                require(ENV_NAME.matches(it)) { "Invalid variable name in '{$content}' of '$text'" }
            })
            TODAY.matches(content) -> {
                val match = TODAY.matchEntire(content)!!
                val days = match.groupValues[2].toIntOrNull() ?: 0
                Part.Today(if (match.groupValues[1] == "-") -days else days, match.groupValues[3].ifEmpty { null })
            }
            else -> throw IllegalArgumentException("Unknown placeholder '{$content}' in '$text'")
        }
        return part to end + 1
    }
}

private class JsonReader(val json: JsonObject) {
    fun get(key: String): JsonElement? = json[key]?.takeUnless { it == JsonNull }

    fun obj(key: String): JsonObject? = get(key)?.let { it as? JsonObject ?: fail("$key must be an object") }

    fun primitive(key: String): JsonPrimitive? = get(key)?.let { it as? JsonPrimitive ?: fail("$key must be a scalar") }

    fun strings(key: String): List<String> = get(key)?.let { value ->
        (value as? JsonArray ?: fail("$key must be a list")).map { (it as? JsonPrimitive)?.content ?: fail("$key must list scalars") }
    }.orEmpty()

    fun stringMap(key: String): Map<String, String> =
        obj(key)?.mapValues { (name, value) -> (value as? JsonPrimitive)?.content ?: fail("$key.$name must be a scalar") }.orEmpty()
}

private fun <T> JsonObject.read(where: String, keys: Set<String>, body: JsonReader.() -> T): T {
    val unknown = this.keys - keys
    if (unknown.isNotEmpty()) fail("$where has unknown keys $unknown; expected some of $keys")
    return JsonReader(this).body()
}

private fun fail(message: String): Nothing = throw IllegalArgumentException(message)

/** Whether a scalar is a whole number. */
internal val JsonPrimitive.isWholeNumber: Boolean get() = !isString && longOrNull != null
