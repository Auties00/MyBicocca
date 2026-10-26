package it.attendance100.mybicocca.codegen.model

/**
 * Data an SDK ships along with its code, such as a catalog crawled from a website: a JSON value
 * of [type], which the resource builder named [builder] produces, and which every target writes
 * into its platform's resources with a typed loader.
 *
 * @property name camelCase name of the loader, and of the data's file (`<name>.json`).
 * @property type The record the data decodes as.
 * @property builder Id of the builder that fetches the data again.
 */
data class Resource(
    val name: String,
    val type: TypeRef.Named,
    val builder: String,
    val description: String? = null,
)
