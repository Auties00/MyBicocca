package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Where an API's generated declarations go. For an API with prefix `Esse3` in package `…esse3`:
 *
 * | Declaration                     | Location                                   |
 * |---------------------------------|--------------------------------------------|
 * | Records of module `Careers`     | `…esse3.dto.Esse3CareersTypes.kt`          |
 * | Every enum                      | `…esse3.dto.Esse3EnumTypes.kt`             |
 * | Operations of module `Careers`  | `…esse3.api.Esse3CareersApi`               |
 * | Facade                          | `…esse3.api.Esse3Api`                      |
 * | Client, JSON and request policy | `…esse3.api.Esse3Runtime` (internal)       |
 * | Exception                       | `…esse3.exception.Esse3Exception`          |
 * | Roles, if operations name any   | `…esse3.dto.Esse3PermissionLevel` (enum)   |
 * | Servers, if any                 | `…esse3.api.Esse3Server` (enum)            |
 * | Auth schemes, if any            | `…esse3.dto.Esse3AuthTypes.kt`             |
 * | Serializers, if any             | `…esse3.dto.Esse3Serializers.kt`           |
 * | Scrapers, if any                | `…esse3.dto.Esse3Scrapers.kt`              |
 *
 * Modules extend `RestApi`, in [runtimePackage]: the [KotlinRuntime] that [KotlinTarget] writes
 * as its shared files.
 */
internal class KotlinLayout(private val spec: ApiSpec, packageName: String, val runtimePackage: String) {
    val dtoPackage = "$packageName.dto"
    val apiPackage = "$packageName.api"

    val enumsFileName = "${spec.prefix}EnumTypes.kt"
    val facadeName = "${spec.prefix}Api"

    val runtime = "$apiPackage.${spec.prefix}Runtime"
    val exception = "$packageName.exception.${spec.prefix}Exception"
    val roles = "$dtoPackage.${spec.prefix}PermissionLevel"
    val rolesFileName = "${spec.prefix}PermissionTypes.kt"
    val server = "$apiPackage.${spec.prefix}Server"

    /**
     * The API parameters [module] takes: those it uses, and those the client's retry requests
     * name, which `RestApi` fills their placeholders with.
     */
    fun moduleParameters(module: ApiModule): List<Parameter> {
        val used = spec.parametersOf(module).toSet() + spec.retryParameters
        return spec.parameters.filter { it in used }
    }

    /** A declaration of the shared runtime. */
    fun runtime(name: String): String = "$runtimePackage.$name"

    val authFileName = "${spec.prefix}AuthTypes.kt"
    val authBase = "${spec.prefix}Auth"

    /** The class of the credentials of auth scheme [name]. */
    fun authClass(name: String): String = "${spec.prefix}${name}Auth"

    /**
     * The parameter type of [auth]: the scheme's class when there is one, else the interface
     * every scheme implements.
     */
    fun authType(auth: AuthRequirement, file: KotlinFile): String {
        val type = auth.schemes.singleOrNull()?.let(::authClass) ?: authBase
        file.import("$dtoPackage.$type")
        return type
    }

    fun authDescription(auth: AuthRequirement): String {
        val schemes = auth.schemes.joinToString(" or ") { "[${authClass(it)}]" }
        return if (auth.required) "The credentials: $schemes." else "The credentials, if any: $schemes."
    }

    fun recordsFileName(module: ApiModule): String = "${spec.prefix}${module.name}Types.kt"

    /** The serializer reading [field] of [record] out of the HTML fragment it is sent as. */
    fun htmlSerializer(record: RecordType, field: Field): String =
        "${record.name}${field.name.replaceFirstChar(Char::uppercaseChar)}HtmlSerializer"

    fun serviceName(module: ApiModule): String = "${spec.prefix}${module.name}Api"

    /** Renders [type], importing what it references into [file]. */
    fun render(type: TypeRef, file: KotlinFile): String = when (type) {
        is TypeRef.Named -> {
            file.import("$dtoPackage.${type.name}")
            type.name
        }
        is TypeRef.ListOf -> "List<${render(type.element, file)}>"
        is TypeRef.MapOf -> "Map<${render(type.key, file)}, ${render(type.value, file)}>"
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> "String"
            PrimitiveKind.INT32 -> "Int"
            PrimitiveKind.INT64 -> "Long"
            PrimitiveKind.FLOAT32 -> "Float"
            PrimitiveKind.FLOAT64 -> "Double"
            PrimitiveKind.BOOLEAN -> "Boolean"
            PrimitiveKind.LOCAL_DATE -> imported("java.time.LocalDate", file)
            PrimitiveKind.LOCAL_DATE_TIME -> imported("java.time.LocalDateTime", file)
            PrimitiveKind.LOCAL_TIME -> imported("java.time.LocalTime", file)
            PrimitiveKind.JSON_OBJECT -> imported("kotlinx.serialization.json.JsonObject", file)
            PrimitiveKind.JSON_VALUE -> imported("kotlinx.serialization.json.JsonElement", file)
            PrimitiveKind.BINARY -> imported("io.ktor.utils.io.ByteReadChannel", file)
            PrimitiveKind.FILE -> imported(runtime("FileUpload"), file)
        }
    }

    private fun imported(qualifiedName: String, file: KotlinFile): String {
        file.import(qualifiedName)
        return qualifiedName.substringAfterLast('.')
    }
}
