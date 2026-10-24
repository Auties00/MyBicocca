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
) {
    val pathParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.PATH }
    val queryParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.QUERY }
    val formParameters: List<Parameter> get() = parameters.filter { it.location == ParameterLocation.FORM }
}

enum class HttpMethod { GET, POST, PUT, PATCH, DELETE }

/**
 * A path, query or form parameter. A list value is sent as one comma-separated value, the
 * Swagger/OpenAPI default for query parameters.
 *
 * @property name camelCase name of the parameter.
 * @property wireName Name of the parameter on the wire.
 * @property valueDescriptions Descriptions of the accepted values, by wire value, when
 *   [type] is an enum.
 */
data class Parameter(
    val name: String,
    val wireName: String,
    val location: ParameterLocation,
    val type: TypeRef,
    val required: Boolean,
    val description: String? = null,
    val valueDescriptions: Map<String, String> = emptyMap(),
)

enum class ParameterLocation { PATH, QUERY, FORM }

/** The body an operation sends. */
sealed interface RequestBody {
    val description: String?

    /** A value encoded as JSON. */
    data class Json(val type: TypeRef, override val description: String? = null) : RequestBody
}

/** What a successful response carries. */
sealed interface ResponseBody {
    /** Nothing the caller needs. */
    data object None : ResponseBody

    /** A JSON value of [type]. */
    data class Json(val type: TypeRef) : ResponseBody

    /** Raw bytes, such as a PDF. */
    data object Binary : ResponseBody
}
