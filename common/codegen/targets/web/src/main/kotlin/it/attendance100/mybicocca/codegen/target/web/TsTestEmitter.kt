package it.attendance100.mybicocca.codegen.target.web

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
 * Writes an API's live tests (see [TestPlan]) as vitest files on top of the live test runtime
 * (the target's `web-live` resources), in the test output directory's folder of the API's
 * subpackage:
 *
 * - `<Prefix>Live.ts`: the API's account, how to create its client, the sources cases take values
 *   from, the canonical form of its results and what its responses look like;
 * - `<Prefix><Module>.live.test.ts`: a test per case, calling the generated SDK.
 *
 * They import the SDK through its folders' `index.ts`, at [sources] (the SDK's output directory,
 * relative to the test output directory).
 */
internal class TsTestEmitter(private val plan: TestPlan, private val layout: TsLayout, private val sources: String) {
    private val spec: ApiSpec = plan.spec
    private val support = "${spec.prefix}Live"
    private val supportFile = "${layout.packageName}/$support.ts"
    private val apiType = layout.facadeName
    private val codecs = TsCodecs(layout)

    fun files(): List<GeneratedFile> = buildList {
        add(TsFile(supportFile, spec.id).also { supportFile(it) }.toGeneratedFile())
        for ((module, cases) in plan.cases.groupBy { it.operation.module }) {
            val path = "${layout.packageName}/${spec.prefix}${module.name}.live.test.ts"
            add(TsFile(path, spec.id).also { testFile(it, module.name, cases) }.toGeneratedFile())
        }
    }

    // Imports.

    /** Imports [name] from the SDK folder [folder] (`api`, `models` or `errors`) of the API's subpackage. */
    private fun sdk(file: TsFile, name: String, folder: String): String {
        file.import(name, "${up(file)}$sources/${layout.packageName}/$folder/index.js")
        return name.removePrefix("type ")
    }

    private fun runtime(file: TsFile, name: String): String {
        file.import(name, "${up(file)}$sources/${layout.runtimePath}/index.js")
        return name.removePrefix("type ")
    }

    private fun live(file: TsFile, name: String): String {
        file.importFile(name, LIVE_FILE)
        return name.removePrefix("type ")
    }

    private fun up(file: TsFile): String = "../".repeat(file.path.count { it == '/' })

    /** The type of [type], importing what it names from the SDK. */
    private fun render(type: TypeRef, file: TsFile): String = when (type) {
        is TypeRef.Named -> sdk(file, "type ${type.name}", "models")
        is TypeRef.ListOf -> render(type.element, file).let { if (' ' in it) "($it)[]" else "$it[]" }
        is TypeRef.MapOf -> if (type.key == TypeRef.STRING) "Record<string, ${render(type.value, file)}>" else "${runtime(file, "type WireMap")}<${render(type.key, file)}, ${render(type.value, file)}>"
        is TypeRef.Primitive -> when (type.kind) {
            PrimitiveKind.STRING -> "string"
            PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "number"
            PrimitiveKind.BOOLEAN -> "boolean"
            PrimitiveKind.LOCAL_DATE -> runtime(file, "type LocalDate")
            PrimitiveKind.LOCAL_DATE_TIME -> runtime(file, "type LocalDateTime")
            PrimitiveKind.LOCAL_TIME -> runtime(file, "type LocalTime")
            PrimitiveKind.JSON_OBJECT -> runtime(file, "type JsonObject")
            PrimitiveKind.JSON_VALUE -> runtime(file, "type JsonValue")
            PrimitiveKind.BINARY -> "Uint8Array"
            PrimitiveKind.FILE -> runtime(file, "type FileUpload")
        }
    }

    // The support module.

    private fun supportFile(file: TsFile) {
        sdk(file, "type $apiType", "api")
        live(file, "type LiveApi")
        val account = plan.account
        file.body.apply {
            line("/** What the responses of ${spec.prefix} look like, for the runtime to check real ones against. */")
            line("const SHAPES = ${TsSyntax.string(plan.shapes)};")
            line()
            line("/** The live tests of ${spec.prefix}: see `common/specs/${spec.id}/tests.yaml`. */")
            block("export const $support: LiveApi<$apiType> = {", "};") {
                line("id: ${TsSyntax.string(spec.id)},")
                line("env: [${account.env.joinToString { TsSyntax.string(it) }}],")
                line("requestsPerSecond: ${plan.limits.requestsPerSecond},")
                line("maxRequests: ${plan.limits.maxRequests},")
                line("shapes: SHAPES,")
                if (plan.skips.isEmpty()) {
                    line("skips: {},")
                } else {
                    block("skips: {", "},") {
                        for (skip in plan.skips) line("${TsSyntax.string(skip.operation)}: ${TsSyntax.string(skip.reason)},")
                    }
                }
                create(file)
                if (plan.replays.isNotEmpty()) replay(file)
            }
            for ((name, credentials) in account.credentials) {
                line()
                credentialsFunction(name, credentials, file)
            }
            for (source in plan.sources) {
                line()
                sourceFunction(source, file)
            }
            for (record in fingerprintedRecords()) {
                line()
                fingerprintFunction(record, file)
            }
        }
    }

    private fun CodeWriter.create(file: TsFile) {
        val account = plan.account
        val creator = sdk(file, "create${layout.facadeName}", "api")
        if (account.headers.isNotEmpty()) live(file, "LiveTemplates")
        block("create(fetch, anonymous) {") {
            val headers = account.headers.entries.joinToString(", ") { (name, template) -> "${TsSyntax.propertyKey(name)}: LiveTemplates.resolve(${TsSyntax.string(template)})" }
            line("const headers: Record<string, string> = anonymous ? {} : { $headers };".replace("{  }", "{}"))
            val arguments = buildList {
                if (account.parameters.isNotEmpty()) {
                    add(account.parameters.joinToString(", ", "{ ", " }") { (parameter, arg) -> "${TsSyntax.propertyKey(parameter.name)}: ${argument(arg, parameter.type, file)}" })
                }
                add("{ fetch, headers }")
                val settings = spec.client.settings
                val last = settings.indexOfLast { it.name in account.settings }
                settings.take(last + 1).forEach { add(TsSyntax.string(account.settings[it.name] ?: it.default)) }
            }
            line("return $creator(${arguments.joinToString(", ")});")
        }
        line(",")
    }

    private fun CodeWriter.replay(file: TsFile) {
        block("async replay(live, caseId, values) {") {
            block("switch (caseId) {") {
                for (case in plan.replays) {
                    val undo = case.write!!.undo
                    val args = undo.args.mapValues { (name, arg) ->
                        when {
                            arg is Arg.Ref -> fromText("values[${TsSyntax.string(name)}]!", undo.operation.inputType(name)!!, file)
                            name == Input.AUTH -> argument(arg, null, file)
                            else -> argument(arg, undo.operation.inputType(name)!!, file)
                        }
                    }
                    line("case ${TsSyntax.string(case.id)}:")
                    indent {
                        line("await ${invocation(undo, args)};")
                        line("return true;")
                    }
                }
                line("default:")
                indent { line("return false;") }
            }
        }
        line(",")
    }

    private fun CodeWriter.credentialsFunction(name: String, credentials: Credentials, file: TsFile) {
        val type = sdk(file, layout.authClass(name), "models")
        live(file, "type LiveCase")
        val statements = CodeWriter("  ")
        val values = credentials.scheme.parameters.map { valueOf(credentials.values.getValue(it.name), TypeRef.STRING, it.name, statements, file) }
        val written = statements.toString().trimEnd()
        val parameter = if (written.isEmpty() && values.none { "live" in it }) "_live" else "live"
        block("export async function ${credentialsName(name)}($parameter: LiveCase<$apiType>): Promise<$type> {") {
            written.takeIf { it.isNotEmpty() }?.let(::lines)
            line("return new $type(${values.joinToString(", ")});")
        }
    }

    private fun credentialsName(scheme: String): String = "${scheme.decapitalized()}Credentials"

    private fun CodeWriter.sourceFunction(source: Source, file: TsFile) {
        live(file, "type LiveCase")
        val result = render(source.operation.resultType!!, file)
        val statements = CodeWriter("  ")
        val args = arguments(source.call, statements, file)
        block("export function ${source.id}(live: LiveCase<$apiType>): Promise<$result> {") {
            block("return live.source(${TsSyntax.string(source.id)}, ${callOptions(source.operation)}, async (live) => {", "});") {
                statements.toString().trimEnd().takeIf { it.isNotEmpty() }?.let(::lines)
                line("return ${invocation(source.call, args)};")
            }
        }
    }

    // Test files.

    private fun testFile(file: TsFile, module: String, cases: List<TestCase>) {
        file.import("describe", "vitest")
        live(file, "liveCase")
        file.importFile(support, supportFile)
        file.body.apply {
            line("// The live tests of the ${spec.prefix} $module module, planned from common/specs/${spec.id}/tests.yaml.")
            block("describe(${TsSyntax.string("${spec.prefix} $module")}, () => {", "});") {
                separated(cases) { case(it, file) }
            }
        }
    }

    private fun CodeWriter.case(case: TestCase, file: TsFile) {
        val options = buildList {
            if (case.effect.wireName != "read") add("effect: ${TsSyntax.string(case.effect.wireName)}")
            if (case.index != 0) add("index: ${case.index}")
        }.joinToString(", ", "{ ", " }").replace("{  }", "{}")
        block("liveCase($support, ${TsSyntax.string(case.id)}, $options, async (live) => {", "});") {
            val names = Names()
            val args = arguments(case.call, this, file, names)
            when (case.kind) {
                CaseKind.UNKNOWN_ID -> line("await live.refused(true, async () => ${invocation(case.call, args)});")
                CaseKind.INVALID_CREDENTIALS, CaseKind.ANONYMOUS -> line("await live.refused(false, async () => ${invocation(case.call, args)});")
                CaseKind.SMOKE, CaseKind.COMBINATION -> read(case, args, names, file)
                CaseKind.WRITE -> write(case, args, names, file)
            }
        }
    }

    private fun CodeWriter.read(case: TestCase, args: Map<String, String>, names: Names, file: TsFile) {
        val result = case.operation.resultType
        val errors = case.errors.takeIf { it.isNotEmpty() }?.joinToString(", ", "errors: [", "]") { TsSyntax.string(it) }
        val options = callOptions(case.operation, errors)
        if (result == null || (!case.compare && case.expectations.isEmpty())) {
            line("await live.call($options, async () => ${invocation(case.call, args)});")
            return
        }
        val value = names.local("result")
        line("const $value = await live.call($options, async () => ${invocation(case.call, args)});")
        if (case.compare) line("live.fingerprint((f) => ${fingerprint(result, value, file)});")
        for (expectation in case.expectations) expectation(expectation, value, args, file)
    }

    private fun CodeWriter.write(case: TestCase, args: Map<String, String>, names: Names, file: TsFile) {
        val write = case.write!!
        val refs = Refs(args, null, null)
        write.snapshot?.let { (call, path) ->
            val snapshot = names.local("snapshot")
            line("const $snapshot = await live.call(${callOptions(call.operation)}, async () => ${invocation(call, refArguments(call, refs, file))});")
            refs.before = names.local("before")
            line("const ${refs.before} = live.choose(\"the value to restore\", ${path(snapshot, path, file)});")
        }
        if (case.operation.resultType != null) {
            refs.result = names.local("result")
            line("const ${refs.result} = await live.call(${callOptions(case.operation)}, async () => ${invocation(case.call, args)});")
        } else {
            line("await live.call(${callOptions(case.operation)}, async () => ${invocation(case.call, args)});")
        }
        val undoArgs = refArguments(write.undo, refs, file)
        val replayable = case in plan.replays
        if (replayable) {
            val saved = write.undo.args.filterValues { it is Arg.Ref }.keys.joinToString(", ") { "${TsSyntax.propertyKey(it)}: String(${undoArgs.getValue(it)})" }
            line("live.journal({ $saved });")
        }
        val verify = write.verify
        fun CodeWriter.undo() {
            line("await live.call(${callOptions(write.undo.operation)}, async () => ${invocation(write.undo, undoArgs)});")
            if (replayable) line("live.unjournal();")
        }
        if (verify == null) {
            undo()
        } else {
            // The change is undone whatever the check finds.
            block("try {", "} finally {") { verifyCall(verify.first, verify.second, names.local("verified"), refs, "expectContains", file) }
            indent { undo() }
            line("}")
        }
        if (verify != null && write.snapshot == null) verifyCall(verify.first, verify.second, names.local("undone"), refs, "expectNotContains", file)
        write.snapshot?.let { (call, path) ->
            val restored = names.local("restored")
            line("const $restored = await live.call(${callOptions(call.operation)}, async () => ${invocation(call, refArguments(call, refs, file))});")
            line("live.expectContains(${TsSyntax.string(path.toString())}, ${path(restored, path, file)}, ${refs.before});")
        }
    }

    private fun CodeWriter.verifyCall(call: Call, contains: Expectation.Contains, name: String, refs: Refs, check: String, file: TsFile) {
        line("const $name = await live.call(${callOptions(call.operation)}, async () => ${invocation(call, refArguments(call, refs, file))});")
        line("live.$check(${TsSyntax.string(contains.path.toString())}, ${path(name, contains.path, file)}, ${refValue(contains.value, contains.path.type, refs, file)});")
    }

    private fun CodeWriter.expectation(expectation: Expectation, result: String, args: Map<String, String>, file: TsFile) {
        val values = path(result, expectation.path, file)
        val where = TsSyntax.string(expectation.path.toString())
        val refs = Refs(args, result, null)
        when (expectation) {
            is Expectation.NonEmpty -> line("live.expectNonEmpty($where, $values);")
            is Expectation.Contains -> line("live.expectContains($where, $values, ${refValue(expectation.value, expectation.path.type, refs, file)});")
            is Expectation.Equals -> line("live.expectEquals($where, $values, ${refValue(expectation.value, expectation.path.type, refs, file)});")
            is Expectation.Matches -> line("live.expectMatches($where, $values, ${TsSyntax.string(expectation.regex)});")
            is Expectation.Count -> line("live.expectCount($where, $values, ${expectation.min ?: "null"}, ${expectation.max ?: "null"});")
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

    private fun arguments(call: Call, writer: CodeWriter, file: TsFile, names: Names = Names()): Map<String, String> =
        call.args.mapValues { (name, arg) ->
            val value = valueOf(arg, call.operation.inputType(name), name, writer, file)
            if (arg is Arg.Pick || arg is Arg.Text || arg is Arg.AccountCredentials) {
                val variable = names.local(TsSyntax.variable(if (name in RESERVED_LOCALS) "${name}Value" else name))
                writer.line("const $variable = $value;")
                variable
            } else {
                value
            }
        }

    private fun refArguments(call: Call, refs: Refs, file: TsFile): Map<String, String> =
        call.args.mapValues { (name, arg) -> refValue(arg, call.operation.inputType(name), refs, file) }

    private fun refValue(arg: Arg, type: TypeRef?, refs: Refs, file: TsFile): String = when (arg) {
        is Arg.Ref -> {
            val root = when (arg.root) {
                Arg.Ref.Root.ARGS -> refs.args.getValue(arg.input!!)
                Arg.Ref.Root.RESULT -> refs.result ?: error("No result to refer to")
                Arg.Ref.Root.BEFORE -> refs.before ?: error("No saved value to refer to")
            }
            val value = if (arg.path.steps.isEmpty()) root else "live.first(${TsSyntax.string(arg.path.toString())}, ${path(root, arg.path, file)})"
            type?.let { convert(value, arg.path.type, it, file) } ?: value
        }
        is Arg.WithField -> "{ ...${refValue(arg.base, null, refs, file)}, ${TsSyntax.propertyKey(arg.field.name)}: ${refValue(arg.value, arg.field.type, refs, file)} }"
        is Arg.AccountCredentials -> credentials(arg, file)
        else -> argument(arg, type, file)
    }

    private fun valueOf(arg: Arg, type: TypeRef?, name: String, writer: CodeWriter, file: TsFile): String = when (arg) {
        is Arg.Pick -> {
            val target = if (arg.wrap) (type as TypeRef.ListOf).element else type!!
            file.importFile(arg.source.id, supportFile)
            val candidates = convertAll(path("await ${arg.source.id}(live)", arg.path, file), arg.path.type, target, file)
            val chosen = "live.choose(${TsSyntax.string(name)}, $candidates)"
            if (arg.wrap) "[$chosen]" else chosen
        }
        is Arg.AccountCredentials -> credentials(arg, file)
        else -> argument(arg, type, file)
    }

    private fun credentials(arg: Arg.AccountCredentials, file: TsFile): String {
        val name = credentialsName(arg.credentials.scheme.name)
        file.importFile(name, supportFile)
        return "await $name(live)"
    }

    private fun argument(arg: Arg, type: TypeRef?, file: TsFile): String = when (arg) {
        is Arg.Literal -> literal(arg.value, type!!, file)
        is Arg.Text -> {
            live(file, "LiveTemplates")
            fromText("LiveTemplates.resolve(${TsSyntax.string(arg.template)})", type!!, file)
        }
        is Arg.Json -> {
            val json = TsSyntax.string(arg.value.toString())
            val text = if (arg.templated) "${live(file, "LiveTemplates")}.resolveJson($json)" else json
            "live.decode(${decoder(type!!, file)}, $text)"
        }
        Arg.Unknown -> if ((type as TypeRef.Primitive).kind == PrimitiveKind.STRING) TsSyntax.string(UNKNOWN_TEXT) else UNKNOWN_ID.toString()
        is Arg.InvalidCredentials -> invalidCredentials(arg.scheme, file)
        is Arg.AccountCredentials -> credentials(arg, file)
        is Arg.Pick, is Arg.Ref, is Arg.WithField -> error("$arg needs statements or refs")
    }

    /** The decoder of [type], importing what it uses from the SDK. */
    private fun decoder(type: TypeRef, file: TsFile): String {
        val scratch = TsFile(layout.api("Scratch.ts"), spec.id)
        val expression = codecs.decoder(type, scratch)
        // The decoder's names come from the SDK's modules; import them from its folders instead.
        Regex("""\b(decode\w+|LocalDate|LocalDateTime|LocalTime|WireMap)\b""").findAll(expression).map { it.value }.distinct().forEach { name ->
            val declaration = name.removePrefix("decode")
            if (name.startsWith("decode") && declaration in spec.declarations) sdk(file, name, "models") else runtime(file, name)
        }
        return expression
    }

    private fun invalidCredentials(scheme: AuthScheme, file: TsFile): String {
        val type = sdk(file, layout.authClass(scheme.name), "models")
        return "new $type(${scheme.parameters.joinToString(", ") { TsSyntax.string(INVALID_CREDENTIAL) }})"
    }

    /** The call of the generated SDK: required inputs in order, then an options object for the optional ones. */
    private fun invocation(call: Call, args: Map<String, String>): String {
        val operation = call.operation.operation
        val receiver = if (call.anonymous) "live.anonymousApi" else "live.api"
        val nonPath = operation.parameters.filter { it.location != ParameterLocation.PATH }
        val positional = buildList {
            operation.pathParameters.forEach { add(args.getValue(it.name)) }
            if (operation.body is RequestBody.Json) add(args.getValue(Input.BODY))
            nonPath.filter { it.required }.forEach { add(args.getValue(it.name)) }
            if (operation.auth?.required == true) add(args.getValue(Input.AUTH))
        }
        val options = buildList {
            nonPath.filterNot { it.required }.forEach { parameter -> args[parameter.name]?.let { add("${TsSyntax.propertyKey(parameter.name)}: $it") } }
            if (operation.auth?.required == false) args[Input.AUTH]?.let { add("auth: $it") }
        }
        val all = positional + listOfNotNull(options.takeIf { it.isNotEmpty() }?.joinToString(", ", "{ ", " }"))
        return "$receiver.${call.operation.module.name.decapitalized()}.${operation.name}(${all.joinToString(", ")})"
    }

    /** The shape of [operation]'s response, and the options saying where its value is in it, as arguments of `call`. */
    private fun callOptions(operation: OperationRef, extra: String? = null): String {
        val response = operation.operation.response
        val type = operation.resultType
        val shape = when (response) {
            is ResponseBody.Json, is ResponseBody.ScriptVariable -> type?.let { TsSyntax.string(Shapes.shape(spec, it).toString()) } ?: "undefined"
            else -> "undefined"
        }
        val options = listOfNotNull(
            (response as? ResponseBody.Json)?.envelope?.takeIf { it.isNotEmpty() }?.joinToString(", ", "envelope: [", "]") { TsSyntax.string(it) },
            (response as? ResponseBody.ScriptVariable)?.let { "variable: ${TsSyntax.string(it.variable)}" },
            extra,
        )
        return "$shape, ${if (options.isEmpty()) "{}" else options.joinToString(", ", "{ ", " }")}"
    }

    // Values and types.

    private fun literal(value: JsonPrimitive, type: TypeRef, file: TsFile): String {
        if (TestTypes.enum(spec, type) != null) return TsSyntax.string(value.content)
        return when ((type as? TypeRef.Primitive)?.kind) {
            PrimitiveKind.STRING -> TsSyntax.string(value.content)
            PrimitiveKind.INT32, PrimitiveKind.INT64 -> value.content.toLong().toString()
            PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> value.content.toDouble().toString()
            PrimitiveKind.BOOLEAN -> (value.booleanOrNull ?: value.content.toBoolean()).toString()
            PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME -> fromText(TsSyntax.string(value.content), type, file)
            else -> error("No literal of $type")
        }
    }

    private fun fromText(text: String, type: TypeRef, file: TsFile): String {
        TestTypes.enum(spec, type)?.let { return "($text as ${sdk(file, "type ${it.name}", "models")})" }
        return when ((type as? TypeRef.Primitive)?.kind) {
            PrimitiveKind.STRING -> text
            PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> "Number($text)"
            PrimitiveKind.BOOLEAN -> "($text === \"true\")"
            PrimitiveKind.LOCAL_DATE -> "${runtime(file, "LocalDate")}.parse($text)!"
            PrimitiveKind.LOCAL_DATE_TIME -> "((text) => ${runtime(file, "LocalDateTime")}.parse(text.includes(\"T\") ? text : `\${text}T00:00:00`)!)($text)"
            PrimitiveKind.LOCAL_TIME -> "${runtime(file, "LocalTime")}.parse($text)!"
            else -> error("Can't convert text to $type")
        }
    }

    private fun convertAll(values: String, from: TypeRef, to: TypeRef, file: TsFile): String {
        val converted = convert("it", from, to, file)
        return if (converted == "it") values else "$values.map((it) => $converted)"
    }

    private fun convert(value: String, from: TypeRef, to: TypeRef, file: TsFile): String {
        if (from == to) return value
        val fromKind = (from as? TypeRef.Primitive)?.kind
        val toKind = (to as? TypeRef.Primitive)?.kind
        return when {
            from is TypeRef.ListOf && to is TypeRef.ListOf -> {
                val element = convert("it", from.element, to.element, file)
                if (element == "it") value else "$value.map((it) => $element)"
            }
            TestTypes.enum(spec, from) != null && toKind == PrimitiveKind.STRING -> value
            fromKind == PrimitiveKind.STRING && TestTypes.enum(spec, to) != null -> fromText(value, to, file)
            toKind == PrimitiveKind.STRING -> "String($value)"
            fromKind == PrimitiveKind.STRING && toKind in NUMBERS -> "Number.parseInt($value, 10)"
            fromKind in NUMBERS && toKind in NUMBERS -> value
            else -> error("Can't convert $from to $to")
        }
    }

    private fun path(root: String, path: Path, file: TsFile): String {
        live(file, "isPresent")
        var expression = "[$root]"
        var type = path.root
        for (step in path.steps) {
            when (step) {
                is Path.Step.Get -> {
                    expression += ".map((it) => ${access("it", step.field.name)}).filter(isPresent)"
                    type = step.field.type
                }
                Path.Step.Each -> {
                    expression += when {
                        type is TypeRef.MapOf && type.key == TypeRef.STRING -> ".flatMap((it) => Object.values(it))"
                        type is TypeRef.MapOf -> ".flatMap((it) => Array.from(it.values()))"
                        else -> ".flat()"
                    }
                    type = (type as? TypeRef.ListOf)?.element ?: (type as TypeRef.MapOf).value
                }
                is Path.Step.At -> {
                    expression += ".map((it) => it[${step.index}]).filter(isPresent)"
                    type = (type as TypeRef.ListOf).element
                }
                is Path.Step.Where -> expression += ".filter((it) => ${access("it", step.field.name)} === ${literal(step.value, step.field.type, file)})"
            }
        }
        return expression
    }

    private fun access(value: String, field: String): String {
        val key = TsSyntax.propertyKey(field)
        return if (key.startsWith("\"")) "$value[$key]" else "$value.$key"
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

    private fun CodeWriter.fingerprintFunction(record: RecordType, file: TsFile) {
        live(file, "type LiveFingerprint")
        val type = sdk(file, "type ${record.name}", "models")
        block("export function ${fingerprintName(record)}(f: LiveFingerprint, value: $type | null | undefined): void {") {
            line("if (value === null || value === undefined) return f.nil();")
            if (record.fields.isEmpty()) {
                line("f.obj(value, () => {});")
            } else {
                block("f.obj(value, () => {", "});") {
                    for (field in record.fields) line("${fingerprint(field.type, access("value", field.name), file)};")
                }
            }
        }
    }

    private fun fingerprintName(record: RecordType): String = "write${record.name}"

    /** The call writing [value] (of [type]) to the fingerprint `f`. */
    private fun fingerprint(type: TypeRef, value: String, file: TsFile, receiver: String = "f"): String {
        TestTypes.enum(spec, type)?.let { return "$receiver.str($value)" }
        TestTypes.record(spec, type)?.let {
            file.importFile(fingerprintName(it), supportFile)
            return "${fingerprintName(it)}($receiver, $value)"
        }
        return when (type) {
            is TypeRef.ListOf -> "$receiver.list($value, (it) => ${fingerprint(type.element, "it", file, receiver)})"
            is TypeRef.MapOf -> {
                val entries = if (type.key == TypeRef.STRING) "($value === null || $value === undefined ? null : Object.entries($value))" else value
                "$receiver.map($entries, (k, key) => ${fingerprint(type.key, "key", file, "k")}, (it) => ${fingerprint(type.value, "it", file, receiver)})"
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

    companion object {
        /** The file of the live test runtime, relative to the test output directory. */
        const val LIVE_FILE = "runtime/live.ts"

        private const val UNKNOWN_ID = 987_654_321
        private const val UNKNOWN_TEXT = "mybicocca-unknown-id"
        private const val INVALID_CREDENTIAL = "mybicocca-invalid-credential"
        private val NUMBERS = setOf(PrimitiveKind.INT32, PrimitiveKind.INT64, PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64)
        private val RESERVED_LOCALS = setOf("result", "snapshot", "before", "verified", "undone", "restored", "live", "it", "f")
    }
}
