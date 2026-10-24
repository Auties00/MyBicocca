package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.Text
import it.attendance100.mybicocca.codegen.core.toScreamingSnakeCase
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.ParameterLocation

/**
 * Emits what the API's [it.attendance100.mybicocca.codegen.model.ClientSpec] describes, on top
 * of the shared runtime:
 *
 * - the internal `Runtime` object: the JSON settings, the request policy every module extends
 *   `RestApi` with, the client settings and the factory of the HTTP client;
 * - the API's exception, with a constant per known error code;
 * - the enum of the roles operations declare, when they declare any.
 */
internal class ClientEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {
    private val client = spec.client
    private val exceptionName = layout.exception.substringAfterLast('.')
    private val rolesName = layout.roles.substringAfterLast('.')

    fun emitRuntime(file: KotlinFile) {
        listOf(
            "io.ktor.client.HttpClient", "io.ktor.client.HttpClientConfig", "kotlinx.serialization.json.Json", layout.exception,
            layout.runtime("ClientSettings"), layout.runtime("RestClientOptions"), layout.runtime("RestPolicy"), layout.runtime("createRestClient"),
        ).forEach(file::import)
        file.body.apply {
            line("/** How the generated [${layout.facadeName}] facade and its modules talk to the ${spec.prefix} API. */")
            block("internal object ${layout.runtime.substringAfterLast('.')} {") {
                line("/** Lenient on input, since responses add fields over time and servers mix scalar types. */")
                block("val json = Json {") {
                    line("coerceInputValues = true")
                    line("ignoreUnknownKeys = true")
                    line("isLenient = true")
                    if (client.sendsDefaults) line("encodeDefaults = true")
                    if (!client.sendsNulls) line("explicitNulls = false")
                }
                line()
                policy(file)
                line()
                settings(file)
                line()
                line("fun createHttpClient(json: Json, settings: ClientSettings, config: HttpClientConfig<*>.() -> Unit): HttpClient =")
                indent { options() }
            }
        }
    }

    private fun CodeWriter.policy(file: KotlinFile) {
        val arguments = buildList {
            add("baseUrl = ${client.baseUrl?.let(KotlinSyntax::string) ?: "null"}")
            if (client.emptyStatuses.isNotEmpty()) add("emptyStatuses = ${client.emptyStatuses.sorted().joinToString(", ", "setOf(", ")")}")
            val error = client.error
            if (error.detect.isNotEmpty() || error.code.isNotEmpty() || error.message.isNotEmpty()) {
                file.import(layout.runtime("ErrorFormat"))
                val parts = listOf("detect" to error.detect, "code" to error.code, "message" to error.message)
                    .filter { it.second.isNotEmpty() }
                    .map { (name, pointers) -> "$name = ${pointers.joinToString(", ", "listOf(", ")") { KotlinSyntax.string(it) }}" }
                add("errors = ErrorFormat(${parts.joinToString(", ")})")
            }
            if (client.retries.isNotEmpty()) {
                file.import(layout.runtime("RetryRule"))
                val rules = client.retries.map { rule ->
                    val parts = buildList {
                        rule.status?.let { add("status = $it") }
                        rule.code?.let { add("code = ${KotlinSyntax.string(it)}") }
                        rule.before?.let { before ->
                            file.import(layout.runtime("RetryRequest"))
                            file.import("io.ktor.http.HttpMethod")
                            val headers = if (before.headers.isEmpty()) "" else
                                ", headers = ${before.headers.entries.joinToString(", ", "mapOf(", ")") { (name, value) -> "${KotlinSyntax.string(name)} to ${KotlinSyntax.string(value)}" }}"
                            add("before = RetryRequest(HttpMethod.${before.method.name.lowercase().replaceFirstChar(Char::uppercaseChar)}, ${KotlinSyntax.string(before.path)}$headers)")
                        }
                        rule.delaySeconds?.let {
                            file.import("kotlin.time.Duration.Companion.seconds")
                            add("delay = $it.seconds")
                        }
                    }
                    rule.description?.let { "// ${Text.singleLine(it)}\n" }.orEmpty() + "RetryRule(${parts.joinToString(", ")})"
                }
                add("retries = listOf(\n${rules.joinToString(",\n") { it.prependIndent("    ") }},\n)")
            }
            val roles = if (spec.usesRoles) ", it.roles.filterIsInstance<$rolesName>().toSet()" else ""
            if (spec.usesRoles) file.import(layout.roles)
            add("failure = { $exceptionName(it.statusCode, it.errorCode, it.errorMessage$roles) }")
        }
        commaList("val policy = RestPolicy(", arguments, ")")
    }

    private fun CodeWriter.settings(file: KotlinFile) {
        if (client.settings.isEmpty()) {
            line("fun settings() = ClientSettings(emptyList())")
            return
        }
        file.import(layout.runtime("ClientSetting"))
        val definitions = client.settings.map { setting ->
            val location = if (setting.location == ParameterLocation.QUERY) "QUERY" else "HEADER"
            "ClientSetting(${KotlinSyntax.string(setting.name)}, ClientSetting.Location.$location, ${KotlinSyntax.string(setting.wireName)}, ${KotlinSyntax.string(setting.default)})"
        }
        line("fun settings() = ClientSettings(")
        indent { commaList("listOf(", definitions, ")") }
        line(")")
    }

    private fun CodeWriter.options() {
        val options = buildList {
            client.timeoutSeconds?.let { add("timeoutMillis = ${it * 1000L}L") }
            if (client.keepsCookies) add("keepsCookies = true")
            if (client.caches) add("caches = true")
            if (client.headers.isNotEmpty()) add("headers = ${map(client.headers)}")
            if (client.query.isNotEmpty()) add("query = ${map(client.query)}")
        }
        if (options.isEmpty()) {
            line("createRestClient(json, RestClientOptions(), settings, config)")
        } else {
            line("createRestClient(")
            indent {
                line("json,")
                commaList("RestClientOptions(", options, "),")
                line("settings,")
                line("config,")
            }
            line(")")
        }
    }

    private fun map(entries: Map<String, String>): String =
        entries.entries.joinToString(", ", "mapOf(", ")") { (name, value) -> "${KotlinSyntax.string(name)} to ${KotlinSyntax.string(value)}" }

    fun emitException(file: KotlinFile) {
        file.import(layout.runtime("ApiException"))
        val codes = client.error.codes
        file.body.apply {
            line("/**")
            line(" * A request the ${spec.prefix} API rejected. Match on [errorCode], such as one of the constants")
            line(" * below, or on [statusCode] when the server sent none; never on the message.")
            if (spec.usesRoles) {
                line(" *")
                line(" * @property expectedRoles The roles the operation requires, a hint when the status is 401 or 403.")
            }
            line(" */")
            val parameters = listOf("statusCode: Int", "errorCode: String?", "errorMessage: String") +
                listOfNotNull(if (spec.usesRoles) "val expectedRoles: Set<$rolesName> = emptySet()" else null)
            if (spec.usesRoles) file.import(layout.roles)
            commaList("class $exceptionName(", parameters, ") : ApiException(${KotlinSyntax.string(spec.prefix)}, statusCode, errorCode, errorMessage)" + if (codes.isEmpty()) "" else " {")
            if (codes.isNotEmpty()) {
                indent {
                    block("companion object {") {
                        codes.forEachIndexed { index, code ->
                            if (index > 0) line()
                            code.description?.let { line("/** ${KotlinSyntax.docText(it)} */") }
                            line("const val ${code.name.toScreamingSnakeCase()} = ${KotlinSyntax.string(code.value)}")
                        }
                    }
                }
                line("}")
            }
        }
    }

    fun emitRoles(file: KotlinFile) {
        file.body.apply {
            line("/** The roles ${spec.prefix} operations may require, as their specs name them. */")
            block("enum class $rolesName {") {
                spec.roles.forEachIndexed { index, role -> line(role + if (index < spec.roles.lastIndex) "," else "") }
            }
        }
    }
}
