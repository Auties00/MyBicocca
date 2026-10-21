package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Names and files of an API's generated TypeScript declarations, and the hand-written ones
 * they rely on. For an API with prefix `Esse3`:
 *
 * | Declaration                    | File                     |
 * |--------------------------------|--------------------------|
 * | Records of module `Careers`    | `Esse3CareersTypes.ts`   |
 * | Every enum                     | `Esse3EnumTypes.ts`      |
 * | Operations of module `Careers` | `Esse3CareersApi.ts`     |
 * | Facade                         | `Esse3Api.ts`            |
 * | Servers, if any                | `Esse3Server.ts`         |
 * | Auth schemes, if any           | `Esse3AuthTypes.ts`      |
 * | API parameters, if any         | `Esse3Parameters.ts`     |
 * | Scrapers, if any               | `Esse3Scrapers.ts`       |
 * | Re-exports of all of the above | `index.ts`               |
 *
 * A record `X` is an interface with `decodeX(json)` and `encodeX(value)` functions; an enum
 * `X` is a string-literal union with a same-named `const` of its values and `decodeX(json)`.
 * A map with string keys is a `Record`, any other a runtime `WireMap`. API parameters are
 * the fields of the `Esse3Parameters` interface, which the facade passes to every module.
 *
 * The hand-written runtime:
 * - The shared runtime package ([runtimeModule]): `LocalDate`, `LocalDateTime`, the lenient
 *   `decode*` helpers, the temporal codec, `pathSegment` and the request types.
 * - The API's client module ([clientModule]): `Esse3Client` with `json`, `jsonList` (empty on
 *   HTTP 404), `bytes` and `send`, and the `Esse3PermissionLevel` union of role names. An API
 *   with script or HTML responses also needs `script(request, variable, decode)`, decoding the
 *   JSON a script assigns to a variable, and `html(request, scrape)`, reading the page with a
 *   function of `Esse3Scrapers.ts` and failing when it returns `undefined`.
 */
internal class TsLayout(private val spec: ApiSpec, val runtimeModule: String, val clientModule: String) {
    val enumsFile = "${spec.prefix}EnumTypes.ts"
    val facadeName = "${spec.prefix}Api"
    val client = "${spec.prefix}Client"
    val roles = "${spec.prefix}PermissionLevel"
    val server = "${spec.prefix}Server"
    val serverFile = "${spec.prefix}Server.ts"
    val authFile = "${spec.prefix}AuthTypes.ts"
    val authBase = "${spec.prefix}Auth"
    val parameters = "${spec.prefix}Parameters"
    val parametersFile = "${spec.prefix}Parameters.ts"

    fun authClass(name: String): String = "${spec.prefix}${name}Auth"

    /** The parameter type of [auth]: the scheme's class when there is one, else the union of all. */
    fun authType(auth: AuthRequirement, file: TsFile): String {
        val type = auth.schemes.singleOrNull()?.let(::authClass) ?: authBase
        file.import(type, moduleOf(authFile))
        return type
    }

    fun authDescription(auth: AuthRequirement): String {
        val schemes = auth.schemes.joinToString(" or ") { "{@link ${authClass(it)}}" }
        return if (auth.required) "The credentials: $schemes." else "The credentials, if any: $schemes."
    }

    /** A TypeScript literal for [literal] as a value of [type]; enum values are their wire strings. */
    fun literal(type: TypeRef, literal: DefaultValue.Literal): String = when (type) {
        is TypeRef.Named -> TsSyntax.string(literal.value)
        is TypeRef.Primitive -> if (type.kind == PrimitiveKind.STRING) TsSyntax.string(literal.value) else literal.value
        is TypeRef.ListOf, is TypeRef.MapOf -> error("Collections can't have a literal default")
    }

    private val fileByDeclaration: Map<String, String> =
        spec.modules.flatMap { module -> module.records.map { it.name to recordsFile(module) } }.toMap() +
            spec.enums.associate { it.name to enumsFile }

    fun recordsFile(module: ApiModule): String = "${spec.prefix}${module.name}Types.ts"

    fun serviceName(module: ApiModule): String = "${spec.prefix}${module.name}Api"

    /** The import specifier of a generated file, relative to another generated file. */
    fun moduleOf(fileName: String): String = "./${fileName.removeSuffix(".ts")}.js"

    fun decoderName(declaration: String): String = "decode$declaration"

    fun encoderName(declaration: String): String = "encode$declaration"

    /** Imports declaration [name] (or one of its functions) into [file]. */
    fun importDeclaration(file: TsFile, declaration: String, name: String = declaration) {
        file.import(name, moduleOf(fileByDeclaration.getValue(declaration)))
    }

    fun importRuntime(file: TsFile, name: String) = file.import(name, runtimeModule)

    fun declaration(type: TypeRef.Named) = spec.resolve(type)

    fun isEnum(type: TypeRef.Named): Boolean = spec.resolve(type) is EnumType

    fun isRecord(type: TypeRef.Named): Boolean = spec.resolve(type) is RecordType

    /** Renders [type], importing what it references into [file]. */
    fun render(type: TypeRef, file: TsFile): String = when (type) {
        is TypeRef.Named -> {
            importDeclaration(file, type.name)
            type.name
        }
        is TypeRef.ListOf -> {
            val element = render(type.element, file)
            if (element.contains(' ')) "($element)[]" else "$element[]"
        }
        is TypeRef.MapOf -> if (type.key == TypeRef.STRING) {
            "Record<string, ${render(type.value, file)}>"
        } else {
            "${runtimeType("WireMap", file)}<${render(type.key, file)}, ${render(type.value, file)}>"
        }
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> "string"
            PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "number"
            PrimitiveKind.BOOLEAN -> "boolean"
            PrimitiveKind.LOCAL_DATE -> runtimeType("LocalDate", file)
            PrimitiveKind.LOCAL_DATE_TIME -> runtimeType("LocalDateTime", file)
            PrimitiveKind.LOCAL_TIME -> runtimeType("LocalTime", file)
            PrimitiveKind.JSON_OBJECT -> runtimeType("JsonObject", file)
            PrimitiveKind.JSON_VALUE -> runtimeType("JsonValue", file)
            PrimitiveKind.BINARY -> "Uint8Array"
        }
    }

    private fun runtimeType(name: String, file: TsFile): String {
        importRuntime(file, name)
        return name
    }
}
