package it.attendance100.mybicocca.codegen.reader.openapi

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.parser.core.models.ParseOptions
import it.attendance100.mybicocca.codegen.core.ReaderContext
import it.attendance100.mybicocca.codegen.core.SpecReader
import it.attendance100.mybicocca.codegen.core.decodeOptions
import it.attendance100.mybicocca.codegen.model.ApiSpec
import kotlinx.serialization.Serializable
import kotlin.io.path.absolutePathString

/**
 * Reads an API described by hand in OpenAPI 3.0, with the `x-mb-*` extensions documented in
 * [Extensions] for what plain OpenAPI can't say (type prefix, envelopes, auth schemes as
 * headers, fixed headers, open enums). Use it for APIs that publish no spec of their own.
 */
class OpenApiReader : SpecReader {
    override val id: String = "openapi"

    /** @property spec The OpenAPI file, relative to the spec directory. */
    @Serializable
    data class Options(val spec: String)

    override fun read(context: ReaderContext): ApiSpec {
        val file = context.specDir.resolve(context.options.decodeOptions<Options>().spec)
        val result = OpenAPIParser().readLocation(file.absolutePathString(), null, ParseOptions().apply { isResolve = false })
        val openApi = result.openAPI ?: error("Failed to parse $file: ${result.messages}")
        require(result.messages.isNullOrEmpty()) { "Invalid OpenAPI in $file:\n" + result.messages.joinToString("\n") { "  - $it" } }
        return OpenApiModelBuilder(openApi).build()
    }
}
