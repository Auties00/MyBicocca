package it.attendance100.mybicocca.codegen.target.web

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
 * Names and files of an API's generated TypeScript declarations. The API's files go in the folder
 * of its subpackage ([packageName]), which several APIs may share, split like the Kotlin SDK's
 * packages. For an API with prefix `Esse3` in subpackage `esse3`:
 *
 * | Declaration                    | File                                |
 * |--------------------------------|-------------------------------------|
 * | Facade                         | `esse3/api/Esse3Api.ts`             |
 * | Operations of module `Careers` | `esse3/api/Esse3CareersApi.ts`      |
 * | Client and `createEsse3Api`    | `esse3/api/Esse3Client.ts`          |
 * | Servers, if any                | `esse3/api/Esse3Server.ts`          |
 * | API parameters, if any         | `esse3/api/Esse3Parameters.ts`      |
 * | Records of module `Careers`    | `esse3/models/Esse3CareersTypes.ts` |
 * | Every enum                     | `esse3/models/Esse3EnumTypes.ts`    |
 * | Auth schemes, if any           | `esse3/models/Esse3AuthTypes.ts`    |
 * | Roles, if operations name any  | `esse3/models/Esse3PermissionLevel.ts` |
 * | Scrapers, if any               | `esse3/models/Esse3Scrapers.ts`     |
 * | Error                          | `esse3/errors/Esse3Error.ts`        |
 *
 * Each folder's `index.ts` re-exports it, written by [WebTarget.indexFiles] for every API sharing
 * the folder.
 *
 * A record `X` is an interface with `decodeX(json)` and `encodeX(value)` functions; an enum
 * `X` is a string-literal union with a same-named `const` of its values and `decodeX(json)`.
 * A map with string keys is a `Record`, any other a runtime `WireMap`. API parameters are
 * the fields of the `Esse3Parameters` interface, which the facade passes to every module.
 *
 * They build on the runtime in [runtimePath], which [WebTarget] writes as its shared files:
 * `LocalDate`, `LocalDateTime`, the lenient `decode*` helpers, the temporal codec,
 * `pathSegment`, the request types and `RestClient`. `Esse3Client` has `json`, `jsonList`,
 * `bytes`, `text`, `script`, `html` and `send` (see [TsClientEmitter]).
 */
internal class TsLayout(private val spec: ApiSpec, val packageName: String, val runtimePath: String) {
    val facadeName = "${spec.prefix}Api"
    val facadeFile = api("$facadeName.ts")
    val client = "${spec.prefix}Client"
    val clientFile = api("$client.ts")
    val server = "${spec.prefix}Server"
    val serverFile = api("$server.ts")
    val parameters = "${spec.prefix}Parameters"
    val parametersFile = api("$parameters.ts")
    val enumsFile = models("${spec.prefix}EnumTypes.ts")
    val authFile = models("${spec.prefix}AuthTypes.ts")
    val authBase = "${spec.prefix}Auth"
    val roles = "${spec.prefix}PermissionLevel"
    val rolesFile = models("$roles.ts")
    val error = "${spec.prefix}Error"
    val errorFile = errors("$error.ts")

    /** The file of the API's declaration [name] in the subpackage's `api` folder. */
    fun api(name: String): String = "$packageName/api/$name"

    /** The file of the API's declaration [name] in the subpackage's `models` folder. */
    fun models(name: String): String = "$packageName/models/$name"

    /** The file of the API's declaration [name] in the subpackage's `errors` folder. */
    fun errors(name: String): String = "$packageName/errors/$name"

    fun authClass(name: String): String = "${spec.prefix}${name}Auth"

    /** The parameter type of [auth]: the scheme's class when there is one, else the union of all. */
    fun authType(auth: AuthRequirement, file: TsFile): String {
        val type = auth.schemes.singleOrNull()?.let(::authClass) ?: authBase
        file.importFile("type $type", authFile)
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

    fun recordsFile(module: ApiModule): String = models("${spec.prefix}${module.name}Types.ts")

    fun serviceName(module: ApiModule): String = "${spec.prefix}${module.name}Api"

    fun serviceFile(module: ApiModule): String = api("${serviceName(module)}.ts")

    /** The file of the API's scrapers (see [TsScrapeEmitter]). */
    val scrapersFile = models("${spec.prefix}Scrapers.ts")

    /** The function reading [field] of [record] out of the HTML fragment it is sent as. */
    fun htmlFunction(record: RecordType, field: Field): String =
        "scrape${record.name}${field.name.replaceFirstChar(Char::uppercaseChar)}"

    fun decoderName(declaration: String): String = "decode$declaration"

    fun encoderName(declaration: String): String = "encode$declaration"

    /** Imports declaration [name] (or one of its functions) into [file]. */
    fun importDeclaration(file: TsFile, declaration: String, name: String = declaration) {
        file.importFile(name, fileByDeclaration.getValue(declaration))
    }

    fun importRuntime(file: TsFile, name: String) = file.importFile(name, "$runtimePath/index.ts")

    val resourcesName = "${spec.prefix}Resources"
    val resourcesFile = api("$resourcesName.ts")

    /** The file of the API's resource [name], in the subpackage's `resources` folder. */
    fun resource(name: String): String = "$packageName/resources/${spec.id}/$name.json"

    fun declaration(type: TypeRef.Named) = spec.resolve(type)

    fun isEnum(type: TypeRef.Named): Boolean = spec.resolve(type) is EnumType

    fun isRecord(type: TypeRef.Named): Boolean = spec.resolve(type) is RecordType

    /** Renders [type], importing what it references into [file] as types. */
    fun render(type: TypeRef, file: TsFile): String = when (type) {
        is TypeRef.Named -> {
            importDeclaration(file, type.name, "type ${type.name}")
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
            PrimitiveKind.FILE -> {
                importRuntime(file, "type FileUpload")
                "FileUpload"
            }
        }
    }

    private fun runtimeType(name: String, file: TsFile): String {
        importRuntime(file, "type $name")
        return name
    }
}
