package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.ParameterLocation

/**
 * Emits what the API's [it.attendance100.mybicocca.codegen.model.ClientSpec] describes, on top
 * of the runtime's `RestClient`: the API's client, with the request helpers the modules call,
 * the API's error with a constant per known error code, the union of the roles operations
 * declare, and `create<Prefix>Api`, which builds the facade with its own client.
 *
 * `fetch` keeps cookies and times out on its own, so the client ignores those settings; with
 * `caches`, the runtime's `ResponseCache` keeps responses as on every platform. Client settings
 * are constructor parameters.
 */
internal class TsClientEmitter(private val spec: ApiSpec, private val layout: TsLayout) {
    private val client = spec.client
    private val errorName = layout.error

    /** The client, its policy, and `create<Prefix>Api`. */
    fun emitClient(file: TsFile) {
        listOf("ApiFailure", "DecodeError", "RestClient", "scrapeResponse", "scriptResponse", "unwrapEnvelope").forEach { layout.importRuntime(file, it) }
        listOf("RestConfiguration", "RestPolicy", "RestRequest", "ScrapeNode").forEach { layout.importRuntime(file, "type $it") }
        file.importFile(layout.facadeName, layout.facadeFile)
        file.importFile(errorName, layout.errorFile)
        if (spec.usesRoles) file.importFile("type ${layout.roles}", layout.rolesFile)
        if (spec.parameters.isNotEmpty()) file.importFile("type ${layout.parameters}", layout.parametersFile)
        file.body.apply {
            policy(this)
            line()
            clientClass(this)
            line()
            factory(this)
        }
    }

    /** The roles operations declare; only emitted when they declare any. */
    fun emitRoles(file: TsFile) {
        file.body.apply {
            line("/** The roles ${spec.prefix} operations may require, as their specs name them. */")
            line("export type ${layout.roles} =")
            indent { spec.roles.forEachIndexed { index, role -> line("| ${TsSyntax.string(role)}" + if (index == spec.roles.lastIndex) ";" else "") } }
        }
    }

    fun emitError(file: TsFile) {
        val roles = spec.usesRoles
        layout.importRuntime(file, "type ApiFailure")
        if (roles) file.importFile("type ${layout.roles}", layout.rolesFile)
        val out = file.body
        out.line("/**")
        out.line(" * A request the ${spec.prefix} API rejected. Match on `code`, such as one of the constants below,")
        out.line(" * or on `status` when the server sent none; never on the message.")
        out.line(" */")
        out.block("export class $errorName extends Error {") {
            line("override readonly name = ${TsSyntax.string(errorName)};")
            line()
            for (code in client.error.codes) {
                code.description?.let { line("/** ${TsSyntax.docText(it)} */") }
                line("static readonly ${code.name} = ${TsSyntax.string(code.value)};")
                line()
            }
            line("readonly status: number;")
            line("/** The machine-readable error code the body carried, if any. */")
            line("readonly code: string | undefined;")
            line("/** The human-readable message the body carried, else the code, else a description of the status. */")
            line("readonly errorMessage: string;")
            line("/** The human-readable message the body carried, if any. */")
            line("readonly serverMessage: string | undefined;")
            line()
            if (roles) line("/** @param expectedRoles The roles the operation requires, a hint when the status is 401 or 403. */")
            val parameters = listOf("failure: ApiFailure") + listOfNotNull(if (roles) "readonly expectedRoles: readonly ${layout.roles}[] = []" else null)
            block("constructor(${parameters.joinToString(", ")}) {") {
                line("const code = failure.code === undefined || failure.code === failure.message ? \"\" : ` (\${failure.code})`;")
                line("super(`${spec.prefix} request failed with status \${failure.status}\${code}: \${failure.message}`);")
                line("this.status = failure.status;")
                line("this.code = failure.code;")
                line("this.errorMessage = failure.message;")
                line("this.serverMessage = failure.serverMessage;")
            }
        }
    }

    private fun policy(out: CodeWriter) {
        val entries = buildList {
            if (client.emptyStatuses.isNotEmpty()) add("emptyStatuses: ${client.emptyStatuses.sorted().joinToString(", ", "[", "]")}")
            val format = client.error
            val parts = listOf("detect" to format.detect, "code" to format.code, "message" to format.message)
                .filter { it.second.isNotEmpty() }
                .map { (name, pointers) -> "$name: ${pointers.joinToString(", ", "[", "]") { TsSyntax.string(it) }}" }
            if (parts.isNotEmpty()) add("errors: { ${parts.joinToString(", ")} }")
            if (client.retries.isNotEmpty()) {
                val rules = client.retries.map { rule ->
                    val ruleParts = buildList {
                        rule.status?.let { add("status: $it") }
                        rule.code?.let { add("code: ${TsSyntax.string(it)}") }
                        rule.before?.let { before ->
                            val headers = if (before.headers.isEmpty()) "" else ", headers: ${record(before.headers)}"
                            add("before: { method: ${TsSyntax.string(before.method.name)}, path: ${TsSyntax.string(before.path)}$headers }")
                        }
                        rule.delaySeconds?.let { add("delaySeconds: $it") }
                    }
                    rule.description?.let { "// ${TsSyntax.docText(it)}\n" }.orEmpty() + "{ ${ruleParts.joinToString(", ")} }"
                }
                add("retries: [\n${rules.joinToString(",\n") { it.prependIndent("  ") }},\n]")
            }
            if (client.caches) add("caches: true")
        }
        if (entries.isEmpty()) out.line("const policy: RestPolicy = {};") else out.commaList("const policy: RestPolicy = {", entries, "};", trailingComma = true)
    }

    private fun clientClass(out: CodeWriter) {
        val baseUrl = client.baseUrl ?: spec.servers.firstOrNull()?.url ?: error("API '${spec.id}' has no base URL")
        val rolesParameter = if (spec.usesRoles) "roles: readonly ${layout.roles}[], " else ""
        val rolesArgument = if (spec.usesRoles) "roles, " else ""
        out.line("/** Sends the generated ${spec.prefix} operations' requests and reads their responses and errors. */")
        out.block("export class ${layout.client} {") {
            line("static readonly defaultBaseUrl = ${TsSyntax.string(baseUrl)};")
            line()
            line("readonly configuration: RestConfiguration;")
            line("private readonly rest: RestClient;")
            line()
            val docs = listOf(" * @param configuration Where to send requests; set an `Authorization` header to call operations that need a logged-in user.") +
                client.settings.map { setting -> " * @param ${setting.name} ${setting.description?.let(TsSyntax::docText) ?: setting.name}" }
            line("/**")
            docs.forEach(::line)
            line(" */")
            val parameters = listOf("configuration: Partial<RestConfiguration> = {}") +
                client.settings.map { "readonly ${TsSyntax.variable(it.name)} = ${TsSyntax.string(it.default)}" } +
                listOfNotNull(if (spec.retryParameters.isEmpty()) null else "parameters: Readonly<Record<string, string>> = {}")
            commaList("constructor(", parameters, ") {", trailingComma = true)
            indent {
                val headers = if (client.headers.isEmpty()) "" else ", headers: { ...${record(client.headers)}, ...configuration.headers }"
                line("this.configuration = { baseUrl: ${layout.client}.defaultBaseUrl, ...configuration$headers };")
                line("this.rest = new RestClient(this.configuration, policy${if (spec.retryParameters.isEmpty()) "" else ", parameters"});")
            }
            line("}")
            line()
            block("async json<T>(request: RestRequest, ${rolesParameter}decode: (json: unknown) => T | undefined): Promise<T> {") {
                line("const value = decode(unwrapEnvelope(await body((await this.execute(request, ${rolesArgument}true))!), request.envelope ?? []));")
                line("if (value === undefined) throw new DecodeError(`Unexpected response to \${request.method} \${request.path}`);")
                line("return value;")
            }
            line()
            line("/** Like {@link json} for a list, which is empty when the status is one of the policy's empty ones. */")
            block("async jsonList<T>(request: RestRequest, ${rolesParameter}decodeItem: (json: unknown) => T | undefined): Promise<T[]> {") {
                line("const response = await this.execute(request, ${rolesArgument}true, true);")
                line("if (response === undefined) return [];")
                line("const json = unwrapEnvelope(await body(response), request.envelope ?? []);")
                line("if (!Array.isArray(json)) throw new DecodeError(`Expected a list in response to \${request.method} \${request.path}`);")
                block("return json.map((item, index) => {", "});") {
                    line("const value = decodeItem(item);")
                    line("if (value === undefined) throw new DecodeError(`Unexpected item \${index} in response to \${request.method} \${request.path}`);")
                    line("return value;")
                }
            }
            line()
            block("async bytes(request: RestRequest${if (spec.usesRoles) ", roles: readonly ${layout.roles}[]" else ""}): Promise<Uint8Array> {") {
                line("return new Uint8Array(await (await this.execute(request, ${rolesArgument}false, false, false))!.arrayBuffer());")
            }
            line()
            block("async text(request: RestRequest${if (spec.usesRoles) ", roles: readonly ${layout.roles}[]" else ""}): Promise<string> {") {
                line("return (await this.execute(request, ${rolesArgument}false))!.text();")
            }
            line()
            line("/** Decodes the JSON a script assigns to `variable`. */")
            block("async script<T>(request: RestRequest, ${rolesParameter}variable: string, decode: (json: unknown) => T | undefined): Promise<T> {") {
                line("return scriptResponse((await this.execute(request, ${rolesArgument}false))!, variable, decode);")
            }
            line()
            line("/** Reads the page with `scrape`, failing when it returns `undefined`. */")
            block("async html<T>(request: RestRequest, ${rolesParameter}scrape: (document: ScrapeNode) => T | undefined): Promise<T> {") {
                line("return scrapeResponse((await this.execute(request, ${rolesArgument}false))!, scrape);")
            }
            line()
            block("async send(request: RestRequest${if (spec.usesRoles) ", roles: readonly ${layout.roles}[]" else ""}): Promise<void> {") {
                line("await this.execute(request, ${rolesArgument}true);")
            }
            line()
            block("private async execute(request: RestRequest, ${rolesParameter}readsJson: boolean, allowEmpty = false, stores = true): Promise<Response | undefined> {") {
                val query = client.query.map { (name, value) -> "[${TsSyntax.string(name)}, ${TsSyntax.string(value)}]" } +
                    client.settings.filter { it.location == ParameterLocation.QUERY }.map { "[${TsSyntax.string(it.wireName)}, this.${TsSyntax.variable(it.name)}]" }
                val headers = client.settings.filter { it.location == ParameterLocation.HEADER }.map { "[${TsSyntax.string(it.wireName)}, this.${TsSyntax.variable(it.name)}]" }
                val sent = if (query.isEmpty() && headers.isEmpty()) "request" else {
                    val parts = listOfNotNull(
                        if (query.isEmpty()) null else "query: [...(request.query ?? []), ${query.joinToString(", ")}]",
                        if (headers.isEmpty()) null else "headers: [...(request.headers ?? []), ${headers.joinToString(", ")}]",
                    )
                    line("const sent: RestRequest = { ...request, ${parts.joinToString(", ")} };")
                    "sent"
                }
                line("try {")
                indent { line("return await this.rest.execute($sent, readsJson, allowEmpty, stores);") }
                block("} catch (error) {") {
                    line("if (error instanceof ApiFailure) throw new $errorName(error${if (spec.usesRoles) ", roles" else ""});")
                    line("throw error;")
                }
            }
        }
        out.line()
        out.line("/** The body of `response` as JSON; a blank one is `null`. */")
        out.block("async function body(response: Response): Promise<unknown> {") {
            line("const text = await response.text();")
            line("return text.trim() === \"\" ? null : JSON.parse(text);")
        }
    }

    private fun factory(out: CodeWriter) {
        val settings = client.settings.map { "${TsSyntax.variable(it.name)} = ${TsSyntax.string(it.default)}" }
        val parameters = listOfNotNull(if (spec.parameters.isEmpty()) null else "parameters: ${layout.parameters}") +
            "configuration: Partial<RestConfiguration> = {}" + settings
        val clientArguments = listOf("configuration") + client.settings.map { TsSyntax.variable(it.name) } +
            listOfNotNull(
                spec.retryParameters.takeIf { it.isNotEmpty() }?.joinToString(", ", "{ ", " }") {
                    "${TsSyntax.propertyKey(it.name)}: String(parameters.${it.name})"
                }
            )
        val facadeArguments = listOf("new ${layout.client}(${clientArguments.joinToString(", ")})") + listOfNotNull(if (spec.parameters.isEmpty()) null else "parameters")
        out.line("/** Creates the ${spec.prefix} API with its own client. */")
        out.block("export function create${layout.facadeName}(${parameters.joinToString(", ")}): ${layout.facadeName} {") {
            line("return new ${layout.facadeName}(${facadeArguments.joinToString(", ")});")
        }
    }

    private fun record(entries: Map<String, String>): String =
        entries.entries.joinToString(", ", "{ ", " }") { (name, value) -> "${TsSyntax.propertyKey(name)}: ${TsSyntax.string(value)}" }
}
