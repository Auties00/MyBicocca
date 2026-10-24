package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Emits a module as a class of async methods. Required inputs are positional (path
 * parameters, the body, then the rest in order); optional ones go in a trailing `options`
 * object, as is idiomatic in TypeScript. API parameters are read from `this.parameters`.
 */
internal class TsServiceEmitter(
    private val spec: ApiSpec,
    private val layout: TsLayout,
    private val codecs: TsCodecs,
    private val scrapers: TsScrapeEmitter,
) {
    fun emit(module: ApiModule, file: TsFile) {
        file.import(layout.client, layout.clientModule)
        file.body.block("export class ${layout.serviceName(module)} {") {
            if (spec.parameters.isEmpty()) {
                line("constructor(private readonly client: ${layout.client}) {}")
            } else {
                file.import(layout.parameters, layout.moduleOf(layout.parametersFile))
                commaListOrLine(
                    "constructor(",
                    listOf("private readonly client: ${layout.client}", "private readonly parameters: ${layout.parameters}"),
                    ") {}",
                    MAX_LINE_LENGTH,
                    trailingComma = true,
                )
            }
            for (operation in module.operations) {
                line()
                method(module, operation, file)
            }
        }
    }

    private fun CodeWriter.method(module: ApiModule, operation: Operation, file: TsFile) {
        val nonPath = operation.parameters.filter { it.location != ParameterLocation.PATH }
        val optional = nonPath.filterNot { it.required }
        val auth = operation.auth
        val positional = buildList {
            operation.pathParameters.forEach { add("${TsSyntax.variable(it.name)}: ${layout.render(it.type, file)}") }
            when (val body = operation.body) {
                is RequestBody.Json -> add("$BODY: ${layout.render(body.type, file)}")
                RequestBody.EmptyObject, null -> Unit
            }
            nonPath.filter { it.required }.forEach { add("${TsSyntax.variable(it.name)}: ${layout.render(it.type, file)}") }
            if (auth != null && auth.required) add("$AUTH: ${layout.authType(auth, file)}")
        }
        val optionalAuth = auth?.takeUnless { it.required }
        val options = if (optional.isEmpty() && optionalAuth == null) null else nested {
            block("$OPTIONS: {", "} = {}") {
                optional.forEach { line("${TsSyntax.propertyKey(it.name)}?: ${layout.render(it.type, file)} | null;") }
                optionalAuth?.let { line("$AUTH?: ${layout.authType(it, file)} | null;") }
            }
        }

        documentation(operation, optional.toSet())
        val returnType = when (val response = operation.response) {
            ResponseBody.None -> "void"
            ResponseBody.Binary -> "Uint8Array"
            ResponseBody.Text -> "string"
            is ResponseBody.Json -> layout.render(response.type, file)
            is ResponseBody.ScriptVariable -> layout.render(response.type, file)
            is ResponseBody.Html -> layout.render(response.type, file)
        }
        val parameters = positional + listOfNotNull(options)
        val header = "): Promise<$returnType> {"
        commaListOrLine("async ${operation.name}(", parameters, header, MAX_LINE_LENGTH, trailingComma = true)
        indent {
            val (call, reading) = when (val response = operation.response) {
                ResponseBody.None -> "await this.client.send" to emptyList()
                ResponseBody.Binary -> "return this.client.bytes" to emptyList()
                ResponseBody.Text -> "return this.client.text" to emptyList()
                is ResponseBody.Json -> when (val type = response.type) {
                    is TypeRef.ListOf -> "return this.client.jsonList" to listOf(codecs.decoder(type.element, file))
                    else -> "return this.client.json" to listOf(codecs.decoder(type, file))
                }
                is ResponseBody.ScriptVariable ->
                    "return this.client.script" to listOf(TsSyntax.string(response.variable), codecs.decoder(response.type, file))
                is ResponseBody.Html -> {
                    val function = scrapers.responseFunction(module, operation)
                    file.import(function, layout.moduleOf(scrapers.fileName))
                    "return this.client.html" to listOf(function)
                }
            }
            val request = nested { block("{", "}") { requestFields(module, operation, optional.toSet(), file) } }
            val roles = if (spec.usesRoles) roles(operation) else null
            commaList("$call(", listOfNotNull(request, roles) + reading, ");", trailingComma = true)
        }
        line("}")
    }

    private fun CodeWriter.requestFields(module: ApiModule, operation: Operation, optional: Set<Parameter>, file: TsFile) {
        val apiParameters = spec.parameters.groupBy { it.location }
        line("method: ${TsSyntax.string(operation.method.name)},")
        line("path: ${path(module, operation, file)},")
        val fixedQuery = operation.query.map { (name, value) -> "[${TsSyntax.string(name)}, ${TsSyntax.string(value)}]" }
        val query = apiParameters[ParameterLocation.QUERY].orEmpty() + operation.queryParameters
        if (fixedQuery.isNotEmpty() || query.isNotEmpty()) lines("query: ${pairs(query, optional, file, fixedQuery)},")
        val fixedForm = operation.form.map { (name, value) -> "[${TsSyntax.string(name)}, ${TsSyntax.string(value)}]" }
        val (files, fields) = operation.formParameters.partition { it.type == TypeRef.Primitive(PrimitiveKind.FILE) }
        if (fixedForm.isNotEmpty() || fields.isNotEmpty()) lines("form: ${pairs(fields, optional, file, fixedForm)},")
        if (files.isNotEmpty()) lines("files: ${array(files.map { "[${TsSyntax.string(it.wireName)}, ${valueOf(it, optional, file)}]" })},")
        if (operation.multipart) line("multipart: true,")
        val headers = operation.headers.map { (name, value) -> "[${TsSyntax.string(name)}, ${template(value)}]" } +
            (apiParameters[ParameterLocation.HEADER].orEmpty() + operation.headerParameters).map { "[${TsSyntax.string(it.wireName)}, ${valueOf(it, optional, file)}]" }
        if (headers.isNotEmpty()) lines("headers: ${array(headers)},")
        (apiParameters[ParameterLocation.COOKIE].orEmpty() + operation.cookieParameters).takeIf { it.isNotEmpty() }?.let { lines("cookies: ${pairs(it, optional, file)},") }
        when (val body = operation.body) {
            is RequestBody.Json -> line("body: ${codecs.encode(BODY, body.type, null, false, file)},")
            RequestBody.EmptyObject -> line("body: {},")
            null -> Unit
        }
        operation.auth?.let { line(if (it.required) "auth: $AUTH," else "auth: $OPTIONS.$AUTH,") }
        operation.server?.let { server ->
            file.import(layout.server, layout.moduleOf(layout.serverFile))
            line("server: ${layout.server}.$server,")
        }
        val envelope = (operation.response as? ResponseBody.Json)?.envelope.orEmpty()
        if (envelope.isNotEmpty()) line("envelope: ${envelope.joinToString(", ", "[", "]") { TsSyntax.string(it) }},")
    }

    /** A header value; one naming API parameters is a template literal over them. */
    private fun template(value: String): String {
        if (PLACEHOLDER.find(value) == null) return TsSyntax.string(value)
        val literals = PLACEHOLDER.split(value).map(TsSyntax::templateContent)
        val placeholders = PLACEHOLDER.findAll(value).map { "\${this.parameters.${it.groupValues[1]}}" }.toList()
        return "`" + literals.zip(placeholders + "").joinToString("") { (literal, placeholder) -> literal + placeholder } + "`"
    }

    /** Name/value pairs as an array literal, wrapped one per line when there are several. */
    private fun CodeWriter.pairs(parameters: List<Parameter>, optional: Set<Parameter>, file: TsFile, fixed: List<String> = emptyList()): String =
        array(fixed + parameters.map { pair(it, optional, file) })

    /** A name/value pair, or for a bracket-encoded or repeated parameter the spread of its fields. */
    private fun pair(parameter: Parameter, optional: Set<Parameter>, file: TsFile): String {
        if (parameter.encoding == ParameterEncoding.REPEATED) {
            layout.importRuntime(file, "repeated")
            val value = if (parameter in optional) "$OPTIONS.${parameter.name}" else TsSyntax.variable(parameter.name)
            return "...repeated(${TsSyntax.string(parameter.wireName)}, $value)"
        }
        if (parameter.encoding != ParameterEncoding.BRACKETS) return "[${TsSyntax.string(parameter.wireName)}, ${valueOf(parameter, optional, file)}]"
        layout.importRuntime(file, "brackets")
        val value = if (parameter in optional) "$OPTIONS.${parameter.name}" else TsSyntax.variable(parameter.name)
        val fields = "brackets(${TsSyntax.string(parameter.wireName)}, ${codecs.encode(value, parameter.type, null, false, file)})"
        return if (parameter in optional) "...($value == null ? [] : $fields)" else "...$fields"
    }

    private fun CodeWriter.array(items: List<String>): String =
        if (items.size <= INLINE_ITEMS) items.joinToString(", ", "[", "]") else nested { commaList("[", items, "]", trailingComma = true) }

    /**
     * The expression of a parameter's value; an optional one falls back to its default, if
     * any, and a JSON-encoded one is written as JSON text.
     */
    private fun valueOf(parameter: Parameter, optional: Set<Parameter>, file: TsFile): String {
        val value = when {
            parameter in spec.parameters -> "this.parameters.${parameter.name}"
            parameter in optional -> "$OPTIONS.${parameter.name}"
            else -> TsSyntax.variable(parameter.name)
        }
        val default = parameter.default
        if (parameter.encoding == ParameterEncoding.JSON) {
            val json = "JSON.stringify(${codecs.encode(value, parameter.type, null, false, file)})"
            if (parameter !in optional) return json
            return "$value == null ? ${default?.let { TsSyntax.string(it.value) } ?: "undefined"} : $json"
        }
        parameter.pattern?.let { pattern ->
            val formatted = "${codecs.runtime(file, "encodeTemporal")}($value, ${TsSyntax.string(pattern)})"
            return if (parameter in optional) "$value == null ? undefined : $formatted" else formatted
        }
        if (parameter !in optional || default == null) return value
        return "$value ?? ${layout.literal(parameter.type, default)}"
    }

    /** The full path as a template literal, with path parameters as encoded segments, or as given when raw. */
    private fun path(module: ApiModule, operation: Operation, file: TsFile): String {
        val names = spec.parameters.filter { it.location == ParameterLocation.PATH }.associate { it.wireName to "this.parameters.${it.name}" } +
            operation.pathParameters.associate { it.wireName to TsSyntax.variable(it.name) }
        val raw = operation.pathParameters.filter { it.raw }.map { it.wireName }.toSet()
        val path = module.basePath.trimEnd('/') + operation.path
        if (PLACEHOLDER.find(path) == null) return TsSyntax.string(path)
        val literals = PLACEHOLDER.split(path).map(TsSyntax::templateContent)
        val placeholders = PLACEHOLDER.findAll(path).map { match ->
            val wireName = match.groupValues[1]
            if (wireName in raw) {
                "\${${names.getValue(wireName)}}"
            } else {
                layout.importRuntime(file, "pathSegment")
                "\${pathSegment(${names.getValue(wireName)})}"
            }
        }.toList()
        return "`" + literals.zip(placeholders + "").joinToString("") { (literal, placeholder) -> literal + placeholder } + "`"
    }

    private fun roles(operation: Operation): String = operation.roles.joinToString(", ", "[", "]") { TsSyntax.string(it) }

    private fun CodeWriter.documentation(operation: Operation, optional: Set<Parameter>) {
        val entries = buildList {
            operation.pathParameters.forEach { add(docName(it, optional) to it) }
            operation.body?.let { add(BODY to null) }
            operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { add(docName(it, optional) to it) }
            operation.auth?.let { add((if (it.required) AUTH else "$OPTIONS.$AUTH") to null) }
        }.filter { (name, parameter) ->
            inputDescription(operation, name, parameter) != null || parameter?.valueDescriptions?.isNotEmpty() == true
        }
        if (operation.summary == null && entries.isEmpty()) return

        line("/**")
        operation.summary?.let { line(" * ${TsSyntax.docText(it)}") }
        if (operation.summary != null && entries.isNotEmpty()) line(" *")
        for ((name, parameter) in entries) {
            val description = inputDescription(operation, name, parameter)
            line(" * @param $name ${description?.let(TsSyntax::docText) ?: "One of the accepted values below."}")
            parameter?.let { valueDocs(it) }
        }
        line(" */")
    }

    private fun inputDescription(operation: Operation, name: String, parameter: Parameter?): String? = when {
        parameter != null -> parameter.description
        name.endsWith(AUTH) -> operation.auth?.let(layout::authDescription)
        else -> operation.body?.description
    }

    private fun docName(parameter: Parameter, optional: Set<Parameter>): String =
        if (parameter in optional) "$OPTIONS.${parameter.name}" else TsSyntax.variable(parameter.name)

    private fun CodeWriter.valueDocs(parameter: Parameter) {
        val enum = (parameter.type as? TypeRef.Named)?.let { spec.resolve(it) } as? EnumType ?: return
        if (parameter.valueDescriptions.isEmpty()) return
        val namesByWireValue = enum.values.associate { it.wireValue to it.name }
        line(" *   Accepted values:")
        for ((wireValue, description) in parameter.valueDescriptions) {
            val name = namesByWireValue[wireValue] ?: continue
            line(" *   - {@link ${enum.name}.$name}: ${TsSyntax.docText(description)}")
        }
    }

    private companion object {
        const val BODY = "body"
        const val OPTIONS = "options"
        const val AUTH = "auth"
        const val INLINE_ITEMS = 2
        const val MAX_LINE_LENGTH = 100
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
