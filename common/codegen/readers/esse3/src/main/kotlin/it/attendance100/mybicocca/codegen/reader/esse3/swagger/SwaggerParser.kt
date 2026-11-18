package it.attendance100.mybicocca.codegen.reader.esse3.swagger

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.parser.core.models.ParseOptions
import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.nameWithoutExtension

/**
 * Parses the Esse3 Swagger 2.0 spec files (as scraped from Esse3's SwaggerUI) into [RawSpec]s.
 *
 * Besides standard Swagger, it understands the CINECA extensions the specs use:
 * `x-enabled-profiles` (who may call an operation), `x-query-params` (the accepted values of
 * the generic `q` query parameter) and `x-schema` (a response schema declared outside
 * `content`).
 */
internal object SwaggerParser {
    private val SPEC_PREFIX = Regex("""^(p\d+-|frk-)""")
    private val SPEC_SUFFIX = Regex("""Api[Vv]\d+$""")

    fun parse(file: Path): RawSpec {
        val options = ParseOptions().apply {
            isResolve = false
            isResolveFully = false
        }
        val result = OpenAPIParser().readLocation(file.absolutePathString(), null, options)
        val openApi = result.openAPI ?: error("Failed to parse ${file.fileName}: ${result.messages}")
        return RawSpec(
            name = specName(file.nameWithoutExtension),
            basePath = basePath(openApi),
            operations = operations(openApi),
            definitions = definitions(openApi),
        )
    }

    /** `p01-carriereApiV1` → `Carriere`. */
    fun specName(fileNameWithoutExtension: String): String =
        fileNameWithoutExtension.replace(SPEC_PREFIX, "").replace(SPEC_SUFFIX, "").replaceFirstChar { it.uppercaseChar() }

    private fun basePath(openApi: OpenAPI): String {
        val serverUrl = openApi.servers?.firstOrNull()?.url ?: "/"
        val path = if (serverUrl.startsWith("http")) URI(serverUrl).path ?: "/" else serverUrl
        return if (path == "/") "/" else path.trimEnd('/')
    }

    private fun operations(openApi: OpenAPI): List<RawOperation> {
        val paths = openApi.paths ?: return emptyList()
        return paths.flatMap { (path, item) ->
            listOf(
                HttpMethod.GET to item.get,
                HttpMethod.POST to item.post,
                HttpMethod.PUT to item.put,
                HttpMethod.DELETE to item.delete,
                HttpMethod.PATCH to item.patch,
            ).mapNotNull { (method, operation) ->
                operation?.takeIf { it.operationId != null }?.let { operation(path, method, it) }
            }
        }
    }

    private fun operation(path: String, method: HttpMethod, operation: Operation): RawOperation {
        val parameters = operation.parameters.orEmpty()
        val queryValues = queryParamValues(operation)
        fun parametersIn(location: String) = parameters.filter { it.`in` == location }.map { parameter(it, queryValues) }

        val formParams = parametersIn("formData").toMutableList()
        var bodyParam: RawParameter? = null

        val content = operation.requestBody?.content
        val formContent = content?.get("application/x-www-form-urlencoded")
        val jsonContent = content?.get("application/json")
        if (formContent != null && jsonContent == null) {
            val schema = formContent.schema
            val required = schema?.required.orEmpty().toSet()
            schema?.properties?.forEach { (name, property) ->
                formParams += RawParameter(
                    name = name,
                    type = schemaType(property),
                    required = name in required,
                    description = property.description,
                    enumValues = property.enum?.map { it.toString() }?.takeIf { it.isNotEmpty() },
                )
            }
        } else {
            val schema = (jsonContent ?: content?.values?.firstOrNull())?.schema
            if (schema != null) {
                bodyParam = RawParameter("body", schemaType(schema), operation.requestBody.required ?: true, operation.requestBody.description)
            }
        }
        if (bodyParam == null) {
            bodyParam = parameters.lastOrNull { (it.`in` == "body" || it.`in` == null) && it.schema != null }?.let {
                RawParameter(it.name ?: "body", schemaType(it.schema), it.required ?: true, it.description)
            }
        }

        return RawOperation(
            operationId = operation.operationId,
            method = method,
            path = path,
            pathParams = parametersIn("path"),
            queryParams = parametersIn("query"),
            formParams = formParams,
            bodyParam = bodyParam,
            responseType = responseType(operation),
            roles = roles(operation),
            summary = operation.summary,
        )
    }

    private fun parameter(parameter: Parameter, queryValues: List<QueryValue>): RawParameter {
        val schema = parameter.schema
        var enumValues = schema?.enum?.map { it.toString() }?.takeIf { it.isNotEmpty() }
        var enumValueDocs: Map<String, String>? = null
        if (parameter.name == "q" && queryValues.isNotEmpty()) {
            enumValues = queryValues.map { it.value }
            enumValueDocs = queryValues
                .mapNotNull { value -> value.description?.let { value.value to it } }
                .toMap()
                .takeIf { it.isNotEmpty() }
        }
        return RawParameter(
            name = parameter.name,
            type = schema?.let(::schemaType) ?: RawType.Primitive(PrimitiveKind.STRING),
            required = parameter.required ?: false,
            description = parameter.description,
            enumValues = enumValues,
            enumValueDocs = enumValueDocs,
        )
    }

    private data class QueryValue(val value: String, val description: String?)

    /** The `x-query-params` extension: accepted values of the generic `q` parameter. */
    private fun queryParamValues(operation: Operation): List<QueryValue> {
        val raw = operation.extensions?.get("x-query-params") as? List<*> ?: return emptyList()
        return raw.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            val name = map["name"] as? String ?: return@mapNotNull null
            QueryValue(name, map["description"] as? String)
        }
    }

    private fun schemaType(schema: Schema<*>): RawType {
        schema.`$ref`?.let { return RawType.Reference(refName(it)) }
        if (schema is ArraySchema || schema.type == "array") {
            return RawType.ListOf(schema.items?.let(::schemaType) ?: RawType.Primitive(PrimitiveKind.STRING))
        }
        if (schema is ComposedSchema) {
            schema.allOf?.firstNotNullOfOrNull { it.`$ref` }?.let { return RawType.Reference(refName(it)) }
        }
        return scalar(schema.type, schema.format)
    }

    private fun scalar(type: String?, format: String?): RawType.Primitive = RawType.Primitive(
        when (type) {
            "string" -> when (format) {
                "date" -> PrimitiveKind.LOCAL_DATE
                "date-time" -> PrimitiveKind.LOCAL_DATE_TIME
                "binary" -> PrimitiveKind.BINARY
                else -> PrimitiveKind.STRING
            }
            "integer" -> if (format == "int64") PrimitiveKind.INT64 else PrimitiveKind.INT32
            "number" -> if (format == "float") PrimitiveKind.FLOAT32 else PrimitiveKind.FLOAT64
            "boolean" -> PrimitiveKind.BOOLEAN
            "object" -> PrimitiveKind.JSON_OBJECT
            "file" -> PrimitiveKind.BINARY
            else -> PrimitiveKind.STRING
        }
    )

    private fun responseType(operation: Operation): RawType? {
        val response = operation.responses?.let { it["200"] ?: it["201"] } ?: return null
        response.content?.let { content -> (content["application/json"] ?: content.values.firstOrNull())?.schema }?.let { return schemaType(it) }
        val schemaExtension = response.extensions?.get("x-schema") as? Map<*, *>
        return (schemaExtension?.get("\$ref") as? String)?.let { RawType.Reference(refName(it)) }
    }

    /** The `x-enabled-profiles` extension, mapped to the role names of the Esse3 runtime. */
    private fun roles(operation: Operation): List<String> {
        val profiles = operation.extensions?.get("x-enabled-profiles") as? List<*> ?: return emptyList()
        return profiles.asSequence()
            .filterIsInstance<Map<*, *>>()
            .flatMap { it.values }
            .mapNotNull { (it as? Map<*, *>)?.get("kind") as? String }
            .map(::roleName)
            .distinct()
            .toList()
    }

    private fun roleName(kind: String): String = when (kind) {
        "ALL" -> "ANY"
        "AUTHENTICATED" -> "AUTHENTICATED_USER"
        "STUDENTE" -> "STUDENT"
        "DOCENTE" -> "TEACHER"
        "USER_TECNICO" -> "TECHNICAL_USER"
        "SEGRETERIA" -> "ADMIN_OFFICE"
        "SOGG_EST" -> "EXTERNAL_SUBJECT"
        "IMMATRICOLATI_IN_IPOTESI" -> "PROVISIONAL_ENROLLED_STUDENT"
        "REGISTRATO" -> "REGISTERED_USER"
        else -> "UNKNOWN"
    }

    private fun definitions(openApi: OpenAPI): List<RawDefinition> {
        val schemas = openApi.components?.schemas ?: return emptyList()
        return schemas.mapNotNull { (name, schema) ->
            val (properties, required) = flatten(schema, schemas)
            properties.takeIf { it.isNotEmpty() }?.let { RawDefinition(name, it, required) }
        }
    }

    /** Collects a schema's properties, merging `allOf` parts into one flat list. */
    private fun flatten(schema: Schema<*>, all: Map<String, Schema<*>>): Pair<List<RawProperty>, Set<String>> {
        val required = schema.required.orEmpty().toMutableSet()
        if (schema is ComposedSchema && schema.allOf != null) {
            val properties = mutableListOf<RawProperty>()
            for (part in schema.allOf) {
                val resolved = part.`$ref`?.let { all[refName(it)] } ?: part
                val (partProperties, partRequired) = flatten(resolved, all)
                properties += partProperties
                required += partRequired
            }
            return properties.associateBy { it.wireName }.values.toList() to required
        }
        val properties = schema.properties.orEmpty().map { (wireName, property) -> property(wireName, property) }
        return properties to required
    }

    private fun property(wireName: String, schema: Schema<*>): RawProperty {
        val isArray = schema is ArraySchema || schema.type == "array"
        val type = when {
            isArray -> RawType.ListOf(schema.items?.`$ref`?.let { RawType.Reference(refName(it)) } ?: scalar(schema.items?.type, schema.items?.format))
            schema.`$ref` != null -> RawType.Reference(refName(schema.`$ref`))
            else -> scalar(schema.type, schema.format)
        }
        return RawProperty(
            wireName = wireName,
            type = type,
            schemaType = schema.type,
            schemaFormat = schema.format,
            enumValues = schema.enum?.map { it.toString() },
            description = schema.description,
        )
    }

    private fun refName(ref: String): String = ref.substringAfterLast('/')
}
