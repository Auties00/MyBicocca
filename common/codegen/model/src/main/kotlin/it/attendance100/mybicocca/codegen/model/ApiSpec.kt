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
 * @property servers Base URLs operations can target (see [Operation.server]). Empty when the
 *   API has a single base URL, which its runtime then provides.
 * @property authSchemes Ways a caller can authenticate, used by [Operation.auth].
 */
data class ApiSpec(
    val id: String,
    val prefix: String,
    val modules: List<ApiModule>,
    val enums: List<EnumType>,
    val servers: List<Server> = emptyList(),
    val authSchemes: List<AuthScheme> = emptyList(),
) {
    /** Whether any operation declares roles, in which case targets pass them to the runtime. */
    val usesRoles: Boolean by lazy { modules.any { module -> module.operations.any { it.roles.isNotEmpty() } } }

    /** Every declared record and enum, by name. */
    val declarations: Map<String, TypeDeclaration> by lazy {
        (modules.flatMap { it.records } + enums).associateBy { it.name }
    }

    /** Resolves a [TypeRef.Named] to its declaration. */
    fun resolve(ref: TypeRef.Named): TypeDeclaration =
        declarations[ref.name] ?: error("Unknown type '${ref.name}' in API '$id'")
}

/**
 * A base URL of an API.
 *
 * @property name PascalCase name of the server (e.g. `AppV4`).
 */
data class Server(val name: String, val url: String)

/**
 * A way to authenticate: credentials the caller supplies, sent as headers.
 *
 * @property name PascalCase name of the scheme (e.g. `Account`).
 * @property parameters The credentials, in order.
 * @property headers Header values by header name. `{parameter}` placeholders are replaced by
 *   the credential of that name.
 */
data class AuthScheme(
    val name: String,
    val parameters: List<AuthParameter>,
    val headers: Map<String, String>,
    val description: String? = null,
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
