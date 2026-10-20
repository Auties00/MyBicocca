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
                null -> Unit
            }
            operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { parameter ->
                val type = layout.render(parameter.type)
                add(if (parameter.required) "${SwiftSyntax.identifier(parameter.name)}: $type" else "${SwiftSyntax.identifier(parameter.name)}: $type? = nil")
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
            commaList("try await client.$method(", listOf(request, "roles: ${roles(operation)}"), ")")
        }
        line("}")
    }

    private fun CodeWriter.requestArguments(module: ApiModule, operation: Operation): List<String> = buildList {
        add("method: .${operation.method.name.lowercase()}")
        add("path: ${path(module, operation)}")
        operation.queryParameters.takeIf { it.isNotEmpty() }?.let { add("query: ${items(it)}") }
        operation.formParameters.takeIf { it.isNotEmpty() }?.let { add("form: ${items(it)}") }
        if (operation.body != null) add("body: $BODY")
    }

    /** The full path as a string literal, with path parameters interpolated as encoded segments. */
    private fun path(module: ApiModule, operation: Operation): String {
        val names = operation.pathParameters.associate { it.wireName to SwiftSyntax.identifier(it.name) }
        val literal = SwiftSyntax.string(module.basePath.trimEnd('/') + operation.path)
        return PLACEHOLDER.replace(literal) { "\\(RestRequest.pathSegment(${names.getValue(it.groupValues[1])}))" }
    }

    /** Name/value pairs as an array literal, wrapped one per line when there are several. */
    private fun CodeWriter.items(parameters: List<Parameter>): String {
        val pairs = parameters.map { "(${SwiftSyntax.string(it.wireName)}, ${SwiftSyntax.identifier(it.name)})" }
        return if (pairs.size <= INLINE_ITEMS) pairs.joinToString(", ", "[", "]") else nested { commaList("[", pairs, "]") }
    }

    private fun roles(operation: Operation): String =
        operation.roles.joinToString(", ", "[", "]") { ".${layout.role(it)}" }

    private fun CodeWriter.documentation(operation: Operation) {
        val inputs = buildList {
            operation.pathParameters.forEach { add(it.name to it) }
            operation.body?.let { add(BODY to null) }
            operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { add(it.name to it) }
        }
        val documented = inputs.filter { (name, parameter) ->
            val description = if (name == BODY && parameter == null) operation.body?.description else parameter?.description
            description != null || parameter?.valueDescriptions?.isNotEmpty() == true
        }
        if (operation.summary == null && documented.isEmpty()) return

        operation.summary?.let { line("/// ${SwiftSyntax.docText(it)}") }
        if (documented.isEmpty()) return
        if (operation.summary != null) line("///")
        line("/// - Parameters:")
        for ((name, parameter) in documented) {
            val description = if (parameter == null) operation.body?.description else parameter.description
            line("///   - $name: ${description?.let(SwiftSyntax::docText) ?: "One of the accepted values below."}")
            if (parameter != null) valueDocs(parameter)
        }
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
        const val INLINE_ITEMS = 2
        const val MAX_LINE_LENGTH = 100
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
