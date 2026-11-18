package it.attendance100.mybicocca.codegen.target.web

import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.DefaultValue
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
 * Emits `<Prefix>Scrapers.ts`, which reads the API's scraped records and HTML responses out of
 * DOM nodes through the runtime's `scrape` helpers:
 *
 * - `scrape<Record>(node)` reads a record, or returns `undefined` when a required value is
 *   missing; hand-written code can call it on a page it fetched itself;
 * - `scrape<Module><Operation>(document)` reads an operation's response, for the client's
 *   `html`.
 *
 * Every helper accepts and returns `undefined` for a missing value, so it travels to the end
 * of a pipeline.
 */
internal class TsScrapeEmitter(private val spec: ApiSpec, private val layout: TsLayout) {
    val fileName = layout.scrapersFile

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
        "scrape${module.name}${operation.name.replaceFirstChar(Char::uppercaseChar)}"

    fun emit(file: TsFile) {
        layout.importRuntime(file, "scrape")
        // Fragment readers take the HTML itself; only record and response readers take a node.
        if (scrapedRecords.isNotEmpty() || htmlOperations.isNotEmpty()) file.import("type ScrapeNode", layout.runtimeModule)
        val sections = buildList<CodeWriter.() -> Unit> {
            scrapedRecords.forEach { add { record(it, file) } }
            htmlOperations.forEach { (module, operation) -> add { response(module, operation, file) } }
            htmlFields.forEach { (record, field) -> add { htmlField(record, field, file) } }
        }
        file.body.separated(sections) { it(this) }
    }

    /** Reads each field into a numbered constant, so a field's name never shadows the node. */
    private fun CodeWriter.record(record: RecordType, file: TsFile) {
        val missing = record.scrape!!.missing
        layout.importDeclaration(file, record.name)
        line("/** Reads {@link ${record.name}} out of `node`, or returns `undefined` when a value it requires is missing. */")
        block("export function ${functionName(record)}(node: ScrapeNode): ${record.name} | undefined {") {
            if (missing.isNotEmpty()) line("const missing = ${missing.joinToString(", ", "[", "]") { TsSyntax.string(it) }};")
            val compiler = Compiler(if (missing.isEmpty()) "[]" else "missing", file)
            val properties = mutableListOf<String>()
            record.fields.forEachIndexed { index, field ->
                val local = "value$index"
                val scrape = field.scrape
                if (scrape == null) {
                    properties += "${TsSyntax.propertyKey(field.name)}: ${defaultLiteral(field.type, field.default!!)}"
                    return@forEachIndexed
                }
                val value = compiler.value(field.type, scrape, "node")
                when {
                    !value.optional -> line("const $local = ${value.code};")
                    field.default == null && !field.nullable -> {
                        line("const $local = ${value.code};")
                        line("if ($local === undefined) return undefined;")
                    }
                    else -> line("const $local = ${value.code} ?? ${defaultLiteral(field.type, field.default ?: DefaultValue.Null)};")
                }
                properties += "${TsSyntax.propertyKey(field.name)}: $local"
            }
            if (properties.isEmpty()) line("return {};") else commaList("return {", properties, "};", trailingComma = true)
        }
    }

    private fun CodeWriter.response(module: ApiModule, operation: Operation, file: TsFile) {
        val response = operation.response as ResponseBody.Html
        block("export function ${responseFunction(module, operation)}(document: ScrapeNode): ${layout.render(response.type, file)} | undefined {") {
            line("return ${Compiler("[]", file).value(response.type, response.scrape, "document").code};")
        }
    }

    /** Reads [field] of [record] out of the HTML fragment it is sent as, for the record's decoder. */
    private fun CodeWriter.htmlField(record: RecordType, field: Field, file: TsFile) {
        val scrape = (field.codec as Codec.EmbeddedHtml).scrape
        val type = layout.render(field.type, file) + if (field.type is TypeRef.ListOf) "" else " | undefined"
        block("export function ${layout.htmlFunction(record, field)}(html: string): $type {") {
            line("return ${Compiler("[]", file).value(field.type, scrape, "scrape.fragment(html)").code};")
        }
    }

    private fun defaultLiteral(type: TypeRef, default: DefaultValue): String = when (default) {
        DefaultValue.Null -> "null"
        DefaultValue.EmptyList -> "[]"
        DefaultValue.EmptyMap -> "{}"
        is DefaultValue.Literal -> layout.literal(type, default)
        DefaultValue.Zero -> when ((type as TypeRef.Primitive).kind) {
            PrimitiveKind.STRING -> "\"\""
            PrimitiveKind.BOOLEAN -> "false"
            else -> "0"
        }
    }

    private fun functionName(record: RecordType): String = "scrape${record.name}"

    /** TypeScript code, and whether it may evaluate to `undefined`. */
    private data class Expression(val code: String, val optional: Boolean)

    /** Compiles pipelines to TypeScript expressions; [missing] is the expression of the record's missing texts. */
    private inner class Compiler(private val missing: String, private val file: TsFile) {
        /** Callback parameters are numbered, so nested ones don't shadow each other. */
        private var callbacks = 0

        fun value(type: TypeRef, steps: List<ScrapeStep>, input: String): Expression {
            val scraped = pipeline(steps, Expression(input, optional = false), ScrapeKind.ELEMENT)
            return when (type) {
                is TypeRef.ListOf -> {
                    val item = "item${++callbacks}"
                    val read = if (Scrapes.isRecordList(type.element, spec)) {
                        "${recordFunction(type.element)}($item)"
                    } else {
                        convert(type.element, Expression("scrape.text($item, undefined, $missing)", optional = true)).code
                    }
                    Expression("scrape.compact(${scraped.code}.map(($item) => $read))", optional = false)
                }
                else -> when (Scrapes.kindFor(type, spec)) {
                    ScrapeKind.ELEMENT -> if (scraped.optional) {
                        val node = "node${++callbacks}"
                        Expression("scrape.map(${scraped.code}, ($node) => ${recordFunction(type)}($node))", optional = true)
                    } else {
                        Expression("${recordFunction(type)}(${scraped.code})", optional = true)
                    }
                    ScrapeKind.TEXT -> convert(type, scraped)
                    else -> scraped
                }
            }
        }

        private fun recordFunction(type: TypeRef): String {
            val record = spec.resolve(type as TypeRef.Named) as RecordType
            return functionName(record)
        }

        /** Converts text to [type]: numbers that don't parse and unknown values of closed enums are missing. */
        private fun convert(type: TypeRef, text: Expression): Expression = when (type) {
            is TypeRef.Named -> {
                val decoder = layout.decoderName(type.name)
                layout.importDeclaration(file, type.name, decoder)
                Expression("$decoder(${text.code})", optional = true)
            }
            is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> text
                PrimitiveKind.INT32, PrimitiveKind.INT64 -> Expression("scrape.integer(${text.code})", optional = true)
                PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> Expression("scrape.number(${text.code})", optional = true)
                else -> error("Can't scrape a ${type.kind}")
            }
            else -> error("Can't scrape $type from text")
        }

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
                is ScrapeStep.SelectAll -> Expression("scrape.selectAll($v, ${str(step.selector)})", optional = false)
                ScrapeStep.Root -> call("root", v)
                ScrapeStep.Next -> call("next", v)
                ScrapeStep.LastSibling -> call("lastSibling", v)
                is ScrapeStep.Follow -> call("follow", v, str(step.attribute))
                is ScrapeStep.Sections -> Expression("scrape.sections($v, ${str(step.header)})", optional = false)
                ScrapeStep.First -> Expression("$v[0]", optional = true)
                is ScrapeStep.Where -> {
                    val candidate = "candidate${++callbacks}"
                    val test = pipeline(step.test, Expression(candidate, optional = false), ScrapeKind.ELEMENT)
                    Expression("$v.filter(($candidate) => ${if (test.optional) "${test.code} === true" else test.code})", optional = false)
                }
                is ScrapeStep.Text -> call("text", v, step.exclude?.let(::str) ?: "undefined", missing)
                ScrapeStep.OwnText -> call("ownText", v, missing)
                ScrapeStep.Html -> call("html", v)
                is ScrapeStep.Attribute -> call("attribute", v, str(step.name), missing)
                is ScrapeStep.Url -> call("url", v, str(step.attribute))
                ScrapeStep.Lines -> call("lines", v, missing)
                ScrapeStep.Exists -> Expression("$v !== undefined", optional = false)
                is ScrapeStep.Regex -> call("regex", v, str(step.pattern))
                is ScrapeStep.RemovePrefix -> call("removePrefix", v, str(step.prefix))
                is ScrapeStep.RemoveSuffix -> call("removeSuffix", v, str(step.suffix))
                is ScrapeStep.Unwrap -> call("unwrap", v, str(step.prefix), str(step.suffix))
                is ScrapeStep.NullIf -> call("nullIf", v, step.values.joinToString(", ", "[", "]") { str(it) })
                is ScrapeStep.QueryParameter -> call("queryParameter", v, str(step.name))
                is ScrapeStep.Replace -> call("replace", v, step.replacements.entries.joinToString(", ", "{ ", " }") { "${str(it.key)}: ${str(it.value)}" })
                is ScrapeStep.Equals -> call("equals", v, str(step.value))
                is ScrapeStep.Contains -> call("contains", v, str(step.value))
                ScrapeStep.Not -> if (value.optional) call("not", v) else Expression("!($v)", optional = false)
                is ScrapeStep.FirstOf -> firstOf(step, value, kind)
            }
        }

        /**
         * Each alternative reads the same input, so it is repeated in each of them; alternatives
         * yielding elements are tried until one isn't empty, the others until one isn't missing.
         */
        private fun firstOf(step: ScrapeStep.FirstOf, value: Expression, kind: ScrapeKind): Expression {
            val alternatives = step.alternatives.map { pipeline(it, value, kind) }
            return if (Scrapes.output(step.alternatives.first(), kind) == ScrapeKind.ELEMENTS) {
                val tried = alternatives.dropLast(1).map { "scrape.nonEmpty(${it.code})" } + alternatives.last().code
                Expression(tried.joinToString(" ?? "), optional = false)
            } else {
                Expression(alternatives.joinToString(" ?? ") { it.code }, alternatives.last().optional)
            }
        }

        private fun call(helper: String, vararg arguments: String): Expression =
            Expression("scrape.$helper(${arguments.joinToString(", ")})", optional = true)

        private fun str(value: String): String = TsSyntax.string(value)
    }
}
