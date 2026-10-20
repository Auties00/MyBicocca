package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Where an API's generated declarations go, and the hand-written runtime declarations they
 * rely on. For an API with prefix `Esse3` in package `…esse3`:
 *
 * | Declaration                     | Location                               |
 * |---------------------------------|----------------------------------------|
 * | Records of module `Careers`     | `…esse3.dto.Esse3CareersTypes.kt`      |
 * | Every enum                      | `…esse3.dto.Esse3EnumTypes.kt`         |
 * | Operations of module `Careers`  | `…esse3.api.Esse3CareersApi`           |
 * | Facade                          | `…esse3.api.Esse3Api`                  |
 *
 * The runtime the API provides by hand:
 * - `…api.Esse3AbstractApi`: base class of every module, with the `execute*` request helpers.
 * - `…api.Esse3Runtime`: the facade's shared `json` and `createHttpClient(json, config)`.
 * - `…dto.Esse3PermissionLevel`: enum of the role names operations declare.
 * - The serializers named by [KotlinTarget.Options.temporalSerializers].
 */
internal class KotlinLayout(private val spec: ApiSpec, packageName: String) {
    val dtoPackage = "$packageName.dto"
    val apiPackage = "$packageName.api"

    val enumsFileName = "${spec.prefix}EnumTypes.kt"
    val facadeName = "${spec.prefix}Api"

    val abstractApi = "$apiPackage.${spec.prefix}AbstractApi"
    val runtime = "$apiPackage.${spec.prefix}Runtime"
    val roles = "$dtoPackage.${spec.prefix}PermissionLevel"

    fun recordsFileName(module: ApiModule): String = "${spec.prefix}${module.name}Types.kt"

    fun serviceName(module: ApiModule): String = "${spec.prefix}${module.name}Api"

    /** Renders [type], importing what it references into [file]. */
    fun render(type: TypeRef, file: KotlinFile): String = when (type) {
        is TypeRef.Named -> {
            file.import("$dtoPackage.${type.name}")
            type.name
        }
        is TypeRef.ListOf -> "List<${render(type.element, file)}>"
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> "String"
            PrimitiveKind.INT32 -> "Int"
            PrimitiveKind.INT64 -> "Long"
            PrimitiveKind.FLOAT32 -> "Float"
            PrimitiveKind.FLOAT64 -> "Double"
            PrimitiveKind.BOOLEAN -> "Boolean"
            PrimitiveKind.LOCAL_DATE -> imported("java.time.LocalDate", file)
            PrimitiveKind.LOCAL_DATE_TIME -> imported("java.time.LocalDateTime", file)
            PrimitiveKind.JSON_OBJECT -> imported("kotlinx.serialization.json.JsonObject", file)
            PrimitiveKind.BINARY -> imported("io.ktor.utils.io.ByteReadChannel", file)
        }
    }

    private fun imported(qualifiedName: String, file: KotlinFile): String {
        file.import(qualifiedName)
        return qualifiedName.substringAfterLast('.')
    }
}
