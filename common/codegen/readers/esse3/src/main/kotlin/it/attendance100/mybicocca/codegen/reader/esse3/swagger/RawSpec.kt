package it.attendance100.mybicocca.codegen.reader.esse3.swagger

import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.PrimitiveKind

/**
 * One Esse3 spec file as written by CINECA: original (mostly Italian) names, before any
 * translation or naming policy is applied.
 *
 * @property name Name derived from the file name (e.g. `Carriere` for `p01-carriereApiV1.yaml`).
 */
internal data class RawSpec(
    val name: String,
    val basePath: String,
    val operations: List<RawOperation>,
    val definitions: List<RawDefinition>,
)

internal data class RawOperation(
    val operationId: String,
    val method: HttpMethod,
    val path: String,
    val pathParams: List<RawParameter>,
    val queryParams: List<RawParameter>,
    val formParams: List<RawParameter>,
    val bodyParam: RawParameter?,
    val responseType: RawType?,
    /** Role names from `x-enabled-profiles`, mapped to the runtime's names, in declared order. */
    val roles: List<String>,
    val summary: String?,
)

internal data class RawParameter(
    val name: String,
    val type: RawType,
    val required: Boolean,
    val description: String?,
    val enumValues: List<String>? = null,
    val enumValueDocs: Map<String, String>? = null,
)

internal data class RawDefinition(
    val name: String,
    val properties: List<RawProperty>,
    val requiredFields: Set<String>,
)

internal data class RawProperty(
    val wireName: String,
    val type: RawType,
    /** The schema's own `type`/`format`, which decide the wire format of dates. */
    val schemaType: String?,
    val schemaFormat: String?,
    val enumValues: List<String>?,
    val description: String?,
)

internal sealed interface RawType {
    data class Primitive(val kind: PrimitiveKind) : RawType
    data class Reference(val definitionName: String) : RawType
    data class ListOf(val element: RawType) : RawType
}
