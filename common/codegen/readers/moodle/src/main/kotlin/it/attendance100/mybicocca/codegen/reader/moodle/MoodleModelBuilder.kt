package it.attendance100.mybicocca.codegen.reader.moodle

import it.attendance100.mybicocca.codegen.core.Text
import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthParameter
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Converts dumped Moodle web service functions into the model:
 *
 * - Each component is a module (named by `modules.json`), and each function an operation of
 *   it, named after the function without its component (`core_course_get_contents` is
 *   `getContents` of module `Course`). Every call is a form POST to the REST server, naming
 *   the function in the query and carrying the user's token as the `wstoken` field.
 * - Parameters are form fields; lists and objects are bracket-encoded, as Moodle reads them,
 *   and so are booleans, which Moodle only accepts as `1` and `0`.
 * - Every object structure is a record. Structures with the same keys and types, which
 *   Moodle repeats across functions, are one record, named by `types.json` or after the key
 *   holding it (`discussions` holds `Discussion`s).
 * - Moodle cleans responses against these descriptions, so required values are present and
 *   optional ones may be missing, and optional values are nullable. A required value isn't
 *   nullable but defaults to its zero (`0`, `""`, `false`), which Moodle means by "not set":
 *   values allow null unless they say otherwise, and nearly none do.
 * - PHP writes an empty object as `[]`, so object fields accept arrays of objects too
 *   ([Codec.FoldedObject]).
 */
internal class MoodleModelBuilder(
    private val dump: MoodleDump,
    private val id: String,
    private val prefix: String,
    private val moduleNames: Overlay,
    private val typeNames: Overlay,
    private val memberNames: Overlay,
) {
    /** An object structure of the dump, at [path], held by [key] of the structure at [parent]. */
    private data class Use(val path: String, val parent: String?, val key: String?, val structure: Description.Object, val function: DumpFunction)

    private val uses: List<Use> = dump.functions.flatMap(::uses)
    private val signatureByPath: Map<String, String> = uses.associate { it.path to signature(it.structure) }
    private val usesBySignature: Map<String, List<Use>> = uses.groupBy { signature(it.structure) }
    private val typeNameBySignature: Map<String, String> = typeNames()

    /** Every object path of the dump, in order, as `types.json` keys them. */
    val structurePaths: List<String> get() = uses.map { it.path }

    /** Every component of the dump, in order, as `modules.json` keys them. */
    val components: List<String> get() = dump.functions.map { it.component }.distinct()

    /** Every member wire name of the dump, sorted, as `names.json` keys them. */
    val wireNames: List<String>
        get() = (uses.flatMap { it.structure.keys.keys } + dump.functions.flatMap { it.parameters.keys.keys }).distinct().sorted()

    fun build(): ApiSpec {
        val operationsByModule = linkedMapOf<String, MutableList<Operation>>()
        val recordOwners = mutableMapOf<String, String>()
        for (function in dump.functions) {
            val module = moduleName(function.component)
            val operation = operation(function)
            operationsByModule.getOrPut(module) { mutableListOf() } += operation
            operation.parameters.forEach { claim(it.type, module, recordOwners) }
            claim((operation.response as? ResponseBody.Json)?.type, module, recordOwners)
        }
        val records = usesBySignature.values.map { record(it.first()) }
        return ApiSpec(
            id = id,
            prefix = prefix,
            modules = operationsByModule.map { (module, operations) ->
                ApiModule(module, basePath = "", records = records.filter { recordOwners[it.name] == module }, operations = operations)
            },
            enums = emptyList(),
            authSchemes = listOf(
                AuthScheme(
                    name = TOKEN_SCHEME,
                    parameters = listOf(AuthParameter("token", "The user's web service token.")),
                    headers = emptyMap(),
                    description = "A user's web service token, from the mobile app login.",
                    form = mapOf("wstoken" to "{token}"),
                ),
            ),
        )
    }

    private fun uses(function: DumpFunction): List<Use> = buildList {
        fun walk(description: Description, path: String, parent: String?, key: String?) {
            when (description) {
                is Description.Object -> {
                    add(Use(path, parent, key, description, function))
                    description.keys.forEach { (child, value) -> walk(value, "$path/$child", path, child) }
                }
                is Description.ListOf -> walk(description.content, "$path[]", parent, key)
                is Description.Value -> Unit
            }
        }
        function.parameters.keys.forEach { (key, value) -> walk(value, "${function.name}/parameters/$key", null, key) }
        function.returns?.let { walk(it, "${function.name}/returns", null, null) }
    }

    /** What makes two structures the same record: their keys, in order, and how each is described. */
    private fun signature(structure: Description.Object): String =
        structure.keys.entries.joinToString(",", "{", "}") { (key, value) -> "$key=${memberSignature(value)}" }

    private fun memberSignature(description: Description): String = when (description) {
        is Description.Value -> "${description.type}:${description.presence}:${description.nullable}"
        is Description.Object -> "${description.presence}:${description.nullable}${signature(description)}"
        is Description.ListOf -> "${description.presence}:${description.nullable}[${memberSignature(description.content)}]"
    }

    /**
     * Names each record: the first name `types.json` gives one of its paths, else one derived
     * from its first path. Derived names that clash are qualified by their parent's name.
     */
    private fun typeNames(): Map<String, String> {
        val names = linkedMapOf<String, String>()
        for ((signature, group) in usesBySignature) {
            group.firstNotNullOfOrNull { typeNames[it.path] }?.let { names[signature] = it }
        }
        names.values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.firstOrNull()?.let { name ->
            error("types.json names different structures '$name'")
        }

        val derived = usesBySignature.filterKeys { it !in names }.mapValues { (_, group) -> derivedName(group.first()) }
        val clashes = derived.values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys + names.values
        // Parents first, so a qualified name can use its parent's final name.
        for ((signature, name) in derived.entries.sortedBy { (signature) -> usesBySignature.getValue(signature).first().path.count { it == '/' } }) {
            val use = usesBySignature.getValue(signature).first()
            val qualified = if (name in clashes) (use.parent?.let { names[signatureByPath.getValue(it)] } ?: functionBase(use.function)) + name else name
            var unique = qualified
            var suffix = 2
            while (unique in names.values) unique = "$qualified${suffix++}"
            names[signature] = unique
        }
        return names
    }

    private fun derivedName(use: Use): String = when (use.key) {
        null -> functionBase(use.function) + if (use.path.endsWith("[]")) "Item" else "Response"
        else -> singular(memberName(use.key).capitalized())
    }

    /** `core_course_get_contents` → `CourseGetContents`. */
    private fun functionBase(function: DumpFunction): String =
        function.name.split('_').let { words -> if (words.first() in GENERIC_COMPONENT_WORDS) words.drop(1) else words }
            .joinToString("") { it.capitalized() }

    private fun moduleName(component: String): String = moduleNames[component]
        ?: component.split('_').let { words -> if (words.size > 1 && words.first() in GENERIC_COMPONENT_WORDS) words.drop(1) else words }
            .joinToString("") { it.capitalized() }

    private fun memberName(wireName: String): String = memberNames[wireName] ?: Text.lowerCamel(wireName)

    private fun record(use: Use): RecordType = RecordType(
        name = prefix + typeNameBySignature.getValue(signature(use.structure)),
        fields = use.structure.keys.map { (key, value) -> field(key, value, "${use.path}/$key") },
        description = use.structure.description.ifEmpty { null },
    )

    private fun field(key: String, description: Description, path: String): Field {
        val type = type(description, path, key)
        val (nullable, default) = when (description) {
            is Description.ListOf -> false to DefaultValue.EmptyList
            is Description.Object ->
                if (description.presence == Presence.REQUIRED && !description.nullable) false to null else true to DefaultValue.Null
            is Description.Value -> when (description.presence) {
                Presence.REQUIRED -> false to DefaultValue.Zero
                Presence.OPTIONAL -> true to DefaultValue.Null
                Presence.DEFAULT -> literal((type as TypeRef.Primitive).kind, description)?.let { false to it } ?: (true to DefaultValue.Null)
            }
        }
        val codec = if (description is Description.Object) Codec.FoldedObject else null
        return Field(memberName(key), key, type, nullable, default, codec, description.description.ifEmpty { null })
    }

    /** A value's default as a model literal, or null when it has none or it doesn't fit its type. */
    private fun literal(kind: PrimitiveKind, description: Description.Value): DefaultValue.Literal? {
        val default = description.default as? JsonPrimitive ?: return null
        if (default is JsonNull) return null
        val text = default.content
        val value = when (kind) {
            PrimitiveKind.BOOLEAN -> when (text) {
                "1", "true" -> "true"
                "0", "false", "" -> "false"
                else -> null
            }
            PrimitiveKind.INT32, PrimitiveKind.INT64 -> text.toLongOrNull()?.toString()
            PrimitiveKind.FLOAT64 -> text.toDoubleOrNull()?.toString()
            else -> text
        }
        return value?.let(DefaultValue::Literal)
    }

    private fun type(description: Description, path: String, key: String?): TypeRef = when (description) {
        is Description.Value -> TypeRef.Primitive(primitive(description.type, key))
        is Description.Object -> TypeRef.Named(prefix + typeNameBySignature.getValue(signatureByPath.getValue(path)))
        is Description.ListOf -> TypeRef.ListOf(type(description.content, "$path[]", key))
    }

    /**
     * The primitive of a `PARAM_*` type. Integers are 32-bit, except those named like times,
     * dates and sizes, which outgrow them.
     */
    private fun primitive(type: String, key: String?): PrimitiveKind = when (type) {
        "int" -> if (key != null && LONG_INTEGER.containsMatchIn(key)) PrimitiveKind.INT64 else PrimitiveKind.INT32
        "float" -> PrimitiveKind.FLOAT64
        "bool" -> PrimitiveKind.BOOLEAN
        else -> PrimitiveKind.STRING
    }

    private fun operation(function: DumpFunction): Operation = Operation(
        name = Text.lowerCamel(function.name.removePrefix(function.component + "_")),
        method = HttpMethod.POST,
        path = REST_SERVER,
        parameters = function.parameters.keys.map { (key, value) ->
            Parameter(
                name = memberName(key),
                wireName = key,
                location = ParameterLocation.FORM,
                type = type(value, "${function.name}/parameters/$key", key),
                required = value.presence == Presence.REQUIRED,
                description = value.description.ifEmpty { null },
                encoding = if (value is Description.Value && value.type != "bool") ParameterEncoding.PLAIN else ParameterEncoding.BRACKETS,
            )
        },
        body = null,
        response = function.returns?.let { ResponseBody.Json(type(it, "${function.name}/returns", null)) } ?: ResponseBody.None,
        summary = function.description.ifEmpty { null },
        query = mapOf("wsfunction" to function.name, "moodlewsrestformat" to "json"),
        auth = AuthRequirement(listOf(TOKEN_SCHEME), required = function.loginrequired),
    )

    /** Assigns the records [type] reaches to [module], unless an earlier module has them. */
    private fun claim(type: TypeRef?, module: String, owners: MutableMap<String, String>) {
        when (type) {
            is TypeRef.Named -> if (owners.putIfAbsent(type.name, module) == null) {
                val use = usesBySignature.values.first { prefix + typeNameBySignature.getValue(signature(it.first().structure)) == type.name }.first()
                use.structure.keys.forEach { (key, value) -> claim(type(value, "${use.path}/$key", key), module, owners) }
            }
            is TypeRef.ListOf -> claim(type.element, module, owners)
            is TypeRef.MapOf -> claim(type.value, module, owners)
            is TypeRef.Primitive, null -> Unit
        }
    }

    private companion object {
        const val TOKEN_SCHEME = "Token"
        const val REST_SERVER = "/webservice/rest/server.php"

        /** Component words that say nothing about the component (`core_course`, `mod_forum`). */
        val GENERIC_COMPONENT_WORDS = setOf("core", "mod", "tool")

        /** Integers named like times, dates and sizes, or one of the bare timestamp names. */
        val LONG_INTEGER = Regex("time|date|size|bytes|access|login|^(created|modified|start|end|locked)$")

        /** `Discussions` → `Discussion`, `Replies` → `Reply`, `Statuses` → `Status`. */
        fun singular(name: String): String = when {
            name.endsWith("ies") -> name.dropLast(3) + "y"
            listOf("sses", "xes", "ches", "shes", "uses").any(name::endsWith) -> name.dropLast(2)
            name.endsWith("s") && !name.endsWith("ss") -> name.dropLast(1)
            else -> name
        }
    }
}
