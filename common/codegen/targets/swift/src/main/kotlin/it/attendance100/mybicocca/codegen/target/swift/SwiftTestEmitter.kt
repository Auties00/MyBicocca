package it.attendance100.mybicocca.codegen.target.swift

import it.attendance100.mybicocca.codegen.core.Arg
import it.attendance100.mybicocca.codegen.core.Call
import it.attendance100.mybicocca.codegen.core.CaseKind
import it.attendance100.mybicocca.codegen.core.CodeWriter
import it.attendance100.mybicocca.codegen.core.Credentials
import it.attendance100.mybicocca.codegen.core.Expectation
import it.attendance100.mybicocca.codegen.core.GeneratedFile
import it.attendance100.mybicocca.codegen.core.Input
import it.attendance100.mybicocca.codegen.core.OperationRef
import it.attendance100.mybicocca.codegen.core.Path
import it.attendance100.mybicocca.codegen.core.Shapes
import it.attendance100.mybicocca.codegen.core.Source
import it.attendance100.mybicocca.codegen.core.TestCase
import it.attendance100.mybicocca.codegen.core.TestPlan
import it.attendance100.mybicocca.codegen.core.TestTypes
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.core.generatedMarker
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Writes an API's live tests (see [TestPlan]) as XCTest classes on top of the live test runtime
 * (the target's `swift-live` resources), in the test output directory's folder of the API's
 * module:
 *
 * - `<Prefix>Live.swift`: an enum holding the API's account, how to create its client, the sources
 *   cases take values from, the canonical form of its results and what its responses look like;
 * - `<Prefix><Module>LiveTests.swift`: a test per case, calling the generated SDK.
 */
internal class SwiftTestEmitter(private val plan: TestPlan, private val layout: SwiftLayout) {
    private val spec: ApiSpec = plan.spec
    private val support = "${spec.prefix}Live"
    private val apiType = layout.facadeName

    fun files(): List<GeneratedFile> = buildList {
        add(file("${layout.module}/$support.swift", xctest = false) { supportFile(it) })
        for ((module, cases) in plan.cases.groupBy { it.operation.module }) {
            add(file("${layout.module}/${spec.prefix}${module.name}LiveTests.swift", xctest = true) { testFile(it, module.name, cases) })
        }
    }

    private fun file(path: String, xctest: Boolean, emit: (CodeWriter) -> Unit): GeneratedFile {
        val body = CodeWriter().also(emit)
        val imports = listOfNotNull(layout.runtimeModule, layout.module, "Foundation", "XCTest".takeIf { xctest }).sorted()
        return GeneratedFile(path, "// ${generatedMarker(spec.id)}. Do not edit.\n\n${imports.joinToString("") { "import $it\n" }}\n$body")
    }

    // The support enum.

    private fun supportFile(out: CodeWriter) {
        val account = plan.account
        out.apply {
            line("/// The live tests of ${spec.prefix}: see `common/specs/${spec.id}/tests.yaml`.")
            block("enum $support {") {
                line("static let api = LiveApi<$apiType>(")
                indent {
                    line("id: ${SwiftSyntax.string(spec.id)},")
                    line("env: [${account.env.joinToString { SwiftSyntax.string(it) }}],")
                    line("requestsPerSecond: ${plan.limits.requestsPerSecond},")
                    line("maxRequests: ${plan.limits.maxRequests},")
                    line("shapes: shapes,")
                    if (plan.skips.isEmpty()) {
                        line("skips: [],")
                    } else {
                        commaList("skips: [", plan.skips.map { "(${SwiftSyntax.string(it.operation)}, ${SwiftSyntax.string(it.reason)})" }, "],")
                    }
                    create()
                    line("failure: { error in (error as? ${layout.error}).map { (status: \$0.status, code: \$0.code) } }${if (plan.replays.isNotEmpty()) "," else ""}")
                    if (plan.replays.isNotEmpty()) replay()
                }
                line(")")
                for ((name, credentials) in account.credentials) {
                    line()
                    credentialsFunction(name, credentials)
                }
                for (source in plan.sources) {
                    line()
                    sourceFunction(source)
                }
                for (record in fingerprintedRecords()) {
                    line()
                    fingerprintFunction(record)
                }
                line()
                line("/// What the responses of ${spec.prefix} look like, for the runtime to check real ones against.")
                line("static let shapes = ${SwiftSyntax.string(plan.shapes)}")
            }
        }
    }

    private fun CodeWriter.create() {
        val account = plan.account
        line("create: { observer, session, anonymous in")
        indent {
            line("var configuration = ${layout.client}.defaultConfiguration")
            line("configuration.session = session")
            line("configuration.observer = observer")
            if (account.headers.isNotEmpty()) {
                block("if !anonymous {") {
                    for ((name, template) in account.headers) line("configuration.headers[${SwiftSyntax.string(name)}] = LiveTemplates.resolve(${SwiftSyntax.string(template)})")
                }
            }
            val arguments = account.parameters.map { (parameter, arg) -> "${SwiftSyntax.identifier(parameter.name)}: ${constant(arg, parameter.type)}" } +
                "configuration: configuration" +
                spec.client.settings.filter { it.name in account.settings }.map { "${SwiftSyntax.identifier(it.name)}: ${SwiftSyntax.string(account.settings.getValue(it.name))}" }
            line("return $apiType(${arguments.joinToString(", ")})")
        }
        line("},")
    }

    private fun CodeWriter.replay() {
        line("replay: { live, caseId, values in")
        indent {
            block("switch caseId {") {
                for (case in plan.replays) {
                    val undo = case.write!!.undo
                    line("case ${SwiftSyntax.string(case.id)}:")
                    indent {
                        val statements = CodeWriter()
                        val args = undo.args.mapValues { (name, arg) ->
                            val type = undo.operation.inputType(name)
                            when {
                                arg is Arg.Ref -> fromText("values[${SwiftSyntax.string(name)}] ?? \"\"", type!!)
                                arg is Arg.AccountCredentials -> credentials(arg)
                                else -> constant(arg, type!!)
                            }
                        }
                        statements.toString().trimEnd().takeIf { it.isNotEmpty() }?.let(::lines)
                        line("_ = ${invocation(undo, args)}")
                        line("return true")
                    }
                }
                line("default:")
                indent { line("return false") }
            }
        }
        line("}")
    }

    private fun CodeWriter.credentialsFunction(name: String, credentials: Credentials) {
        val type = layout.authClass(name)
        val statements = CodeWriter()
        val values = credentials.scheme.parameters.map { "${SwiftSyntax.identifier(it.name)}: ${valueOf(credentials.values.getValue(it.name), TypeRef.STRING, it.name, statements)}" }
        block("static func ${credentialsName(name)}(_ live: LiveCase<$apiType>) async throws -> $type {") {
            statements.toString().trimEnd().takeIf { it.isNotEmpty() }?.let(::lines)
            line("return $type(${values.joinToString(", ")})")
        }
    }

    private fun credentialsName(scheme: String): String = "${scheme.decapitalized()}Credentials"

    private fun credentials(arg: Arg.AccountCredentials): String = "try await $support.${credentialsName(arg.credentials.scheme.name)}(live)"

    private fun CodeWriter.sourceFunction(source: Source) {
        val result = layout.render(source.operation.resultType!!)
        val statements = CodeWriter()
        val args = arguments(source.call, statements)
        block("static func ${source.id}(_ live: LiveCase<$apiType>) async throws -> $result {") {
            block("try await live.source(${SwiftSyntax.string(source.id)}, ${callOptions(source.operation)}) { live in") {
                statements.toString().trimEnd().takeIf { it.isNotEmpty() }?.let(::lines)
                line("return ${invocation(source.call, args)}")
            }
        }
    }

    // Test classes.

    private fun testFile(out: CodeWriter, module: String, cases: List<TestCase>) {
        out.apply {
            line("/// The live tests of the ${spec.prefix} $module module, planned from `common/specs/${spec.id}/tests.yaml`.")
            block("final class ${spec.prefix}${module}LiveTests: XCTestCase {") {
                separated(cases) { case(it) }
            }
        }
    }

    private fun CodeWriter.case(case: TestCase) {
        val options = buildList {
            if (case.effect.wireName != "read") add("effect: ${SwiftSyntax.string(case.effect.wireName)}")
            if (case.index != 0) add("index: ${case.index}")
        }
        block("func test_${case.methodName}() async throws {") {
            block("try await liveCase($support.api, ${SwiftSyntax.string(case.id)}${options.joinToString("") { ", $it" }}) { live in") {
                val names = Names()
                val args = arguments(case.call, this, names)
                when (case.kind) {
                    CaseKind.UNKNOWN_ID -> line("try await live.refused(acceptsEmpty: true) { ${invocation(case.call, args)} }")
                    CaseKind.INVALID_CREDENTIALS, CaseKind.ANONYMOUS -> line("try await live.refused(acceptsEmpty: false) { ${invocation(case.call, args)} }")
                    CaseKind.SMOKE, CaseKind.COMBINATION -> read(case, args, names)
                    CaseKind.WRITE -> write(case, args, names)
                }
            }
        }
    }

    private fun CodeWriter.read(case: TestCase, args: Map<String, String>, names: Names) {
        val result = case.operation.resultType
        val errors = case.errors.takeIf { it.isNotEmpty() }?.joinToString(", ", "errors: [", "]") { SwiftSyntax.string(it) }
        val options = callOptions(case.operation, errors)
        if (result == null || (!case.compare && case.expectations.isEmpty())) {
            line("_ = try await live.call($options) { ${invocation(case.call, args)} }")
            return
        }
        val value = names.local("result")
        line("let $value = try await live.call($options) { ${invocation(case.call, args)} }")
        if (case.compare) line("live.fingerprint { f in ${fingerprint(result, value, nullable = false, depth = 0)} }")
        for (expectation in case.expectations) expectation(expectation, value, args)
    }

    private fun CodeWriter.write(case: TestCase, args: Map<String, String>, names: Names) {
        val write = case.write!!
        val refs = Refs(args, null, null)
        write.snapshot?.let { (call, path) ->
            val snapshot = names.local("snapshot")
            line("let $snapshot = try await live.call(${callOptions(call.operation)}) { ${invocation(call, refArguments(call, refs))} }")
            refs.before = names.local("before")
            line("let ${refs.before} = try live.choose(\"the value to restore\", ${path(snapshot, path)})")
        }
        if (case.operation.resultType != null) {
            refs.result = names.local("result")
            line("let ${refs.result} = try await live.call(${callOptions(case.operation)}) { ${invocation(case.call, args)} }")
        } else {
            line("_ = try await live.call(${callOptions(case.operation)}) { ${invocation(case.call, args)} }")
        }
        val undoArgs = refArguments(write.undo, refs)
        val replayable = case in plan.replays
        if (replayable) {
            val saved = write.undo.args.filterValues { it is Arg.Ref }.keys.joinToString(", ") { "${SwiftSyntax.string(it)}: ${text(undoArgs.getValue(it), write.undo.operation.inputType(it)!!)}" }
            line("live.journal([$saved])")
        }
        val verify = write.verify
        line("do {")
        indent {
            verify?.let { (call, contains) -> verifyCall(call, contains, names.local("verified"), refs, "expectContains") }
        }
        line("} catch {")
        indent {
            line("_ = try? await live.call(${callOptions(write.undo.operation)}) { ${invocation(write.undo, undoArgs)} }")
            line("throw error")
        }
        line("}")
        line("_ = try await live.call(${callOptions(write.undo.operation)}) { ${invocation(write.undo, undoArgs)} }")
        if (replayable) line("live.unjournal()")
        if (verify != null && write.snapshot == null) verifyCall(verify.first, verify.second, names.local("undone"), refs, "expectNotContains")
        write.snapshot?.let { (call, path) ->
            val restored = names.local("restored")
            line("let $restored = try await live.call(${callOptions(call.operation)}) { ${invocation(call, refArguments(call, refs))} }")
            line("try live.expectContains(${SwiftSyntax.string(path.toString())}, ${path(restored, path)}, ${refs.before})")
        }
    }

    private fun CodeWriter.verifyCall(call: Call, contains: Expectation.Contains, name: String, refs: Refs, check: String) {
        line("let $name = try await live.call(${callOptions(call.operation)}) { ${invocation(call, refArguments(call, refs))} }")
        line("try live.$check(${SwiftSyntax.string(contains.path.toString())}, ${path(name, contains.path)}, ${refValue(contains.value, contains.path.type, refs)})")
    }

    private fun CodeWriter.expectation(expectation: Expectation, result: String, args: Map<String, String>) {
        val values = path(result, expectation.path)
        val where = SwiftSyntax.string(expectation.path.toString())
        val refs = Refs(args, result, null)
        when (expectation) {
            is Expectation.NonEmpty -> line("try live.expectNonEmpty($where, $values)")
            is Expectation.Contains -> line("try live.expectContains($where, $values, ${refValue(expectation.value, expectation.path.type, refs)})")
            is Expectation.Equals -> line("try live.expectEquals($where, $values, ${refValue(expectation.value, expectation.path.type, refs)})")
            is Expectation.Matches -> line("try live.expectMatches($where, $values, ${SwiftSyntax.string(expectation.regex)})")
            is Expectation.Count -> line("try live.expectCount($where, $values, ${expectation.min ?: "nil"}, ${expectation.max ?: "nil"})")
        }
    }

    // Arguments.

    private class Names {
        private val used = mutableSetOf<String>()

        fun local(name: String): String {
            var candidate = name
            var suffix = 2
            while (!used.add(candidate)) candidate = "$name${suffix++}"
            return candidate
        }
    }

    private class Refs(val args: Map<String, String>, var result: String?, var before: String?)

    private fun arguments(call: Call, writer: CodeWriter, names: Names = Names()): Map<String, String> =
        call.args.mapValues { (name, arg) ->
            val value = valueOf(arg, call.operation.inputType(name), name, writer)
            if (arg is Arg.Pick || arg is Arg.Text || arg is Arg.AccountCredentials || arg is Arg.Json) {
                val reserved = name in RESERVED_LOCALS || SwiftSyntax.identifier(name) != name
                val variable = names.local(if (reserved) "${name}Value" else name)
                writer.line("let $variable = $value")
                variable
            } else {
                value
            }
        }

    private fun refArguments(call: Call, refs: Refs): Map<String, String> =
        call.args.mapValues { (name, arg) -> refValue(arg, call.operation.inputType(name), refs) }

    private fun refValue(arg: Arg, type: TypeRef?, refs: Refs): String = when (arg) {
        is Arg.Ref -> {
            val root = when (arg.root) {
                Arg.Ref.Root.ARGS -> refs.args.getValue(arg.input!!)
                Arg.Ref.Root.RESULT -> refs.result ?: error("No result to refer to")
                Arg.Ref.Root.BEFORE -> refs.before ?: error("No saved value to refer to")
            }
            val value = if (arg.path.steps.isEmpty()) root else "try live.first(${SwiftSyntax.string(arg.path.toString())}, ${path(root, arg.path)})"
            type?.let { convert(value, arg.path.type, it, total = true) } ?: value
        }
        is Arg.WithField -> {
            val field = SwiftSyntax.identifier(arg.field.name)
            "{ var copy = ${refValue(arg.base, null, refs)}; copy.$field = ${refValue(arg.value, arg.field.type, refs)}; return copy }()"
        }
        is Arg.AccountCredentials -> credentials(arg)
        else -> argument(arg, type)
    }

    private fun valueOf(arg: Arg, type: TypeRef?, name: String, writer: CodeWriter): String = when (arg) {
        is Arg.Pick -> {
            val target = if (arg.wrap) (type as TypeRef.ListOf).element else type!!
            val candidates = convertAll(path("try await $support.${arg.source.id}(live)", arg.path), arg.path.type, target)
            val chosen = "try live.choose(${SwiftSyntax.string(name)}, $candidates)"
            if (arg.wrap) "[$chosen]" else chosen
        }
        is Arg.AccountCredentials -> credentials(arg)
        else -> argument(arg, type)
    }

    private fun argument(arg: Arg, type: TypeRef?): String = when (arg) {
        is Arg.Literal -> literal(arg.value, type!!)
        is Arg.Text -> fromText("LiveTemplates.resolve(${SwiftSyntax.string(arg.template)})", type!!)
        is Arg.Json -> "try live.decode(${layout.render(type!!)}.self, json: ${SwiftSyntax.string(arg.value.toString())})"
        Arg.Unknown -> if ((type as TypeRef.Primitive).kind == PrimitiveKind.STRING) SwiftSyntax.string(UNKNOWN_TEXT) else UNKNOWN_ID.toString()
        is Arg.InvalidCredentials -> invalidCredentials(arg.scheme)
        is Arg.AccountCredentials -> credentials(arg)
        is Arg.Pick, is Arg.Ref, is Arg.WithField -> error("$arg needs statements or refs")
    }

    private fun constant(arg: Arg, type: TypeRef): String = argument(arg, type)

    private fun invalidCredentials(scheme: AuthScheme): String =
        "${layout.authClass(scheme.name)}(${scheme.parameters.joinToString(", ") { "${SwiftSyntax.identifier(it.name)}: ${SwiftSyntax.string(INVALID_CREDENTIAL)}" }})"

    /** The call of the generated SDK, with its labels in declaration order. */
    private fun invocation(call: Call, args: Map<String, String>): String {
        val operation = call.operation.operation
        val receiver = if (call.anonymous) "live.anonymousApi" else "live.api"
        val labels = buildList {
            operation.pathParameters.forEach { add(it.name) }
            if (operation.body is RequestBody.Json) add(Input.BODY)
            operation.parameters.filter { it.location != ParameterLocation.PATH }.forEach { add(it.name) }
            if (operation.auth != null) add(Input.AUTH)
        }
        val arguments = labels.mapNotNull { label -> args[label]?.let { "${SwiftSyntax.identifier(label)}: $it" } }
        return "try await $receiver.${SwiftSyntax.identifier(call.operation.module.name.decapitalized())}.${SwiftSyntax.identifier(operation.name)}(${arguments.joinToString(", ")})"
    }

    private fun callOptions(operation: OperationRef, extra: String? = null): String {
        val response = operation.operation.response
        val type = operation.resultType
        val shape = when (response) {
            is ResponseBody.Json, is ResponseBody.ScriptVariable -> type?.let { SwiftSyntax.string(Shapes.shape(spec, it).toString()) } ?: "nil"
            else -> "nil"
        }
        return listOfNotNull(
            shape,
            (response as? ResponseBody.Json)?.envelope?.takeIf { it.isNotEmpty() }?.joinToString(", ", "envelope: [", "]") { SwiftSyntax.string(it) },
            (response as? ResponseBody.ScriptVariable)?.let { "variable: ${SwiftSyntax.string(it.variable)}" },
            extra,
        ).joinToString(", ")
    }

    // Values and types.

    private fun enumCase(type: TypeRef, wire: String): String {
        val enum = TestTypes.enum(spec, type)!!
        val value = enum.values.first { it.wireValue == wire }
        return "${enum.name}.${SwiftSyntax.identifier(layout.caseNames(enum).getValue(value.name))}"
    }

    private fun literal(value: JsonPrimitive, type: TypeRef): String {
        if (TestTypes.enum(spec, type) != null) return enumCase(type, value.content)
        return when ((type as? TypeRef.Primitive)?.kind) {
            PrimitiveKind.STRING -> SwiftSyntax.string(value.content)
            PrimitiveKind.INT32 -> value.content.toLong().toString()
            PrimitiveKind.INT64 -> "Int64(${value.content.toLong()})"
            PrimitiveKind.FLOAT32 -> "Float(${value.content.toDouble()})"
            PrimitiveKind.FLOAT64 -> value.content.toDouble().toString()
            PrimitiveKind.BOOLEAN -> (value.booleanOrNull ?: value.content.toBoolean()).toString()
            PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME -> fromText(SwiftSyntax.string(value.content), type)
            else -> error("No literal of $type")
        }
    }

    private fun fromText(text: String, type: TypeRef): String {
        TestTypes.enum(spec, type)?.let { return "try LiveText.wire(${it.name}.self, $text)" }
        return when ((type as? TypeRef.Primitive)?.kind) {
            PrimitiveKind.STRING -> text
            PrimitiveKind.INT32 -> "try LiveText.int($text)"
            PrimitiveKind.INT64 -> "try LiveText.int64($text)"
            PrimitiveKind.FLOAT32 -> "try LiveText.float($text)"
            PrimitiveKind.FLOAT64 -> "try LiveText.double($text)"
            PrimitiveKind.BOOLEAN -> "LiveText.bool($text)"
            PrimitiveKind.LOCAL_DATE -> "try LiveText.date($text)"
            PrimitiveKind.LOCAL_DATE_TIME -> "try LiveText.dateTime($text)"
            PrimitiveKind.LOCAL_TIME -> "try LiveText.time($text)"
            else -> error("Can't convert text to $type")
        }
    }

    /** The text a journal saves of [value] of [type], which [fromText] reads back. */
    private fun text(value: String, type: TypeRef): String =
        if (TestTypes.enum(spec, type) != null) "$value.wireValue" else "String(describing: $value)"

    private fun convertAll(values: String, from: TypeRef, to: TypeRef): String {
        val converted = convert("\$0", from, to, total = false)
        return if (converted == "\$0") values else "$values.compactMap { $converted }"
    }

    /** [value] of type [from] converted to [to]; [total] conversions may not fail, others may give nil. */
    private fun convert(value: String, from: TypeRef, to: TypeRef, total: Boolean): String {
        if (from == to) return value
        val fromKind = (from as? TypeRef.Primitive)?.kind
        val toKind = (to as? TypeRef.Primitive)?.kind
        return when {
            from is TypeRef.ListOf && to is TypeRef.ListOf -> "$value.compactMap { ${convert("\$0", from.element, to.element, total = false)} }"
            TestTypes.enum(spec, from) != null && toKind == PrimitiveKind.STRING -> "$value.wireValue"
            fromKind == PrimitiveKind.STRING && TestTypes.enum(spec, to) != null ->
                if (total) fromText(value, to) else "${TestTypes.enum(spec, to)!!.name}(wireValue: $value)"
            toKind == PrimitiveKind.STRING -> "String($value)"
            fromKind == PrimitiveKind.STRING && toKind == PrimitiveKind.INT32 -> if (total) "try LiveText.int($value)" else "Int($value)"
            fromKind == PrimitiveKind.STRING && toKind == PrimitiveKind.INT64 -> if (total) "try LiveText.int64($value)" else "Int64($value)"
            toKind == PrimitiveKind.INT32 -> "Int($value)"
            toKind == PrimitiveKind.INT64 -> "Int64($value)"
            toKind == PrimitiveKind.FLOAT64 -> "Double($value)"
            toKind == PrimitiveKind.FLOAT32 -> "Float($value)"
            else -> error("Can't convert $from to $to")
        }
    }

    private fun path(root: String, path: Path): String {
        var expression = "[$root]"
        var type = path.root
        for (step in path.steps) {
            when (step) {
                is Path.Step.Get -> {
                    expression += ".compactMap { \$0.${SwiftSyntax.identifier(step.field.name)} }"
                    type = step.field.type
                }
                Path.Step.Each -> {
                    expression += if (type is TypeRef.MapOf) ".flatMap { Array(\$0.entries.values) }" else ".flatMap { \$0 }"
                    type = (type as? TypeRef.ListOf)?.element ?: (type as TypeRef.MapOf).value
                }
                is Path.Step.At -> {
                    expression += ".compactMap { \$0.indices.contains(${step.index}) ? \$0[${step.index}] : nil }"
                    type = (type as TypeRef.ListOf).element
                }
                is Path.Step.Where -> expression += ".filter { \$0.${SwiftSyntax.identifier(step.field.name)} == ${literal(step.value, step.field.type)} }"
            }
        }
        return expression
    }

    // Fingerprints.

    private fun fingerprintedRecords(): List<RecordType> {
        val records = linkedMapOf<String, RecordType>()
        fun visit(type: TypeRef) {
            when (type) {
                is TypeRef.Named -> TestTypes.record(spec, type)?.let { record ->
                    if (records.put(record.name, record) == null) record.fields.forEach { visit(it.type) }
                }
                is TypeRef.ListOf -> visit(type.element)
                is TypeRef.MapOf -> {
                    visit(type.key)
                    visit(type.value)
                }
                is TypeRef.Primitive -> Unit
            }
        }
        plan.cases.filter { it.compare }.mapNotNull { it.operation.resultType }.forEach(::visit)
        return records.values.sortedBy { it.name }
    }

    private fun CodeWriter.fingerprintFunction(record: RecordType) {
        block("static func ${fingerprintName(record)}(_ f: LiveFingerprint, _ value: ${record.name}?) {") {
            line("guard let value else { return f.nil() }")
            if (record.fields.isEmpty()) {
                line("f.obj {}")
            } else {
                block("f.obj {") {
                    for (field in record.fields) line(fingerprint(field.type, "value.${SwiftSyntax.identifier(field.name)}", field.nullable, depth = 0))
                }
            }
        }
    }

    private fun fingerprintName(record: RecordType): String = "write${record.name}"

    /** The call writing [value] (of [type], optional when [nullable]) to the fingerprint `f`. */
    private fun fingerprint(type: TypeRef, value: String, nullable: Boolean, depth: Int, receiver: String = "f"): String {
        val safe = if (nullable) "?." else "."
        TestTypes.enum(spec, type)?.let { return "$receiver.str($value${safe}wireValue)" }
        TestTypes.record(spec, type)?.let { return "$support.${fingerprintName(it)}($receiver, $value)" }
        val item = "item$depth"
        return when (type) {
            is TypeRef.ListOf -> "$receiver.list($value) { $item in ${fingerprint(type.element, item, nullable = false, depth = depth + 1, receiver)} }"
            is TypeRef.MapOf -> {
                val key = "key$depth"
                val keyReceiver = "k$depth"
                "$receiver.map($value${safe}entries, { $keyReceiver, $key in ${fingerprint(type.key, key, nullable = false, depth = depth + 1, keyReceiver)} }) { $item in ${fingerprint(type.value, item, nullable = false, depth = depth + 1, receiver)} }"
            }
            is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> "$receiver.str($value)"
                PrimitiveKind.INT32, PrimitiveKind.INT64 -> "$receiver.int($value)"
                PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "$receiver.num($value)"
                PrimitiveKind.BOOLEAN -> "$receiver.bool($value)"
                PrimitiveKind.LOCAL_DATE -> "$receiver.date($value)"
                PrimitiveKind.LOCAL_DATE_TIME -> "$receiver.dateTime($value)"
                PrimitiveKind.LOCAL_TIME -> "$receiver.time($value)"
                PrimitiveKind.JSON_OBJECT, PrimitiveKind.JSON_VALUE -> "$receiver.json($value)"
                PrimitiveKind.BINARY, PrimitiveKind.FILE -> "$receiver.nil()"
            }
            is TypeRef.Named -> "$receiver.nil()"
        }
    }

    private companion object {
        const val UNKNOWN_ID = 987_654_321
        const val UNKNOWN_TEXT = "mybicocca-unknown-id"
        const val INVALID_CREDENTIAL = "mybicocca-invalid-credential"
        val RESERVED_LOCALS = setOf("result", "snapshot", "before", "verified", "undone", "restored", "live", "copy", "f")
    }
}
