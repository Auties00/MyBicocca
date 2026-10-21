package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Emits a module as a class of suspend functions extending the API's `AbstractApi`, whose
 * request helpers form the runtime contract:
 *
 * - `executeJson<Method><T>(endpoint, roles) { … }` decodes the body as `T`;
 * - `executeJson<Method>List<T>(…)` decodes a list (Esse3's runtime returns an empty one on
 *   HTTP 404);
 * - `executeStream<Method>(…)` returns the body as a `ByteReadChannel`;
 * - `executeScript<Method><T>(…, variable = "name")` decodes as `T` the JSON a script assigns to
 *   a variable;
 * - `executeHtml<Method>(…, scrape = <Prefix>Scrapers::<function>)` parses the body as an HTML
 *   document, with the request URL as its base, and returns what `scrape` reads from it,
 *   failing when it returns null;
 * - `execute<Method>(endpoint) { … }` plus `ensureSuccess(response, roles)` when the body
 *   isn't needed.
 *
 * `roles` is passed only by APIs whose operations declare roles. When the spec uses them, the
 * helpers also take the named arguments `server` (a value of the API's `Server` enum) and
 * `envelope` (the keys the response value is nested in). The trailing block configures the
 * Ktor request: parameters, headers, cookies and body. JSON-encoded parameters are written
 * with the module's `json`.
 *
 * The API parameters a module uses are constructor parameters, passed by the facade.
 */
internal class ServiceEmitter(private val spec: ApiSpec, private val layout: KotlinLayout, private val scrapers: ScrapeEmitter) {

    fun emit(module: ApiModule, file: KotlinFile) {
        file.import("io.ktor.client.HttpClient")
        file.import("kotlinx.serialization.json.Json")
        file.import(layout.abstractApi)
        val constructor = listOf("client: HttpClient", "json: Json") +
            spec.parametersOf(module).map { "private val ${KotlinSyntax.identifier(it.name)}: ${layout.render(it.type, file)}" }
        file.body.apply {
            line("class ${layout.serviceName(module)}(")
            indent {
                constructor.forEachIndexed { index, parameter -> line(parameter + if (index < constructor.lastIndex) "," else "") }
            }
            line(") : ${layout.abstractApi.substringAfterLast('.')}(client, json, ${KotlinSyntax.string(module.basePath)}) {")
            line()
            indent {
                separated(module.operations) { function(module, it, file) }
            }
            line("}")
        }
    }

    private fun CodeWriter.function(module: ApiModule, operation: Operation, file: KotlinFile) {
        requireReifiable((operation.body as? RequestBody.Json)?.type, "${operation.name} body")
        requireReifiable((operation.response as? ResponseBody.Json)?.type, "${operation.name} response")
        requireReifiable((operation.response as? ResponseBody.ScriptVariable)?.type, "${operation.name} response")
        doc(operation)
        val parameters = signatureParameters(operation, file)
        val returnType = returnType(operation.response, file)
        val returns = returnType?.let { ": $it" }.orEmpty()
        if (parameters.isEmpty()) {
            line("suspend fun ${KotlinSyntax.identifier(operation.name)}()$returns {")
        } else {
            commaList("suspend fun ${KotlinSyntax.identifier(operation.name)}(", parameters, ")$returns {")
        }
        indent { call(module, operation, file) }
        line("}")
    }

    /**
     * Path parameters, then the body, then the remaining parameters, then the credentials.
     * Optional parameters default to their declared value, or to null (and aren't sent).
     */
    private fun signatureParameters(operation: Operation, file: KotlinFile): List<String> = buildList {
        operation.pathParameters.forEach { add("${KotlinSyntax.identifier(it.name)}: ${layout.render(it.type, file)}") }
        when (val body = operation.body) {
            is RequestBody.Json -> add("$BODY: ${layout.render(body.type, file)}")
            RequestBody.EmptyObject, null -> Unit
        }
        operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { parameter ->
            val name = KotlinSyntax.identifier(parameter.name)
            val type = layout.render(parameter.type, file)
            val default = parameter.default
            add(
                when {
                    parameter.required -> "$name: $type"
                    default != null && parameter.encoding == ParameterEncoding.PLAIN -> "$name: $type = ${literal(parameter.type, default, file)}"
                    else -> "$name: $type? = null"
                }
            )
        }
        operation.auth?.let { auth ->
            val type = layout.authType(auth, file)
            add(if (auth.required) "$AUTH: $type" else "$AUTH: $type? = null")
        }
    }

    /**
     * Bodies and responses are converted by their reified type, which can't name the
     * serializer a date needs, so a date may only appear inside a record there.
     */
    private fun requireReifiable(type: TypeRef?, where: String) {
        val hasDate = when (type) {
            is TypeRef.Primitive -> type.kind.isTemporal
            is TypeRef.ListOf -> return requireReifiable(type.element, where)
            is TypeRef.MapOf -> {
                requireReifiable(type.key, where)
                return requireReifiable(type.value, where)
            }
            is TypeRef.Named, null -> false
        }
        require(!hasDate) { "'$where': the kotlin target can't convert a date or time outside a record" }
    }

    private fun returnType(response: ResponseBody, file: KotlinFile): String? = when (response) {
        ResponseBody.None -> null
        ResponseBody.Binary -> layout.render(TypeRef.Primitive(PrimitiveKind.BINARY), file)
        is ResponseBody.Json -> layout.render(response.type, file)
        is ResponseBody.ScriptVariable -> layout.render(response.type, file)
        is ResponseBody.Html -> layout.render(response.type, file)
    }

    private fun CodeWriter.call(module: ApiModule, operation: Operation, file: KotlinFile) {
        val method = operation.method.name.lowercase().capitalized()
        val roles = if (spec.usesRoles) roles(operation, file) else null
        val target = listOfNotNull(endpoint(operation), roles) + targetArguments(operation, file)
        val hasBlock = operation.body != null ||
            operation.parameters.any { it.location != ParameterLocation.PATH } ||
            spec.parameters.any { it.location != ParameterLocation.PATH } ||
            operation.headers.isNotEmpty() ||
            operation.query.isNotEmpty() ||
            operation.form.isNotEmpty() ||
            operation.auth != null

        val response = operation.response
        val invocation = when (response) {
            ResponseBody.None -> "val response = execute$method(${(listOf(endpoint(operation)) + targetArguments(operation, file)).joinToString(", ")})"
            ResponseBody.Binary -> "return executeStream$method(${target.joinToString(", ")})"
            is ResponseBody.Json -> when (val type = response.type) {
                is TypeRef.ListOf -> "return executeJson${method}List<${layout.render(type.element, file)}>(${target.joinToString(", ")})"
                else -> "return executeJson$method<${layout.render(type, file)}>(${target.joinToString(", ")})"
            }
            is ResponseBody.ScriptVariable -> {
                val arguments = target + "variable = ${KotlinSyntax.string(response.variable)}"
                "return executeScript$method<${layout.render(response.type, file)}>(${arguments.joinToString(", ")})"
            }
            is ResponseBody.Html -> {
                file.import("${layout.dtoPackage}.${scrapers.objectName}")
                val arguments = target + "scrape = ${scrapers.objectName}::${scrapers.responseFunction(module, operation)}"
                "return executeHtml$method(${arguments.joinToString(", ")})"
            }
        }
        if (hasBlock) {
            block("$invocation {") { requestConfiguration(operation, file) }
        } else {
            line(invocation)
        }
        if (response == ResponseBody.None) line(listOfNotNull("response", roles).joinToString(", ", "ensureSuccess(", ")"))
    }

    /** The named `server` and `envelope` arguments, when the operation needs them. */
    private fun targetArguments(operation: Operation, file: KotlinFile): List<String> = buildList {
        operation.server?.let { server ->
            file.import(layout.server)
            add("server = ${layout.server.substringAfterLast('.')}.$server")
        }
        val envelope = (operation.response as? ResponseBody.Json)?.envelope.orEmpty()
        if (envelope.isNotEmpty()) add("envelope = ${envelope.joinToString(", ", "listOf(", ")") { KotlinSyntax.string(it) }}")
    }

    private fun CodeWriter.requestConfiguration(operation: Operation, file: KotlinFile) {
        when (operation.body) {
            is RequestBody.Json -> {
                jsonContent(file)
                line("setBody($BODY)")
            }
            RequestBody.EmptyObject -> {
                jsonContent(file)
                file.import("kotlinx.serialization.json.JsonObject")
                line("setBody(JsonObject(emptyMap()))")
            }
            null -> Unit
        }

        val form = operation.formParameters
        val authForm = operation.auth?.let { auth -> spec.authSchemes.any { it.name in auth.schemes && it.form.isNotEmpty() } } == true
        if (form.isNotEmpty() || operation.form.isNotEmpty() || authForm) {
            file.import("io.ktor.client.request.setBody")
            file.import("io.ktor.client.request.forms.FormDataContent")
            file.import("io.ktor.http.parameters")
            block("setBody(FormDataContent(parameters {", "}))") {
                for ((name, value) in operation.form) line("append(${KotlinSyntax.string(name)}, ${KotlinSyntax.string(value)})")
                for (parameter in form) send(parameter, "append", file) { formValue(parameter, it) }
                if (authForm) {
                    val send = "forEach { (name, value) -> append(name, value) }"
                    line(if (operation.auth!!.required) "$AUTH.form.$send" else "$AUTH?.form?.$send")
                }
            }
        }

        val apiParameters = spec.parameters.groupBy { it.location }
        val query = apiParameters[ParameterLocation.QUERY].orEmpty() + operation.queryParameters
        val headers = apiParameters[ParameterLocation.HEADER].orEmpty() + operation.headerParameters
        val cookies = apiParameters[ParameterLocation.COOKIE].orEmpty() + operation.cookieParameters

        if (operation.query.isNotEmpty() || query.isNotEmpty()) file.import("io.ktor.client.request.parameter")
        for ((name, value) in operation.query) {
            line("parameter(${KotlinSyntax.string(name)}, ${KotlinSyntax.string(value)})")
        }
        for (parameter in query) {
            send(parameter, "parameter", file) { queryValue(parameter, it) }
        }

        if (operation.headers.isNotEmpty() || headers.isNotEmpty() || operation.auth != null) {
            file.import("io.ktor.client.request.header")
        }
        for ((name, value) in operation.headers) {
            line("header(${KotlinSyntax.string(name)}, ${template(value)})")
        }
        for (parameter in headers) {
            send(parameter, "header", file) { queryValue(parameter, it) }
        }
        if (cookies.isNotEmpty()) cookieHeader(cookies, file)
        operation.auth?.let { auth ->
            val send = "forEach { (name, value) -> header(name, value) }"
            line(if (auth.required) "$AUTH.headers.$send" else "$AUTH?.headers?.$send")
        }
    }

    private fun CodeWriter.jsonContent(file: KotlinFile) {
        file.import("io.ktor.client.request.setBody")
        file.import("io.ktor.http.ContentType")
        file.import("io.ktor.http.contentType")
        line("contentType(ContentType.Application.Json)")
    }

    /**
     * Sends [parameter] with [function] (`parameter`, `append` or `header`), skipping it when
     * omitted. [value] writes a plain value; a JSON-encoded one is written by the module's
     * `json`, falling back to its default's JSON text.
     */
    private fun CodeWriter.send(parameter: Parameter, function: String, file: KotlinFile, value: (String) -> String) {
        val name = KotlinSyntax.identifier(parameter.name)
        val wireName = KotlinSyntax.string(parameter.wireName)
        if (parameter.encoding == ParameterEncoding.REPEATED) {
            val element = (parameter.type as TypeRef.ListOf).element
            val value = if (isEnum(element)) "it.value" else if (element == TypeRef.STRING) "it" else "it.toString()"
            line("$name${if (parameter.isOmittable) "?" else ""}.forEach { $function($wireName, $value) }")
            return
        }
        if (parameter.encoding == ParameterEncoding.BRACKETS) {
            // Form fields are appended to the form's builder, query parameters to the URL's.
            val receiver = if (function == "append") "" else "url.parameters."
            file.import("${layout.dtoPackage}.appendBrackets")
            file.import("kotlinx.serialization.json.encodeToJsonElement")
            if (parameter.isOmittable) {
                line("$name?.let { ${receiver}appendBrackets($wireName, json.encodeToJsonElement(it)) }")
            } else {
                line("${receiver}appendBrackets($wireName, json.encodeToJsonElement($name))")
            }
            return
        }
        if (parameter.encoding == ParameterEncoding.JSON) {
            file.import("kotlinx.serialization.encodeToString")
            val default = parameter.default
            when {
                parameter.required -> line("$function($wireName, json.encodeToString($name))")
                default != null -> line("$function($wireName, $name?.let { json.encodeToString(it) } ?: ${KotlinSyntax.string(default.value)})")
                else -> line("$name?.let { $function($wireName, json.encodeToString(it)) }")
            }
            return
        }
        val pattern = parameter.pattern
        val write: (String) -> String = if (pattern == null) value else { variable ->
            file.import("java.time.format.DateTimeFormatter")
            "$variable.format(DateTimeFormatter.ofPattern(${KotlinSyntax.string(pattern)}))"
        }
        if (parameter.isOmittable) {
            line("$name?.let { $function($wireName, ${write("it")}) }")
        } else {
            line("$function($wireName, ${write(name)})")
        }
    }

    /**
     * Sends [cookies] as one `Cookie` header, percent-encoding their values: Ktor's own cookie
     * encoding writes spaces as `+`, which servers read literally.
     */
    private fun CodeWriter.cookieHeader(cookies: List<Parameter>, file: KotlinFile) {
        file.import("io.ktor.client.request.header")
        file.import("io.ktor.http.HttpHeaders")
        file.import("io.ktor.http.encodeURLParameter")
        val pairs = cookies.map { parameter ->
            val name = KotlinSyntax.identifier(parameter.name)
            val prefix = KotlinSyntax.string("${parameter.wireName}=")
            val default = parameter.default
            when (parameter.encoding) {
                ParameterEncoding.JSON -> {
                    file.import("kotlinx.serialization.encodeToString")
                    when {
                        parameter.required -> "$prefix + json.encodeToString($name).encodeURLParameter()"
                        default != null -> "$prefix + ($name?.let { json.encodeToString(it) } ?: ${KotlinSyntax.string(default.value)}).encodeURLParameter()"
                        else -> "$name?.let { $prefix + json.encodeToString(it).encodeURLParameter() }"
                    }
                }
                ParameterEncoding.PLAIN ->
                    if (parameter.isOmittable) "$name?.let { $prefix + ${formValue(parameter, "it")}.encodeURLParameter() }"
                    else "$prefix + ${formValue(parameter, name)}.encodeURLParameter()"
                ParameterEncoding.BRACKETS, ParameterEncoding.REPEATED -> error("Cookie parameter '${parameter.name}' can only be plain or JSON")
            }
        }
        if (pairs.size == 1 && !cookies.single().isOmittable) {
            line("header(HttpHeaders.Cookie, ${pairs.single()})")
        } else {
            line("header(HttpHeaders.Cookie, listOfNotNull(${pairs.joinToString(", ")}).joinToString(\"; \"))")
        }
    }

    /** Form and header values must be strings; enums send their wire value and lists are comma-separated. */
    private fun formValue(parameter: Parameter, value: String): String = when (val type = parameter.type) {
        is TypeRef.ListOf -> joined(type, value)
        TypeRef.STRING -> value
        else -> if (isEnum(type)) "$value.value" else "$value.toString()"
    }

    /** Ktor formats query and header values itself; enums send their wire value and lists are comma-separated. */
    private fun queryValue(parameter: Parameter, value: String): String = when (val type = parameter.type) {
        is TypeRef.ListOf -> joined(type, value)
        else -> if (isEnum(type)) "$value.value" else value
    }

    private fun joined(type: TypeRef.ListOf, value: String): String =
        if (isEnum(type.element)) "$value.joinToString(\",\") { it.value }" else "$value.joinToString(\",\")"

    private fun isEnum(type: TypeRef): Boolean = type is TypeRef.Named && spec.resolve(type) is EnumType

    /** A Kotlin literal for [literal] as a value of [type]. */
    private fun literal(type: TypeRef, literal: DefaultValue.Literal, file: KotlinFile): String = when (type) {
        is TypeRef.Named -> {
            val enum = spec.resolve(type) as EnumType
            "${layout.render(type, file)}.${enum.values.first { it.wireValue == literal.value }.name}"
        }
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> KotlinSyntax.string(literal.value)
            PrimitiveKind.INT64 -> "${literal.value}L"
            PrimitiveKind.FLOAT32 -> "${literal.value}f"
            else -> literal.value
        }
        is TypeRef.ListOf, is TypeRef.MapOf -> error("Collections can't have a literal default")
    }

    /** A header value as a string template over the API parameters it names. */
    private fun template(value: String): String {
        val literals = PLACEHOLDER.split(value).map(KotlinSyntax::stringContent)
        val placeholders = PLACEHOLDER.findAll(value).map { "\${${KotlinSyntax.identifier(it.groupValues[1])}}" }.toList()
        return "\"" + literals.zip(placeholders + "").joinToString("") { (literal, placeholder) -> literal + placeholder } + "\""
    }

    /** The path as a string template, relative to the module's base path. */
    private fun endpoint(operation: Operation): String {
        val names = (spec.parameters.filter { it.location == ParameterLocation.PATH } + operation.pathParameters)
            .associate { it.wireName to KotlinSyntax.identifier(it.name) }
        val template = PLACEHOLDER.split(operation.path).map(KotlinSyntax::stringContent)
            .zip(PLACEHOLDER.findAll(operation.path).map { "\${${names.getValue(it.groupValues[1])}}" }.toList() + "")
            .joinToString("") { (literal, placeholder) -> literal + placeholder }
        return "\"$template\""
    }

    private fun roles(operation: Operation, file: KotlinFile): String {
        if (operation.roles.isEmpty()) return "emptySet()"
        file.import(layout.roles)
        val type = layout.roles.substringAfterLast('.')
        return operation.roles.joinToString(", ", "setOf(", ")") { "$type.$it" }
    }

    private fun CodeWriter.doc(operation: Operation) {
        val documented = buildList {
            addAll(operation.pathParameters.map { DocumentedInput(KotlinSyntax.identifier(it.name), it.description, it) })
            operation.body?.let { add(DocumentedInput(BODY, it.description, null)) }
            addAll(operation.parameters.filter { it.location != ParameterLocation.PATH }.map { DocumentedInput(KotlinSyntax.identifier(it.name), it.description, it) })
            operation.auth?.let { auth -> add(DocumentedInput(AUTH, layout.authDescription(auth), null)) }
        }
        val hasInputDocs = documented.any { it.description != null || it.parameter?.valueDescriptions?.isNotEmpty() == true }
        if (operation.summary == null && !hasInputDocs) return

        line("/**")
        operation.summary?.let { line(" * ${KotlinSyntax.docText(it)}") }
        if (hasInputDocs) {
            if (operation.summary != null) line(" *")
            for (input in documented) {
                input.description?.let { line(" * @param ${input.name} ${KotlinSyntax.docText(it)}") }
                valueDocs(input, input.description != null)
            }
        }
        line(" */")
    }

    private fun CodeWriter.valueDocs(input: DocumentedInput, hasDescription: Boolean) {
        val parameter = input.parameter ?: return
        val enum = (parameter.type as? TypeRef.Named)?.let { spec.resolve(it) } as? EnumType ?: return
        if (parameter.valueDescriptions.isEmpty()) return
        line(if (hasDescription) " *  Accepted values:" else " * @param ${input.name} accepted values:")
        val namesByWireValue = enum.values.associate { it.wireValue to it.name }
        for ((wireValue, description) in parameter.valueDescriptions) {
            val valueName = namesByWireValue[wireValue] ?: continue
            line(" *   - [${enum.name}.$valueName]: ${KotlinSyntax.docText(description)}")
        }
    }

    private data class DocumentedInput(val name: String, val description: String?, val parameter: Parameter?)

    private companion object {
        const val BODY = "body"
        const val AUTH = "auth"
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
