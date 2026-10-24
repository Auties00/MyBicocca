package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.Text
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.ParameterLocation

/**
 * Emits what the API's [it.attendance100.mybicocca.codegen.model.ClientSpec] describes, on top
 * of the runtime's `RestClient`: the API's client, with the request helpers the modules call, a
 * facade initializer building it, the API's error with a constant per known error code, and the
 * enum of the roles operations declare, when they declare any.
 *
 * Swift's `URLSession` keeps cookies, caches and times out on its own, so the client ignores
 * those settings; client settings are initializer parameters.
 */
internal class SwiftClientEmitter(private val spec: ApiSpec, private val layout: SwiftLayout) {
    private val client = spec.client
    private val errorName = "${spec.prefix}Error"
    private val roles = "${spec.prefix}PermissionLevel"

    fun emit(out: CodeWriter) {
        clientClass(out)
        out.line()
        facadeInitializer(out)
        out.line()
        errorType(out)
        if (spec.usesRoles) {
            out.line()
            rolesEnum(out)
        }
    }

    private fun clientClass(out: CodeWriter) {
        val baseUrl = client.baseUrl ?: spec.servers.firstOrNull()?.url ?: error("API '${spec.id}' has no base URL")
        val rolesParameter = if (spec.usesRoles) ", roles: Set<$roles>" else ""
        val rolesArgument = if (spec.usesRoles) ", roles: roles" else ""
        out.line("/// Sends the generated ${spec.prefix} operations' requests and reads their responses and errors.")
        out.block("public final class ${layout.client}: Sendable {") {
            val headers = if (client.headers.isEmpty()) "" else ", headers: ${dictionary(client.headers)}"
            line("public static let defaultConfiguration = RestConfiguration(baseURL: URL(string: ${SwiftSyntax.string(baseUrl)})!$headers)")
            line()
            policy(this)
            line()
            line("public let configuration: RestConfiguration")
            for (setting in client.settings) {
                setting.description?.let { line("/// ${SwiftSyntax.docText(it)}") }
                line("public let ${SwiftSyntax.identifier(setting.name)}: String")
            }
            line("private let rest: RestClient")
            line()
            val parameters = listOf("configuration: RestConfiguration = ${layout.client}.defaultConfiguration") +
                client.settings.map { "${SwiftSyntax.identifier(it.name)}: String = ${SwiftSyntax.string(it.default)}" } +
                listOfNotNull(if (spec.retryParameters.isEmpty()) null else "parameters: [String: String] = [:]")
            line("/// - Parameter configuration: Where to send requests; set an `Authorization` header to call operations that need a logged-in user.")
            commaListOrLine("public init(", parameters, ") {", MAX_LINE_LENGTH)
            indent {
                line("self.configuration = configuration")
                for (setting in client.settings) line("self.${SwiftSyntax.identifier(setting.name)} = ${SwiftSyntax.identifier(setting.name)}")
                val parametersArgument = if (spec.retryParameters.isEmpty()) "" else ", parameters: parameters"
                line("rest = RestClient(configuration: configuration, policy: ${layout.client}.policy$parametersArgument)")
            }
            line("}")
            line()
            block("public func json<T: Decodable>(_ request: RestRequest$rolesParameter) async throws -> T {") {
                line("try await execute(request$rolesArgument, readsJSON: true)!.decode(T.self, envelope: request.envelope)")
            }
            line()
            line("/// Like ``json(_:${if (spec.usesRoles) "roles:" else ""})`` for a list, which is empty when the status is one of the policy's empty ones.")
            block("public func jsonList<T: Decodable>(_ request: RestRequest$rolesParameter) async throws -> [T] {") {
                line("guard let response = try await execute(request$rolesArgument, readsJSON: true, allowEmpty: true) else { return [] }")
                line("return try response.decode([T].self, envelope: request.envelope)")
            }
            line()
            block("public func data(_ request: RestRequest$rolesParameter) async throws -> Data {") {
                line("try await execute(request$rolesArgument, readsJSON: false)!.body")
            }
            line()
            block("public func text(_ request: RestRequest$rolesParameter) async throws -> String {") {
                line("try await execute(request$rolesArgument, readsJSON: false)!.text")
            }
            line()
            line("/// Decodes the JSON a script assigns to `variable`.")
            block("public func script<T: Decodable>(_ request: RestRequest, variable: String$rolesParameter) async throws -> T {") {
                line("try await execute(request$rolesArgument, readsJSON: false)!.script(T.self, variable: variable)")
            }
            line()
            line("/// Reads the page with `scrape`, failing when it returns nil.")
            block("public func html<T>(_ request: RestRequest, scrape: (ScrapeNode) -> T?$rolesParameter) async throws -> T {") {
                line("try await execute(request$rolesArgument, readsJSON: false)!.scrape(scrape)")
            }
            line()
            block("public func send(_ request: RestRequest$rolesParameter) async throws {") {
                line("_ = try await execute(request$rolesArgument, readsJSON: true)")
            }
            line()
            block("private func execute(_ request: RestRequest$rolesParameter, readsJSON: Bool, allowEmpty: Bool = false) async throws -> RestResponse? {") {
                val query = client.query.map { (name, value) -> name to SwiftSyntax.string(value) } +
                    client.settings.filter { it.location == ParameterLocation.QUERY }.map { it.wireName to SwiftSyntax.identifier(it.name) }
                val headers = client.settings.filter { it.location == ParameterLocation.HEADER }.map { it.wireName to SwiftSyntax.identifier(it.name) }
                val sent = if (query.isEmpty() && headers.isEmpty()) "request" else {
                    line("var request = request")
                    if (query.isNotEmpty()) {
                        commaList("request.query += [", query.map { (name, value) -> "QueryItem(name: ${SwiftSyntax.string(name)}, value: $value)" }, "]")
                    }
                    if (headers.isNotEmpty()) {
                        commaList("request.headers += [", headers.map { (name, value) -> "QueryItem(name: ${SwiftSyntax.string(name)}, value: $value)" }, "]")
                    }
                    "request"
                }
                line("do {")
                indent { line("return try await rest.execute($sent, readsJSON: readsJSON, allowEmpty: allowEmpty)") }
                block("} catch let failure as ApiFailure {") {
                    line("throw $errorName(failure: failure${if (spec.usesRoles) ", expectedRoles: roles" else ""})")
                }
            }
        }
    }

    private fun policy(out: CodeWriter) {
        val arguments = buildList {
            if (client.emptyStatuses.isNotEmpty()) add("emptyStatuses: ${client.emptyStatuses.sorted().joinToString(", ", "[", "]")}")
            val format = client.error
            val parts = listOf("detect" to format.detect, "code" to format.code, "message" to format.message)
                .filter { it.second.isNotEmpty() }
                .map { (name, pointers) -> "$name: ${pointers.joinToString(", ", "[", "]") { SwiftSyntax.string(it) }}" }
            if (parts.isNotEmpty()) add("errors: ErrorFormat(${parts.joinToString(", ")})")
            if (client.retries.isNotEmpty()) {
                val rules = client.retries.map { rule ->
                    val ruleParts = buildList {
                        rule.status?.let { add("status: $it") }
                        rule.code?.let { add("code: ${SwiftSyntax.string(it)}") }
                        rule.before?.let { before ->
                            val headers = if (before.headers.isEmpty()) "" else ", headers: ${dictionary(before.headers)}"
                            add("before: RetryRequest(method: .${before.method.name.lowercase()}, path: ${SwiftSyntax.string(before.path)}$headers)")
                        }
                        rule.delaySeconds?.let { add("delaySeconds: $it") }
                    }
                    rule.description?.let { "// ${SwiftSyntax.docText(it)}\n" }.orEmpty() + "RetryRule(${ruleParts.joinToString(", ")})"
                }
                add("retries: [\n${rules.joinToString(",\n") { it.prependIndent("    ") }},\n]")
            }
        }
        if (arguments.isEmpty()) {
            out.line("static let policy = RestPolicy()")
        } else {
            out.commaList("static let policy = RestPolicy(", arguments, ")")
        }
    }

    private fun facadeInitializer(out: CodeWriter) {
        val apiParameters = spec.parameters.map { "${SwiftSyntax.identifier(it.name)}: ${layout.render(it.type)}" }
        val settings = client.settings.map { "${SwiftSyntax.identifier(it.name)}: String = ${SwiftSyntax.string(it.default)}" }
        val parameters = apiParameters + "configuration: RestConfiguration = ${layout.client}.defaultConfiguration" + settings
        val clientArguments = listOf("configuration: configuration") +
            client.settings.map { "${SwiftSyntax.identifier(it.name)}: ${SwiftSyntax.identifier(it.name)}" } +
            listOfNotNull(
                spec.retryParameters.takeIf { it.isNotEmpty() }?.joinToString(", ", "parameters: [", "]") {
                    "${SwiftSyntax.string(it.name)}: \"\\(${SwiftSyntax.identifier(it.name)})\""
                }
            )
        val facadeArguments = listOf("client: ${layout.client}(${clientArguments.joinToString(", ")})") +
            spec.parameters.map { "${SwiftSyntax.identifier(it.name)}: ${SwiftSyntax.identifier(it.name)}" }
        out.block("extension ${layout.facadeName} {") {
            commaListOrLine("public init(", parameters, ") {", MAX_LINE_LENGTH)
            indent { commaListOrLine("self.init(", facadeArguments, ")", MAX_LINE_LENGTH) }
            line("}")
        }
    }

    private fun errorType(out: CodeWriter) {
        out.line("/// A request the ${spec.prefix} API rejected. Match on ``code``, such as one of the constants below,")
        out.line("/// or on ``status`` when the server sent none; never on the message.")
        out.block("public struct $errorName: Error, CustomStringConvertible {") {
            for (code in client.error.codes) {
                code.description?.let { line("/// ${SwiftSyntax.docText(it)}") }
                line("public static let ${SwiftSyntax.identifier(Text.lowerCamel(code.name))} = ${SwiftSyntax.string(code.value)}")
                line()
            }
            line("public let status: Int")
            line("/// The machine-readable error code the body carried, if any.")
            line("public let code: String?")
            line("/// The human-readable message the body carried, else the code, else a description of the status.")
            line("public let message: String")
            if (spec.usesRoles) {
                line("/// The roles the operation requires, a hint when the status is 401 or 403.")
                line("public let expectedRoles: Set<$roles>")
            }
            line()
            val rolesParameter = if (spec.usesRoles) ", expectedRoles: Set<$roles> = []" else ""
            block("public init(failure: ApiFailure$rolesParameter) {") {
                line("status = failure.status")
                line("code = failure.code")
                line("message = failure.message")
                if (spec.usesRoles) line("self.expectedRoles = expectedRoles")
            }
            line()
            block("public var description: String {") {
                line("\"${spec.prefix} request failed with status \\(status)\" + (code.map { \$0 == message ? \"\" : \" (\\(\$0))\" } ?? \"\") + \": \\(message)\"")
            }
        }
    }

    private fun rolesEnum(out: CodeWriter) {
        out.line("/// The roles ${spec.prefix} operations may require, as their specs name them.")
        out.block("public enum $roles: String, Hashable, Sendable, CaseIterable {") {
            for (role in spec.roles) line("case ${layout.role(role)} = ${SwiftSyntax.string(role)}")
        }
    }

    private fun dictionary(entries: Map<String, String>): String =
        entries.entries.joinToString(", ", "[", "]") { (name, value) -> "${SwiftSyntax.string(name)}: ${SwiftSyntax.string(value)}" }

    private companion object {
        const val MAX_LINE_LENGTH = 100
    }
}
