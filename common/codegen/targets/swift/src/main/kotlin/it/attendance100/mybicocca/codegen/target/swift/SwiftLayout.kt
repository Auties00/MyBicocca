package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.Text
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Names and files of an API's generated Swift declarations. The API's files go in the folder of
 * its Swift module ([module]), which several APIs may share, split like the Kotlin SDK's packages.
 * For an API with prefix `Esse3` in module `Esse3`:
 *
 * | Declaration                    | File                                   |
 * |--------------------------------|----------------------------------------|
 * | Facade                         | `Esse3/API/Esse3Api.swift`             |
 * | Operations of module `Careers` | `Esse3/API/Esse3CareersApi.swift`      |
 * | Client                         | `Esse3/API/Esse3Client.swift`          |
 * | Servers, if any                | `Esse3/API/Esse3Server.swift` (enum)   |
 * | Records of module `Careers`    | `Esse3/Models/Esse3CareersTypes.swift` |
 * | Every enum                     | `Esse3/Models/Esse3EnumTypes.swift`    |
 * | Auth schemes, if any           | `Esse3/Models/Esse3AuthTypes.swift`    |
 * | Roles, if operations name any  | `Esse3/Models/Esse3PermissionLevel.swift` |
 * | Scrapers, if any               | `Esse3/Models/Esse3Scrapers.swift`     |
 * | Error                          | `Esse3/Errors/Esse3Error.swift`        |
 *
 * They build on the runtime module ([runtimeModule]), which [SwiftTarget] writes as its shared
 * files: `LocalDate`, `LocalDateTime`, `JSONObject`, `WireEnum`, `WireMap`, `RestRequest`,
 * `RestClient` and the lenient `Codable` helpers. `Esse3Client` sends `RestRequest`s and decodes
 * responses and errors, through `json(_:roles:)`, `jsonList(_:roles:)`, `data(_:roles:)`,
 * `text(_:roles:)`, `script(_:variable:roles:)`, `html(_:scrape:roles:)` and `send(_:roles:)`;
 * the `roles` of the `Esse3PermissionLevel` enum only when operations declare roles (see
 * [SwiftClientEmitter]).
 */
internal class SwiftLayout(private val spec: ApiSpec, val module: String, val runtimeModule: String) {
    val enumsFileName = models("${spec.prefix}EnumTypes.swift")
    val facadeName = "${spec.prefix}Api"
    val client = "${spec.prefix}Client"
    val server = "${spec.prefix}Server"
    val error = "${spec.prefix}Error"
    val roles = "${spec.prefix}PermissionLevel"
    val authFileName = models("${spec.prefix}AuthTypes.swift")
    val authBase = "${spec.prefix}Auth"

    /** The file of the API's declaration [name] in the module's `API` folder. */
    fun api(name: String): String = "$module/API/$name"

    /** The file of the API's declaration [name] in the module's `Models` folder. */
    fun models(name: String): String = "$module/Models/$name"

    /** The file of the API's declaration [name] in the module's `Errors` folder. */
    fun errors(name: String): String = "$module/Errors/$name"

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

    fun recordsFileName(module: ApiModule): String = models("${spec.prefix}${module.name}Types.swift")

    /** The enum of the API's scrapers (see [SwiftScrapeEmitter]). */
    val scrapers = "${spec.prefix}Scrapers"

    /** The function of [scrapers] reading [field] of [record] out of the HTML fragment it is sent as. */
    fun htmlFunction(record: RecordType, field: Field): String =
        record.name.removePrefix(spec.prefix).ifEmpty { record.name }.replaceFirstChar(Char::lowercaseChar) +
            field.name.replaceFirstChar(Char::uppercaseChar)

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
            PrimitiveKind.FILE -> "FileUpload"
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
