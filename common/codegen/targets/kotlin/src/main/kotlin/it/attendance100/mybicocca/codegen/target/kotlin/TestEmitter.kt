package it.attendance100.mybicocca.codegen.target.kotlin

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
import it.attendance100.mybicocca.codegen.core.capitalized
import it.attendance100.mybicocca.codegen.core.decapitalized
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.model.Codec
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * Writes an API's live tests (see [TestPlan]) as JUnit 5 classes on top of the live test runtime
 * ([KotlinLiveRuntime]), in the `live` package next to the API's:
 *
 * - `<Prefix>Live`: the API's account, how to create its client, the sources cases take values
 *   from, the canonical form of its results and what its responses look like;
 * - `<Prefix><Module>LiveTest`: a test per case, calling the generated SDK.
 */
internal class TestEmitter(private val plan: TestPlan, private val layout: KotlinLayout, private val packageName: String, private val livePackage: String) {
    private val spec: ApiSpec = plan.spec
    private val support = "${spec.prefix}Live"
    private val facade = "${layout.apiPackage}.${layout.facadeName}"
    private val apiType = layout.facadeName

    fun files(): List<GeneratedFile> = buildList {
        add(file(support) { supportFile(it) })
        for ((module, cases) in plan.cases.groupBy { it.operation.module }) {
            add(file("${spec.prefix}${module.name}LiveTest") { testFile(it, module.name, cases) })
        }
    }

    private fun file(name: String, emit: (KotlinFile) -> Unit): GeneratedFile = KotlinFile(packageName, "$name.kt", spec.id).also(emit).toGeneratedFile()

    // The support object.

    private fun supportFile(file: KotlinFile) {
        file.import(facade)
        file.import("$livePackage.LiveApi")
        file.import("$livePackage.LiveCase")
        file.import("$livePackage.LiveTemplates")
        file.import("io.ktor.client.HttpClientConfig")
        val account = plan.account
        file.body.apply {
            line("/** The live tests of ${spec.prefix}: see `common/specs/${spec.id}/tests.yaml`. */")
            line("internal object $support : LiveApi<$apiType>(")
            indent {
                line("id = ${KotlinSyntax.string(spec.id)},")
                line("env = listOf(${account.env.joinToString { KotlinSyntax.string(it) }}),")
                line("requestsPerSecond = ${plan.limits.requestsPerSecond},")
                line("maxRequests = ${plan.limits.maxRequests},")
                line("shapes = SHAPES,")
                if (plan.skips.isEmpty()) {
                    line("skips = emptyMap(),")
                } else {
                    commaList("skips = mapOf(", plan.skips.map { "${KotlinSyntax.string(it.operation)} to ${KotlinSyntax.string(it.reason)}" }, "),", trailingComma = true)
                }
            }
            line(") {")
            indent {
                create(file)
                if (plan.replays.isNotEmpty()) {
                    line()
                    replay(file)
                }
            }
            line("}")
            for ((name, credentials) in account.credentials) {
                line()
                credentialsFunction(name, credentials, file)
            }
            for (source in plan.sources) {
                line()
                sourceFunction(source, file)
            }
            val records = fingerprintedRecords()
            for (record in records) {
                line()
                fingerprintFunction(record, file)
            }
            line()
            val shapes = plan.shapes.chunked(SHAPE_CHUNK)
            line("/** What the responses of ${spec.prefix} look like, for the runtime to check real ones against. */")
            if (shapes.size == 1) {
                line("private val SHAPES = ${KotlinSyntax.string(shapes.single())}")
            } else {
                commaList("private val SHAPES = listOf(", shapes.map { KotlinSyntax.string(it) }, ").joinToString(\"\")", trailingComma = true)
            }
        }
    }

    private fun CodeWriter.create(file: KotlinFile) {
        val account = plan.account
        line("override fun create(configure: HttpClientConfig<*>.() -> Unit, anonymous: Boolean): $apiType =")
        indent {
            val arguments = account.parameters.map { (parameter, arg) -> "${KotlinSyntax.identifier(parameter.name)} = ${constant(arg, parameter.type, file)}" }
            val settings = account.settings.entries
            val headers = account.headers.entries
            line("$apiType(${arguments.joinToString("") { "$it, " }}httpClientConfig = {")
            indent {
                line("configure()")
                if (headers.isNotEmpty()) {
                    file.import("io.ktor.client.plugins.defaultRequest")
                    file.import("io.ktor.client.request.header")
                    file.import("$livePackage.LiveTemplates")
                    block("if (!anonymous) defaultRequest {") {
                        for ((name, template) in headers) line("header(${KotlinSyntax.string(name)}, LiveTemplates.resolve(${KotlinSyntax.string(template)}))")
                    }
                }
            }
            if (settings.isEmpty()) {
                line("})")
            } else {
                line("}).also {")
                indent { for ((name, value) in settings) line("it.${KotlinSyntax.identifier(name)} = ${KotlinSyntax.string(value)}") }
                line("}")
            }
        }
    }

    private fun CodeWriter.replay(file: KotlinFile) {
        line("override suspend fun LiveCase<$apiType>.replay(case: String, values: Map<String, String>): Boolean {")
        indent {
            block("when (case) {") {
                for (case in plan.replays) {
                    val undo = case.write!!.undo
                    val args = undo.args.mapValues { (name, arg) ->
                        val type = undo.operation.inputType(name)
                        when {
                            arg is Arg.Ref -> fromText("values.getValue(${KotlinSyntax.string(name)})", type!!, file)
                            name == Input.AUTH -> argument(arg, null, file)
                            else -> constant(arg, type!!, file)
                        }
                    }
                    line("${KotlinSyntax.string(case.id)} -> ${invocation(undo, args, file)}")
                }
                line("else -> return false")
            }
            line("return true")
        }
        line("}")
    }

    private fun CodeWriter.credentialsFunction(name: String, credentials: Credentials, file: KotlinFile) {
        val type = layout.authClass(name)
        file.import("${layout.dtoPackage}.$type")
        val writer = this
        val statements = CodeWriter()
        val values = credentials.scheme.parameters.map { parameter ->
            "${KotlinSyntax.identifier(parameter.name)} = ${valueOf(credentials.values.getValue(parameter.name), TypeRef.STRING, parameter.name, statements, file)}"
        }
        writer.line("internal suspend fun LiveCase<$apiType>.${credentialsName(name)}(): $type {")
        writer.indent {
            statements.toString().trimEnd().takeIf { it.isNotEmpty() }?.let(::lines)
            line("return $type(${values.joinToString(", ")})")
        }
        writer.line("}")
    }

    private fun credentialsName(scheme: String): String = "${scheme.decapitalized()}Credentials"

    private fun CodeWriter.sourceFunction(source: Source, file: KotlinFile) {
        val result = source.operation.resultType!!
        val statements = CodeWriter()
        val args = arguments(source.call, statements, file)
        val options = callOptions(source.operation)
        line("internal suspend fun LiveCase<$apiType>.${source.id}(): ${layout.render(result, file)} =")
        indent {
            line("source(${KotlinSyntax.string(source.id)}, $options) {")
            indent {
                statements.toString().trimEnd().takeIf { it.isNotEmpty() }?.let(::lines)
                line(invocation(source.call, args, file))
            }
            line("}")
        }
    }

    // Test classes.

    private fun testFile(file: KotlinFile, module: String, cases: List<TestCase>) {
        file.import("$livePackage.LiveTest")
        file.import("org.junit.jupiter.api.DisplayName")
        file.import("org.junit.jupiter.api.Test")
        file.body.apply {
            line("/** The live tests of the ${spec.prefix} $module module, planned from `common/specs/${spec.id}/tests.yaml`. */")
            block("class ${spec.prefix}${module}LiveTest : LiveTest<$apiType>($support) {") {
                separated(cases) { case(it, file) }
            }
        }
    }

    private fun CodeWriter.case(case: TestCase, file: KotlinFile) {
        line("@Test")
        line("@DisplayName(${KotlinSyntax.string(case.id)})")
        val effect = case.effect.wireName
        val header = buildList {
            add(KotlinSyntax.string(case.id))
            if (effect != "read") add("effect = ${KotlinSyntax.string(effect)}")
            if (case.index != 0) add("index = ${case.index}")
        }
        line("fun ${case.methodName}() = case(${header.joinToString(", ")}) {")
        indent {
            val names = Names()
            val args = arguments(case.call, this, file, names)
            when (case.kind) {
                CaseKind.UNKNOWN_ID -> block("refused(acceptsEmpty = true) {") { line(invocation(case.call, args, file)) }
                CaseKind.INVALID_CREDENTIALS, CaseKind.ANONYMOUS -> block("refused(acceptsEmpty = false) {") { line(invocation(case.call, args, file)) }
                CaseKind.SMOKE, CaseKind.COMBINATION -> read(case, args, names, file)
                CaseKind.WRITE -> write(case, args, names, file)
            }
        }
        line("}")
    }

    private fun CodeWriter.read(case: TestCase, args: Map<String, String>, names: Names, file: KotlinFile) {
        val result = case.operation.resultType
        val errors = case.errors.takeIf { it.isNotEmpty() }?.let { ", errors = listOf(${it.joinToString { error -> KotlinSyntax.string(error) }})" }.orEmpty()
        val options = callOptions(case.operation) + errors
        if (result == null || (!case.compare && case.expectations.isEmpty())) {
            block("call($options) {") { line(invocation(case.call, args, file)) }
            return
        }
        val value = names.local("result")
        line("val $value = call($options) {")
        indent { line(invocation(case.call, args, file)) }
        line("} ?: return@case")
        if (case.compare) line("fingerprint { ${fingerprint(result, value, nullable = false, file)} }")
        for (expectation in case.expectations) expectation(expectation, value, args, file)
    }

    private fun CodeWriter.write(case: TestCase, args: Map<String, String>, names: Names, file: KotlinFile) {
        val write = case.write!!
        val refs = Refs(args, result = null, before = null)
        write.snapshot?.let { (call, path) ->
            val snapshot = names.local("snapshot")
            val snapshotArgs = refArguments(call, refs, file)
            line("val $snapshot = call(${callOptions(call.operation)}) {")
            indent { line(invocation(call, snapshotArgs, file)) }
            line("} ?: return@case")
            refs.before = names.local("before")
            line("val ${refs.before} = choose(\"the value to restore\", ${path(snapshot, path, file)})")
        }
        val resultType = case.operation.resultType
        if (resultType != null && write.usesResult) {
            refs.result = names.local("result")
            line("val ${refs.result} = call(${callOptions(case.operation)}) {")
            indent { line(invocation(case.call, args, file)) }
            line("} ?: return@case")
        } else {
            block("call(${callOptions(case.operation)}) {") { line(invocation(case.call, args, file)) }
        }
        val undoArgs = refArguments(write.undo, refs, file)
        if (case in plan.replays) {
            val saved = write.undo.args.filterValues { it is Arg.Ref }.keys.map { name -> "${KotlinSyntax.string(name)} to ${text(undoArgs.getValue(name), write.undo.operation.inputType(name)!!)}" }
            line("journal(mapOf(${saved.joinToString(", ")}))")
        }
        val verify = write.verify
        fun CodeWriter.undo() {
            block("call(${callOptions(write.undo.operation)}) {") { line(invocation(write.undo, undoArgs, file)) }
            if (case in plan.replays) line("unjournal()")
        }
        if (verify == null) {
            undo()
        } else {
            // The change is undone whatever the check finds.
            block("try {", "} finally {") { verifyCall(verify.first, verify.second, names.local("verified"), refs, "expectContains", file) }
            indent { undo() }
            line("}")
        }
        // After the undo, the change is gone: what the verify read showed no longer holds, and a restore put the old value back.
        if (verify != null && write.snapshot == null) {
            verifyCall(verify.first, verify.second, names.local("undone"), refs, "expectNotContains", file)
        }
        write.snapshot?.let { (call, path) ->
            val restored = names.local("restored")
            line("val $restored = call(${callOptions(call.operation)}) {")
            indent { line(invocation(call, refArguments(call, refs, file), file)) }
            line("} ?: return@case")
            line("expectContains(${KotlinSyntax.string(path.toString())}, ${path(restored, path, file)}, ${refs.before})")
        }
    }

    private fun CodeWriter.verifyCall(call: Call, contains: Expectation.Contains, name: String, refs: Refs, check: String, file: KotlinFile) {
        line("val $name = call(${callOptions(call.operation)}) {")
        indent { line(invocation(call, refArguments(call, refs, file), file)) }
        line("} ?: return@case")
        line("$check(${KotlinSyntax.string(contains.path.toString())}, ${path(name, contains.path, file)}, ${refValue(contains.value, contains.path.type, refs, file)})")
    }

    private fun CodeWriter.expectation(expectation: Expectation, result: String, args: Map<String, String>, file: KotlinFile) {
        val values = path(result, expectation.path, file)
        val where = KotlinSyntax.string(expectation.path.toString())
        val refs = Refs(args, result, null)
        when (expectation) {
            is Expectation.NonEmpty -> line("expectNonEmpty($where, $values)")
            is Expectation.Contains -> line("expectContains($where, $values, ${refValue(expectation.value, expectation.path.type, refs, file)})")
            is Expectation.Equals -> line("expectEquals($where, $values, ${refValue(expectation.value, expectation.path.type, refs, file)})")
            is Expectation.Matches -> line("expectMatches($where, $values, ${KotlinSyntax.string(expectation.regex)})")
            is Expectation.Count -> line("expectCount($where, $values, ${expectation.min}, ${expectation.max})")
        }
    }

    // Arguments.

    /** Local names of a case, kept apart from its arguments'. */
    private class Names {
        private val used = mutableSetOf<String>()

        fun argument(name: String): String = local(name)

        fun local(name: String): String {
            var candidate = name
            var suffix = 2
            while (!used.add(candidate)) candidate = "$name${suffix++}"
            return candidate
        }
    }

    /** What the refs of an undo, verify or expectation stand for: the case's argument variables, its result and the saved value. */
    private class Refs(val args: Map<String, String>, var result: String?, var before: String?)

    /**
     * Writes the statements choosing [call]'s arguments into [writer], and returns the expression
     * of each argument by input name: the variable holding it, or a constant.
     */
    private fun arguments(call: Call, writer: CodeWriter, file: KotlinFile, names: Names = Names()): Map<String, String> {
        val rows = rows(call, writer, file, names)
        return call.args.mapValues { (name, arg) ->
            val type = call.operation.inputType(name)
            val value = rows[name]?.let { (row, path) -> pick(row, path, (arg as Arg.Pick).wrap, type!!, name, file) }
                ?: valueOf(arg, type, name, writer, file)
            if (arg is Arg.Pick || arg is Arg.Text) {
                val reserved = name in RESERVED_LOCALS || KotlinSyntax.identifier(name) != name
                val variable = names.argument(if (reserved) "${name}Value" else name)
                writer.line("val $variable = $value")
                variable
            } else {
                value
            }
        }
    }

    /**
     * Writes the statements choosing the element each of [call]'s [Call.rowPicks] takes values
     * from, and returns the variable holding it and the path from it, by input name.
     */
    private fun rows(call: Call, writer: CodeWriter, file: KotlinFile, names: Names): Map<String, Pair<String, Path>> =
        call.rowPicks.flatMap { rowPick ->
            val variable = names.local("row")
            // Enums sort by their wire value, as on every platform.
            val keys = rowPick.fields.values.joinToString(", ") { field ->
                val key = if (TestTypes.enum(spec, field.type) != null) "value" else "toString()"
                "${path("element", field, file)}.firstOrNull()?.$key"
            }
            val candidates = path("${rowPick.source.id}()", rowPick.rows, file)
            writer.line("val $variable = chooseRow(${KotlinSyntax.string(rowPick.fields.keys.joinToString())}, $candidates) { element -> listOf($keys) }")
            rowPick.fields.map { (name, field) -> name to (variable to field) }
        }.toMap()

    /** The arguments of an undo, verify or snapshot call, whose refs name the case's values. */
    private fun refArguments(call: Call, refs: Refs, file: KotlinFile): Map<String, String> =
        call.args.mapValues { (name, arg) -> refValue(arg, call.operation.inputType(name), refs, file) }

    private fun refValue(arg: Arg, type: TypeRef?, refs: Refs, file: KotlinFile): String = when (arg) {
        is Arg.Ref -> {
            val root = when (arg.root) {
                Arg.Ref.Root.ARGS -> refs.args.getValue(arg.input!!)
                Arg.Ref.Root.RESULT -> refs.result ?: error("No result to refer to")
                Arg.Ref.Root.BEFORE -> refs.before ?: error("No saved value to refer to")
            }
            val value = if (arg.path.steps.isEmpty()) root else "first(${KotlinSyntax.string(arg.path.toString())}, ${path(root, arg.path, file)})"
            type?.let { convert(value, arg.path.type, it, file) } ?: value
        }
        is Arg.WithField -> "${refValue(arg.base, null, refs, file)}.copy(${KotlinSyntax.identifier(arg.field.name)} = ${refValue(arg.value, arg.field.type, refs, file)})"
        else -> argument(arg, type, file)
    }

    /** The expression of a value with no refs, writing what it needs to choose into [writer]. */
    private fun valueOf(arg: Arg, type: TypeRef?, name: String, writer: CodeWriter, file: KotlinFile): String = when (arg) {
        is Arg.Pick -> pick("${arg.source.id}()", arg.path, arg.wrap, type!!, name, file)
        else -> argument(arg, type, file)
    }

    /** One of the values [path] reaches from [root], of an input of type [type], in a list when [wrap]. */
    private fun pick(root: String, path: Path, wrap: Boolean, type: TypeRef, name: String, file: KotlinFile): String {
        val target = if (wrap) (type as TypeRef.ListOf).element else type
        val candidates = convertAll(path(root, path, file), path.type, target, file)
        // Enums sort by their wire value, as on every platform.
        val key = if (TestTypes.enum(spec, target) != null) " { it.value }" else ""
        val chosen = "choose(${KotlinSyntax.string(name)}, $candidates)$key"
        return if (wrap) "listOf($chosen)" else chosen
    }

    /** The expression of a value that needs no statements. */
    private fun argument(arg: Arg, type: TypeRef?, file: KotlinFile): String = when (arg) {
        is Arg.Literal -> literal(arg.value, type!!, file)
        is Arg.Text -> {
            file.import("$livePackage.LiveTemplates")
            fromText("LiveTemplates.resolve(${KotlinSyntax.string(arg.template)})", type!!, file)
        }
        is Arg.Json -> {
            file.import("$livePackage.LiveJson")
            file.import("kotlinx.serialization.decodeFromString")
            val json = KotlinSyntax.string(arg.value.toString())
            val text = if (arg.templated) {
                file.import("$livePackage.LiveTemplates")
                "LiveTemplates.resolveJson($json)"
            } else {
                json
            }
            "LiveJson.decodeFromString<${layout.render(type!!, file)}>($text)"
        }
        Arg.Unknown -> when ((type as TypeRef.Primitive).kind) {
            PrimitiveKind.INT32 -> UNKNOWN_ID.toString()
            PrimitiveKind.INT64 -> "${UNKNOWN_ID}L"
            else -> KotlinSyntax.string(UNKNOWN_TEXT)
        }
        is Arg.InvalidCredentials -> invalidCredentials(arg.scheme, file)
        is Arg.AccountCredentials -> "${credentialsName(arg.credentials.scheme.name)}()"
        is Arg.Pick, is Arg.Ref, is Arg.WithField -> error("$arg needs statements or refs")
    }

    /** A constant: an API parameter's value, or a static argument of a replayed undo. */
    private fun constant(arg: Arg, type: TypeRef, file: KotlinFile): String = argument(arg, type, file)

    private fun invalidCredentials(scheme: AuthScheme, file: KotlinFile): String {
        val type = layout.authClass(scheme.name)
        file.import("${layout.dtoPackage}.$type")
        return "$type(${scheme.parameters.joinToString(", ") { "${KotlinSyntax.identifier(it.name)} = ${KotlinSyntax.string(INVALID_CREDENTIAL)}" }})"
    }

    /** The call of the generated SDK, with named arguments. */
    private fun invocation(call: Call, args: Map<String, String>, file: KotlinFile): String {
        file.import(facade)
        val receiver = if (call.anonymous) "anonymousApi" else "api"
        val module = KotlinSyntax.identifier(call.operation.module.name.decapitalized())
        val operation = KotlinSyntax.identifier(call.operation.operation.name)
        return "$receiver.$module.$operation(${args.entries.joinToString(", ") { (name, value) -> "${KotlinSyntax.identifier(name)} = $value" }})"
    }

    /** The shape of [operation]'s response, and where its value is in it, as arguments of `call`. */
    private fun callOptions(operation: OperationRef): String {
        val response = operation.operation.response
        val type = operation.resultType
        val shape = when (response) {
            is ResponseBody.Json, is ResponseBody.ScriptVariable -> type?.let { KotlinSyntax.string(Shapes.shape(spec, it).toString()) } ?: "null"
            else -> "null"
        }
        return buildList {
            add(shape)
            (response as? ResponseBody.Json)?.envelope?.takeIf { it.isNotEmpty() }?.let { envelope ->
                add("envelope = listOf(${envelope.joinToString { KotlinSyntax.string(it) }})")
            }
            (response as? ResponseBody.ScriptVariable)?.let { add("variable = ${KotlinSyntax.string(it.variable)}") }
        }.joinToString(", ")
    }

    // Values and types.

    private fun literal(value: JsonPrimitive, type: TypeRef, file: KotlinFile): String {
        TestTypes.enum(spec, type)?.let { enum ->
            file.import("${layout.dtoPackage}.${enum.name}")
            return "${enum.name}.${enum.values.first { it.wireValue == value.content }.name}"
        }
        return when ((type as? TypeRef.Primitive)?.kind) {
            PrimitiveKind.STRING -> KotlinSyntax.string(value.content)
            PrimitiveKind.INT32 -> value.content.toLong().toString()
            PrimitiveKind.INT64 -> "${value.content.toLong()}L"
            PrimitiveKind.FLOAT32 -> "${value.content.toDouble()}f"
            PrimitiveKind.FLOAT64 -> value.content.toDouble().toString()
            PrimitiveKind.BOOLEAN -> (value.booleanOrNull ?: value.content.toBoolean()).toString()
            PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME, PrimitiveKind.LOCAL_TIME -> fromText(KotlinSyntax.string(value.content), type, file)
            else -> error("No literal of $type")
        }
    }

    /** [text], an expression of a `String`, converted to [type]. */
    private fun fromText(text: String, type: TypeRef, file: KotlinFile): String {
        TestTypes.enum(spec, type)?.let { enum ->
            file.import("${layout.dtoPackage}.${enum.name}")
            file.import("$livePackage.LiveJson")
            file.import("kotlinx.serialization.json.JsonPrimitive")
            return "LiveJson.decodeFromJsonElement(${enum.name}.Serializer, JsonPrimitive($text))"
        }
        return when ((type as? TypeRef.Primitive)?.kind) {
            PrimitiveKind.STRING -> text
            PrimitiveKind.INT32 -> "$text.toInt()"
            PrimitiveKind.INT64 -> "$text.toLong()"
            PrimitiveKind.FLOAT32 -> "$text.toFloat()"
            PrimitiveKind.FLOAT64 -> "$text.toDouble()"
            PrimitiveKind.BOOLEAN -> "$text.toBoolean()"
            PrimitiveKind.LOCAL_DATE -> {
                file.import("java.time.LocalDate")
                "LocalDate.parse($text)"
            }
            PrimitiveKind.LOCAL_DATE_TIME -> {
                file.import("java.time.LocalDateTime")
                // A date stands for its midnight.
                "$text.let { if ('T' in it) LocalDateTime.parse(it) else LocalDateTime.parse(it + \"T00:00:00\") }"
            }
            PrimitiveKind.LOCAL_TIME -> {
                file.import("java.time.LocalTime")
                "LocalTime.parse($text)"
            }
            else -> error("Can't convert text to $type")
        }
    }

    /** The text a journal saves of [value] of [type], which [fromText] reads back. */
    private fun text(value: String, type: TypeRef): String = when {
        TestTypes.enum(spec, type) != null -> "$value.value"
        else -> "$value.toString()"
    }

    /** Each of [values] (a list of [from]) converted to [to]. */
    private fun convertAll(values: String, from: TypeRef, to: TypeRef, file: KotlinFile): String {
        if (from == to) return values
        return "$values.map { ${convert("it", from, to, file)} }"
    }

    /** [value] of type [from] converted to [to]; null when it can't be. */
    private fun convert(value: String, from: TypeRef, to: TypeRef, file: KotlinFile): String {
        if (from == to) return value
        val fromKind = (from as? TypeRef.Primitive)?.kind
        val toKind = (to as? TypeRef.Primitive)?.kind
        return when {
            from is TypeRef.ListOf && to is TypeRef.ListOf -> "$value.map { ${convert("it", from.element, to.element, file)} }"
            TestTypes.enum(spec, from) != null && toKind == PrimitiveKind.STRING -> "$value.value"
            fromKind == PrimitiveKind.STRING && TestTypes.enum(spec, to) != null -> fromText(value, to, file)
            toKind == PrimitiveKind.STRING -> "$value.toString()"
            toKind == PrimitiveKind.INT32 -> if (fromKind == PrimitiveKind.STRING) "$value.toIntOrNull()" else "$value.toInt()"
            toKind == PrimitiveKind.INT64 -> if (fromKind == PrimitiveKind.STRING) "$value.toLongOrNull()" else "$value.toLong()"
            toKind == PrimitiveKind.FLOAT64 -> "$value.toDouble()"
            toKind == PrimitiveKind.FLOAT32 -> "$value.toFloat()"
            else -> error("Can't convert $from to $to")
        }
    }

    /** The values [path] reaches from [root], an expression of its root type, as a list. */
    private fun path(root: String, path: Path, file: KotlinFile): String {
        var expression = "listOf($root)"
        var type = path.root
        for (step in path.steps) {
            when (step) {
                is Path.Step.Get -> {
                    expression += ".mapNotNull { it.${KotlinSyntax.identifier(step.field.name)} }"
                    type = step.field.type
                }
                Path.Step.Each -> {
                    expression += if (type is TypeRef.MapOf) ".flatMap { it.values }" else ".flatten()"
                    type = (type as? TypeRef.ListOf)?.element ?: (type as TypeRef.MapOf).value
                }
                is Path.Step.At -> {
                    expression += ".mapNotNull { it.getOrNull(${step.index}) }"
                    type = (type as TypeRef.ListOf).element
                }
                is Path.Step.Where -> expression += ".filter { it.${KotlinSyntax.identifier(step.field.name)} == ${literal(step.value, step.field.type, file)} }"
            }
        }
        return expression
    }

    // Fingerprints.

    /** The records whose canonical form the compared results need. */
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

    private fun CodeWriter.fingerprintFunction(record: RecordType, file: KotlinFile) {
        file.import("$livePackage.LiveFingerprint")
        file.import("${layout.dtoPackage}.${record.name}")
        val fields = record.fields
        line("internal fun LiveFingerprint.${fingerprintName(record)}(value: ${record.name}?) {")
        indent {
            line("if (value == null) return nil()")
            if (fields.isEmpty()) {
                line("obj(value) {}")
            } else {
                block("obj(value) {") {
                    for (field in fields) {
                        val value = "value.${KotlinSyntax.identifier(field.name)}"
                        val type = field.type
                        // A list sent as an object's values has no order every platform keeps.
                        line(
                            if (field.codec == Codec.ObjectValues && type is TypeRef.ListOf) "unordered($value) { ${fingerprint(type.element, "it", nullable = false, file)} }"
                            else fingerprint(type, value, field.nullable, file)
                        )
                    }
                }
            }
        }
        line("}")
    }

    private fun fingerprintName(record: RecordType): String = "write${record.name}"

    /** The statement writing [value] (of [type], null when [nullable]) to a `LiveFingerprint` receiver. */
    private fun fingerprint(type: TypeRef, value: String, nullable: Boolean, file: KotlinFile): String {
        val safe = if (nullable) "?." else "."
        TestTypes.enum(spec, type)?.let { return "str($value${safe}value)" }
        TestTypes.record(spec, type)?.let { return "${fingerprintName(it)}($value)" }
        return when (type) {
            is TypeRef.ListOf -> "list($value) { ${fingerprint(type.element, "it", nullable = false, file)} }"
            is TypeRef.MapOf -> "map($value, { ${fingerprint(type.key, "it", nullable = false, file)} }) { ${fingerprint(type.value, "it", nullable = false, file)} }"
            is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> "str($value)"
                PrimitiveKind.INT32 -> "int($value${safe}toLong())"
                PrimitiveKind.INT64 -> "int($value)"
                PrimitiveKind.FLOAT32 -> "num($value${safe}toDouble())"
                PrimitiveKind.FLOAT64 -> "num($value)"
                PrimitiveKind.BOOLEAN -> "bool($value)"
                PrimitiveKind.LOCAL_DATE -> "date($value)"
                PrimitiveKind.LOCAL_DATE_TIME -> "dateTime($value)"
                PrimitiveKind.LOCAL_TIME -> "time($value)"
                PrimitiveKind.JSON_OBJECT, PrimitiveKind.JSON_VALUE -> "json($value)"
                PrimitiveKind.BINARY, PrimitiveKind.FILE -> "nil()"
            }
            is TypeRef.Named -> "nil()"
        }
    }

    private companion object {
        /** The longest string literal of the shapes; the JVM limits a constant to 64 KB of UTF-8. */
        const val SHAPE_CHUNK = 16_000
        const val UNKNOWN_ID = 987_654_321
        const val UNKNOWN_TEXT = "mybicocca-unknown-id"
        const val INVALID_CREDENTIAL = "mybicocca-invalid-credential"

        /** Names of the locals cases declare, which arguments mustn't take. */
        val RESERVED_LOCALS = setOf("result", "snapshot", "before", "verified", "undone", "restored", "api", "anonymousApi", "it")
    }
}
