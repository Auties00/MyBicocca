package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.Glossary
import it.attendance100.mybicocca.codegen.esse3.renderTranslated
import it.attendance100.mybicocca.codegen.esse3.sanitizeParamName
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedOperation
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import it.attendance100.mybicocca.codegen.esse3.spec.ResolvedType
import it.attendance100.mybicocca.codegen.esse3.spec.TypeMapping
import java.io.File

object ApiGenerator {

    private const val PREFIX = "Esse3"

    fun generate(spec: ParsedSpec, outputDir: File, glossary: Glossary, basePackage: String) {
        if (spec.operations.isEmpty()) return

        val apiPackage = "$basePackage.api.esse3"
        val dtoPackage = "$basePackage.dto.esse3"

        val className = glossary.translate("${PREFIX}${spec.specName}Api")
        val fileName = "$className.kt"
        val file = File(outputDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("package $apiPackage")
        sb.appendLine()

        val imports = collectImports(spec, glossary, dtoPackage)
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
            generateFunction(sb, op, glossary)
        }

        sb.appendLine("}")

        file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }

    private fun collectImports(spec: ParsedSpec, glossary: Glossary, dtoPackage: String): Set<String> {
        val imports = mutableSetOf<String>()
        imports.add("io.ktor.client.HttpClient")
        imports.add("io.ktor.client.request.parameter")
        imports.add("io.ktor.client.request.setBody")
        imports.add("io.ktor.http.ContentType")
        imports.add("io.ktor.http.contentType")
        imports.add("kotlinx.serialization.json.Json")

        val hasPermissions = spec.operations.any { it.permissions.isNotEmpty() }
        if (hasPermissions) {
            imports.add("$dtoPackage.Esse3PermissionLevel")
        }

        val referencedTypes = mutableSetOf<String>()
        for (op in spec.operations) {
            collectReferences(op.responseType, referencedTypes)
            op.bodyParam?.let { collectReferences(it.type, referencedTypes) }
        }

        for (typeName in referencedTypes) {
            val translated = glossary.translate("${PREFIX}$typeName")
            imports.add("$dtoPackage.$translated")
        }

        val hasDateParams = spec.operations.any { op ->
            op.pathParams.any { isDateSimpleType(it.type) } ||
                    op.queryParams.any { isDateSimpleType(it.type) }
        }
        if (hasDateParams) {
            imports.add("java.time.LocalDate")
            imports.add("java.time.LocalDateTime")
        }

        val hasBinaryResponse = spec.operations.any { isBinaryResponse(it) }
        if (hasBinaryResponse) {
            imports.add("io.ktor.utils.io.ByteReadChannel")
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

    private fun generateFunction(sb: StringBuilder, op: ParsedOperation, glossary: Glossary) {
        val originalName = op.operationId.replaceFirstChar { it.lowercaseChar() }
        val functionName = glossary.translate(originalName).replaceFirstChar { it.lowercaseChar() }
        val returnType = resolveReturnType(op, glossary)

        val params = buildParameterList(op, glossary)

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

        val endpoint = buildEndpointString(op, glossary)

        val permissionSet = if (op.permissions.isNotEmpty()) {
            val perms = op.permissions.joinToString(", ") { "Esse3PermissionLevel.$it" }
            "setOf($perms)"
        } else {
            "emptySet()"
        }

        val methodCall = when {
            returnType == null -> generateVoidCall(op, endpoint, permissionSet, glossary)
            isBinaryResponse(op) -> generateStreamCall(op, endpoint, permissionSet, glossary)
            op.responseType is ResolvedType.ListOf -> generateListCall(op, endpoint, permissionSet, returnType, glossary)
            else -> generateTypedCall(op, endpoint, permissionSet, returnType, glossary)
        }

        sb.append(methodCall)
        sb.appendLine("    }")
    }

    private fun resolveReturnType(op: ParsedOperation, glossary: Glossary): String? {
        val type = op.responseType ?: return null
        if (type is ResolvedType.Simple && type.kotlinType == "String") return "String"
        return type.renderTranslated(glossary, PREFIX)
    }

    private fun buildParameterList(op: ParsedOperation, glossary: Glossary): List<String> {
        val params = mutableListOf<String>()

        for (param in op.pathParams) {
            val kotlinType = param.type.renderTranslated(glossary, PREFIX)
            params.add("${resolveParamName(param.name, glossary)}: $kotlinType")
        }

        if (op.bodyParam != null) {
            val kotlinType = op.bodyParam.type.renderTranslated(glossary, PREFIX)
            params.add("body: $kotlinType")
        }

        val requiredQuery = op.queryParams.filter { it.required }
        val optionalQuery = op.queryParams.filter { !it.required }

        for (param in requiredQuery) {
            val kotlinType = param.type.renderTranslated(glossary, PREFIX)
            params.add("${resolveParamName(param.name, glossary)}: $kotlinType")
        }

        for (param in optionalQuery) {
            val kotlinType = param.type.renderTranslated(glossary, PREFIX)
            val nullableType = if (kotlinType.endsWith("?")) kotlinType else "$kotlinType?"
            params.add("${resolveParamName(param.name, glossary)}: $nullableType = null")
        }

        return params
    }

    private fun buildEndpointString(op: ParsedOperation, glossary: Glossary): String {
        var path = op.path
        for (param in op.pathParams) {
            val kotlinName = resolveParamName(param.name, glossary)
            path = path.replace("{${param.name}}", "\${${kotlinName}}")
        }
        return path
    }

    private fun generateVoidCall(op: ParsedOperation, endpoint: String, permissionSet: String, glossary: Glossary): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase()
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        sb.appendLine("        val response = execute${method.replaceFirstChar { it.uppercaseChar() }}(\"$endpoint\")${if (hasBlock) " {" else ""}")

        if (hasBlock) {
            generateBlockContent(sb, op, glossary)
            sb.appendLine("        }")
        }

        sb.appendLine("        ensureSuccess(response, $permissionSet)")
        return sb.toString()
    }

    private fun generateListCall(op: ParsedOperation, endpoint: String, permissionSet: String, returnType: String, glossary: Glossary): String {
        val sb = StringBuilder()
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        val innerType = returnType.removePrefix("List<").removeSuffix(">")

        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }

        sb.append("        return executeJson${method}List<$innerType>(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, glossary)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun isBinaryResponse(op: ParsedOperation): Boolean {
        val type = op.responseType ?: return false
        return type is ResolvedType.Simple && type.kotlinType == "ByteReadChannel"
    }

    private fun generateStreamCall(op: ParsedOperation, endpoint: String, permissionSet: String, glossary: Glossary): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        sb.append("        return executeStream$method(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, glossary)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun generateTypedCall(op: ParsedOperation, endpoint: String, permissionSet: String, returnType: String, glossary: Glossary): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null

        sb.append("        return executeJson$method<$returnType>(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, glossary)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun generateBlockContent(sb: StringBuilder, op: ParsedOperation, glossary: Glossary) {
        if (op.bodyParam != null) {
            sb.appendLine("            contentType(ContentType.Application.Json)")
            sb.appendLine("            setBody(body)")
        }

        for (param in op.queryParams) {
            val paramName = resolveParamName(param.name, glossary)
            if (param.required) {
                sb.appendLine("            parameter(\"${param.name}\", $paramName)")
            } else {
                sb.appendLine("            $paramName?.let { parameter(\"${param.name}\", it) }")
            }
        }
    }

    private fun resolveParamName(name: String, glossary: Glossary): String {
        return sanitizeParamName(glossary.translate(name))
    }
}