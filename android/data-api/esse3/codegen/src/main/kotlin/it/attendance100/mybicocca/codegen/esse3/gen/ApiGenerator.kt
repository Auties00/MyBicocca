package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.Dictionary
import it.attendance100.mybicocca.codegen.esse3.renderTranslated
import it.attendance100.mybicocca.codegen.esse3.sanitizeParamName
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedOperation
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import it.attendance100.mybicocca.codegen.esse3.spec.ResolvedType
import it.attendance100.mybicocca.codegen.esse3.spec.TypeMapping
import java.io.File

object ApiGenerator {

    private const val API_PACKAGE = "it.attendance100.mybicocca.data.api.esse3"
    private const val DTO_PACKAGE = "it.attendance100.mybicocca.data.dto.esse3"
    private const val PREFIX = "Esse3"

    fun generate(spec: ParsedSpec, outputDir: File, dictionary: Dictionary) {
        if (spec.operations.isEmpty()) return

        val className = dictionary.translate("${PREFIX}${spec.specName}Api")
        val fileName = "$className.kt"
        val file = File(outputDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("package $API_PACKAGE")
        sb.appendLine()

        val imports = collectImports(spec, dictionary)
        for (imp in imports.sorted()) {
            sb.appendLine("import $imp")
        }
        sb.appendLine()

        sb.appendLine("class $className(")
        sb.appendLine("    client: HttpClient,")
        sb.appendLine("    json: Json")
        sb.appendLine(") : Esse3AbstractApi(client, json, \"${spec.basePath}\") {")
        sb.appendLine()

        for ((index, op) in spec.operations.withIndex()) {
            if (index > 0) sb.appendLine()
            generateFunction(sb, op, dictionary)
        }

        sb.appendLine("}")

        file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }

    private fun collectImports(spec: ParsedSpec, dictionary: Dictionary): Set<String> {
        val imports = mutableSetOf<String>()
        imports.add("io.ktor.client.HttpClient")
        imports.add("io.ktor.client.request.parameter")
        imports.add("io.ktor.client.request.setBody")
        imports.add("io.ktor.http.ContentType")
        imports.add("io.ktor.http.contentType")
        imports.add("kotlinx.serialization.json.Json")

        val hasPermissions = spec.operations.any { it.permissions.isNotEmpty() }
        if (hasPermissions) {
            imports.add("$DTO_PACKAGE.Esse3PermissionLevel")
        }

        val referencedTypes = mutableSetOf<String>()
        for (op in spec.operations) {
            collectReferences(op.responseType, referencedTypes)
            op.bodyParam?.let { collectReferences(it.type, referencedTypes) }
        }

        for (typeName in referencedTypes) {
            val translated = dictionary.translate("${PREFIX}$typeName")
            imports.add("$DTO_PACKAGE.$translated")
        }

        val hasDateParams = spec.operations.any { op ->
            op.pathParams.any { isDateSimpleType(it.type) } ||
                    op.queryParams.any { isDateSimpleType(it.type) }
        }
        if (hasDateParams) {
            imports.add("java.time.LocalDate")
            imports.add("java.time.LocalDateTime")
        }

        return imports
    }

    private fun isDateSimpleType(type: ResolvedType): Boolean {
        return type is ResolvedType.Simple && TypeMapping.isDateType(type.kotlinType)
    }

    private fun collectReferences(type: ResolvedType?, refs: MutableSet<String>) {
        when (type) {
            is ResolvedType.Reference -> refs.add(type.definitionName)
            is ResolvedType.ListOf -> collectReferences(type.inner, refs)
            else -> {}
        }
    }

    private fun generateFunction(sb: StringBuilder, op: ParsedOperation, dictionary: Dictionary) {
        val originalName = op.operationId.replaceFirstChar { it.lowercaseChar() }
        val functionName = dictionary.translate(originalName).replaceFirstChar { it.lowercaseChar() }
        val returnType = resolveReturnType(op, dictionary)

        val params = buildParameterList(op, dictionary)

        sb.append("    suspend fun $functionName(")
        if (params.isNotEmpty()) {
            sb.appendLine()
            for ((index, param) in params.withIndex()) {
                sb.append("        $param")
                if (index < params.size - 1) sb.appendLine(",")
                else sb.appendLine()
            }
            sb.append("    )")
        } else {
            sb.append(")")
        }

        if (returnType != null) {
            sb.append(": $returnType")
        }

        sb.appendLine(" {")

        val endpoint = buildEndpointString(op, dictionary)

        val permissionSet = if (op.permissions.isNotEmpty()) {
            val perms = op.permissions.joinToString(", ") { "Esse3PermissionLevel.$it" }
            "setOf($perms)"
        } else {
            "emptySet()"
        }

        val methodCall = when {
            returnType == null -> generateVoidCall(op, endpoint, permissionSet, dictionary)
            op.isListResponse -> generateListCall(op, endpoint, permissionSet, returnType, dictionary)
            else -> generateTypedCall(op, endpoint, permissionSet, returnType, dictionary)
        }

        sb.append(methodCall)
        sb.appendLine("    }")
    }

    private fun resolveReturnType(op: ParsedOperation, dictionary: Dictionary): String? {
        val type = op.responseType ?: return null
        if (type is ResolvedType.Simple && type.kotlinType == "String") return "String"
        return type.renderTranslated(dictionary, PREFIX)
    }

    private fun buildParameterList(op: ParsedOperation, dictionary: Dictionary): List<String> {
        val params = mutableListOf<String>()

        for (param in op.pathParams) {
            val kotlinType = param.type.renderTranslated(dictionary, PREFIX)
            params.add("${resolveParamName(param.name, dictionary)}: $kotlinType")
        }

        if (op.bodyParam != null) {
            val kotlinType = op.bodyParam.type.renderTranslated(dictionary, PREFIX)
            params.add("body: $kotlinType")
        }

        val requiredQuery = op.queryParams.filter { it.required }
        val optionalQuery = op.queryParams.filter { !it.required }

        for (param in requiredQuery) {
            val kotlinType = param.type.renderTranslated(dictionary, PREFIX)
            params.add("${resolveParamName(param.name, dictionary)}: $kotlinType")
        }

        for (param in optionalQuery) {
            val kotlinType = param.type.renderTranslated(dictionary, PREFIX)
            val nullableType = if (kotlinType.endsWith("?")) kotlinType else "$kotlinType?"
            params.add("${resolveParamName(param.name, dictionary)}: $nullableType = null")
        }

        return params
    }

    private fun buildEndpointString(op: ParsedOperation, dictionary: Dictionary): String {
        var path = op.path
        for (param in op.pathParams) {
            val kotlinName = resolveParamName(param.name, dictionary)
            path = path.replace("{${param.name}}", "\${${kotlinName}}")
        }
        return path
    }

    private fun generateVoidCall(op: ParsedOperation, endpoint: String, permissionSet: String, dictionary: Dictionary): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase()
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        sb.appendLine("        val response = execute${method.replaceFirstChar { it.uppercaseChar() }}(\"$endpoint\")${if (hasBlock) " {" else ""}")

        if (hasBlock) {
            generateBlockContent(sb, op, dictionary)
            sb.appendLine("        }")
        }

        sb.appendLine("        ensureSuccess(response, $permissionSet)")
        return sb.toString()
    }

    private fun generateListCall(op: ParsedOperation, endpoint: String, permissionSet: String, returnType: String, dictionary: Dictionary): String {
        val sb = StringBuilder()
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        val innerType = returnType.removePrefix("List<").removeSuffix(">")

        sb.append("        return executeJsonGetList<$innerType>(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, dictionary)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun generateTypedCall(op: ParsedOperation, endpoint: String, permissionSet: String, returnType: String, dictionary: Dictionary): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        sb.append("        return executeJson$method<$returnType>(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, dictionary)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun generateBlockContent(sb: StringBuilder, op: ParsedOperation, dictionary: Dictionary) {
        if (op.bodyParam != null) {
            sb.appendLine("            contentType(ContentType.Application.Json)")
            sb.appendLine("            setBody(body)")
        }

        for (param in op.queryParams) {
            val paramName = resolveParamName(param.name, dictionary)
            if (param.required) {
                sb.appendLine("            parameter(\"${param.name}\", $paramName)")
            } else {
                sb.appendLine("            $paramName?.let { parameter(\"${param.name}\", it) }")
            }
        }
    }

    private fun resolveParamName(name: String, dictionary: Dictionary): String {
        return sanitizeParamName(dictionary.translate(name))
    }
}
