package it.attendance100.mybicocca.codegen.model

/**
 * Language-neutral description of a data API: what readers produce from an API's own spec
 * format, and what targets turn into an SDK.
 *
 * Every name in the model is a final logical identifier, already translated and made
 * unique by the reader. Declaration names are PascalCase and member names are camelCase;
 * targets only adapt them to their language's conventions and escape reserved words.
 *
 * @property id Stable lowercase identifier of the API (e.g. `esse3`), used for configuration
 *   and output paths.
 * @property prefix Prefix of the API's top-level names (e.g. `Esse3`). Declarations already
 *   carry it; targets also use it to name the API's hand-written runtime support types.
 * @property modules The API's sub-APIs, each grouping operations and the records they use.
 * @property enums Enumerations shared by every module.
 * @property servers Base URLs operations can target (see [Operation.server]). Operations naming
 *   none use the base URL the API's runtime provides, so this is empty when the API has a single
 *   one.
 * @property authSchemes Ways a caller can authenticate, used by [Operation.auth].
 * @property parameters Values the caller gives once, when creating the API, rather than to
 *   each operation: path parameters fill the placeholders of every operation whose path names
 *   them, and query, header and cookie parameters are sent by every operation. They are
 *   required and plainly encoded.
 */
data class ApiSpec(
    val id: String,
    val prefix: String,
    val modules: List<ApiModule>,
    val enums: List<EnumType>,
    val servers: List<Server> = emptyList(),
    val authSchemes: List<AuthScheme> = emptyList(),
    val parameters: List<Parameter> = emptyList(),
) {
    /** Whether any operation declares roles, in which case targets pass them to the runtime. */
    val usesRoles: Boolean by lazy { modules.any { module -> module.operations.any { it.roles.isNotEmpty() } } }

    /** Every declared record and enum, by name. */
    val declarations: Map<String, TypeDeclaration> by lazy {
        (modules.flatMap { it.records } + enums).associateBy { it.name }
    }

    /**
     * The API [parameters] [module] uses: those every operation sends, the path ones its
     * operations' paths name, and those its operations' headers name.
     */
    fun parametersOf(module: ApiModule): List<Parameter> = parameters.filter { parameter ->
        parameter.location != ParameterLocation.PATH || module.operations.any { operation ->
            "{${parameter.wireName}}" in operation.path || operation.headers.values.any { "{${parameter.name}}" in it }
        }
    }

    /** Resolves a [TypeRef.Named] to its declaration. */
    fun resolve(ref: TypeRef.Named): TypeDeclaration =
        declarations[ref.name] ?: error("Unknown type '${ref.name}' in API '$id'")

    /**
     * Whether [type] can key a [TypeRef.MapOf]: a type whose values have one string form,
     * namely strings, integers, ISO dates, enums and delimited records.
     */
    fun isMapKey(type: TypeRef): Boolean = when (type) {
        is TypeRef.Primitive -> type.kind in MAP_KEY_KINDS
        is TypeRef.Named -> when (val declaration = declarations[type.name]) {
            is EnumType -> true
            is RecordType -> declaration.encoding is RecordEncoding.Delimited
            null -> false
        }
        is TypeRef.ListOf, is TypeRef.MapOf -> false
    }

    companion object {
        private val MAP_KEY_KINDS = setOf(PrimitiveKind.STRING, PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.LOCAL_DATE)

        /**
         * Joins the parts of an API described by several specs, such as a REST API and the web
         * pages of the same site, into one: their modules side by side, and the enums, servers
         * and auth schemes of every part. Parts may repeat a declaration only identically, and
         * must agree on the API's identity and parameters, which every operation uses.
         */
        fun merge(parts: List<ApiSpec>): ApiSpec {
            require(parts.isNotEmpty()) { "An API needs at least one spec" }
            val first = parts.first()
            if (parts.size == 1) return first
            for (part in parts) {
                require(part.id == first.id && part.prefix == first.prefix) {
                    "The specs of one API must share its id and prefix: '${first.id}'/${first.prefix} and '${part.id}'/${part.prefix}"
                }
                require(part.parameters == first.parameters) { "The specs of API '${first.id}' must declare the same API parameters" }
            }
            return ApiSpec(
                id = first.id,
                prefix = first.prefix,
                modules = parts.flatMap { it.modules },
                enums = union(parts.flatMap { it.enums }, "Enum") { it.name },
                servers = union(parts.flatMap { it.servers }, "Server") { it.name },
                authSchemes = union(parts.flatMap { it.authSchemes }, "Auth scheme") { it.name },
                parameters = first.parameters,
            )
        }

        /** The distinct [items], failing when two of them share a name but differ. */
        private fun <T> union(items: List<T>, kind: String, name: (T) -> String): List<T> =
            items.groupBy(name).map { (key, declared) ->
                require(declared.distinct().size == 1) { "$kind '$key' is declared differently by the specs of one API" }
                declared.first()
            }
    }
}

/**
 * A base URL of an API.
 *
 * @property name PascalCase name of the server (e.g. `AppV4`).
 */
data class Server(val name: String, val url: String)

/**
 * A way to authenticate: credentials the caller supplies, sent as headers or form fields.
 *
 * @property name PascalCase name of the scheme (e.g. `Account`).
 * @property parameters The credentials, in order.
 * @property headers Header values by header name. `{parameter}` placeholders are replaced by
 *   the credential of that name.
 * @property form Form field values by field name, with the same placeholders. Operations
 *   accepting the scheme send a form body, so they can't send a JSON one.
 */
data class AuthScheme(
    val name: String,
    val parameters: List<AuthParameter>,
    val headers: Map<String, String>,
    val description: String? = null,
    val form: Map<String, String> = emptyMap(),
)

/** @property name camelCase name of a credential of an [AuthScheme]. */
data class AuthParameter(val name: String, val description: String? = null)

/**
 * A sub-API: a group of operations served under a common [basePath], with the records it
 * declares.
 *
 * @property name PascalCase name without the API prefix (e.g. `Careers`).
 * @property basePath Path prefix of every operation, relative to the API's base URL.
 * @property records Records declared by this module, in declaration order.
 * @property operations Operations of this module, in declaration order.
 */
data class ApiModule(
    val name: String,
    val basePath: String,
    val records: List<RecordType>,
    val operations: List<Operation>,
)
