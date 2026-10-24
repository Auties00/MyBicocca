package it.attendance100.mybicocca.codegen.target.kotlin

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.ScrapeKind
import it.attendance100.mybicocca.codegen.model.ScrapeStep
import it.attendance100.mybicocca.codegen.model.Scrapes
import it.attendance100.mybicocca.codegen.model.TypeRef

/**
 * Emits `<Prefix>Scrapers`, which reads the API's scraped records and HTML responses out of
 * Jsoup nodes, with the helpers its pipelines compile to as private functions of the same file:
 *
 * - `<record>(node)` reads a record, or returns null when a required value is missing; it is
 *   public, so hand-written code can read a page it fetched itself;
 * - `<module><Operation>(document)` reads an operation's response, for the runtime's
 *   `executeHtml*` helpers;
 * - a serializer per field sent as an HTML fragment ([Codec.EmbeddedHtml]), named by
 *   [KotlinLayout.htmlSerializer].
 *
 * Each step of a pipeline becomes a call wrapping the previous step's expression; every helper
 * accepts and returns null, so a missing value travels to the end of the pipeline.
 */
internal class ScrapeEmitter(private val spec: ApiSpec, private val layout: KotlinLayout, private val records: RecordEmitter) {
    val fileName = "${spec.prefix}Scrapers.kt"
    val objectName = "${spec.prefix}Scrapers"

    private val scrapedRecords = spec.modules.flatMap { it.records }.filter { it.scrape != null }
    private val htmlOperations = spec.modules.flatMap { module ->
        module.operations.filter { it.response is ResponseBody.Html }.map { module to it }
    }

    private val htmlFields = spec.modules.flatMap { it.records }.flatMap { record ->
        record.fields.filter { it.codec is Codec.EmbeddedHtml }.map { record to it }
    }

    val isNeeded: Boolean get() = scrapedRecords.isNotEmpty() || htmlOperations.isNotEmpty() || htmlFields.isNotEmpty()

    /** The function reading [operation]'s response. */
    fun responseFunction(module: ApiModule, operation: Operation): String =
        module.name.replaceFirstChar(Char::lowercaseChar) + operation.name.replaceFirstChar(Char::uppercaseChar)

    fun emit(file: KotlinFile) {
        file.import("org.jsoup.nodes.Node")
        val compiler = Compiler(file)
        // The object is rendered first, so the compiler knows the patterns, enums and helpers it used.
        val scrapers = file.body.nested {
            block("object $objectName {") {
                separated(scrapedRecords) { record(it, compiler) }
                if (scrapedRecords.isNotEmpty() && htmlOperations.isNotEmpty()) line()
                separated(htmlOperations) { (module, operation) -> response(module, operation, compiler) }
            }
        }
        val serializers = htmlFields.map { (record, field) -> file.body.nested { htmlSerializer(record, field, compiler) } }
        file.body.apply {
            if (scrapedRecords.isNotEmpty() || htmlOperations.isNotEmpty()) lines(scrapers)
            for (serializer in serializers) {
                if (scrapedRecords.isNotEmpty() || htmlOperations.isNotEmpty() || serializer != serializers.first()) line()
                lines(serializer)
            }
            for (enum in compiler.enums) {
                line()
                enumParser(enum, file)
            }
            if (compiler.patterns.isNotEmpty()) line()
            compiler.patterns.forEachIndexed { index, pattern -> line("private val ${patternName(index)} = Regex(${KotlinSyntax.string(pattern)})") }
            for (helper in Helper.entries.filter { it in compiler.helpers }) {
                line()
                lines(helper.code)
                helper.imports.forEach(file::import)
            }
        }
    }

    /** Reads every field in the constructor call, so a field's name never shadows the node. */
    private fun CodeWriter.record(record: RecordType, compiler: Compiler) {
        val type = layout.render(TypeRef.Named(record.name), compiler.file)
        val missing = record.scrape!!.missing.let { texts ->
            if (texts.isEmpty()) "emptyList()" else texts.joinToString(", ", "listOf(", ")") { KotlinSyntax.string(it) }
        }
        val arguments = record.fields.mapNotNull { field ->
            field.scrape?.let {
                val value = compiler.value(field.type, it, "node", missing)
                "${KotlinSyntax.identifier(field.name)} = ${value.code}${if (value.nullable) fallback(field) else ""}"
            }
        }
        line("/** Reads [$type] out of [node], or returns null when a value it requires is missing. */")
        if (arguments.isEmpty()) {
            line("fun ${functionName(record)}(node: Node): $type? = $type()")
        } else {
            commaList("fun ${functionName(record)}(node: Node): $type? = $type(", arguments, ")")
        }
    }

    private fun CodeWriter.response(module: ApiModule, operation: Operation, compiler: Compiler) {
        val response = operation.response as ResponseBody.Html
        val type = layout.render(response.type, compiler.file)
        line("internal fun ${responseFunction(module, operation)}(document: Node): $type? =")
        indent { line(compiler.value(response.type, response.scrape, "document", "emptyList()").code) }
    }

    /** The serializer reading [field] out of the HTML fragment it is sent as; it writes the value back as an empty string. */
    private fun CodeWriter.htmlSerializer(record: RecordType, field: Field, compiler: Compiler) {
        val file = compiler.file
        listOf(
            "kotlinx.serialization.KSerializer", "kotlinx.serialization.descriptors.PrimitiveKind", "kotlinx.serialization.descriptors.PrimitiveSerialDescriptor",
            "kotlinx.serialization.encoding.Decoder", "kotlinx.serialization.encoding.Encoder", "kotlinx.serialization.json.JsonDecoder",
            "kotlinx.serialization.json.JsonPrimitive", "kotlinx.serialization.json.contentOrNull", "org.jsoup.Jsoup",
        ).forEach(file::import)
        val name = layout.htmlSerializer(record, field)
        val isList = field.type is TypeRef.ListOf
        val type = layout.render(field.type, file) + if (isList) "" else "?"
        val scrape = (field.codec as Codec.EmbeddedHtml).scrape
        line("/** [${record.name}.${field.name}], read out of the HTML fragment it is sent as. It is written back as an empty string. */")
        block("object $name : KSerializer<$type> {") {
            val descriptor = "PrimitiveSerialDescriptor(${KotlinSyntax.string(name)}, PrimitiveKind.STRING)"
            if (isList) {
                line("override val descriptor = $descriptor")
            } else {
                file.import("kotlinx.serialization.descriptors.nullable")
                line("override val descriptor = $descriptor.nullable")
            }
            line()
            line("override fun serialize(encoder: Encoder, value: $type) = encoder.encodeString(\"\")")
            line()
            block("override fun deserialize(decoder: Decoder): $type {") {
                line("val html = if (decoder is JsonDecoder) (decoder.decodeJsonElement() as? JsonPrimitive)?.contentOrNull else decoder.decodeString()")
                line("if (html.isNullOrBlank()) return ${if (isList) "emptyList()" else "null"}")
                compiler.qualifier = "$objectName."
                line("return ${compiler.value(field.type, scrape, "Jsoup.parseBodyFragment(html)", "emptyList()").code}")
                compiler.qualifier = ""
            }
        }
    }

    /** What a field takes when its pipeline yields no value: its default, or no record at all. */
    private fun fallback(field: Field): String {
        return when (val default = field.default) {
            null -> if (field.nullable) "" else " ?: return null"
            DefaultValue.Null -> ""
            else -> " ?: " + records.defaultValue(field.type, default)
        }
    }

    private fun CodeWriter.enumParser(enum: EnumType, file: KotlinFile) {
        val type = layout.render(TypeRef.Named(enum.name), file)
        block("private fun ${enumFunction(enum)}(value: String): $type? = when (value) {") {
            for (value in enum.values) line("${KotlinSyntax.string(value.wireValue)} -> $type.${value.name}")
            line(
                when {
                    !enum.open -> "else -> null"
                    enum.values.any { it.name == "Unknown" } -> "else -> $type.Unknown"
                    else -> "else -> $type.Unknown(value)"
                }
            )
        }
    }

    private fun functionName(record: RecordType): String =
        record.name.removePrefix(spec.prefix).ifEmpty { record.name }.replaceFirstChar(Char::lowercaseChar)

    private fun enumFunction(enum: EnumType): String = "parse${enum.name}"

    /** Compiles pipelines to Kotlin expressions, collecting the patterns, enums and helpers they use. */
    private inner class Compiler(val file: KotlinFile) {
        val patterns = linkedSetOf<String>()
        val enums = linkedSetOf<EnumType>()
        val helpers = mutableSetOf<Helper>()

        /** Lambda parameters are numbered, so nested ones don't shadow each other. */
        private var lambdas = 0

        /** What record functions are called through: nothing inside the scrapers' object, the object outside it. */
        var qualifier = ""

        /**
         * The expression reading a value of [type] with [steps] from [input], a non-null node,
         * and whether it may be null: lists never are.
         */
        fun value(type: TypeRef, steps: List<ScrapeStep>, input: String, missing: String): Expression {
            val scraped = pipeline(steps, Expression(input, nullable = false), ScrapeKind.ELEMENT, missing)
            return when (type) {
                is TypeRef.ListOf -> if (Scrapes.isRecordList(type.element, spec)) {
                    Expression("${scraped.code}.mapNotNull { $qualifier${functionName(record(type.element))}(it) }", nullable = false)
                } else {
                    val text = Expression("${use(Helper.TEXT)}(it, null, $missing)", nullable = true)
                    Expression("${scraped.code}.mapNotNull { ${convert(type.element, text).code} }", nullable = false)
                }
                else -> when (Scrapes.kindFor(type, spec)) {
                    ScrapeKind.ELEMENT -> if (scraped.nullable) {
                        scraped.then("let { $qualifier${functionName(record(type))}(it) }", nullable = true)
                    } else {
                        Expression("$qualifier${functionName(record(type))}(${scraped.code})", nullable = true)
                    }
                    ScrapeKind.TEXT -> convert(type, scraped)
                    else -> scraped
                }
            }
        }

        /** Converts text to [type]: numbers that don't parse and unknown values of closed enums are missing. */
        private fun convert(type: TypeRef, text: Expression): Expression = when (type) {
            is TypeRef.Named -> {
                val enum = spec.resolve(type) as EnumType
                enums += enum
                text.then("let(::${enumFunction(enum)})", nullable = true)
            }
            is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> text
                PrimitiveKind.INT32 -> text.then("toIntOrNull()", nullable = true)
                PrimitiveKind.INT64 -> text.then("toLongOrNull()", nullable = true)
                PrimitiveKind.FLOAT32 -> text.then("toFloatOrNull()", nullable = true)
                PrimitiveKind.FLOAT64 -> text.then("toDoubleOrNull()", nullable = true)
                else -> error("Can't scrape a ${type.kind}")
            }
            else -> error("Can't scrape $type from text")
        }

        private fun record(type: TypeRef): RecordType = spec.resolve(type as TypeRef.Named) as RecordType

        private fun pipeline(steps: List<ScrapeStep>, input: Expression, kind: ScrapeKind, missing: String): Expression {
            var expression = input
            var current = kind
            for (step in steps) {
                expression = step(step, expression, current, missing)
                current = Scrapes.output(listOf(step), current) ?: error("Invalid scrape step $step after $current")
            }
            return expression
        }

        private fun step(step: ScrapeStep, value: Expression, kind: ScrapeKind, missing: String): Expression {
            val v = value.code
            return when (step) {
                is ScrapeStep.Select -> call(Helper.SELECT, v, str(step.selector))
                is ScrapeStep.SelectAll -> Expression("${use(Helper.SELECT_ALL)}($v, ${str(step.selector)})", nullable = false)
                ScrapeStep.Root -> value.then("ownerDocument()", nullable = true)
                ScrapeStep.Next -> call(Helper.NEXT, v)
                ScrapeStep.LastSibling -> value.then("siblingNodes()", nullable = false).then("lastOrNull()", nullable = true)
                is ScrapeStep.Follow -> call(Helper.FOLLOW, v, str(step.attribute))
                is ScrapeStep.Sections -> Expression("${use(Helper.SECTIONS)}($v, ${str(step.header)})", nullable = false)
                ScrapeStep.First -> Expression("$v.firstOrNull()", nullable = true)
                is ScrapeStep.Where -> {
                    val candidate = "candidate${++lambdas}"
                    val test = pipeline(step.test, Expression(candidate, nullable = false), ScrapeKind.ELEMENT, missing)
                    Expression("$v.filter { $candidate -> ${if (test.nullable) "${test.code} == true" else test.code} }", nullable = false)
                }
                is ScrapeStep.Text -> call(Helper.TEXT, v, step.exclude?.let(::str) ?: "null", missing)
                ScrapeStep.OwnText -> call(Helper.OWN_TEXT, v, missing)
                ScrapeStep.Html -> {
                    file.import("org.jsoup.nodes.Element")
                    call(Helper.PRESENT, "($v as? Element)?.html()")
                }
                is ScrapeStep.Attribute -> call(Helper.ATTRIBUTE, v, str(step.name), missing)
                is ScrapeStep.Url -> call(Helper.PRESENT, value.then("absUrl(${str(step.attribute)})", nullable = false).code)
                ScrapeStep.Lines -> call(Helper.LINES, v, missing)
                ScrapeStep.Exists -> Expression("($v != null)", nullable = false)
                is ScrapeStep.Regex -> call(Helper.REGEX, v, pattern(step.pattern))
                is ScrapeStep.RemovePrefix -> call(Helper.PRESENT, value.then("removePrefix(${str(step.prefix)})", nullable = false).code)
                is ScrapeStep.RemoveSuffix -> call(Helper.PRESENT, value.then("removeSuffix(${str(step.suffix)})", nullable = false).code)
                is ScrapeStep.Unwrap -> call(Helper.PRESENT, value.then("removeSurrounding(${str(step.prefix)}, ${str(step.suffix)})", nullable = false).code)
                is ScrapeStep.NullIf -> call(Helper.NULL_IF, v, list(step.values))
                is ScrapeStep.QueryParameter -> call(Helper.QUERY_PARAMETER, v, str(step.name))
                is ScrapeStep.Replace -> call(Helper.REPLACE, v, "mapOf(${step.replacements.entries.joinToString(", ") { "${str(it.key)} to ${str(it.value)}" }})")
                is ScrapeStep.Equals -> value.then("equals(${str(step.value)}, ignoreCase = true)", nullable = false)
                is ScrapeStep.Contains -> value.then("contains(${str(step.value)}, ignoreCase = true)", nullable = false)
                ScrapeStep.Not -> value.then("not()", nullable = false)
                is ScrapeStep.FirstOf -> firstOf(step, value, kind, missing)
            }
        }

        /** Alternatives yielding elements are tried until one isn't empty; the others until one isn't null. */
        private fun firstOf(step: ScrapeStep.FirstOf, value: Expression, kind: ScrapeKind, missing: String): Expression {
            val input = "input${++lambdas}"
            val alternatives = step.alternatives.map { pipeline(it, Expression(input, value.nullable), kind, missing) }
            return if (Scrapes.output(step.alternatives.first(), kind) == ScrapeKind.ELEMENTS) {
                val joined = alternatives.map { it.code }.reduce { first, next -> "$first.ifEmpty { $next }" }
                Expression("${value.code}.let { $input -> $joined }", nullable = false)
            } else {
                val nullable = alternatives.last().nullable
                Expression("${value.code}.let { $input -> ${alternatives.joinToString(" ?: ") { it.code }} }", nullable)
            }
        }

        /** A call of [helper], whose result is null when missing. */
        private fun call(helper: Helper, vararg arguments: String): Expression =
            Expression("${use(helper)}(${arguments.joinToString(", ")})", nullable = true)

        /** Records that the generated file needs [helper], and with it the helpers it calls. */
        private fun use(helper: Helper): String {
            if (helpers.add(helper)) helper.uses.forEach(::use)
            return helper.function
        }

        private fun pattern(pattern: String): String {
            patterns += pattern
            return patternName(patterns.indexOf(pattern))
        }

        private fun str(value: String): String = KotlinSyntax.string(value)

        private fun list(values: List<String>): String = values.joinToString(", ", "listOf(", ")") { str(it) }
    }

    private fun patternName(index: Int): String = "PATTERN_$index"

    /** Kotlin code, and whether it may evaluate to null. */
    data class Expression(val code: String, val nullable: Boolean) {
        /** A member access on this value, null-safe when this may be null; [nullable] is whether the member may return null. */
        fun then(member: String, nullable: Boolean): Expression =
            Expression(if (this.nullable) "$code?.$member" else "$code.$member", this.nullable || nullable)
    }

    /**
     * The steps' runtime, written into the generated file so the API's hand-written runtime
     * needs nothing for scraping; only the helpers a spec uses are written.
     */
    private enum class Helper(val function: String, val code: String, val uses: List<Helper> = emptyList(), val imports: List<String> = emptyList()) {
        PRESENT(
            "present",
            """
            /** The text trimmed, or null when blank or a text the page shows for a missing value. */
            private fun present(text: String?, missing: List<String> = emptyList()): String? {
                val value = text?.replace('\u00a0', ' ')?.trim() ?: return null
                return value.takeIf { it.isNotEmpty() && missing.none { absent -> absent.equals(it, ignoreCase = true) } }
            }
            """.trimIndent(),
        ),
        SELECT(
            "select",
            "private fun select(node: Node?, selector: String): Node? = (node as? Element)?.selectFirst(selector)",
            imports = listOf("org.jsoup.nodes.Element"),
        ),
        SELECT_ALL(
            "selectAll",
            "private fun selectAll(node: Node?, selector: String): List<Node> = (node as? Element)?.select(selector).orEmpty()",
            imports = listOf("org.jsoup.nodes.Element"),
        ),
        NEXT(
            "next",
            "private fun next(node: Node?): Node? = (node as? Element)?.nextElementSibling()",
            imports = listOf("org.jsoup.nodes.Element"),
        ),
        ATTRIBUTE(
            "attribute",
            """
            private fun attribute(node: Node?, name: String, missing: List<String>): String? =
                present(node?.takeIf { it.hasAttr(name) }?.attr(name), missing)
            """.trimIndent(),
            listOf(PRESENT),
        ),
        FOLLOW(
            "follow",
            """
            private fun follow(node: Node?, attribute: String): Node? {
                val selector = attribute(node, attribute, emptyList()) ?: return null
                return runCatching { node?.ownerDocument()?.selectFirst(selector) }.getOrNull()
            }
            """.trimIndent(),
            listOf(ATTRIBUTE),
        ),
        SECTIONS(
            "sections",
            """
            private fun sections(node: Node?, header: String): List<Node> {
                val element = node as? Element ?: return emptyList()
                val sections = mutableListOf<Element>()
                for (child in element.children()) {
                    if (child.`is`(header)) sections += Element("section").also { it.setBaseUri(element.baseUri()) }
                    sections.lastOrNull()?.appendChild(child.clone())
                }
                return sections
            }
            """.trimIndent(),
            imports = listOf("org.jsoup.nodes.Element"),
        ),
        TEXT(
            "text",
            """
            private fun text(node: Node?, exclude: String?, missing: List<String>): String? = present(
                when (node) {
                    is TextNode -> node.text()
                    is Element -> if (exclude == null) node.text() else node.clone().also { it.select(exclude).remove() }.text()
                    else -> null
                },
                missing,
            )
            """.trimIndent(),
            listOf(PRESENT),
            listOf("org.jsoup.nodes.Element", "org.jsoup.nodes.TextNode"),
        ),
        OWN_TEXT(
            "ownText",
            """
            private fun ownText(node: Node?, missing: List<String>): String? = present(
                when (node) {
                    is TextNode -> node.text()
                    is Element -> node.ownText()
                    else -> null
                },
                missing,
            )
            """.trimIndent(),
            listOf(PRESENT),
            listOf("org.jsoup.nodes.Element", "org.jsoup.nodes.TextNode"),
        ),
        LINES(
            "lines",
            """
            private fun lines(node: Node?, missing: List<String>): String? {
                val siblings = node?.siblingNodes() ?: return null
                val rendered = buildString {
                    for (sibling in siblings) {
                        when {
                            sibling is TextNode -> append(sibling.wholeText)
                            sibling is Element && sibling.tagName() == "br" -> append('\n')
                            sibling is Element && sibling.tagName() == "ul" -> sibling.select("li").forEach { append("\n• ").append(it.text().trim()) }
                            sibling is Element -> append(sibling.text().trim())
                        }
                    }
                }
                return present(rendered.lines().joinToString("\n") { it.trim() }.replace(Regex("\n{3,}"), "\n\n"), missing)
            }
            """.trimIndent(),
            listOf(PRESENT),
            listOf("org.jsoup.nodes.Element", "org.jsoup.nodes.TextNode"),
        ),
        REGEX(
            "regex",
            """
            private fun regex(text: String?, pattern: Regex): String? {
                val match = pattern.find(text ?: return null) ?: return null
                return present(if (match.groupValues.size > 1) match.groupValues[1] else match.value)
            }
            """.trimIndent(),
            listOf(PRESENT),
        ),
        NULL_IF(
            "nullIf",
            """
            private fun nullIf(text: String?, values: List<String>): String? =
                text?.takeIf { value -> values.none { it.equals(value, ignoreCase = true) } }
            """.trimIndent(),
        ),
        QUERY_PARAMETER(
            "queryParameter",
            """
            private fun queryParameter(text: String?, name: String): String? =
                present(text?.let { runCatching { Url(it).parameters[name] }.getOrNull() })
            """.trimIndent(),
            listOf(PRESENT),
            listOf("io.ktor.http.Url"),
        ),
        REPLACE(
            "replace",
            """
            private fun replace(text: String?, replacements: Map<String, String>): String? =
                text?.let { value -> replacements.entries.firstOrNull { it.key.equals(value, ignoreCase = true) }?.value ?: value }
            """.trimIndent(),
        ),
    }
}
