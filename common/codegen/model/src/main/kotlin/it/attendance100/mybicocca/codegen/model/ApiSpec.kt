package it.attendance100.mybicocca.codegen.model

/**
 * Language-neutral description of a data API: what readers produce from an API's own spec
 * format, and what targets turn into an SDK.
 *
 * Every name in the model is a final logical identifier, already translated and made
 * unique by the reader. Declaration names are PascalCase and member names are camelCase;
 * targets only adapt them to their language's conventions and escape reserved words.
 *
 * @property id Stable lowercase identifier of the API (e.g. `esse3`), used for configuration
 *   and output paths.
 * @property prefix Prefix of the API's top-level names (e.g. `Esse3`). Declarations already
 *   carry it; targets also use it to name the API's hand-written runtime support types.
 * @property modules The API's sub-APIs, each grouping operations and the records they use.
 * @property enums Enumerations shared by every module.
 */
data class ApiSpec(
    val id: String,
    val prefix: String,
    val modules: List<ApiModule>,
    val enums: List<EnumType>,
) {
    /** Every declared record and enum, by name. */
    val declarations: Map<String, TypeDeclaration> by lazy {
        (modules.flatMap { it.records } + enums).associateBy { it.name }
    }

    /** Resolves a [TypeRef.Named] to its declaration. */
    fun resolve(ref: TypeRef.Named): TypeDeclaration =
        declarations[ref.name] ?: error("Unknown type '${ref.name}' in API '$id'")
}

/**
 * A sub-API: a group of operations served under a common [basePath], with the records it
 * declares.
 *
 * @property name PascalCase name without the API prefix (e.g. `Careers`).
 * @property basePath Path prefix of every operation, relative to the API's base URL.
 * @property records Records declared by this module, in declaration order.
 * @property operations Operations of this module, in declaration order.
 */
data class ApiModule(
    val name: String,
    val basePath: String,
    val records: List<RecordType>,
    val operations: List<Operation>,
)
