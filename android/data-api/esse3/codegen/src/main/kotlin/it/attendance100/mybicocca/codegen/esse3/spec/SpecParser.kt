package it.attendance100.mybicocca.codegen.esse3.spec

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.ParseOptions
import java.io.File

object SpecParser {
    private val FrkRegex = Regex("^(p\\d+-|frk-)")
    private val ApiRegex = Regex("Api[Vv]\\d+$")

    fun parse(yamlFile: File): ParsedSpec {
        val options = ParseOptions().apply {
            isResolve = false
            isResolveFully = false
        }
        val result = OpenAPIParser().readLocation(yamlFile.absolutePath, null, options)
        val openApi = result.openAPI
            ?: error("Failed to parse ${yamlFile.name}: ${result.messages}")

        val basePath = extractBasePath(openApi)
        val specName = deriveSpecName(yamlFile.nameWithoutExtension)

        val operations = parseOperations(openApi, basePath)
        val definitions = parseDefinitions(openApi)

        return ParsedSpec(
            specName = specName,
            basePath = basePath,
            operations = operations,
            definitions = definitions
        )
    }

    private fun extractBasePath(openApi: OpenAPI): String {
        val serverUrl = openApi.servers?.firstOrNull()?.url ?: "/"
        return if (serverUrl.startsWith("http")) {
            val path = java.net.URI(serverUrl).path ?: "/"
            if (path == "/") "/" else path.trimEnd('/')
        } else {
            if (serverUrl == "/") "/" else serverUrl.trimEnd('/')
        }
    }

    fun deriveSpecName(filenameNoExt: String): String {
        var name = filenameNoExt
        name = name.replace(FrkRegex, "")
        name = name.replace(ApiRegex, "")
        return name.replaceFirstChar { it.uppercaseChar() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseOperations(openApi: OpenAPI, basePath: String): List<ParsedOperation> {
        val ops = mutableListOf<ParsedOperation>()
        val paths = openApi.paths ?: return ops

        for ((pathStr, pathItem) in paths) {
            val methodMap = mapOf(
                "get" to pathItem.get,
                "post" to pathItem.post,
                "put" to pathItem.put,
                "delete" to pathItem.delete,
                "patch" to pathItem.patch
            )

            for ((method, operation) in methodMap) {
                if (operation == null) continue
                val operationId = operation.operationId ?: continue

                val params = operation.parameters ?: emptyList()
                val pathParams = mutableListOf<ParsedParameter>()
                val queryParams = mutableListOf<ParsedParameter>()
                val formParams = mutableListOf<ParsedParameter>()
                var bodyParam: ParsedParameter? = null

                val xQueryParams = extractXQueryParams(operation)

                for (param in params) {
                    when (param.`in`) {
                        "path" -> pathParams.add(parseParameter(param, ParameterLocation.PATH, xQueryParams))
                        "query" -> queryParams.add(parseParameter(param, ParameterLocation.QUERY, xQueryParams))
                        "formData" -> formParams.add(parseParameter(param, ParameterLocation.FORM, xQueryParams))
                    }
                }

                val requestBody = operation.requestBody
                if (requestBody != null) {
                    val content = requestBody.content
                    val formMediaType = content?.get("application/x-www-form-urlencoded")
                    val jsonMediaType = content?.get("application/json")
                    if (formMediaType != null && jsonMediaType == null) {
                        val schema = formMediaType.schema
                        val schemaProperties = schema?.properties
                        if (schemaProperties != null) {
                            val requiredFields = (schema.required ?: emptyList()).toSet()
                            for ((propName, propSchema) in schemaProperties) {
                                formParams.add(
                                    parseFormDataProperty(propName, propSchema, propName in requiredFields)
                                )
                            }
                        }
                    } else {
                        val mediaType = jsonMediaType ?: content?.values?.firstOrNull()
                        val schema = mediaType?.schema
                        if (schema != null) {
                            val bodyType = resolveSchemaType(schema)
                            bodyParam = ParsedParameter(
                                name = "body",
                                type = bodyType,
                                required = requestBody.required ?: true,
                                location = ParameterLocation.BODY,
                                description = requestBody.description
                            )
                        }
                    }
                }

                if (bodyParam == null) {
                    for (param in params) {
                        if (param.`in` == "body" || param.`in` == null) {
                            val schema = param.schema
                            if (schema != null) {
                                val bodyType = resolveSchemaType(schema)
                                bodyParam = ParsedParameter(
                                    name = param.name ?: "body",
                                    type = bodyType,
                                    required = param.required ?: true,
                                    location = ParameterLocation.BODY,
                                    description = param.description
                                )
                            }
                        }
                    }
                }

                val responseType = resolveResponseType(operation)
                val permissions = extractPermissions(operation)

                ops.add(
                    ParsedOperation(
                        operationId = operationId,
                        httpMethod = method.uppercase(),
                        path = pathStr,
                        pathParams = pathParams,
                        queryParams = queryParams,
                        formParams = formParams,
                        bodyParam = bodyParam,
                        responseType = responseType,
                        permissions = permissions,
                        summary = operation.summary,
                        description = operation.description
                    )
                )
            }
        }
        return ops
    }

    private fun parseParameter(
        param: Parameter,
        location: ParameterLocation,
        xQueryParams: List<XQueryParam>,
    ): ParsedParameter {
        val schema = param.schema
        val type = if (schema != null) {
            resolveSchemaType(schema)
        } else {
            TypeMapping.resolveScalar("string", null)
        }

        var enumValues: List<String>? = schema?.enum?.map { it.toString() }?.takeIf { it.isNotEmpty() }
        var enumValueDocs: Map<String, String>? = null
        if (param.name == "q" && xQueryParams.isNotEmpty()) {
            enumValues = xQueryParams.map { it.value }
            enumValueDocs = xQueryParams
                .filter { it.description != null }
                .associate { it.value to it.description!! }
                .takeIf { it.isNotEmpty() }
        }

        return ParsedParameter(
            name = param.name,
            type = type,
            required = param.required ?: false,
            location = location,
            description = param.description,
            enumValues = enumValues,
            enumValueDocs = enumValueDocs,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFormDataProperty(name: String, schema: Schema<*>, required: Boolean): ParsedParameter {
        val type = resolveSchemaType(schema)
        val enumValues = schema.enum?.map { it.toString() }?.takeIf { it.isNotEmpty() }
        return ParsedParameter(
            name = name,
            type = type,
            required = required,
            location = ParameterLocation.FORM,
            description = schema.description,
            enumValues = enumValues,
            enumValueDocs = null,
        )
    }

    private data class XQueryParam(val value: String, val description: String?)

    @Suppress("UNCHECKED_CAST")
    private fun extractXQueryParams(operation: Operation): List<XQueryParam> {
        val extensions = operation.extensions ?: return emptyList()
        val raw = extensions["x-query-params"] as? List<*> ?: return emptyList()
        return raw.mapNotNull { entry ->
            (entry as? Map<String, Any?>)?.let { map ->
                val name = map["name"] as? String ?: return@mapNotNull null
                val description = map["description"] as? String
                XQueryParam(value = name, description = description)
            }
        }
    }

    private fun resolveSchemaType(schema: Schema<*>): ResolvedType {
        val ref = schema.`$ref`
        if (ref != null) {
            return ResolvedType.Reference(extractRefName(ref))
        }

        if (schema is ArraySchema || schema.type == "array") {
            val items = schema.items
            val inner = if (items != null) resolveSchemaType(items)
            else ResolvedType.Simple("String")
            return ResolvedType.ListOf(inner)
        }

        if (schema is ComposedSchema && schema.allOf != null) {
            for (sub in schema.allOf) {
                if (sub.`$ref` != null) {
                    return ResolvedType.Reference(extractRefName(sub.`$ref`))
                }
            }
        }

        return TypeMapping.resolveScalar(schema.type, schema.format)
    }

    private fun resolveResponseType(operation: Operation): ResolvedType? {
        val responses = operation.responses ?: return null
        val successResponse = responses["200"] ?: responses["201"] ?: return null
        val content = successResponse.content
        if (content != null) {
            val mediaType = content["application/json"] ?: content.values.firstOrNull()
            val schema = mediaType?.schema
            if (schema != null) {
                return resolveSchemaType(schema)
            }
        }
        val extensions = successResponse.extensions
        if (extensions != null) {
            @Suppress("UNCHECKED_CAST")
            val schemaExt = extensions["x-schema"] as? Map<String, Any?>
            if (schemaExt != null) {
                val ref = schemaExt[$$"$ref"] as? String
                if (ref != null) {
                    return ResolvedType.Reference(extractRefName(ref))
                }
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractPermissions(operation: Operation): List<String> {
        val extensions = operation.extensions ?: return emptyList()
        val profiles = extensions["x-enabled-profiles"] as? List<*> ?: return emptyList()

        val result = mutableSetOf<String>()
        for (entry in profiles) {
            if (entry is Map<*, *>) {
                for ((_, value) in entry) {
                    if (value is Map<*, *>) {
                        val kind = value["kind"] as? String
                        if (kind != null) {
                            result.add(TypeMapping.mapPermissionKind(kind))
                        }
                    }
                }
            }
        }
        return result.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseDefinitions(openApi: OpenAPI): List<ParsedDefinition> {
        val schemas = openApi.components?.schemas ?: return emptyList()
        val defs = mutableListOf<ParsedDefinition>()

        for ((name, schema) in schemas) {
            val (properties, requiredFields) = flattenSchema(schema, schemas)
            if (properties.isEmpty()) continue

            defs.add(
                ParsedDefinition(
                    name = name,
                    properties = properties,
                    requiredFields = requiredFields
                )
            )
        }
        return defs
    }

    @Suppress("UNCHECKED_CAST")
    private fun flattenSchema(
        schema: Schema<*>,
        allSchemas: Map<String, Schema<*>>
    ): Pair<List<ParsedProperty>, Set<String>> {
        val properties = mutableListOf<ParsedProperty>()
        val requiredFields = mutableSetOf<String>()

        if (schema is ComposedSchema && schema.allOf != null) {
            for (sub in schema.allOf) {
                val resolved = if (sub.`$ref` != null) {
                    val refName = extractRefName(sub.`$ref`)
                    allSchemas[refName]
                } else {
                    sub
                }
                if (resolved != null) {
                    val (subProps, subRequired) = flattenSchema(resolved, allSchemas)
                    properties.addAll(subProps)
                    requiredFields.addAll(subRequired)
                }
            }
            requiredFields.addAll(schema.required ?: emptyList())
            val deduped = properties.associateBy { it.wireName }.values.toList()
            return deduped to requiredFields
        }

        requiredFields.addAll(schema.required ?: emptyList())

        val schemaProperties = schema.properties ?: return properties to requiredFields

        for ((propName, propSchema) in schemaProperties) {
            val prop = parseProperty(propName, propSchema)
            properties.add(prop)
        }

        return properties to requiredFields
    }

    private fun parseProperty(wireName: String, schema: Schema<*>): ParsedProperty {
        val ref = schema.`$ref`?.let { extractRefName(it) }
        val isArray = schema is ArraySchema || schema.type == "array"
        val itemRef = if (isArray) schema.items?.`$ref`?.let { extractRefName(it) } else null
        val itemType = if (isArray) schema.items?.type else null
        val itemFormat = if (isArray) schema.items?.format else null

        @Suppress("UNCHECKED_CAST")
        val enumValues = schema.enum?.map { it.toString() }

        return ParsedProperty(
            wireName = wireName,
            type = schema.type,
            format = schema.format,
            ref = ref,
            isArray = isArray,
            itemRef = itemRef,
            itemType = itemType,
            itemFormat = itemFormat,
            enumValues = enumValues,
            description = schema.description
        )
    }

    private fun extractRefName(ref: String): String {
        return ref.substringAfterLast("/")
    }
}
