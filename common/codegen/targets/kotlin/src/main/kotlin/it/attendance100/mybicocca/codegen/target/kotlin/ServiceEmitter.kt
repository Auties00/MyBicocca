package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
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
 * - `executeJson<Method>List<T>(…)` decodes a list, returning an empty one on HTTP 404;
 * - `executeStream<Method>(…)` returns the body as a `ByteReadChannel`;
 * - `execute<Method>(endpoint) { … }` plus `ensureSuccess(response, roles)` when the body
 *   isn't needed.
 *
 * The trailing block configures the Ktor request (parameters and body).
 */
internal class ServiceEmitter(private val spec: ApiSpec, private val layout: KotlinLayout) {

    fun emit(module: ApiModule, file: KotlinFile) {
        file.import("io.ktor.client.HttpClient")
        file.import("kotlinx.serialization.json.Json")
        file.import(layout.abstractApi)
        file.body.apply {
            line("class ${layout.serviceName(module)}(")
            indent {
                line("client: HttpClient,")
                line("json: Json")
            }
            line(") : ${layout.abstractApi.substringAfterLast('.')}(client, json, ${KotlinSyntax.string(module.basePath)}) {")
            line()
            indent {
                separated(module.operations) { function(it, file) }
            }
            line("}")
        }
    }

    private fun CodeWriter.function(operation: Operation, file: KotlinFile) {
        doc(operation)
        val parameters = signatureParameters(operation, file)
        val returnType = returnType(operation.response, file)
        val returns = returnType?.let { ": $it" }.orEmpty()
        if (parameters.isEmpty()) {
            line("suspend fun ${KotlinSyntax.identifier(operation.name)}()$returns {")
        } else {
            commaList("suspend fun ${KotlinSyntax.identifier(operation.name)}(", parameters, ")$returns {")
        }
        indent { call(operation, file) }
        line("}")
    }

    /** Path parameters, then the body, then the remaining parameters; optional ones default to null. */
    private fun signatureParameters(operation: Operation, file: KotlinFile): List<String> = buildList {
        operation.pathParameters.forEach { add("${KotlinSyntax.identifier(it.name)}: ${layout.render(it.type, file)}") }
        when (val body = operation.body) {
            is RequestBody.Json -> add("$BODY: ${layout.render(body.type, file)}")
            null -> Unit
        }
        operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { parameter ->
            val type = layout.render(parameter.type, file)
            add(
                if (parameter.required) "${KotlinSyntax.identifier(parameter.name)}: $type"
                else "${KotlinSyntax.identifier(parameter.name)}: $type? = null"
            )
        }
    }

    private fun returnType(response: ResponseBody, file: KotlinFile): String? = when (response) {
        ResponseBody.None -> null
        ResponseBody.Binary -> layout.render(TypeRef.Primitive(PrimitiveKind.BINARY), file)
        is ResponseBody.Json -> layout.render(response.type, file)
    }

    private fun CodeWriter.call(operation: Operation, file: KotlinFile) {
        val method = operation.method.name.lowercase().capitalized()
        val endpoint = endpoint(operation)
        val roles = roles(operation, file)
        val hasBlock = operation.body != null || operation.parameters.any { it.location != ParameterLocation.PATH }

        val response = operation.response
        val invocation = when (response) {
            ResponseBody.None -> "val response = execute$method($endpoint)"
            ResponseBody.Binary -> "return executeStream$method($endpoint, $roles)"
            is ResponseBody.Json -> when (val type = response.type) {
                is TypeRef.ListOf -> "return executeJson${method}List<${layout.render(type.element, file)}>($endpoint, $roles)"
                else -> "return executeJson$method<${layout.render(type, file)}>($endpoint, $roles)"
            }
        }
        if (hasBlock) {
            block("$invocation {") { requestConfiguration(operation, file) }
        } else {
            line(invocation)
        }
        if (response == ResponseBody.None) line("ensureSuccess(response, $roles)")
    }

    private fun CodeWriter.requestConfiguration(operation: Operation, file: KotlinFile) {
        if (operation.body is RequestBody.Json) {
            file.import("io.ktor.client.request.setBody")
            file.import("io.ktor.http.ContentType")
            file.import("io.ktor.http.contentType")
            line("contentType(ContentType.Application.Json)")
            line("setBody($BODY)")
        }

        val form = operation.formParameters
        if (form.isNotEmpty()) {
            file.import("io.ktor.client.request.setBody")
            file.import("io.ktor.client.request.forms.FormDataContent")
            file.import("io.ktor.http.parameters")
            block("setBody(FormDataContent(parameters {", "}))") {
                for (parameter in form) {
                    val name = KotlinSyntax.identifier(parameter.name)
                    val wireName = KotlinSyntax.string(parameter.wireName)
                    if (parameter.required) {
                        line("append($wireName, ${formValue(parameter, name)})")
                    } else {
                        line("$name?.let { append($wireName, ${formValue(parameter, "it")}) }")
                    }
                }
            }
        }

        for (parameter in operation.queryParameters) {
            file.import("io.ktor.client.request.parameter")
            val name = KotlinSyntax.identifier(parameter.name)
            val wireName = KotlinSyntax.string(parameter.wireName)
            if (parameter.required) {
                line("parameter($wireName, ${queryValue(parameter, name)})")
            } else {
                line("$name?.let { parameter($wireName, ${queryValue(parameter, "it")}) }")
            }
        }
    }

    /** Form values must be strings; enums send their wire value and lists are comma-separated. */
    private fun formValue(parameter: Parameter, value: String): String = when (val type = parameter.type) {
        is TypeRef.ListOf -> joined(type, value)
        TypeRef.STRING -> value
        else -> if (isEnum(type)) "$value.value" else "$value.toString()"
    }

    /** Ktor formats query values itself; enums send their wire value and lists are comma-separated. */
    private fun queryValue(parameter: Parameter, value: String): String = when (val type = parameter.type) {
        is TypeRef.ListOf -> joined(type, value)
        else -> if (isEnum(type)) "$value.value" else value
    }

    private fun joined(type: TypeRef.ListOf, value: String): String =
        if (isEnum(type.element)) "$value.joinToString(\",\") { it.value }" else "$value.joinToString(\",\")"

    private fun isEnum(type: TypeRef): Boolean = type is TypeRef.Named && spec.resolve(type) is EnumType

    /** The path as a string template, relative to the module's base path. */
    private fun endpoint(operation: Operation): String {
        val names = operation.pathParameters.associate { it.wireName to KotlinSyntax.identifier(it.name) }
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
        val PLACEHOLDER = Regex("""\{([^}]+)}""")
    }
}
