package it.attendance100.mybicocca.codegen.esse3.gen

import it.attendance100.mybicocca.codegen.esse3.Dictionary
import it.attendance100.mybicocca.codegen.esse3.spec.ParsedSpec
import java.io.File

object FacadeGenerator {

    private const val API_PACKAGE = "it.attendance100.mybicocca.data.api.esse3"

    fun generate(specs: List<ParsedSpec>, outputDir: File, dictionary: Dictionary) {
        val apiSpecs = specs.filter { it.operations.isNotEmpty() }
        if (apiSpecs.isEmpty()) return

        val file = File(outputDir, "Esse3Api.kt")
        val sb = StringBuilder()

        sb.appendLine("package $API_PACKAGE")
        sb.appendLine()
        sb.appendLine("import io.ktor.client.HttpClient")
        sb.appendLine("import io.ktor.client.HttpClientConfig")
        sb.appendLine("import io.ktor.client.plugins.contentnegotiation.ContentNegotiation")
        sb.appendLine("import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage")
        sb.appendLine("import io.ktor.client.plugins.cookies.HttpCookies")
        sb.appendLine("import io.ktor.serialization.kotlinx.json.json")
        sb.appendLine("import kotlinx.serialization.json.Json")
        sb.appendLine()
        sb.appendLine("class Esse3Api(httpClientConfig: HttpClientConfig<*>.() -> Unit = {}) : AutoCloseable {")
        sb.appendLine()
        sb.appendLine("    private val json = Json {")
        sb.appendLine("        coerceInputValues = true")
        sb.appendLine("        encodeDefaults = true")
        sb.appendLine("        ignoreUnknownKeys = true")
        sb.appendLine("        isLenient = true")
        sb.appendLine("        prettyPrint = false")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    private val client = HttpClient {")
        sb.appendLine("        httpClientConfig()")
        sb.appendLine()
        sb.appendLine("        install(ContentNegotiation) {")
        sb.appendLine("            json(json)")
        sb.appendLine("        }")
        sb.appendLine()
        sb.appendLine("        install(HttpCookies) {")
        sb.appendLine("            storage = AcceptAllCookiesStorage()")
        sb.appendLine("        }")
        sb.appendLine()
        sb.appendLine("        followRedirects = true")
        sb.appendLine("    }")
        sb.appendLine()

        for (spec in apiSpecs) {
            val className = dictionary.translate("Esse3${spec.specName}Api")
            val fieldName = className.removePrefix("Esse3").removeSuffix("Api").replaceFirstChar { it.lowercaseChar() }
            sb.appendLine("    val $fieldName = $className(client, json)")
        }

        sb.appendLine()
        sb.appendLine("    override fun close() {")
        sb.appendLine("        client.close()")
        sb.appendLine("    }")
        sb.appendLine("}")

        file.parentFile.mkdirs()
        file.writeText(sb.toString())
    }
}
