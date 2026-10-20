package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
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
 * | Re-exports of all of the above | `index.ts`               |
 *
 * A record `X` is an interface with `decodeX(json)` and `encodeX(value)` functions; an enum
 * `X` is a string-literal union with a same-named `const` of its values and `decodeX(json)`.
 *
 * The hand-written runtime:
 * - The shared runtime package ([runtimeModule]): `LocalDate`, `LocalDateTime`, the lenient
 *   `decode*` helpers, the temporal codec, `pathSegment` and the request types.
 * - The API's client module ([clientModule]): `Esse3Client` with `json`, `jsonList` (empty on
 *   HTTP 404), `bytes` and `send`, and the `Esse3PermissionLevel` union of role names.
 */
internal class TsLayout(private val spec: ApiSpec, val runtimeModule: String, val clientModule: String) {
    val enumsFile = "${spec.prefix}EnumTypes.ts"
    val facadeName = "${spec.prefix}Api"
    val client = "${spec.prefix}Client"
    val roles = "${spec.prefix}PermissionLevel"

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
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> "string"
            PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "number"
            PrimitiveKind.BOOLEAN -> "boolean"
            PrimitiveKind.LOCAL_DATE -> runtimeType("LocalDate", file)
            PrimitiveKind.LOCAL_DATE_TIME -> runtimeType("LocalDateTime", file)
            PrimitiveKind.JSON_OBJECT -> runtimeType("JsonObject", file)
            PrimitiveKind.BINARY -> "Uint8Array"
        }
    }

    private fun runtimeType(name: String, file: TsFile): String {
        importRuntime(file, name)
        return name
    }
}
