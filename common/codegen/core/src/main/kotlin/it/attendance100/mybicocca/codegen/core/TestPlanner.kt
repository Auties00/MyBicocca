package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.Effect
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.PrimitiveKind
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Works out the live tests of an API from its spec and `tests.yaml` (see [LiveTestsConfig]):
 *
 * - **Reads** are called with every pair of parameter values tried at least once (see
 *   [Pairwise]), up to the config's limit: every value of an enum, both booleans, optional
 *   parameters left out and sent. The first combination sends only what the operation needs.
 * - **Ids** come from other operations' results: a parameter takes the values of a field of the
 *   same name (`careerId`, or `id` of a record named after the entity) in a read callable without
 *   inputs, or with inputs found the same way. `tests.yaml` can say where a value comes from.
 * - **Errors**: a read also gets an id no server knows, and invalid credentials, which must be
 *   refused with the API's error.
 * - **Writes**: benign operations are called once; reversible ones once, then undone as
 *   `tests.yaml` says. Neither takes values the planner picked itself: only those `tests.yaml`
 *   gives. Irreversible operations are never called.
 *
 * Operations no case calls are listed with the reason, such as a role the account lacks or an
 * input nobody gives. Mistakes in `tests.yaml` (an unknown operation, field or parameter, a value
 * of the wrong type) fail generation.
 */
class TestPlanner(private val spec: ApiSpec, private val config: LiveTestsConfig) {
    private val operations: Map<String, OperationRef> =
        spec.modules.flatMap { module -> module.operations.map { OperationRef(module, it) } }.associateBy { it.id }
    private val roles = config.account.roles.toSet()
    private val sources = LinkedHashMap<String, Source?>()
    private val sourceIds = mutableSetOf<String>()
    private val skips = mutableListOf<Skip>()
    private val credentials: Map<String, Credentials> by lazy { credentials() }

    fun plan(): TestPlan {
        val unknown = (config.operations.keys + config.account.anonymous) - operations.keys
        require(unknown.isEmpty()) { "tests.yaml of '${spec.id}' names operations that don't exist: ${unknown.sorted()}" }
        val account = account()
        val cases = operations.values.flatMap { operation -> cases(operation) }
        val roots = (cases.flatMap { case ->
            listOfNotNull(case.call, case.write?.undo, case.write?.snapshot?.first, case.write?.verify?.first)
        } + sources.values.filterNotNull().map { it.call }).mapNotNull { it.operation.resultType }
        return TestPlan(
            spec = spec,
            account = account,
            limits = config.limits,
            sources = sources.values.filterNotNull(),
            cases = cases,
            skips = skips.toList(),
            shapes = Shapes.json(spec, roots).toString(),
            replays = cases.filter { it.write != null && isReplayable(it) },
        )
    }

    // The account.

    private fun account(): PlannedAccount {
        val account = config.account
        for ((name, template) in account.headers) {
            try {
                Templates.check(template)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("account.headers.$name: ${e.message}", e)
            }
        }
        val unknownSettings = account.settings.keys - spec.client.settings.map { it.name }.toSet()
        require(unknownSettings.isEmpty()) { "account.settings names unknown settings $unknownSettings" }
        val unknownParameters = account.parameters.keys - spec.parameters.map { it.name }.toSet()
        require(unknownParameters.isEmpty()) { "account.parameters names unknown API parameters $unknownParameters" }
        val parameters = spec.parameters.map { parameter ->
            val value = account.parameters[parameter.name] ?: throw IllegalArgumentException("account.parameters needs a value for the API parameter '${parameter.name}'")
            val arg = constant(value, parameter.type, "account.parameters.${parameter.name}")
                ?: throw IllegalArgumentException("account.parameters.${parameter.name} can't be left out")
            parameter to arg
        }
        val env = (account.env + account.headers.values.flatMap(Templates::envVariables) +
            config.operations.values.flatMap { tests -> tests.inputs.values.flatMap(::envVariables) } +
            config.defaults.values.flatMap(::envVariables) +
            account.auth.values.flatMap { it.values }.flatMap(::envVariables) +
            account.parameters.values.flatMap(::envVariables)).distinct()
        return PlannedAccount(env, account.headers, parameters, account.settings, credentials)
    }

    private fun envVariables(value: ValueSpec): List<String> = when (value) {
        is ValueSpec.Text -> Templates.envVariables(value.template)
        is ValueSpec.OneOf -> value.values.flatMap(::envVariables)
        is ValueSpec.Pick -> value.with.values.flatMap(::envVariables)
        else -> emptyList()
    }

    private fun credentials(): Map<String, Credentials> = config.account.auth.mapValues { (name, values) ->
        val scheme = spec.authSchemes.firstOrNull { it.name == name }
            ?: throw IllegalArgumentException("account.auth names unknown auth scheme '$name'; the API has ${spec.authSchemes.map { it.name }}")
        val unknown = values.keys - scheme.parameters.map { it.name }.toSet()
        require(unknown.isEmpty()) { "account.auth.$name names unknown credentials $unknown" }
        Credentials(scheme, scheme.parameters.associate { parameter ->
            val value = values[parameter.name] ?: throw IllegalArgumentException("account.auth.$name needs '${parameter.name}'")
            val where = "account.auth.$name.${parameter.name}"
            parameter.name to when (val resolved = value(value, TypeRef.STRING, where, null, stack = emptySet())) {
                is Resolved.Value -> resolved.arg ?: throw IllegalArgumentException("$where can't be left out")
                is Resolved.Missing -> throw IllegalArgumentException("$where can't be worked out: ${resolved.reason}")
            }
        })
    }

    /** The credentials to call [operation] with: an [Arg] for its `auth` input, null when it takes none, or why it can't be called. */
    private fun auth(operation: OperationRef): Resolved {
        val auth = operation.operation.auth ?: return Resolved.Value(null)
        val usable = auth.schemes.firstNotNullOfOrNull { credentials[it] }
        return when {
            usable != null -> Resolved.Value(Arg.AccountCredentials(usable))
            auth.required -> Resolved.Missing("needs credentials of ${auth.schemes.joinToString(" or ")} in account.auth")
            else -> Resolved.Value(null)
        }
    }

    // Cases.

    private fun cases(operation: OperationRef): List<TestCase> {
        val tests = config.operations[operation.id] ?: LiveTestsConfig.OperationTests()
        val effect = operation.operation.effect ?: error("Operation ${operation.id} has no effect")
        val required = operation.operation.roles
        fun skip(reason: String): List<TestCase> {
            skips += Skip(operation.id, reason)
            return emptyList()
        }
        if (tests.skip != null) {
            validate(operation, tests, effect)
            return skip(tests.skip)
        }
        when {
            required.isNotEmpty() && required.none { it in roles } ->
                return skip("needs one of the roles ${required.sorted()}; the account has ${roles.sorted()}")
            effect == Effect.IRREVERSIBLE -> return skip("irreversible")
            effect == Effect.REVERSIBLE && tests.undo == null -> return skip("reversible, but tests.yaml doesn't say how to undo it")
        }
        val dimensions = when (val resolved = dimensions(operation, tests, explicitOnly = effect != Effect.READ)) {
            is Dimensions.Missing -> return skip(resolved.reason)
            is Dimensions.Found -> resolved
        }
        val auth = when (val resolved = auth(operation)) {
            is Resolved.Missing -> return skip(resolved.reason)
            is Resolved.Value -> resolved.arg
        }
        fun args(row: List<Int>): Map<String, Arg> = buildMap {
            putAll(dimensions.fixed)
            dimensions.varying.forEachIndexed { index, (name, values) -> values[row[index]]?.let { put(name, it) } }
            auth?.let { put(Input.AUTH, it) }
        }
        val first = args(List(dimensions.varying.size) { 0 })
        val expectations = tests.expect.map { expectation(it, operation) }
        if (effect == Effect.REVERSIBLE) {
            val write = write(operation, tests, first)
            return listOf(TestCase("${operation.id}#write", CaseKind.WRITE, Call(operation, first), 0, effect, expectations, tests.errors, write = write))
        }

        val combinations = if (effect == Effect.READ) {
            Pairwise.rows(dimensions.varying.map { it.second.size }, config.limits.casesPerOperation, operation.id.hashCode().toLong())
        } else {
            listOf(List(dimensions.varying.size) { 0 })
        }
        val compare = effect == Effect.READ && !tests.volatile && operation.resultType != null
        return buildList {
            combinations.forEachIndexed { index, row ->
                val kind = if (index == 0) CaseKind.SMOKE else CaseKind.COMBINATION
                add(TestCase("${operation.id}#${index + 1}", kind, Call(operation, args(row)), index, effect, expectations, tests.errors, compare))
            }
            if (effect != Effect.READ) return@buildList
            if (config.limits.negative && tests.negative) {
                unknownId(operation, first)?.let { add(TestCase("${operation.id}#unknown-id", CaseKind.UNKNOWN_ID, it, 0, effect)) }
                invalidCredentials(operation, first)?.let { add(TestCase("${operation.id}#invalid-credentials", CaseKind.INVALID_CREDENTIALS, it, 0, effect)) }
            }
            if (operation.id in config.account.anonymous) {
                add(TestCase("${operation.id}#anonymous", CaseKind.ANONYMOUS, Call(operation, first, anonymous = true), 0, effect))
            }
        }
    }

    /**
     * Works out the inputs, undo and checks `tests.yaml` gives a skipped operation, so that its
     * mistakes fail generation even before it is enabled; values that can't be found are fine.
     */
    private fun validate(operation: OperationRef, tests: LiveTestsConfig.OperationTests, effect: Effect) {
        val dimensions = dimensions(operation, tests, explicitOnly = effect != Effect.READ) as? Dimensions.Found ?: return
        tests.expect.forEach { expectation(it, operation) }
        if (tests.undo != null) {
            val args = dimensions.fixed + dimensions.varying.mapNotNull { (name, values) -> values.first()?.let { name to it } }
            write(operation, tests, args + listOfNotNull((auth(operation) as? Resolved.Value)?.arg?.let { Input.AUTH to it }))
        }
    }

    /** The call of [operation] with an id no server knows, in its first required path parameter whose value comes from another call. */
    private fun unknownId(operation: OperationRef, args: Map<String, Arg>): Call? {
        val parameter = operation.operation.pathParameters.firstOrNull { parameter ->
            args[parameter.name] is Arg.Pick && (parameter.type as? TypeRef.Primitive)?.kind in ID_KINDS
        } ?: return null
        return Call(operation, args + (parameter.name to Arg.Unknown))
    }

    private val invalidCredentialModules = mutableSetOf<String>()

    /** The call of [operation] with credentials no server accepts: for the first operation of a module that needs them. */
    private fun invalidCredentials(operation: OperationRef, args: Map<String, Arg>): Call? {
        val auth = operation.operation.auth?.takeIf { it.required } ?: return null
        val credentials = args[Input.AUTH] as? Arg.AccountCredentials ?: return null
        if (!invalidCredentialModules.add(operation.module.name)) return null
        check(credentials.credentials.scheme.name in auth.schemes)
        return Call(operation, args + (Input.AUTH to Arg.InvalidCredentials(credentials.credentials.scheme)))
    }

    // Writes.

    private fun write(operation: OperationRef, tests: LiveTestsConfig.OperationTests, args: Map<String, Arg>): WritePlan {
        val where = "operations.${operation.id}"
        val result = operation.resultType
        val refs = Refs(operation, result, before = null)
        return when (val undo = tests.undo!!) {
            is UndoSpec.Call -> {
                val target = this.operations[undo.call] ?: throw IllegalArgumentException("$where.undo calls unknown operation '${undo.call}'")
                WritePlan(snapshot = null, verify = verify(tests.verify, refs, where), undo = call(target, undo.with, refs, "$where.undo", inheritFrom = args))
            }
            is UndoSpec.Restore -> {
                val read = this.operations[undo.read] ?: throw IllegalArgumentException("$where.undo.restore reads unknown operation '${undo.read}'")
                val snapshot = call(read, undo.with, refs, "$where.undo.restore", inheritFrom = args)
                val readType = read.resultType ?: throw IllegalArgumentException("$where.undo.restore reads '${undo.read}', which returns nothing")
                val saved = path(undo.pick, readType, emptyMap(), "$where.undo.restore.pick")
                val input = undo.into.substringBefore('.')
                val inputType = operation.inputType(input) ?: throw IllegalArgumentException("$where.undo.restore.into names unknown input '$input'")
                if (input !in args) throw IllegalArgumentException("$where.undo.restore.into names '$input', which the call doesn't send")
                // The undo sends the same values the call did, rather than picking them again.
                val same = args.mapValues { (name, arg) -> if (name == Input.AUTH) arg else Arg.Ref(Arg.Ref.Root.ARGS, name, Path.identity(operation.inputType(name)!!)) }
                val base = same.getValue(input)
                val restored = if ('.' in undo.into) {
                    val record = TestTypes.record(spec, inputType) ?: throw IllegalArgumentException("$where.undo.restore.into: '$input' isn't a record")
                    val name = undo.into.substringAfter('.')
                    val field = record.fields.firstOrNull { it.name == name } ?: throw IllegalArgumentException("$where.undo.restore.into: ${record.name} has no field '$name'")
                    requireAssignable(saved.type, field.type, "$where.undo.restore")
                    Arg.WithField(base, record, field, Arg.Ref(Arg.Ref.Root.BEFORE, null, Path.identity(saved.type)))
                } else {
                    requireAssignable(saved.type, inputType, "$where.undo.restore")
                    Arg.Ref(Arg.Ref.Root.BEFORE, null, Path.identity(saved.type))
                }
                val verifyRefs = Refs(operation, result, before = saved.type)
                WritePlan(
                    snapshot = snapshot to saved,
                    verify = verify(tests.verify, verifyRefs, where),
                    undo = Call(operation, same + (input to restored)),
                )
            }
        }
    }

    private fun verify(verify: VerifySpec?, refs: Refs, where: String): Pair<Call, Expectation.Contains>? {
        verify ?: return null
        val operation = operations[verify.call] ?: throw IllegalArgumentException("$where.verify calls unknown operation '${verify.call}'")
        val call = call(operation, verify.with, refs, "$where.verify", inheritFrom = emptyMap())
        val type = operation.resultType ?: throw IllegalArgumentException("$where.verify calls '${verify.call}', which returns nothing")
        val path = path(verify.path, type, emptyMap(), "$where.verify.contains.path")
        val value = argument(verify.value, path.type, "$where.verify.contains.value", refs)
        return call to Expectation.Contains(path, value)
    }

    /**
     * A call of [operation] with [with]: values that may refer to the call being undone or verified;
     * a required input [with] doesn't give takes the argument of the same name of [inheritFrom].
     */
    private fun call(operation: OperationRef, with: Map<String, ValueSpec>, refs: Refs, where: String, inheritFrom: Map<String, Arg>): Call {
        val inputs = inputs(operation)
        val unknown = with.keys - inputs.map { it.name }.toSet()
        require(unknown.isEmpty()) { "$where gives unknown inputs $unknown of ${operation.id}; it takes ${inputs.map { it.name }}" }
        val args = buildMap {
            for (input in inputs) {
                val value = with[input.name]
                when {
                    value != null -> argument(value, input.type, "$where.with.${input.name}", refs).let { put(input.name, it) }
                    input.required && input.name in inheritFrom -> {
                        val inherited = Arg.Ref(Arg.Ref.Root.ARGS, input.name, Path.identity(refs.operation.inputType(input.name)!!))
                        requireAssignable(inherited.path.type, input.type, "$where.with.${input.name}")
                        put(input.name, inherited)
                    }
                    input.required -> throw IllegalArgumentException("$where needs a value for '${input.name}' of ${operation.id}")
                }
            }
            when (val auth = auth(operation)) {
                is Resolved.Value -> auth.arg?.let { put(Input.AUTH, it) }
                is Resolved.Missing -> throw IllegalArgumentException("$where: ${operation.id} ${auth.reason}")
            }
        }
        return Call(operation, args)
    }

    /** What [refs] can refer to: the arguments of [operation], its [result] and the value a restore saved. */
    private class Refs(val operation: OperationRef, val result: TypeRef?, val before: TypeRef?)

    /** Whether the undo of [case] only takes values a journal can save as text. */
    private fun isReplayable(case: TestCase): Boolean {
        val undo = case.write?.undo ?: return false
        return undo.args.all { (name, arg) ->
            when (arg) {
                is Arg.Literal, is Arg.Text, is Arg.Json, is Arg.AccountCredentials -> true
                is Arg.Ref -> isScalar(arg.path.type)
                else -> name == Input.AUTH
            }
        }
    }

    // Inputs and their values.

    /** An input of an operation: a parameter or the body. */
    private class InputDef(val name: String, val type: TypeRef, val required: Boolean, val parameter: Parameter?)

    /** The inputs of [operation], in the order of its generated signatures: path parameters, the body, then the others. */
    private fun inputs(operation: OperationRef): List<InputDef> = buildList {
        val op = operation.operation
        op.pathParameters.forEach { add(InputDef(it.name, it.type, true, it)) }
        (op.body as? RequestBody.Json)?.let { add(InputDef(Input.BODY, it.type, true, null)) }
        op.parameters.filter { it.location != ParameterLocation.PATH }.forEach { add(InputDef(it.name, it.type, it.required, it)) }
    }

    private sealed interface Dimensions {
        /**
         * @property fixed The arguments every combination sends.
         * @property varying The values each varying input takes, null when left out.
         */
        class Found(val fixed: Map<String, Arg>, val varying: List<Pair<String, List<Arg?>>>) : Dimensions

        class Missing(val reason: String) : Dimensions
    }

    private fun dimensions(operation: OperationRef, tests: LiveTestsConfig.OperationTests, explicitOnly: Boolean): Dimensions {
        val inputs = inputs(operation)
        val unknown = tests.inputs.keys - inputs.map { it.name }.toSet()
        require(unknown.isEmpty()) { "operations.${operation.id}.inputs names unknown inputs $unknown; it takes ${inputs.map { it.name }}" }
        val fixed = linkedMapOf<String, Arg>()
        val varying = mutableListOf<Pair<String, List<Arg?>>>()
        val missing = mutableListOf<String>()
        for (input in inputs) {
            val where = if (input.name in tests.inputs) "operations.${operation.id}.inputs.${input.name}" else "defaults.${input.name}"
            val explicit = tests.inputs[input.name] ?: config.defaults[input.name]
            if (explicit != null) {
                val values = (explicit as? ValueSpec.OneOf)?.values ?: listOf(explicit)
                val all = values.map { value(it, input.type, where, null, stack = setOf(operation.id)) }
                // An optional input goes without the default values nothing provides, such as
                // those taken from the operation itself.
                val resolved = if (input.name !in tests.inputs && !input.required) all.filterIsInstance<Resolved.Value>() else all
                resolved.filterIsInstance<Resolved.Missing>().firstOrNull()?.let { return Dimensions.Missing("${input.name}: ${it.reason}") }
                val given = resolved.map { (it as Resolved.Value).arg }
                require(!input.required || given.none { it == null }) { "$where can't be left out: the operation needs it" }
                // A default is one of the values an optional input tries, as is leaving it out.
                val args = if (input.name !in tests.inputs && !input.required) (listOf(null) + given).distinct() else given
                if (args.size == 1) args.single()?.let { fixed[input.name] = it } else varying += input.name to args
                continue
            }
            if (explicitOnly) {
                if (input.required) missing += input.name
                continue
            }
            val values = generated(operation, input)
            when {
                values.isEmpty() -> if (input.required) missing += input.name
                values.size == 1 && values.single() != null -> fixed[input.name] = values.single()!!
                values.size > 1 -> varying += input.name to values
            }
        }
        if (missing.isNotEmpty()) {
            val how = if (explicitOnly) "only takes values from tests.yaml" else "nothing provides them"
            return Dimensions.Missing("needs inputs ${missing.joinToString()} ($how)")
        }
        return Dimensions.Found(fixed, varying)
    }

    /** The values the planner tries for [input] on its own; empty when it finds none. */
    private fun generated(operation: OperationRef, input: InputDef): List<Arg?> {
        val optional = if (input.required) emptyList() else listOf(null)
        val type = input.type
        TestTypes.enum(spec, type)?.let { enum -> return optional + enum.values.map { Arg.Literal(JsonPrimitive(it.wireValue)) } }
        if (type is TypeRef.Primitive) {
            when (type.kind) {
                PrimitiveKind.BOOLEAN -> return optional + listOf(Arg.Literal(JsonPrimitive(true)), Arg.Literal(JsonPrimitive(false)))
                PrimitiveKind.LOCAL_DATE, PrimitiveKind.LOCAL_DATE_TIME -> return optional + Arg.Text("{today}")
                else -> Unit
            }
        }
        // Optional filters only take ids from other results: other values, such as a status or a
        // page number, rarely mean the same thing in two operations.
        if (!input.required && !isIdName(input.name)) return optional
        val inferred = infer(operation, input, stack = setOf(operation.id))
        return when {
            inferred != null -> optional + inferred
            else -> optional
        }
    }

    /** Whether [name] names an id or a code: `id`, `careerId`, `courseCode`. */
    private fun isIdName(name: String): Boolean = Text.words(name).lastOrNull()?.lowercase() in ID_WORDS

    private sealed interface Resolved {
        /** @property arg The value, or null to leave the input out. */
        class Value(val arg: Arg?) : Resolved

        class Missing(val reason: String) : Resolved
    }

    /** A value of `tests.yaml` that refers to nothing but constants: an API parameter's. */
    private fun constant(value: ValueSpec, type: TypeRef, where: String): Arg? = when (value) {
        is ValueSpec.Pick, is ValueSpec.Ref, is ValueSpec.OneOf -> throw IllegalArgumentException("$where must be a constant")
        else -> (value(value, type, where, null, stack = emptySet()) as Resolved.Value).arg
    }

    /** [value] as an argument of type [type]; refs need [refs], the call being undone or verified. */
    private fun value(value: ValueSpec, type: TypeRef, where: String, refs: Refs?, stack: Set<String>): Resolved = when (value) {
        ValueSpec.Omit -> Resolved.Value(null)
        is ValueSpec.Scalar -> Resolved.Value(literal(value.value, type, where))
        is ValueSpec.Text -> Resolved.Value(
            if (Templates.parse(value.template).all { it is Templates.Part.Literal }) literal(JsonPrimitive(value.template), type, where)
            else Arg.Text(value.template).also { requireText(type, where) }
        )
        is ValueSpec.Json -> {
            val structured = TestTypes.record(spec, type) != null || type is TypeRef.ListOf || type is TypeRef.MapOf ||
                (type as? TypeRef.Primitive)?.kind in setOf(PrimitiveKind.JSON_OBJECT, PrimitiveKind.JSON_VALUE)
            require(structured) { "$where: JSON objects and lists only fit records, lists, maps and JSON, not ${describe(type)}" }
            val templates = strings(value.value).filter { text -> Templates.parse(text).any { it !is Templates.Part.Literal } }
            Resolved.Value(Arg.Json(value.value, templated = templates.isNotEmpty()))
        }
        is ValueSpec.OneOf -> throw IllegalArgumentException("$where: oneOf is only allowed as a whole input value")
        is ValueSpec.Ref -> {
            refs ?: throw IllegalArgumentException("$where: \$${value.root} only works in undo and verify")
            val ref = when (value.root) {
                "args" -> {
                    val input = value.path.substringBefore('.').substringBefore('[')
                    val inputType = refs.operation.inputType(input)
                        ?: throw IllegalArgumentException("$where: ${refs.operation.id} has no input '$input'")
                    Arg.Ref(Arg.Ref.Root.ARGS, input, path(value.path.removePrefix(input).removePrefix("."), inputType, emptyMap(), where))
                }
                "result" -> {
                    val result = refs.result ?: throw IllegalArgumentException("$where: ${refs.operation.id} returns nothing")
                    Arg.Ref(Arg.Ref.Root.RESULT, null, path(value.path, result, emptyMap(), where))
                }
                else -> {
                    val before = refs.before ?: throw IllegalArgumentException("$where: \$before only works with a restore")
                    Arg.Ref(Arg.Ref.Root.BEFORE, null, path(value.path, before, emptyMap(), where))
                }
            }
            requireAssignable(ref.path.type, type, where)
            Resolved.Value(ref)
        }
        is ValueSpec.Pick -> {
            val operation = operations[value.from] ?: throw IllegalArgumentException("$where takes values from unknown operation '${value.from}'")
            val effect = operation.operation.effect
            require(effect == Effect.READ || effect == Effect.BENIGN) { "$where takes values from ${operation.id}, which is $effect; only reads and benign operations can be" }
            val required = operation.operation.roles
            require(required.isEmpty() || required.any { it in roles }) { "$where takes values from ${operation.id}, which needs one of the roles $required" }
            val result = operation.resultType ?: throw IllegalArgumentException("$where takes values from ${operation.id}, which returns nothing")
            val source = source(operation, value.with, stack, where)
            if (source == null) {
                Resolved.Missing("${operation.id}, which it takes values from, can't be called")
            } else {
                val path = path(value.pick, result, value.where, "$where.pick")
                Resolved.Value(pick(source, path, type) ?: throw IllegalArgumentException("$where picks ${describe(path.type)}, which doesn't fit ${describe(type)}"))
            }
        }
    }

    /** Every string in [json], keys left out. */
    private fun strings(json: JsonElement): List<String> = when (json) {
        is JsonObject -> json.values.flatMap(::strings)
        is JsonArray -> json.flatMap(::strings)
        is JsonPrimitive -> if (json.isString) listOf(json.content) else emptyList()
    }

    /** A [Arg.Pick] of [path] into an input of type [type], unwrapping lists the input doesn't take; null when the types don't fit. */
    private fun pick(source: Source, path: Path, type: TypeRef): Arg.Pick? {
        var reached = path
        while (true) {
            if (assignable(reached.type, type)) return Arg.Pick(source, reached)
            if (type is TypeRef.ListOf && assignable(reached.type, type.element)) return Arg.Pick(source, reached, wrap = true)
            val next = reached.type
            if (next !is TypeRef.ListOf) return null
            reached = Path(reached.root, reached.steps + Path.Step.Each, next.element)
        }
    }

    private fun literal(value: JsonPrimitive, type: TypeRef, where: String): Arg.Literal {
        val enum = TestTypes.enum(spec, type)
        val valid = when {
            enum != null -> enum.values.any { it.wireValue == value.content } || enum.open
            type is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> true
                PrimitiveKind.INT32, PrimitiveKind.INT64 -> value.content.toLongOrNull() != null
                PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> value.doubleOrNull != null || value.content.toDoubleOrNull() != null
                PrimitiveKind.BOOLEAN -> value.booleanOrNull != null
                PrimitiveKind.LOCAL_DATE -> ISO_DATE.matches(value.content)
                PrimitiveKind.LOCAL_DATE_TIME -> ISO_DATE_TIME.matches(value.content) || ISO_DATE.matches(value.content)
                PrimitiveKind.LOCAL_TIME -> ISO_TIME.matches(value.content)
                PrimitiveKind.JSON_VALUE -> true
                else -> false
            }
            type is TypeRef.ListOf -> false
            else -> false
        }
        require(valid) { "$where: '${value.content}' isn't a value of ${describe(type)}" }
        return Arg.Literal(value)
    }

    private fun requireText(type: TypeRef, where: String) {
        val kind = (type as? TypeRef.Primitive)?.kind
        require(kind != null && (kind.isScalar || kind.isTemporal) || TestTypes.enum(spec, type) != null) {
            "$where: text with placeholders only fits scalars, dates and enums, not ${describe(type)}"
        }
    }

    // Sources and inference.

    /**
     * The source calling [operation] with [with] and its other required inputs found as for a
     * case, or null when some can't be; [stack] holds the operations already being resolved.
     */
    private fun source(operation: OperationRef, with: Map<String, ValueSpec>, stack: Set<String>, where: String): Source? {
        val key = operation.id + with.entries.sortedBy { it.key }.joinToString(",", "(", ")") { "${it.key}=${it.value}" }
        if (key in sources) return sources[key]
        if (operation.id in stack || stack.size > MAX_SOURCE_DEPTH) return null
        val inner = stack + operation.id
        val tests = config.operations[operation.id] ?: LiveTestsConfig.OperationTests()
        val inputs = inputs(operation)
        val unknown = with.keys - inputs.map { it.name }.toSet()
        require(unknown.isEmpty()) { "$where gives unknown inputs $unknown of ${operation.id}; it takes ${inputs.map { it.name }}" }
        val args = linkedMapOf<String, Arg>()
        for (input in inputs) {
            val given = with[input.name] ?: (tests.inputs[input.name] ?: config.defaults[input.name])?.let { (it as? ValueSpec.OneOf)?.values?.first() ?: it }
            val byDefault = input.name !in with && input.name !in tests.inputs
            val arg = when {
                given != null -> when (val resolved = value(given, input.type, "$where.with.${input.name}", null, inner)) {
                    is Resolved.Value -> resolved.arg
                    // As for cases, an optional input goes without a default nothing provides.
                    is Resolved.Missing -> if (byDefault && !input.required) null else return null.also { sources[key] = null }
                }
                input.required -> infer(operation, input, inner) ?: return null.also { sources[key] = null }
                else -> null
            }
            arg?.let { args[input.name] = it }
        }
        when (val auth = auth(operation)) {
            is Resolved.Value -> auth.arg?.let { args[Input.AUTH] = it }
            is Resolved.Missing -> return null.also { sources[key] = null }
        }
        val base = operation.module.name.decapitalized() + operation.operation.name.capitalized()
        var id = base
        var suffix = 2
        while (!sourceIds.add(id)) id = base + suffix++
        return Source(id, Call(operation, args)).also { sources[key] = it }
    }

    /** A field a read's result holds, which an input of the same name may take its values from. */
    private class Candidate(val operation: OperationRef, val path: Path, val names: Set<String>, val record: RecordType)

    /** The fields of the results of the reads the account may call, by lowercase name and wire name. */
    private val candidates: Map<String, List<Candidate>> by lazy {
        val index = mutableMapOf<String, MutableList<Candidate>>()
        for (operation in operations.values) {
            val tests = config.operations[operation.id]
            val required = operation.operation.roles
            if (operation.operation.effect != Effect.READ || tests?.skip != null) continue
            if (required.isNotEmpty() && required.none { it in roles }) continue
            val result = operation.resultType ?: continue
            walk(result, Path.identity(result), depth = 0, visited = emptySet()) { record, path, field ->
                val names = setOf(field.name.lowercase(), field.wireName.lowercase())
                val candidate = Candidate(operation, path, names, record)
                names.forEach { index.getOrPut(it) { mutableListOf() } += candidate }
                // `id` of a record named after an entity provides `<entity>Id`.
                if (field.name == "id") {
                    val entity = record.name.removePrefix(spec.prefix)
                    Text.words(entity).indices.forEach { start ->
                        val key = Text.words(entity).drop(start).joinToString("").lowercase() + "id"
                        index.getOrPut(key) { mutableListOf() } += candidate
                    }
                }
            }
        }
        index
    }

    /** Calls [visit] with every field [type] reaches, and the path to it, unwrapping lists of scalars. */
    private fun walk(
        type: TypeRef,
        path: Path,
        depth: Int,
        visited: Set<String>,
        visit: (RecordType, Path, it.attendance100.mybicocca.codegen.model.Field) -> Unit,
    ) {
        when (type) {
            is TypeRef.ListOf -> walk(type.element, Path(path.root, path.steps + Path.Step.Each, type.element), depth, visited, visit)
            is TypeRef.MapOf -> walk(type.value, Path(path.root, path.steps + Path.Step.Each, type.value), depth, visited, visit)
            is TypeRef.Named -> {
                val record = TestTypes.record(spec, type) ?: return
                if (record.name in visited || depth >= MAX_PATH_DEPTH) return
                for (field in record.fields) {
                    val fieldPath = Path(path.root, path.steps + Path.Step.Get(record, field), field.type)
                    if (isScalar(TestTypes.element(field.type))) {
                        visit(record, unwrapped(fieldPath), field)
                    } else {
                        walk(field.type, fieldPath, depth + 1, visited + record.name, visit)
                    }
                }
            }
            is TypeRef.Primitive -> Unit
        }
    }

    private fun unwrapped(path: Path): Path {
        var reached = path
        while (true) {
            val type = reached.type as? TypeRef.ListOf ?: return reached
            reached = Path(reached.root, reached.steps + Path.Step.Each, type.element)
        }
    }

    /** Where [input] of [operation] can take its values from: the best field of the same name a read provides. */
    private fun infer(operation: OperationRef, input: InputDef, stack: Set<String>): Arg.Pick? {
        val parameter = input.parameter ?: return null
        val element = TestTypes.element(input.type)
        if (element !is TypeRef.Primitive || element.kind !in ID_KINDS) return null
        if (parameter.name.lowercase() in PAGING) return null
        val keys = listOf(parameter.name.lowercase(), parameter.wireName.lowercase()).distinct()
        val found = keys.flatMap { candidates[it].orEmpty() }.distinct()
            .filter { it.operation.id != operation.id && it.operation.id !in stack && assignable(it.path.type, element) }
        val order = operations.keys.withIndex().associate { it.value to it.index }
        val ranked = found.sortedWith(
            compareBy<Candidate>(
                // A source needing no inputs beats one needing some, whose values may not exist.
                { if (inputs(it.operation).none { input -> input.required }) 0 else 1 },
                { if (parameter.name.lowercase() in it.names && parameter.wireName.lowercase() in it.names) 0 else if (keys.any { key -> key in it.names }) 1 else 2 },
                { inputs(it.operation).count { input -> input.required } },
                { if (it.operation.module == operation.module) 0 else 1 },
                { it.path.steps.size },
                { order.getValue(it.operation.id) },
            )
        )
        for (candidate in ranked.take(MAX_CANDIDATES)) {
            val source = source(candidate.operation, emptyMap(), stack, "the source of ${operation.id}.${input.name}") ?: continue
            return pick(source, candidate.path, input.type)
        }
        return null
    }

    // Paths and expectations.

    private fun expectation(spec: ExpectationSpec, operation: OperationRef): Expectation {
        val where = "operations.${operation.id}.expect"
        val result = operation.resultType ?: throw IllegalArgumentException("$where: ${operation.id} returns nothing to check")
        val path = path(spec.path, result, emptyMap(), "$where.path")
        return when (spec) {
            is ExpectationSpec.NonEmpty -> Expectation.NonEmpty(path)
            is ExpectationSpec.Contains -> Expectation.Contains(path, argument(spec.value, path.type, "$where.value", null))
            is ExpectationSpec.Equals -> Expectation.Equals(path, argument(spec.value, path.type, "$where.value", null))
            is ExpectationSpec.Matches -> Expectation.Matches(path, spec.regex)
            is ExpectationSpec.Count -> Expectation.Count(path, spec.min, spec.max)
        }
    }

    /** A value that may refer to another call, never left out. */
    private fun argument(value: ValueSpec, type: TypeRef, where: String, refs: Refs?): Arg =
        when (val resolved = value(value, type, where, refs, stack = emptySet())) {
            is Resolved.Value -> resolved.arg ?: throw IllegalArgumentException("$where can't be left out")
            is Resolved.Missing -> throw IllegalArgumentException("$where can't be worked out: ${resolved.reason}")
        }

    /**
     * The path [text] from a value of [root]: field names (or wire names) separated by dots, `[*]`
     * for every element or map value, `[n]` for one element; a field of a list's elements may be
     * named without `[*]`. [where] keeps the elements whose fields have the given values: those
     * holding the last field of the path, or the values reached when it ends with no field.
     */
    fun path(text: String, root: TypeRef, where: Map<String, JsonPrimitive>, context: String): Path {
        val steps = mutableListOf<Path.Step>()
        var type = root
        for (token in tokens(text, context)) {
            when {
                token == "[*]" -> {
                    type = when (type) {
                        is TypeRef.ListOf -> type.element
                        is TypeRef.MapOf -> type.value
                        else -> throw IllegalArgumentException("$context: '[*]' needs a list or map, not ${describe(type)}")
                    }
                    steps += Path.Step.Each
                }
                token.startsWith("[") -> {
                    val list = type as? TypeRef.ListOf ?: throw IllegalArgumentException("$context: '$token' needs a list, not ${describe(type)}")
                    steps += Path.Step.At(token.removeSurrounding("[", "]").toInt())
                    type = list.element
                }
                else -> {
                    while (type is TypeRef.ListOf || type is TypeRef.MapOf) {
                        steps += Path.Step.Each
                        type = (type as? TypeRef.ListOf)?.element ?: (type as TypeRef.MapOf).value
                    }
                    val record = TestTypes.record(spec, type) ?: throw IllegalArgumentException("$context: '$token' needs a record, not ${describe(type)}")
                    val field = record.fields.firstOrNull { it.name == token } ?: record.fields.firstOrNull { it.wireName == token }
                        ?: throw IllegalArgumentException("$context: ${record.name} has no field '$token'; it has ${record.fields.map { it.name }}")
                    steps += Path.Step.Get(record, field)
                    type = field.type
                }
            }
        }
        if (where.isEmpty()) return Path(root, steps, type)
        // The filter applies to the records holding the last field, or to the values reached.
        val last = steps.lastOrNull() as? Path.Step.Get
        val (position, record) = if (last != null) {
            steps.lastIndex to last.record
        } else {
            while (type is TypeRef.ListOf || type is TypeRef.MapOf) {
                steps += Path.Step.Each
                type = (type as? TypeRef.ListOf)?.element ?: (type as TypeRef.MapOf).value
            }
            steps.size to (TestTypes.record(spec, type) ?: throw IllegalArgumentException("$context: where needs records, not ${describe(type)}"))
        }
        val filters = where.map { (name, value) ->
            val field = record.fields.firstOrNull { it.name == name } ?: record.fields.firstOrNull { it.wireName == name }
                ?: throw IllegalArgumentException("$context: ${record.name} has no field '$name' to filter on")
            literal(value, field.type, "$context.where.$name")
            Path.Step.Where(record, field, value)
        }
        steps.addAll(position, filters)
        return Path(root, steps, type)
    }

    private fun tokens(text: String, context: String): List<String> {
        val tokens = mutableListOf<String>()
        var index = 0
        while (index < text.length) {
            when (text[index]) {
                '.' -> index++
                '[' -> {
                    val end = text.indexOf(']', index)
                    require(end > index) { "$context: unclosed '[' in '$text'" }
                    val inner = text.substring(index + 1, end)
                    require(inner == "*" || inner.toIntOrNull() != null) { "$context: '[$inner]' must be [*] or an index, in '$text'" }
                    tokens += "[$inner]"
                    index = end + 1
                }
                else -> {
                    val end = text.indexOfAny(charArrayOf('.', '['), index).let { if (it < 0) text.length else it }
                    tokens += text.substring(index, end)
                    index = end
                }
            }
        }
        return tokens
    }

    // Types.

    private fun isScalar(type: TypeRef): Boolean =
        (type as? TypeRef.Primitive)?.kind?.let { it.isScalar || it.isTemporal } == true || TestTypes.enum(spec, type) != null

    /** Whether a value of type [from] can be passed where [to] is expected, converting it if needed. */
    private fun assignable(from: TypeRef, to: TypeRef): Boolean {
        if (from == to) return true
        val fromKind = (from as? TypeRef.Primitive)?.kind
        val toKind = (to as? TypeRef.Primitive)?.kind
        return when {
            fromKind in INTEGER_KINDS && toKind in INTEGER_KINDS -> true
            fromKind in FLOAT_KINDS && toKind in FLOAT_KINDS -> true
            fromKind in INTEGER_KINDS && toKind == PrimitiveKind.STRING -> true
            fromKind == PrimitiveKind.STRING && toKind in INTEGER_KINDS -> true
            TestTypes.enum(spec, from) != null && toKind == PrimitiveKind.STRING -> true
            fromKind == PrimitiveKind.STRING && TestTypes.enum(spec, to) != null -> true
            from is TypeRef.ListOf && to is TypeRef.ListOf -> assignable(from.element, to.element)
            else -> false
        }
    }

    private fun requireAssignable(from: TypeRef, to: TypeRef, where: String) {
        require(assignable(from, to)) { "$where: ${describe(from)} doesn't fit ${describe(to)}" }
    }

    private fun describe(type: TypeRef): String = when (type) {
        is TypeRef.Primitive -> type.kind.name.lowercase()
        is TypeRef.Named -> type.name
        is TypeRef.ListOf -> "a list of ${describe(type.element)}"
        is TypeRef.MapOf -> "a map of ${describe(type.value)}"
    }

    private companion object {
        const val MAX_SOURCE_DEPTH = 3
        const val MAX_PATH_DEPTH = 3
        const val MAX_CANDIDATES = 6

        val INTEGER_KINDS = setOf(PrimitiveKind.INT32, PrimitiveKind.INT64)
        val FLOAT_KINDS = setOf(PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64)

        /** The last words of the names of ids and codes. */
        val ID_WORDS = setOf("id", "ids", "code", "codes")

        /** Parameters that page or sort results, whose values never come from other results. */
        val PAGING = setOf("start", "limit", "offset", "page", "perpage", "pagesize", "order", "sort", "orderby", "limitfrom", "limitnum")

        /** The kinds of ids, which inputs take from other results. */
        val ID_KINDS = setOf(PrimitiveKind.STRING, PrimitiveKind.INT32, PrimitiveKind.INT64)

        val ISO_DATE = Regex("""\d{4}-\d{2}-\d{2}""")
        val ISO_DATE_TIME = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?""")
        val ISO_TIME = Regex("""\d{2}:\d{2}(:\d{2})?""")
    }
}
