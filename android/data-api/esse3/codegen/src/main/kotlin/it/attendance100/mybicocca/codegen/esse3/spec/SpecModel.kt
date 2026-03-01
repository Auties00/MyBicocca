package it.attendance100.mybicocca.codegen.esse3.spec

data class ParsedSpec(
    val specName: String,
    val basePath: String,
    val operations: List<ParsedOperation>,
    val definitions: List<ParsedDefinition>
)

data class ParsedOperation(
    val operationId: String,
    val httpMethod: String,
    val path: String,
    val pathParams: List<ParsedParameter>,
    val queryParams: List<ParsedParameter>,
    val bodyParam: ParsedParameter?,
    val responseType: ResolvedType?,
    val isListResponse: Boolean,
    val permissions: List<String>
)

data class ParsedParameter(
    val name: String,
    val type: ResolvedType,
    val required: Boolean,
    val location: ParameterLocation
)

enum class ParameterLocation {
    PATH, QUERY, BODY
}

data class ParsedDefinition(
    val name: String,
    val properties: List<ParsedProperty>,
    val requiredFields: Set<String>
)

data class ParsedProperty(
    val wireName: String,
    val type: String?,
    val format: String?,
    val ref: String?,
    val isArray: Boolean,
    val itemRef: String?,
    val itemType: String?,
    val itemFormat: String?,
    val enumValues: List<String>?
)

sealed class ResolvedType {
    data class Simple(val kotlinType: String) : ResolvedType()
    data class Reference(val definitionName: String) : ResolvedType()
    data class ListOf(val inner: ResolvedType) : ResolvedType()

    fun render(prefix: String = "Esse3"): String = when (this) {
        is Simple -> kotlinType
        is Reference -> "$prefix$definitionName"
        is ListOf -> "List<${inner.render(prefix)}>"
    }
}
