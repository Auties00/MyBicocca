package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.Text
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Names of an API's generated Swift declarations and of the hand-written ones they rely on.
 * For an API with prefix `Esse3`, all in one Swift module:
 *
 * | Declaration                    | File                         |
 * |--------------------------------|------------------------------|
 * | Records of module `Careers`    | `Esse3CareersTypes.swift`    |
 * | Every enum                     | `Esse3EnumTypes.swift`       |
 * | Operations of module `Careers` | `Esse3CareersApi.swift`      |
 * | Facade                         | `Esse3Api.swift`             |
 * | Servers, if any                | `Esse3Server.swift` (enum)   |
 * | Auth schemes, if any           | `Esse3AuthTypes.swift`       |
 * | Scrapers, if any               | `Esse3Scrapers.swift`        |
 *
 * The hand-written runtime:
 * - The shared runtime module ([runtimeModule]): `LocalDate`, `LocalDateTime`, `JSONObject`,
 *   `WireEnum`, `WireMap`, `RestRequest` and the lenient `Codable` helpers.
 * - `Esse3Client`: sends `RestRequest`s and decodes responses and errors, through
 *   `json(_:roles:)`, `jsonList(_:roles:)` (empty on HTTP 404), `data(_:roles:)` and
 *   `send(_:roles:)`; an API with script or HTML responses also needs `script(_:variable:)`,
 *   decoding the JSON a script assigns to a variable, and `html(_:scrape:)`, reading the page
 *   with a function of `Esse3Scrapers` and failing when it returns nil.
 * - `Esse3PermissionLevel`: enum of the role names operations declare, as lowerCamel cases.
 */
internal class SwiftLayout(private val spec: ApiSpec, val runtimeModule: String) {
    val enumsFileName = "${spec.prefix}EnumTypes.swift"
    val facadeName = "${spec.prefix}Api"
    val client = "${spec.prefix}Client"
    val server = "${spec.prefix}Server"
    val authFileName = "${spec.prefix}AuthTypes.swift"
    val authBase = "${spec.prefix}Auth"

    fun authClass(name: String): String = "${spec.prefix}${name}Auth"

    fun serverCase(name: String): String = SwiftSyntax.identifier(Text.lowerCamel(name))

    /** The parameter type of [auth]: the scheme's struct when there is one, else any scheme. */
    fun authType(auth: AuthRequirement): String = auth.schemes.singleOrNull()?.let(::authClass) ?: "any $authBase"

    fun authDescription(auth: AuthRequirement): String {
        val schemes = auth.schemes.joinToString(" or ") { "``${authClass(it)}``" }
        return if (auth.required) "The credentials: $schemes." else "The credentials, if any: $schemes."
    }

    /** A Swift literal for [literal] as a value of [type]. */
    fun literal(type: TypeRef, literal: DefaultValue.Literal): String = when (type) {
        is TypeRef.Named -> {
            val enum = spec.resolve(type) as EnumType
            ".${SwiftSyntax.identifier(caseNames(enum).getValue(enum.values.first { it.wireValue == literal.value }.name))}"
        }
        is TypeRef.Primitive -> if (type.kind == PrimitiveKind.STRING) SwiftSyntax.string(literal.value) else literal.value
        is TypeRef.ListOf, is TypeRef.MapOf -> error("Collections can't have a literal default")
    }

    fun recordsFileName(module: ApiModule): String = "${spec.prefix}${module.name}Types.swift"

    fun serviceName(module: ApiModule): String = "${spec.prefix}${module.name}Api"

    fun role(name: String): String = SwiftSyntax.identifier(Text.lowerCamel(name))

    fun render(type: TypeRef): String = when (type) {
        is TypeRef.Named -> type.name
        is TypeRef.ListOf -> "[${render(type.element)}]"
        is TypeRef.MapOf -> "WireMap<${render(type.key)}, ${render(type.value)}>"
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> "String"
            PrimitiveKind.INT32 -> "Int"
            PrimitiveKind.INT64 -> "Int64"
            PrimitiveKind.FLOAT32 -> "Float"
            PrimitiveKind.FLOAT64 -> "Double"
            PrimitiveKind.BOOLEAN -> "Bool"
            PrimitiveKind.LOCAL_DATE -> "LocalDate"
            PrimitiveKind.LOCAL_DATE_TIME -> "LocalDateTime"
            PrimitiveKind.LOCAL_TIME -> "LocalTime"
            PrimitiveKind.JSON_OBJECT -> "JSONObject"
            PrimitiveKind.JSON_VALUE -> "JSONValue"
            PrimitiveKind.BINARY -> "Data"
        }
    }

    /**
     * The Swift case name of each value of [enum], unique within it. Converting to lowerCamel
     * can merge names that differed only in casing; later ones get a numeric suffix.
     */
    fun caseNames(enum: EnumType): Map<String, String> {
        val used = mutableSetOf<String>()
        return enum.values.associate { value ->
            val base = Text.lowerCamel(value.name).ifEmpty { "empty" }
            var name = base
            var suffix = 2
            while (!used.add(name)) name = "$base${suffix++}"
            value.name to name
        }
    }
}
