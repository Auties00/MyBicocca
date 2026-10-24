package it.attendance100.mybicocca.codegen.core

import it.attendance100.mybicocca.codegen.model.ApiModule
import it.attendance100.mybicocca.codegen.model.ApiSpec
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.model.Effect
import it.attendance100.mybicocca.codegen.model.EnumType
import it.attendance100.mybicocca.codegen.model.Field
import it.attendance100.mybicocca.codegen.model.Operation
import it.attendance100.mybicocca.codegen.model.Parameter
import it.attendance100.mybicocca.codegen.model.RecordType
import it.attendance100.mybicocca.codegen.model.RequestBody
import it.attendance100.mybicocca.codegen.model.ResponseBody
import it.attendance100.mybicocca.codegen.model.TypeRef
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * The live tests of an API, worked out by the [TestPlanner] from its spec and `tests.yaml`, for
 * targets to write in their language: every case calls one operation of the generated SDK with
 * concrete arguments and checks what comes back. Targets only render it; every decision is made
 * here, so that all platforms run the same cases.
 *
 * @property account The account the tests call the servers with.
 * @property sources Calls whose results other calls take values from, each made at most once per
 *   run; a source's arguments only use the sources before it.
 * @property cases The cases, by module and then operation, in declaration order.
 * @property skips The operations no case calls, with the reason.
 * @property shapes What the responses of the API look like, for the runtimes to check real
 *   responses against (see [Shapes]).
 * @property replays The write cases whose undo a run can make again from the journal of a
 *   previous run that was interrupted before undoing a change: those whose undo only takes
 *   scalars of the case's arguments and result, saved in the journal as text.
 */
class TestPlan(
    val spec: ApiSpec,
    val account: PlannedAccount,
    val limits: LiveTestsConfig.Limits,
    val sources: List<Source>,
    val cases: List<TestCase>,
    val skips: List<Skip>,
    val shapes: String,
    val replays: List<TestCase>,
)

/** An operation of a module. */
data class OperationRef(val module: ApiModule, val operation: Operation) {
    /** `Module.operation`. */
    val id: String get() = "${module.name}.${operation.name}"

    /** The types of the operation's inputs, by [Input] name. */
    fun inputType(name: String): TypeRef? = when (name) {
        Input.BODY -> (operation.body as? RequestBody.Json)?.type
        else -> operation.parameters.firstOrNull { it.name == name }?.type
    }

    /** The type the operation's result is decoded into; null for those returning nothing, bytes or text. */
    val resultType: TypeRef?
        get() = when (val response = operation.response) {
            is ResponseBody.Json -> response.type
            is ResponseBody.ScriptVariable -> response.type
            is ResponseBody.Html -> response.type
            ResponseBody.None, ResponseBody.Binary, ResponseBody.Text -> null
        }
}

/** The names of an operation's inputs: its parameters' names, [BODY] and [AUTH]. */
object Input {
    const val BODY = "body"
    const val AUTH = "auth"
}

/**
 * The account of the [TestPlan].
 *
 * @property env The environment variables the tests need; without them every case is skipped.
 * @property headers Headers every request carries, as templates.
 * @property parameters The values of the API parameters, in their declared order.
 * @property settings The values of the client settings, by name.
 * @property credentials The credentials of the auth schemes the account can use, by scheme.
 */
class PlannedAccount(
    val env: List<String>,
    val headers: Map<String, String>,
    val parameters: List<Pair<Parameter, Arg>>,
    val settings: Map<String, String>,
    val credentials: Map<String, Credentials>,
)

/** Credentials of [scheme], each credential's value by name. */
class Credentials(val scheme: AuthScheme, val values: Map<String, Arg>)

/** A call of [operation] whose result other calls take values from. */
class Source(val id: String, val call: Call) {
    val operation: OperationRef get() = call.operation
}

/**
 * A call of [operation] with [args], by [Input] name: inputs it has no entry for are left out.
 *
 * @property anonymous Whether the call is sent without the account's headers.
 */
class Call(val operation: OperationRef, val args: Map<String, Arg>, val anonymous: Boolean = false)

/** The value of an input of a call. */
sealed interface Arg {
    /** A JSON scalar, of the input's type: an enum's wire value, a date as ISO 8601 text. */
    data class Literal(val value: JsonPrimitive) : Arg

    /** Text whose placeholders the runtime replaces (see [Templates]), converted to the input's type. */
    data class Text(val template: String) : Arg

    /** JSON decoded into the input's type, such as a record. */
    data class Json(val value: JsonElement) : Arg

    /**
     * A value of a [source]'s result: one of those [path] reaches. Which one depends on the case
     * and the run, so that runs try different ones.
     *
     * @property wrap Whether the input is a list, of which the value is the only element.
     */
    data class Pick(val source: Source, val path: Path, val wrap: Boolean = false) : Arg

    /**
     * A value of the call being undone or verified: of its arguments (the [input] named),
     * its result, or the value a restore saved; [path] walks it.
     */
    data class Ref(val root: Root, val input: String?, val path: Path) : Arg {
        enum class Root { ARGS, RESULT, BEFORE }
    }

    /** [base] with [field] of its record set to [value], as a restore needs. */
    data class WithField(val base: Arg, val record: RecordType, val field: Field, val value: Arg) : Arg

    /** A value no server knows, for the cases expecting an unknown id to be refused. */
    data object Unknown : Arg

    /** Credentials of [scheme] that no server accepts. */
    data class InvalidCredentials(val scheme: AuthScheme) : Arg

    /** The account's credentials of [credentials]'s scheme. */
    data class AccountCredentials(val credentials: Credentials) : Arg
}

/**
 * The values a path reaches from a value of [root]: [steps] walk fields, elements and map values,
 * and keep the elements a filter accepts. The values are of type [type]; there may be none or
 * several.
 */
class Path(val root: TypeRef, val steps: List<Step>, val type: TypeRef) {
    sealed interface Step {
        /** The value of [field] of a record. */
        data class Get(val record: RecordType, val field: Field) : Step

        /** Each element of a list, or value of a map. */
        data object Each : Step

        /** The element at [index] of a list. */
        data class At(val index: Int) : Step

        /** The values whose [field] is [value] (a scalar, or an enum's wire value). */
        data class Where(val record: RecordType, val field: Field, val value: JsonPrimitive) : Step
    }

    override fun toString(): String = steps.joinToString("") { step ->
        when (step) {
            is Step.Get -> ".${step.field.name}"
            Step.Each -> "[*]"
            is Step.At -> "[${step.index}]"
            is Step.Where -> "[${step.field.name}=${step.value.content}]"
        }
    }.removePrefix(".")

    companion object {
        /** The path reaching the value itself. */
        fun identity(type: TypeRef): Path = Path(type, emptyList(), type)
    }
}

/** What a case checks. */
enum class CaseKind {
    /** The operation called with only what it needs: the first case of every operation. */
    SMOKE,

    /** One of the other parameter combinations. */
    COMBINATION,

    /** An id no server knows, which must be refused with the API's error, or answered with nothing. */
    UNKNOWN_ID,

    /** Credentials no server accepts, which must be refused with the API's error. */
    INVALID_CREDENTIALS,

    /** No account headers, which must be refused with the API's error. */
    ANONYMOUS,

    /** A reversible change, undone right away. */
    WRITE,
}

/**
 * One call of the generated SDK and what to check of it.
 *
 * @property id `Module.operation#<n>` for combinations, else with the kind's suffix.
 * @property index The case's number among its operation's combinations, from 0, which picks the
 *   values a [Arg.Pick] tries.
 * @property effect The effect of the operation, which the runtime also checks before calling it.
 * @property compare Whether platforms must decode the same result.
 * @property errors Statuses and error codes the call may answer with.
 */
class TestCase(
    val id: String,
    val kind: CaseKind,
    val call: Call,
    val index: Int,
    val effect: Effect,
    val expectations: List<Expectation> = emptyList(),
    val errors: List<String> = emptyList(),
    val compare: Boolean = false,
    val write: WritePlan? = null,
) {
    val operation: OperationRef get() = call.operation

    /** A name for the case that is a valid identifier in every target: `getCareers1`, `getCareersUnknownId`. */
    val methodName: String
        get() {
            val suffix = id.substringAfter('#')
            val words = suffix.split('-').joinToString("") { it.capitalized() }
            return call.operation.operation.name + words
        }
}

/** Something a result must satisfy, about the values [path] reaches. */
sealed interface Expectation {
    val path: Path

    data class NonEmpty(override val path: Path) : Expectation

    data class Contains(override val path: Path, val value: Arg) : Expectation

    data class Equals(override val path: Path, val value: Arg) : Expectation

    data class Matches(override val path: Path, val regex: String) : Expectation

    data class Count(override val path: Path, val min: Int?, val max: Int?) : Expectation
}

/**
 * How a reversible case undoes its change.
 *
 * @property snapshot For a restore: the call whose result holds the value to restore, and the
 *   path to it; the runtime saves it before the change.
 * @property verify A call whose result shows the change: [Expectation.Contains] holds after the
 *   change, and no longer after an undo by call.
 * @property undo The call undoing the change; its arguments may refer to the case's.
 * @property restored For a restore: the value read again after the undo must equal the saved one.
 */
class WritePlan(
    val snapshot: Pair<Call, Path>?,
    val verify: Pair<Call, Expectation.Contains>?,
    val undo: Call,
)

/** An operation no case calls, and why. */
data class Skip(val operation: String, val reason: String)

/** Helpers the planner and targets share. */
object TestTypes {
    /** The element type of [type], unwrapping lists, or [type] itself. */
    fun element(type: TypeRef): TypeRef = if (type is TypeRef.ListOf) element(type.element) else type

    fun record(spec: ApiSpec, type: TypeRef): RecordType? = (type as? TypeRef.Named)?.let { spec.declarations[it.name] as? RecordType }

    fun enum(spec: ApiSpec, type: TypeRef): EnumType? = (type as? TypeRef.Named)?.let { spec.declarations[it.name] as? EnumType }
}

/** A readable summary of a [TestPlan]: every case with its arguments, then the skipped operations by reason. */
object TestPlanReport {
    fun text(plan: TestPlan): String = buildString {
        val operations = plan.cases.map { it.operation.id }.distinct()
        appendLine("${plan.spec.id}: ${plan.cases.size} cases of ${operations.size} operations, ${plan.skips.size} operations skipped, ${plan.sources.size} sources")
        for ((operation, cases) in plan.cases.groupBy { it.operation.id }) {
            appendLine("  $operation")
            for (case in cases) {
                val args = case.call.args.entries.joinToString { (name, arg) -> "$name=${describe(arg)}" }
                appendLine("    #${case.id.substringAfter('#')} ${case.kind.name.lowercase()}($args)${if (case.compare) "" else " [not compared]"}")
                case.write?.let { write ->
                    write.snapshot?.let { (call, path) -> appendLine("      before: ${call.operation.id} -> $path") }
                    appendLine("      undo: ${write.undo.operation.id}(${write.undo.args.entries.joinToString { (name, arg) -> "$name=${describe(arg)}" }})")
                    write.verify?.let { (call, contains) -> appendLine("      verify: ${call.operation.id} contains ${contains.path} = ${describe(contains.value)}") }
                }
            }
        }
        if (plan.skips.isNotEmpty()) appendLine("  skipped:")
        for ((reason, skips) in plan.skips.groupBy { it.reason }.entries.sortedByDescending { it.value.size }) {
            appendLine("    $reason (${skips.size}): ${skips.joinToString { it.operation }}")
        }
    }

    private fun describe(arg: Arg): String = when (arg) {
        is Arg.Literal -> arg.value.toString()
        is Arg.Text -> "\"${arg.template}\""
        is Arg.Json -> arg.value.toString()
        is Arg.Pick -> "${arg.source.operation.id}${arg.source.call.args.takeIf { it.isNotEmpty() }?.entries?.joinToString(",", "(", ")") { "${it.key}=${describe(it.value)}" } ?: ""}:${arg.path}${if (arg.wrap) " as a list" else ""}"
        is Arg.Ref -> "\$${arg.root.name.lowercase()}${arg.input?.let { ".$it" } ?: ""}${arg.path.toString().takeIf { it.isNotEmpty() }?.let { ".$it" } ?: ""}"
        is Arg.WithField -> "${describe(arg.base)} with ${arg.field.name}=${describe(arg.value)}"
        Arg.Unknown -> "<unknown id>"
        is Arg.InvalidCredentials -> "<invalid ${arg.scheme.name}>"
        is Arg.AccountCredentials -> "<account ${arg.credentials.scheme.name}>"
    }
}
