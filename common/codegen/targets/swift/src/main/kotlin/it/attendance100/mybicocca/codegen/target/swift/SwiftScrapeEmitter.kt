package it.attendance100.mybicocca.codegen.target.swift

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
 * parsed pages, through the runtime's `Scrape` helpers over `ScrapeNode`s:
 *
 * - `<record>(_:)` reads a record, or returns nil when a required value is missing; it is
 *   public, so hand-written code can read a page it fetched itself;
 * - `<module><Operation>(_:)` reads an operation's response, for the client's `html`.
 *
 * Every helper accepts and returns optionals, so a missing value travels to the end of a
 * pipeline.
 */
internal class SwiftScrapeEmitter(private val spec: ApiSpec, private val layout: SwiftLayout, private val records: SwiftRecordEmitter) {
    val typeName = layout.scrapers
    val fileName = "$typeName.swift"

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

    fun emit(out: CodeWriter) {
        out.line("/// Reads the ${spec.prefix} API's scraped records and pages out of parsed HTML.")
        out.block("public enum $typeName {") {
            val sections = buildList<CodeWriter.() -> Unit> {
                scrapedRecords.forEach { add { record(it) } }
                htmlOperations.forEach { (module, operation) -> add { response(module, operation) } }
                htmlFields.forEach { (record, field) -> add { htmlField(record, field) } }
            }
            separated(sections) { it(this) }
        }
    }

    /** Reads each field into a numbered local, so a field's name never shadows the node. */
    private fun CodeWriter.record(record: RecordType) {
        val missing = record.scrape!!.missing
        line("/// Reads ``${record.name}`` out of `node`, or returns nil when a value it requires is missing.")
        block("public static func ${functionName(record)}(_ node: ScrapeNode) -> ${record.name}? {") {
            if (missing.isNotEmpty()) line("let missing = ${missing.joinToString(", ", "[", "]") { SwiftSyntax.string(it) }}")
            val compiler = Compiler(if (missing.isEmpty()) "[]" else "missing")
            val arguments = mutableListOf<String>()
            record.fields.forEachIndexed { index, field ->
                val scrape = field.scrape ?: return@forEachIndexed
                val local = "value$index"
                val value = compiler.value(field.type, scrape, "node")
                when {
                    !value.optional -> line("let $local = ${value.code}")
                    field.default == null && !field.nullable -> line("guard let $local = ${value.code} else { return nil }")
                    else -> line("let $local = ${value.code}${fallback(field)}")
                }
                arguments += "${SwiftSyntax.identifier(field.name)}: $local"
            }
            if (arguments.isEmpty()) line("return ${record.name}()") else commaList("return ${record.name}(", arguments, ")")
        }
    }

    private fun CodeWriter.response(module: ApiModule, operation: Operation) {
        val response = operation.response as ResponseBody.Html
        block("static func ${responseFunction(module, operation)}(_ document: ScrapeNode) -> ${layout.render(response.type)}? {") {
            line(Compiler("[]").value(response.type, response.scrape, "document").code)
        }
    }

    /** Reads [field] of [record] out of the HTML fragment it is sent as, for the record's decoder. */
    private fun CodeWriter.htmlField(record: RecordType, field: Field) {
        val scrape = (field.codec as Codec.EmbeddedHtml).scrape
        val type = layout.render(field.type) + if (field.type is TypeRef.ListOf) "" else "?"
        block("static func ${layout.htmlFunction(record, field)}(_ html: String) -> $type {") {
            val fallback = if (field.type is TypeRef.ListOf) "[]" else "nil"
            line("guard let document = Scrape.fragment(html) else { return $fallback }")
            line("return ${Compiler("[]").value(field.type, scrape, "document").code}")
        }
    }

    /** What a field takes when its pipeline yields no value: its default, or nil. */
    private fun fallback(field: Field): String = when (val default = field.default) {
        null, DefaultValue.Null -> ""
        else -> " ?? " + records.defaultLiteral(field.type, default)
    }

    private fun functionName(record: RecordType): String =
        SwiftSyntax.identifier(record.name.removePrefix(spec.prefix).ifEmpty { record.name }.replaceFirstChar(Char::lowercaseChar))

    /** Swift code, and whether it evaluates to an optional. */
    private data class Expression(val code: String, val optional: Boolean)

    /** Compiles pipelines to Swift expressions; [missing] is the expression of the record's missing texts. */
    private inner class Compiler(private val missing: String) {
        /** Closure parameters are numbered, so nested ones don't shadow each other. */
        private var closures = 0

        fun value(type: TypeRef, steps: List<ScrapeStep>, input: String): Expression {
            val scraped = pipeline(steps, Expression(input, optional = false), ScrapeKind.ELEMENT)
            return when (type) {
                is TypeRef.ListOf -> if (Scrapes.isRecordList(type.element, spec)) {
                    Expression("${scraped.code}.compactMap { ${functionName(record(type.element))}(\$0) }", optional = false)
                } else {
                    val text = Expression("Scrape.text(\$0, exclude: nil, missing: $missing)", optional = true)
                    Expression("${scraped.code}.compactMap { ${convert(type.element, text).code} }", optional = false)
                }
                else -> when (Scrapes.kindFor(type, spec)) {
                    ScrapeKind.ELEMENT -> if (scraped.optional) {
                        Expression("${scraped.code}.flatMap { ${functionName(record(type))}(\$0) }", optional = true)
                    } else {
                        Expression("${functionName(record(type))}(${scraped.code})", optional = true)
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
                // Open enums keep unknown values, so only closed ones have a failable initializer.
                val map = if (enum.open) "map" else "flatMap"
                Expression("${text.code}.$map(${enum.name}.init(wireValue:))", optional = true)
            }
            is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> text
                PrimitiveKind.INT32 -> Expression("Scrape.int(${text.code})", optional = true)
                PrimitiveKind.INT64 -> Expression("Scrape.int64(${text.code})", optional = true)
                PrimitiveKind.FLOAT32 -> Expression("Scrape.float(${text.code})", optional = true)
                PrimitiveKind.FLOAT64 -> Expression("Scrape.double(${text.code})", optional = true)
                else -> error("Can't scrape a ${type.kind}")
            }
            else -> error("Can't scrape $type from text")
        }

        private fun record(type: TypeRef): RecordType = spec.resolve(type as TypeRef.Named) as RecordType

        private fun pipeline(steps: List<ScrapeStep>, input: Expression, kind: ScrapeKind): Expression {
            var expression = input
            var current = kind
            for (step in steps) {
                expression = step(step, expression, current)
                current = Scrapes.output(listOf(step), current) ?: error("Invalid scrape step $step after $current")
            }
            return expression
        }

        private fun step(step: ScrapeStep, value: Expression, kind: ScrapeKind): Expression {
            val v = value.code
            return when (step) {
                is ScrapeStep.Select -> call("select", v, str(step.selector))
                is ScrapeStep.SelectAll -> Expression("Scrape.selectAll($v, ${str(step.selector)})", optional = false)
                ScrapeStep.Root -> call("root", v)
                ScrapeStep.Next -> call("next", v)
                ScrapeStep.LastSibling -> call("lastSibling", v)
                is ScrapeStep.Follow -> call("follow", v, str(step.attribute))
                is ScrapeStep.Sections -> Expression("Scrape.sections($v, header: ${str(step.header)})", optional = false)
                ScrapeStep.First -> Expression("$v.first", optional = true)
                is ScrapeStep.Where -> {
                    val candidate = "candidate${++closures}"
                    val test = pipeline(step.test, Expression(candidate, optional = false), ScrapeKind.ELEMENT)
                    Expression("$v.filter { $candidate in ${if (test.optional) "${test.code} == true" else test.code} }", optional = false)
                }
                is ScrapeStep.Text -> call("text", v, "exclude: ${step.exclude?.let(::str) ?: "nil"}", "missing: $missing")
                ScrapeStep.OwnText -> call("ownText", v, "missing: $missing")
                ScrapeStep.Html -> call("html", v)
                is ScrapeStep.Attribute -> call("attribute", v, str(step.name), "missing: $missing")
                is ScrapeStep.Url -> call("url", v, str(step.attribute))
                ScrapeStep.Lines -> call("lines", v, "missing: $missing")
                ScrapeStep.Exists -> Expression("($v != nil)", optional = false)
                is ScrapeStep.Regex -> call("regex", v, str(step.pattern))
                is ScrapeStep.RemovePrefix -> call("removePrefix", v, str(step.prefix))
                is ScrapeStep.RemoveSuffix -> call("removeSuffix", v, str(step.suffix))
                is ScrapeStep.Unwrap -> call("unwrap", v, str(step.prefix), str(step.suffix))
                is ScrapeStep.NullIf -> call("nullIf", v, step.values.joinToString(", ", "[", "]") { str(it) })
                is ScrapeStep.QueryParameter -> call("queryParameter", v, str(step.name))
                is ScrapeStep.Replace -> call("replace", v, step.replacements.entries.joinToString(", ", "[", "]") { "${str(it.key)}: ${str(it.value)}" })
                is ScrapeStep.Equals -> call("equals", v, str(step.value))
                is ScrapeStep.Contains -> call("contains", v, str(step.value))
                ScrapeStep.Not -> if (value.optional) Expression("$v.map { !\$0 }", optional = true) else Expression("!$v", optional = false)
                is ScrapeStep.FirstOf -> firstOf(step, value, kind)
            }
        }

        /**
         * Each alternative reads the same input, so it is repeated in each of them; alternatives
         * yielding elements are tried until one isn't empty, the others until one isn't nil.
         */
        private fun firstOf(step: ScrapeStep.FirstOf, value: Expression, kind: ScrapeKind): Expression {
            val alternatives = step.alternatives.map { pipeline(it, value, kind) }
            return if (Scrapes.output(step.alternatives.first(), kind) == ScrapeKind.ELEMENTS) {
                val tried = alternatives.dropLast(1).map { "Scrape.nonEmpty(${it.code})" } + alternatives.last().code
                Expression(tried.joinToString(" ?? "), optional = false)
            } else {
                Expression(alternatives.joinToString(" ?? ") { it.code }, alternatives.last().optional)
            }
        }

        private fun call(helper: String, vararg arguments: String): Expression =
            Expression("Scrape.$helper(${arguments.joinToString(", ")})", optional = true)

        private fun str(value: String): String = SwiftSyntax.string(value)
    }
}
