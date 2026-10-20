package it.attendance100.mybicocca.codegen.reader.openapi

import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation as OpenApiOperation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter as OpenApiParameter
import it.attendance100.mybicocca.codegen.core.Text
import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.EnumValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterEncoding
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordEncoding
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.Server
import it.attendance100.mybicocca.codegen.model.TypeRef
import java.math.BigDecimal

/**
 * Converts an OpenAPI document into the model:
 *
 * - Each tag is a module, named after the operation's first tag; paths are absolute on the
 *   operation's server, so modules have no base path.
 * - Component schemas with `enum` are enums (integer ones when of type `integer`), the others
 *   records. A record belongs to the module named by `x-mb-module`, else to the first module
 *   that uses it.
 * - An object schema with `additionalProperties` is a map.
 * - A property that isn't `required` takes its `default`, or an empty list or map, or null;
 *   one that is required has no default unless it declares one.
 * - Names come from `x-mb-name`, else from the wire name in camelCase.
 */
internal class OpenApiModelBuilder(private val api: OpenAPI) {
    private val schemas: Map<String, Schema<*>> = api.components?.schemas.orEmpty()

    private val apiParameterNames: List<String> = Extensions.refNames(api.extensions?.get(Extensions.PARAMETERS), Extensions.PARAMETERS)

    private val servers: List<Server> = api.servers.orEmpty().map { server ->
        Server(server.extensions?.get(Extensions.NAME)?.toString() ?: error("Server ${server.url} needs ${Extensions.NAME}"), server.url)
    }

    fun build(): ApiSpec {
        val prefix = rootExtension(Extensions.PREFIX)
        val operationsByModule = linkedMapOf<String, MutableList<Operation>>()
        for ((path, item) in api.paths.orEmpty()) {
            for ((method, operation) in item.methods()) {
                val module = operation.tags?.firstOrNull()?.capitalized() ?: error("Operation ${operation.operationId} has no tag")
                operationsByModule.getOrPut(module) { mutableListOf() } += operation(path, item, method, operation)
            }
        }

        val enumNames = schemas.filterValues { it.enum != null }.keys
        val recordOwners = recordOwners(operationsByModule, enumNames)
        val modules = operationsByModule.map { (module, operations) ->
            ApiModule(
                name = module,
                basePath = "",
                records = schemas.filterKeys { recordOwners[it] == module }.map { (name, schema) -> record(name, schema) },
                operations = operations,
            )
        }
        return ApiSpec(
            id = rootExtension(Extensions.ID),
            prefix = prefix,
            modules = modules,
            enums = schemas.filterKeys { it in enumNames }.map { (name, schema) -> enum(name, schema) },
            servers = servers,
            authSchemes = Extensions.authSchemes(api.components?.extensions?.get(Extensions.AUTH_SCHEMES)),
            parameters = apiParameterNames.map { name ->
                parameter("the API", api.components?.parameters?.get(name) ?: error("Unknown API parameter $name"))
            },
        )
    }

    private fun rootExtension(name: String): String = api.extensions?.get(name)?.toString() ?: error("The document needs $name")

    private fun PathItem.methods(): List<Pair<HttpMethod, OpenApiOperation>> = listOfNotNull(
        get?.let { HttpMethod.GET to it },
        post?.let { HttpMethod.POST to it },
        put?.let { HttpMethod.PUT to it },
        patch?.let { HttpMethod.PATCH to it },
        delete?.let { HttpMethod.DELETE to it },
    )

    private fun operation(path: String, item: PathItem, method: HttpMethod, operation: OpenApiOperation): Operation {
        val name = operation.operationId ?: error("$method $path has no operationId")
        val extensions = operation.extensions.orEmpty()
        val parameters = (item.parameters.orEmpty() + operation.parameters.orEmpty())
            .filterNot { parameter -> parameter.`$ref`?.let(::refName) in apiParameterNames }
            .map { parameter(name, resolve(it)) }
        val formSchema = operation.requestBody?.content?.get(FORM)?.schema?.let(::resolve)
        val form = formSchema?.properties.orEmpty().map { (wire, schema) ->
            val (type, _) = type(schema, "$name form field $wire")
            Parameter(
                name = nameOf(schema, wire),
                wireName = wire,
                location = ParameterLocation.FORM,
                type = type,
                required = wire in formSchema?.required.orEmpty(),
                description = schema.description,
            )
        }
        val body = when {
            extensions[Extensions.EMPTY_BODY] == true -> RequestBody.EmptyObject
            else -> operation.requestBody?.content?.get(JSON)?.schema?.let { RequestBody.Json(type(it, "$name body").first, operation.requestBody.description) }
        }
        return Operation(
            name = name,
            method = method,
            path = extensions[Extensions.PATH]?.toString() ?: path,
            parameters = parameters + form,
            body = body,
            response = response(name, operation),
            summary = operation.summary ?: operation.description,
            server = server(operation, item),
            headers = Extensions.stringMap(api.extensions?.get(Extensions.HEADERS), "${Extensions.HEADERS} of the document") +
                Extensions.stringMap(extensions[Extensions.HEADERS], "${Extensions.HEADERS} of $name"),
            auth = Extensions.authRequirement(extensions[Extensions.AUTH]),
        )
    }

    /** A parameter; one with `content: application/json` is JSON-encoded, its default as JSON text. */
    private fun parameter(operation: String, parameter: OpenApiParameter): Parameter {
        val jsonSchema = parameter.content?.get(JSON)?.schema
        val schema = parameter.schema ?: jsonSchema ?: error("Parameter ${parameter.name} of $operation has no schema")
        val encoding = if (parameter.schema == null) ParameterEncoding.JSON else ParameterEncoding.PLAIN
        val (type, _) = type(schema, "$operation parameter ${parameter.name}")
        return Parameter(
            name = nameOf(parameter.extensions, parameter.name),
            wireName = parameter.name,
            location = when (parameter.`in`) {
                "path" -> ParameterLocation.PATH
                "query" -> ParameterLocation.QUERY
                "header" -> ParameterLocation.HEADER
                "cookie" -> ParameterLocation.COOKIE
                else -> error("Unsupported location '${parameter.`in`}' of parameter ${parameter.name} of $operation")
            },
            type = type,
            required = parameter.required == true,
            description = parameter.description,
            default = schema.default?.let { DefaultValue.Literal(if (encoding == ParameterEncoding.JSON) Json.mapper().writeValueAsString(it) else literal(it)) },
            encoding = encoding,
        )
    }

    private fun response(operation: String, definition: OpenApiOperation): ResponseBody {
        val success = definition.responses.orEmpty().entries.firstOrNull { it.key.startsWith("2") }?.value ?: return ResponseBody.None
        val content = success.content ?: return ResponseBody.None
        content[JSON]?.schema?.let { schema ->
            val envelope = Extensions.stringList(definition.extensions?.get(Extensions.ENVELOPE), "${Extensions.ENVELOPE} of $operation")
            return ResponseBody.Json(type(schema, "$operation response").first, envelope)
        }
        return if (content.isNotEmpty()) ResponseBody.Binary else ResponseBody.None
    }

    /** The server an operation targets: its own, its path's, or the document's first. */
    private fun server(operation: OpenApiOperation, item: PathItem): String? {
        val url = (operation.servers ?: item.servers ?: api.servers)?.firstOrNull()?.url ?: return null
        return servers.firstOrNull { it.url == url }?.name ?: error("Server $url isn't declared at the root")
    }

    private fun record(name: String, schema: Schema<*>): RecordType {
        val required = schema.required.orEmpty().toSet()
        return RecordType(
            name = name,
            description = schema.description,
            encoding = schema.extensions?.get(Extensions.DELIMITED)?.let { RecordEncoding.Delimited(it.toString()) } ?: RecordEncoding.Object,
            fields = schema.properties.orEmpty().map { (wire, property) ->
                val (type, codec) = type(property, "$name.$wire")
                val explicit = property.default
                val (nullable, default) = when {
                    explicit is List<*> -> false to DefaultValue.EmptyList
                    explicit is Map<*, *> -> false to DefaultValue.EmptyMap
                    explicit != null -> (property.nullable == true) to DefaultValue.Literal(literal(explicit))
                    wire in required -> (property.nullable == true) to null
                    type is TypeRef.ListOf -> false to DefaultValue.EmptyList
                    type is TypeRef.MapOf -> false to DefaultValue.EmptyMap
                    else -> true to DefaultValue.Null
                }
                Field(nameOf(property, wire), wire, type, nullable, default, codec, property.description)
            },
        )
    }

    private fun enum(name: String, schema: Schema<*>): EnumType {
        val values = schema.enum.map { it.toString() }
        val names = Extensions.stringList(schema.extensions?.get(Extensions.ENUM_NAMES), "${Extensions.ENUM_NAMES} of $name")
        require(names.isEmpty() || names.size == values.size) { "${Extensions.ENUM_NAMES} of $name must name every value" }
        return EnumType(
            name = name,
            values = values.mapIndexed { index, wire -> EnumValue(names.getOrNull(index) ?: Text.lowerCamel(wire).capitalized(), wire) },
            open = schema.extensions?.get(Extensions.OPEN) as? Boolean ?: true,
            acceptsWrappedValue = schema.extensions?.get(Extensions.WRAPPED) as? Boolean ?: false,
            description = schema.description,
            integer = schema.type == "integer",
        )
    }

    /** The type of [schema], and the codec of its wire format when it isn't the default. */
    private fun type(schema: Schema<*>, where: String): Pair<TypeRef, Codec?> {
        schema.`$ref`?.let { return TypeRef.Named(refName(it)) to null }
        schema.allOf?.let { parts ->
            val ref = parts.singleOrNull()?.`$ref` ?: error("$where: allOf must wrap a single \$ref")
            val codec = when {
                schema.extensions?.get(Extensions.FOLDED) == true -> Codec.FoldedObject
                schema.extensions?.get(Extensions.EMBEDDED) == true -> Codec.EmbeddedJson
                else -> null
            }
            return TypeRef.Named(refName(ref)) to codec
        }
        require(schema.enum == null) { "$where declares an inline enum; declare it as a component schema" }
        val kind = when (schema.type) {
            "array" -> return TypeRef.ListOf(type(schema.items ?: error("$where is an array without items"), "$where items").first) to null
            "object" -> schema.additionalProperties?.let { values -> return map(schema, values, where) to null } ?: PrimitiveKind.JSON_OBJECT
            "string" -> when (schema.format) {
                "date" -> PrimitiveKind.LOCAL_DATE
                "date-time" -> PrimitiveKind.LOCAL_DATE_TIME
                "time" -> PrimitiveKind.LOCAL_TIME
                "binary" -> PrimitiveKind.BINARY
                else -> PrimitiveKind.STRING
            }
            "integer" -> if (schema.format == "int64") PrimitiveKind.INT64 else PrimitiveKind.INT32
            "number" -> if (schema.format == "float") PrimitiveKind.FLOAT32 else PrimitiveKind.FLOAT64
            "boolean" -> PrimitiveKind.BOOLEAN
            null -> PrimitiveKind.JSON_VALUE
            else -> error("$where has unsupported type '${schema.type}'")
        }
        val pattern = schema.extensions?.get(Extensions.PATTERN)?.toString()
        require(pattern == null || kind.isTemporal) { "$where has ${Extensions.PATTERN} but isn't a date or time" }
        val blankAsNull = schema.extensions?.get(Extensions.BLANK_AS_NULL) == true
        val codec = when {
            pattern != null -> Codec.Temporal(pattern, blankAsNull)
            blankAsNull && kind == PrimitiveKind.STRING ->
                Codec.BlankAsNull(Extensions.stringList(schema.extensions?.get(Extensions.ALSO_NULL), "${Extensions.ALSO_NULL} of $where"))
            else -> null
        }
        return TypeRef.Primitive(kind) to codec
    }

    /** A map whose values are [values] (a schema, or `true` for any JSON value), keyed as `x-mb-key` says. */
    private fun map(schema: Schema<*>, values: Any, where: String): TypeRef.MapOf {
        val key = when (val key = schema.extensions?.get(Extensions.KEY)?.toString()) {
            null, "string" -> TypeRef.STRING
            "integer" -> TypeRef.Primitive(PrimitiveKind.INT32)
            "int64" -> TypeRef.Primitive(PrimitiveKind.INT64)
            "date" -> TypeRef.Primitive(PrimitiveKind.LOCAL_DATE)
            else -> TypeRef.Named(key)
        }
        val value = when (values) {
            is Schema<*> -> type(values, "$where values").first
            true -> TypeRef.Primitive(PrimitiveKind.JSON_VALUE)
            else -> error("$where has unsupported additionalProperties '$values'")
        }
        return TypeRef.MapOf(key, value)
    }

    private fun resolve(parameter: OpenApiParameter): OpenApiParameter =
        parameter.`$ref`?.let { ref -> api.components?.parameters?.get(refName(ref)) ?: error("Unknown parameter $ref") } ?: parameter

    private fun resolve(schema: Schema<*>): Schema<*> =
        schema.`$ref`?.let { schemas[refName(it)] ?: error("Unknown schema $it") } ?: schema

    /**
     * Assigns each record to a module: the one its `x-mb-module` names, else the first module
     * whose operations use it, directly or through other records; unused records go to the
     * first module.
     */
    private fun recordOwners(operationsByModule: Map<String, List<Operation>>, enumNames: Set<String>): Map<String, String> {
        val owners = mutableMapOf<String, String>()
        for ((name, schema) in schemas) {
            schema.extensions?.get(Extensions.MODULE)?.let { owners[name] = it.toString() }
        }
        fun claim(type: TypeRef?, module: String) {
            when (type) {
                is TypeRef.Named -> if (type.name !in enumNames && owners.putIfAbsent(type.name, module) == null) {
                    schemas[type.name]?.properties.orEmpty().forEach { (wire, property) -> claim(type(property, "${type.name}.$wire").first, module) }
                }
                is TypeRef.ListOf -> claim(type.element, module)
                is TypeRef.MapOf -> {
                    claim(type.key, module)
                    claim(type.value, module)
                }
                is TypeRef.Primitive, null -> Unit
            }
        }
        for ((module, operations) in operationsByModule) {
            for (operation in operations) {
                operation.parameters.forEach { claim(it.type, module) }
                claim((operation.body as? RequestBody.Json)?.type, module)
                claim((operation.response as? ResponseBody.Json)?.type, module)
            }
        }
        val first = operationsByModule.keys.first()
        return schemas.keys.filter { it !in enumNames }.associateWith { owners[it] ?: first }
    }

    private fun nameOf(schema: Schema<*>, wire: String): String = nameOf(schema.extensions, wire)

    private fun nameOf(extensions: Map<String, Any>?, wire: String): String =
        extensions?.get(Extensions.NAME)?.toString() ?: Text.lowerCamel(wire)

    /** A default value as the model writes literals: strings unquoted, numbers and booleans as in JSON. */
    private fun literal(value: Any): String = when (value) {
        is BigDecimal -> value.toPlainString()
        else -> value.toString()
    }

    private fun refName(ref: String): String = ref.substringAfterLast('/')

    private companion object {
        const val JSON = "application/json"
        const val FORM = "application/x-www-form-urlencoded"
    }
}
