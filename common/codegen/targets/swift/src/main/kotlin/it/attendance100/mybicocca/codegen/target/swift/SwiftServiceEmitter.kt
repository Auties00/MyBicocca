package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.target.swift.SwiftSyntax.doc

/**
 * Emits a module as a struct of `async throws` functions, each building a `RestRequest` and
 * handing it to the API's client (see [SwiftLayout] for the client's contract).
 */
internal class SwiftServiceEmitter(private val spec: ApiSpec, private val layout: SwiftLayout) {

    fun emit(module: ApiModule, out: CodeWriter) {
        out.block("public struct ${layout.serviceName(module)}: Sendable {") {
            line("private let client: ${layout.client}")
            line()
            block("public init(client: ${layout.client}) {") {
                line("self.client = client")
            }
            for (operation in module.operations) {
                line()
                function(module, operation)
            }
        }
    }

    private fun CodeWriter.function(module: ApiModule, operation: Operation) {
        documentation(operation)
        val parameters = buildList {
            operation.pathParameters.forEach { add("${SwiftSyntax.identifier(it.name)}: ${layout.render(it.type)}") }
            when (val body = operation.body) {
                is RequestBody.Json -> add("$BODY: ${layout.render(body.type)}")
                RequestBody.EmptyObject, null -> Unit
            }
            operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { parameter ->
                val name = SwiftSyntax.identifier(parameter.name)
                val type = layout.render(parameter.type)
                val default = parameter.default
                add(
                    when {
                        parameter.required -> "$name: $type"
                        default != null -> "$name: $type = ${layout.literal(parameter.type, default)}"
                        else -> "$name: $type? = nil"
                    }
                )
            }
            operation.auth?.let { auth ->
                val type = layout.authType(auth)
                add(if (auth.required) "$AUTH: $type" else "$AUTH: ($type)? = nil")
            }
        }
        val returns = when (val response = operation.response) {
            ResponseBody.None -> ""
            ResponseBody.Binary -> " -> Data"
            is ResponseBody.Json -> " -> ${layout.render(response.type)}"
        }
        val name = SwiftSyntax.identifier(operation.name)
        commaListOrLine("public func $name(", parameters, ") async throws$returns {", MAX_LINE_LENGTH)
        indent {
            val method = when (val response = operation.response) {
                ResponseBody.None -> "send"
                ResponseBody.Binary -> "data"
                is ResponseBody.Json -> if (response.type is TypeRef.ListOf) "jsonList" else "json"
            }
            val request = nested { commaList("RestRequest(", requestArguments(module, operation), ")") }
            val arguments = listOfNotNull(request, if (spec.usesRoles) "roles: ${roles(operation)}" else null)
            commaList("try await client.$method(", arguments, ")")
        }
        line("}")
    }

    private fun CodeWriter.requestArguments(module: ApiModule, operation: Operation): List<String> = buildList {
        add("method: .${operation.method.name.lowercase()}")
        add("path: ${path(module, operation)}")
        operation.queryParameters.takeIf { it.isNotEmpty() }?.let { add("query: ${items(it)}") }
        operation.formParameters.takeIf { it.isNotEmpty() }?.let { add("form: ${items(it)}") }
        val headers = operation.headers.map { (name, value) -> "(${SwiftSyntax.string(name)}, ${SwiftSyntax.string(value)})" } +
            operation.headerParameters.map(::pair)
        if (headers.isNotEmpty()) add("headers: ${array(headers)}")
        when (operation.body) {
            is RequestBody.Json -> add("body: $BODY")
            RequestBody.EmptyObject -> add("body: EmptyObject()")
            null -> Unit
        }
        if (operation.auth != null) add("auth: $AUTH")
        operation.server?.let { add("server: ${layout.server}.${layout.serverCase(it)}.rawValue") }
        val envelope = (operation.response as? ResponseBody.Json)?.envelope.orEmpty()
        if (envelope.isNotEmpty()) add("envelope: ${envelope.joinToString(", ", "[", "]") { SwiftSyntax.string(it) }}")
    }

    /** The full path as a string literal, with path parameters interpolated as encoded segments. */
    private fun path(module: ApiModule, operation: Operation): String {
        val names = operation.pathParameters.associate { it.wireName to SwiftSyntax.identifier(it.name) }
        val literal = SwiftSyntax.string(module.basePath.trimEnd('/') + operation.path)
        return PLACEHOLDER.replace(literal) { "\\(RestRequest.pathSegment(${names.getValue(it.groupValues[1])}))" }
    }

    /** Name/value pairs as an array literal, wrapped one per line when there are several. */
    private fun CodeWriter.items(parameters: List<Parameter>): String = array(parameters.map(::pair))

    private fun pair(parameter: Parameter): String = "(${SwiftSyntax.string(parameter.wireName)}, ${SwiftSyntax.identifier(parameter.name)})"

    private fun CodeWriter.array(items: List<String>): String =
        if (items.size <= INLINE_ITEMS) items.joinToString(", ", "[", "]") else nested { commaList("[", items, "]") }

    private fun roles(operation: Operation): String =
        operation.roles.joinToString(", ", "[", "]") { ".${layout.role(it)}" }

    private fun CodeWriter.documentation(operation: Operation) {
        val inputs = buildList {
            operation.pathParameters.forEach { add(it.name to it) }
            operation.body?.let { add(BODY to null) }
            operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { add(it.name to it) }
            operation.auth?.let { add(AUTH to null) }
        }
        val documented = inputs.filter { (name, parameter) ->
            val description = inputDescription(operation, name, parameter)
            description != null || parameter?.valueDescriptions?.isNotEmpty() == true
        }
        if (operation.summary == null && documented.isEmpty()) return

        operation.summary?.let { line("/// ${SwiftSyntax.docText(it)}") }
        if (documented.isEmpty()) return
        if (operation.summary != null) line("///")
        line("/// - Parameters:")
        for ((name, parameter) in documented) {
            val description = inputDescription(operation, name, parameter)
            line("///   - $name: ${description?.let(SwiftSyntax::docText) ?: "One of the accepted values below."}")
            if (parameter != null) valueDocs(parameter)
        }
    }

    private fun inputDescription(operation: Operation, name: String, parameter: Parameter?): String? = when {
        parameter != null -> parameter.description
        name == AUTH -> operation.auth?.let(layout::authDescription)
        else -> operation.body?.description
    }

    private fun CodeWriter.valueDocs(parameter: Parameter) {
        val enum = (parameter.type as? TypeRef.Named)?.let { spec.resolve(it) } as? EnumType ?: return
        if (parameter.valueDescriptions.isEmpty()) return
        val cases = layout.caseNames(enum)
        val namesByWireValue = enum.values.associate { it.wireValue to cases.getValue(it.name) }
        line("///     Accepted values:")
        for ((wireValue, description) in parameter.valueDescriptions) {
            val case = namesByWireValue[wireValue] ?: continue
            line("///     - ``${enum.name}/$case``: ${SwiftSyntax.docText(description)}")
        }
    }

    private companion object {
        const val BODY = "body"
        const val AUTH = "auth"
        const val INLINE_ITEMS = 2
        const val MAX_LINE_LENGTH = 100
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
