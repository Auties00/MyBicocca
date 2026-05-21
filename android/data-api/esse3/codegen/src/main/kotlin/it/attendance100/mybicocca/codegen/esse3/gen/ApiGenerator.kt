package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.Glossary
import it.attendance100.mybicocca.codegen.esse3.renderTranslated
import it.attendance100.mybicocca.codegen.esse3.sanitizeParamName
import it.attendance100.mybicocca.codegen.esse3.spec.ParameterLocation
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedOperation
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedParameter
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import it.attendance100.mybicocca.codegen.esse3.spec.ResolvedType
import it.attendance100.mybicocca.codegen.esse3.spec.TypeMapping
import java.io.File

object ApiGenerator {

    private const val PREFIX = "Esse3"

    fun generate(
        spec: ParsedSpec,
        outputDir: File,
        glossary: Glossary,
        basePackage: String,
        paramEnumIndex: Map<String, String>,
    ) {
        if (spec.operations.isEmpty()) return

        val apiPackage = "$basePackage.esse3.api"
        val dtoPackage = "$basePackage.esse3.dto"

        val className = glossary.translate("${PREFIX}${spec.specName}Api")
        val fileName = "$className.kt"
        val file = File(outputDir, fileName)

        val sb = StringBuilder()
        sb.appendLine("package $apiPackage")
        sb.appendLine()

        val imports = collectImports(spec, glossary, dtoPackage, paramEnumIndex)
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
            generateFunction(sb, op, glossary, paramEnumIndex, dtoPackage)
        }

        sb.appendLine("}")

        file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }

    private fun collectImports(
        spec: ParsedSpec,
        glossary: Glossary,
        dtoPackage: String,
        paramEnumIndex: Map<String, String>,
    ): Set<String> {
        val imports = mutableSetOf<String>()
        imports.add("io.ktor.client.HttpClient")
        imports.add("io.ktor.client.request.parameter")
        imports.add("kotlinx.serialization.json.Json")

        val hasPermissions = spec.operations.any { it.permissions.isNotEmpty() }
        if (hasPermissions) {
            imports.add("$dtoPackage.Esse3PermissionLevel")
        }

        val hasJsonBody = spec.operations.any { it.bodyParam != null }
        if (hasJsonBody) {
            imports.add("io.ktor.client.request.setBody")
            imports.add("io.ktor.http.ContentType")
            imports.add("io.ktor.http.contentType")
        }

        val hasFormParams = spec.operations.any { it.formParams.isNotEmpty() }
        if (hasFormParams) {
            imports.add("io.ktor.client.request.setBody")
            imports.add("io.ktor.client.request.forms.FormDataContent")
            imports.add("io.ktor.http.parameters")
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

        for (op in spec.operations) {
            for (param in op.queryParams + op.formParams) {
                val enumClass = paramEnumIndex["${op.operationId}.${param.name}"] ?: continue
                imports.add("$dtoPackage.$enumClass")
            }
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

    private fun generateFunction(
        sb: StringBuilder,
        op: ParsedOperation,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
        dtoPackage: String,
    ) {
        val originalName = op.operationId.replaceFirstChar { it.lowercaseChar() }
        val functionName = glossary.translate(originalName).replaceFirstChar { it.lowercaseChar() }
        val returnType = resolveReturnType(op, glossary)

        val params = buildParameterList(op, glossary, paramEnumIndex)

        generateKDoc(sb, op, glossary, paramEnumIndex)

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
            returnType == null -> generateVoidCall(op, endpoint, permissionSet, glossary, paramEnumIndex)
            isBinaryResponse(op) -> generateStreamCall(op, endpoint, permissionSet, glossary, paramEnumIndex)
            op.responseType is ResolvedType.ListOf -> generateListCall(op, endpoint, permissionSet, returnType, glossary, paramEnumIndex)
            else -> generateTypedCall(op, endpoint, permissionSet, returnType, glossary, paramEnumIndex)
        }

        sb.append(methodCall)
        sb.appendLine("    }")
    }

    private fun generateKDoc(
        sb: StringBuilder,
        op: ParsedOperation,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ) {
        val allParams = buildList {
            addAll(op.pathParams)
            if (op.bodyParam != null) add(op.bodyParam)
            addAll(op.formParams)
            addAll(op.queryParams.filter { it.required })
            addAll(op.queryParams.filter { !it.required })
        }
        val hasParamDocs = allParams.any { it.description != null || it.enumValueDocs != null }
        if (op.summary == null && !hasParamDocs) return

        sb.appendLine("    /**")
        if (op.summary != null) {
            sb.appendLine("     * ${singleLine(op.summary)}")
        }
        if (hasParamDocs) {
            if (op.summary != null) sb.appendLine("     *")
            for (param in allParams) {
                val paramName = resolveParamName(param.name, glossary)
                val name = if (param.location == ParameterLocation.BODY) "body" else paramName
                val mainDesc = param.description?.let { singleLine(it) }
                if (mainDesc != null) {
                    sb.appendLine("     * @param $name $mainDesc")
                }
                val enumDocs = param.enumValueDocs
                val enumClass = paramEnumIndex["${op.operationId}.${param.name}"]
                if (enumDocs != null && enumClass != null) {
                    if (mainDesc == null) {
                        sb.appendLine("     * @param $name accepted values:")
                    } else {
                        sb.appendLine("     *  Accepted values:")
                    }
                    for ((raw, doc) in enumDocs) {
                        sb.appendLine("     *   - [$enumClass.${kotlinNameFor(raw)}]: ${singleLine(doc)}")
                    }
                }
            }
        }
        sb.appendLine("     */")
    }

    private fun singleLine(text: String): String {
        return text.replace(Regex("\\s*\\n\\s*"), " ").trim()
    }

    private fun resolveReturnType(op: ParsedOperation, glossary: Glossary): String? {
        val type = op.responseType ?: return null
        if (type is ResolvedType.Simple && type.kotlinType == "String") return "String"
        return type.renderTranslated(glossary, PREFIX)
    }

    private fun buildParameterList(
        op: ParsedOperation,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ): List<String> {
        val params = mutableListOf<String>()

        for (param in op.pathParams) {
            val kotlinType = param.type.renderTranslated(glossary, PREFIX)
            params.add("${resolveParamName(param.name, glossary)}: $kotlinType")
        }

        if (op.bodyParam != null) {
            val kotlinType = op.bodyParam.type.renderTranslated(glossary, PREFIX)
            params.add("body: $kotlinType")
        }

        for (param in op.formParams) {
            val kotlinType = resolveParamKotlinType(op, param, glossary, paramEnumIndex)
            val rendered = if (param.required) kotlinType else "$kotlinType? = null"
            params.add("${resolveParamName(param.name, glossary)}: $rendered")
        }

        val requiredQuery = op.queryParams.filter { it.required }
        val optionalQuery = op.queryParams.filter { !it.required }

        for (param in requiredQuery) {
            val kotlinType = resolveParamKotlinType(op, param, glossary, paramEnumIndex)
            params.add("${resolveParamName(param.name, glossary)}: $kotlinType")
        }

        for (param in optionalQuery) {
            val kotlinType = resolveParamKotlinType(op, param, glossary, paramEnumIndex)
            val nullableType = if (kotlinType.endsWith("?")) kotlinType else "$kotlinType?"
            params.add("${resolveParamName(param.name, glossary)}: $nullableType = null")
        }

        return params
    }

    private fun resolveParamKotlinType(
        op: ParsedOperation,
        param: ParsedParameter,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ): String {
        val enumClass = paramEnumIndex["${op.operationId}.${param.name}"]
        if (enumClass != null) return enumClass
        return param.type.renderTranslated(glossary, PREFIX)
    }

    private fun buildEndpointString(op: ParsedOperation, glossary: Glossary): String {
        var path = op.path
        for (param in op.pathParams) {
            val kotlinName = resolveParamName(param.name, glossary)
            path = path.replace("{${param.name}}", "\${${kotlinName}}")
        }
        return path
    }

    private fun generateVoidCall(
        op: ParsedOperation,
        endpoint: String,
        permissionSet: String,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase()
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null || op.formParams.isNotEmpty()

        sb.appendLine("        val response = execute${method.replaceFirstChar { it.uppercaseChar() }}(\"$endpoint\")${if (hasBlock) " {" else ""}")

        if (hasBlock) {
            generateBlockContent(sb, op, glossary, paramEnumIndex)
            sb.appendLine("        }")
        }

        sb.appendLine("        ensureSuccess(response, $permissionSet)")
        return sb.toString()
    }

    private fun generateListCall(
        op: ParsedOperation,
        endpoint: String,
        permissionSet: String,
        returnType: String,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ): String {
        val sb = StringBuilder()
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null || op.formParams.isNotEmpty()

        val innerType = returnType.removePrefix("List<").removeSuffix(">")

        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }

        sb.append("        return executeJson${method}List<$innerType>(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, glossary, paramEnumIndex)
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

    private fun generateStreamCall(
        op: ParsedOperation,
        endpoint: String,
        permissionSet: String,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null || op.formParams.isNotEmpty()

        sb.append("        return executeStream$method(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, glossary, paramEnumIndex)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun generateTypedCall(
        op: ParsedOperation,
        endpoint: String,
        permissionSet: String,
        returnType: String,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ): String {
        val sb = StringBuilder()
        val method = op.httpMethod.lowercase().replaceFirstChar { it.uppercaseChar() }
        val hasBlock = op.queryParams.isNotEmpty() || op.bodyParam != null || op.formParams.isNotEmpty()

        sb.append("        return executeJson$method<$returnType>(\"$endpoint\", $permissionSet)")

        if (hasBlock) {
            sb.appendLine(" {")
            generateBlockContent(sb, op, glossary, paramEnumIndex)
            sb.appendLine("        }")
        } else {
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun generateBlockContent(
        sb: StringBuilder,
        op: ParsedOperation,
        glossary: Glossary,
        paramEnumIndex: Map<String, String>,
    ) {
        if (op.bodyParam != null) {
            sb.appendLine("            contentType(ContentType.Application.Json)")
            sb.appendLine("            setBody(body)")
        }

        if (op.formParams.isNotEmpty()) {
            sb.appendLine("            setBody(FormDataContent(parameters {")
            for (param in op.formParams) {
                val paramName = resolveParamName(param.name, glossary)
                val isEnum = paramEnumIndex.containsKey("${op.operationId}.${param.name}")
                val isString = !isEnum && (param.type as? ResolvedType.Simple)?.kotlinType == "String"
                if (param.required) {
                    val accessor = when {
                        isEnum -> "$paramName.value"
                        isString -> paramName
                        else -> "$paramName.toString()"
                    }
                    sb.appendLine("                append(\"${param.name}\", $accessor)")
                } else {
                    val nullableAccessor = when {
                        isEnum -> "it.value"
                        isString -> "it"
                        else -> "it.toString()"
                    }
                    sb.appendLine("                $paramName?.let { append(\"${param.name}\", $nullableAccessor) }")
                }
            }
            sb.appendLine("            }))")
        }

        for (param in op.queryParams) {
            val paramName = resolveParamName(param.name, glossary)
            val isEnum = paramEnumIndex.containsKey("${op.operationId}.${param.name}")
            if (param.required) {
                val accessor = if (isEnum) "$paramName.value" else paramName
                sb.appendLine("            parameter(\"${param.name}\", $accessor)")
            } else {
                val nullableAccessor = if (isEnum) "it.value" else "it"
                sb.appendLine("            $paramName?.let { parameter(\"${param.name}\", $nullableAccessor) }")
            }
        }
    }

    private fun resolveParamName(name: String, glossary: Glossary): String {
        return sanitizeParamName(glossary.translate(name))
    }

    private fun kotlinNameFor(raw: String): String {
        if (raw.isEmpty()) return "Empty"
        return raw.split("_")
            .joinToString("") { part ->
                part.lowercase().replaceFirstChar { it.uppercaseChar() }
            }
    }
}
