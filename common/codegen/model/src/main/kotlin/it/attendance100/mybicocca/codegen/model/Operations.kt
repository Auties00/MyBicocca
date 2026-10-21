package it.attendance100.mybicocca.codegen.model

/**
 * An HTTP operation of an [ApiModule].
 *
 * Targets list the inputs of an operation as: path parameters, the body, then the remaining
 * [parameters] in their declared order.
 *
 * @property name camelCase name of the operation.
 * @property path Path relative to the module's base path, with `{wireName}` placeholders for
 *   path parameters.
 * @property parameters Path, query and form parameters, in declaration order.
 * @property body The request body, if the operation sends one.
 * @property roles Names of the access roles allowed to call the operation, as defined by the
 *   API's runtime. Empty when the API doesn't say.
 * @property server Name of the [Server] the operation targets; null for the API's only one.
 * @property headers Headers sent with every call, by name. A value's `{name}` placeholders are
 *   replaced by the API parameter of that name (see [ApiSpec.parameters]).
 * @property query Query parameters sent with every call, by name, such as the function an
 *   RPC-style endpoint runs.
 * @property form Form fields sent with every call, by name, such as the view a PHP endpoint
 *   renders. An empty value sends the field with no value.
 * @property auth The credentials the operation accepts, if any.
 */
data class Operation(
    val name: String,
    val method: HttpMethod,
    val path: String,
    val parameters: List<Parameter>,
    val body: RequestBody?,
    val response: ResponseBody,
    val roles: Set<String> = emptySet(),
    val summary: String? = null,
    val server: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val auth: AuthRequirement? = null,
    val query: Map<String, String> = emptyMap(),
    val form: Map<String, String> = emptyMap(),
) {
    val pathParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.PATH }
    val queryParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.QUERY }
    val formParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.FORM }
    val headerParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.HEADER }
    val cookieParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.COOKIE }
}

/**
 * The credentials an operation accepts.
 *
 * @property schemes Names of the accepted [AuthScheme]s.
 * @property required Whether a call must be authenticated.
 */
data class AuthRequirement(val schemes: List<String>, val required: Boolean)

enum class HttpMethod { GET, POST, PUT, PATCH, DELETE }

/**
 * A path, query, form, header or cookie parameter. A list value is sent as one
 * comma-separated value, the Swagger/OpenAPI default for query parameters.
 *
 * @property name camelCase name of the parameter.
 * @property wireName Name of the parameter on the wire.
 * @property valueDescriptions Descriptions of the accepted values, by wire value, when
 *   [type] is an enum.
 * @property default The value used when the caller gives none; such a parameter is always
 *   sent. For a [ParameterEncoding.JSON] parameter it is the JSON text to send.
 * @property encoding How the value is written.
 */
data class Parameter(
    val name: String,
    val wireName: String,
    val location: ParameterLocation,
    val type: TypeRef,
    val required: Boolean,
    val description: String? = null,
    val valueDescriptions: Map<String, String> = emptyMap(),
    val default: DefaultValue.Literal? = null,
    val encoding: ParameterEncoding = ParameterEncoding.PLAIN,
) {
    /** Whether the caller may leave the parameter out, in which case it isn't sent. */
    val isOmittable: Boolean get() = !required && default == null
}

/** Where a parameter is sent. Cookie values are percent-encoded. */
enum class ParameterLocation { PATH, QUERY, FORM, HEADER, COOKIE }

enum class ParameterEncoding {
    /** Scalars in their text form; enums as their wire value; lists comma-separated. */
    PLAIN,

    /** The value encoded as JSON text, as a record or map parameter needs. */
    JSON,

    /**
     * The value as PHP-style bracketed fields, as PHP servers read lists and records: a list
     * `ids` as `ids[0]=1&ids[1]=2`, a record `post` as `post[subject]=…`, nesting as deep as
     * the value. Booleans are written `1` and `0`; nulls are left out. Only for query and form
     * parameters.
     */
    BRACKETS,

    /**
     * A list as one field per element, each named after the parameter, as PHP reads a wire name
     * ending in `[]` (`days[]=1&days[]=2`). Only for query and form parameters.
     */
    REPEATED,
}

/** The body an operation sends. */
sealed interface RequestBody {
    val description: String?

    /** A value encoded as JSON. */
    data class Json(val type: TypeRef, override val description: String? = null) : RequestBody

    /** An empty JSON object, `{}`, which some servers require instead of no body. */
    data object EmptyObject : RequestBody {
        override val description: String? get() = null
    }
}

/** What a successful response carries. */
sealed interface ResponseBody {
    /** Nothing the caller needs. */
    data object None : ResponseBody

    /**
     * A JSON value of [type].
     *
     * @property envelope Keys of the objects the value is nested in, outermost first (e.g.
     *   `["data"]` for `{"data": value}`); empty when the body is the value itself.
     */
    data class Json(val type: TypeRef, val envelope: List<String> = emptyList()) : ResponseBody

    /** Raw bytes, such as a PDF. */
    data object Binary : ResponseBody

    /**
     * A JSON value of [type] assigned to the JavaScript [variable] by a script, as in
     * `var elenco_sedi = [...];`. The script may assign other variables too.
     */
    data class ScriptVariable(val variable: String, val type: TypeRef) : ResponseBody

    /**
     * An HTML page, read into a value of [type] by [scrape] from the document. [type] is a
     * scraped record, a list of them, or a value a field could scrape (see [Scrapes.kindFor]).
     */
    data class Html(val type: TypeRef, val scrape: List<ScrapeStep>) : ResponseBody
}
