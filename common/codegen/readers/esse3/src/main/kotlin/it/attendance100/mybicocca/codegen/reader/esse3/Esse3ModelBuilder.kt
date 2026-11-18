package it.attendance100.mybicocca.codegen.reader.esse3

import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.EnumValue
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawDefinition
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawOperation
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawParameter
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawProperty
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawSpec
import it.attendance100.mybicocca.codegen.reader.esse3.swagger.RawType

/**
 * Builds the [ApiSpec] of Esse3 from its raw specs, applying the overlays and Esse3's
 * decoding policy:
 *
 * - Names are translated through the [Glossary] and declarations carry the `Esse3` prefix.
 * - Enums used by several fields or parameters with the same values become one type.
 * - Esse3 omits or nulls fields it declares required, so required scalars default to their
 *   zero value, lists to empty, and dates (sent as `dd/MM/yyyy`, blank when unset) are
 *   always nullable.
 */
internal class Esse3ModelBuilder(
    private val glossary: Glossary,
    private val enumNames: EnumNames,
    private val requiredOverrides: RequiredOverrides,
) {
    fun build(specs: List<RawSpec>): ApiSpec {
        val enums = EnumCatalog.build(specs, glossary, enumNames)
        return ApiSpec(
            id = "esse3",
            prefix = PREFIX,
            modules = specs.mapNotNull { module(it, enums) },
            enums = enums.types,
        )
    }

    private fun module(spec: RawSpec, enums: EnumCatalog): ApiModule? {
        if (spec.definitions.isEmpty() && spec.operations.isEmpty()) return null
        val name = if (spec.operations.isNotEmpty()) {
            glossary.translate("$PREFIX${spec.name}Api").removePrefix(PREFIX).removeSuffix("Api")
        } else {
            glossary.translate("$PREFIX${spec.name}Types.kt").removePrefix(PREFIX).removeSuffix("Types.kt")
        }
        val operations = spec.operations.map { it.operationId to operation(it, enums) }
        val collisions = operations.groupBy({ it.second.name }, { it.first }).filterValues { it.size > 1 }
        require(collisions.isEmpty()) {
            "Operations of ${spec.name} translate to the same name; give them distinct glossary entries: " +
                collisions.entries.joinToString { (name, ids) -> "$ids -> $name" }
        }
        return ApiModule(
            name = name,
            basePath = spec.basePath,
            records = spec.definitions.map { record(it, enums) },
            operations = operations.map { it.second },
        )
    }

    private fun record(definition: RawDefinition, enums: EnumCatalog): RecordType = RecordType(
        name = glossary.translate("$PREFIX${definition.name}"),
        fields = definition.properties.map { field(definition, it, enums) },
    )

    private fun field(definition: RawDefinition, property: RawProperty, enums: EnumCatalog): Field {
        val enumName = enums.forProperty(definition.name, property.wireName)
        val type = enumName?.let { TypeRef.Named(it) } ?: typeRef(property.type)
        val temporalKind = (property.type as? RawType.Primitive)?.kind?.takeIf { enumName == null && it.isTemporal }
        val required = requiredOverrides["${definition.name}.${property.wireName}"] ?: (property.wireName in definition.requiredFields)

        val (nullable, default) = when {
            temporalKind != null -> true to DefaultValue.Null
            type is TypeRef.ListOf -> false to DefaultValue.EmptyList
            !required -> true to DefaultValue.Null
            type is TypeRef.Primitive && type.kind in ZERO_DEFAULT_KINDS -> false to DefaultValue.Zero
            else -> false to null
        }
        val translated = glossary.translate(property.wireName)
        return Field(
            name = if (translated != property.wireName) translated.decapitalized() else translated,
            wireName = property.wireName,
            type = type,
            nullable = nullable,
            default = default,
            codec = temporalKind?.let { Codec.Temporal(TEMPORAL_PATTERNS.getValue(it), blankAsNull = true) },
            description = property.description,
        )
    }

    private fun operation(operation: RawOperation, enums: EnumCatalog): Operation {
        fun parameter(raw: RawParameter, location: ParameterLocation): Parameter {
            val enumName = if (location == ParameterLocation.PATH) null else enums.forParameter(operation.operationId, raw.name)
            return Parameter(
                name = parameterName(raw.name),
                wireName = raw.name,
                location = location,
                type = enumName?.let { TypeRef.Named(it) } ?: typeRef(raw.type),
                required = location == ParameterLocation.PATH || raw.required,
                description = raw.description,
                valueDescriptions = if (enumName != null) raw.enumValueDocs.orEmpty() else emptyMap(),
            )
        }

        val (requiredQuery, optionalQuery) = operation.queryParams.partition { it.required }
        return Operation(
            name = glossary.translate(operation.operationId.decapitalized()).decapitalized(),
            method = operation.method,
            path = operation.path,
            parameters = operation.pathParams.map { parameter(it, ParameterLocation.PATH) } +
                operation.formParams.map { parameter(it, ParameterLocation.FORM) } +
                (requiredQuery + optionalQuery).map { parameter(it, ParameterLocation.QUERY) },
            body = operation.bodyParam?.let { RequestBody.Json(typeRef(it.type), it.description) },
            response = when (val type = operation.responseType) {
                null -> ResponseBody.None
                RawType.Primitive(PrimitiveKind.BINARY) -> ResponseBody.Binary
                else -> ResponseBody.Json(typeRef(type))
            },
            roles = operation.roles.toSet(),
            summary = operation.summary,
        )
    }

    private fun typeRef(type: RawType): TypeRef = when (type) {
        is RawType.Primitive -> TypeRef.Primitive(type.kind)
        is RawType.Reference -> TypeRef.Named(glossary.translate("$PREFIX${type.definitionName}"))
        is RawType.ListOf -> TypeRef.ListOf(typeRef(type.element))
    }

    /** Translates a parameter name and turns it into camelCase (`stu-id` → `stuId`). */
    private fun parameterName(name: String): String =
        glossary.translate(name)
            .split(NON_IDENTIFIER)
            .filter { it.isNotEmpty() }
            .mapIndexed { index, part -> if (index == 0) part.decapitalized() else part.capitalized() }
            .joinToString("")
            .ifEmpty { "param" }

    companion object {
        const val PREFIX = "Esse3"

        private val NON_IDENTIFIER = Regex("[^a-zA-Z0-9_]")

        private val ZERO_DEFAULT_KINDS = setOf(
            PrimitiveKind.STRING,
            PrimitiveKind.INT32,
            PrimitiveKind.INT64,
            PrimitiveKind.FLOAT32,
            PrimitiveKind.FLOAT64,
            PrimitiveKind.BOOLEAN,
        )

        private val TEMPORAL_PATTERNS = mapOf(
            PrimitiveKind.LOCAL_DATE to "dd/MM/yyyy",
            PrimitiveKind.LOCAL_DATE_TIME to "dd/MM/yyyy HH:mm:ss",
        )
    }
}

/**
 * The enums of Esse3. The specs declare enum values inline on each property and parameter;
 * declarations whose values (and value names) coincide are merged into one type, named
 * after a property when one uses it.
 */
internal class EnumCatalog private constructor(
    val types: List<EnumType>,
    private val byProperty: Map<String, String>,
    private val byParameter: Map<String, String>,
) {
    fun forProperty(definition: String, property: String): String? = byProperty["$definition.$property"]

    fun forParameter(operationId: String, parameter: String): String? = byParameter["$operationId.$parameter"]

    private data class Usage(
        val key: String,
        val isProperty: Boolean,
        val baseName: String,
        val values: List<EnumValue>,
    )

    companion object {
        fun build(specs: List<RawSpec>, glossary: Glossary, enumNames: EnumNames): EnumCatalog {
            fun values(namesKey: String, wireValues: List<String>, docs: Map<String, String>?): List<EnumValue> {
                val names = enumNames[namesKey]
                return wireValues.map { wire ->
                    EnumValue(names?.get(wire)?.ifEmpty { null } ?: defaultValueName(wire), wire, docs?.get(wire))
                }
            }

            val usages = buildList {
                for (spec in specs) {
                    for (definition in spec.definitions) {
                        for (property in definition.properties) {
                            val wireValues = property.enumValues?.takeIf { it.isNotEmpty() } ?: continue
                            val key = "${definition.name}.${property.wireName}"
                            add(Usage(key, isProperty = true, property.wireName, values(key, wireValues, null)))
                        }
                    }
                    for (operation in spec.operations) {
                        for (parameter in operation.queryParams + operation.formParams) {
                            val wireValues = parameter.enumValues?.takeIf { it.isNotEmpty() } ?: continue
                            val key = "${operation.operationId}.${parameter.name}"
                            val baseName = operation.operationId.capitalized() + parameter.name.capitalized()
                            add(Usage(key, isProperty = false, baseName, values(key, wireValues, parameter.enumValueDocs)))
                        }
                    }
                }
            }

            val types = mutableListOf<EnumType>()
            val byProperty = mutableMapOf<String, String>()
            val byParameter = mutableMapOf<String, String>()
            val usedNames = mutableSetOf<String>()
            val groups = usages.groupBy { usage -> usage.values.map { it.wireValue to it.name }.sortedBy { it.first } }
            for (group in groups.values) {
                val preferred = group.firstOrNull { it.isProperty } ?: group.first()
                val name = unique("${Esse3ModelBuilder.PREFIX}${glossary.translate(preferred.baseName).capitalized()}", usedNames)
                types += EnumType(name, group.first().values, open = true, acceptsWrappedValue = true)
                for (usage in group) {
                    (if (usage.isProperty) byProperty else byParameter)[usage.key] = name
                }
            }
            return EnumCatalog(types.sortedBy { it.name }, byProperty, byParameter)
        }

        private fun unique(name: String, used: MutableSet<String>): String {
            var candidate = name
            var suffix = 2
            while (candidate in used) candidate = "$name${suffix++}"
            used += candidate
            return candidate
        }

        /** `IN_CORSO` → `InCorso`. */
        private fun defaultValueName(wire: String): String =
            if (wire.isEmpty()) "Empty" else wire.split("_").joinToString("") { it.lowercase().capitalized() }
    }
}
