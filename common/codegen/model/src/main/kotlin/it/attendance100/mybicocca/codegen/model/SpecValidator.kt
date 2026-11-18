package it.attendance100.mybicocca.codegen.model

/**
 * Checks the invariants targets rely on, so that a reader bug fails generation with a clear
 * message instead of producing code that doesn't compile.
 */
object SpecValidator {

    /** Returns every violated invariant of [spec]; empty when the spec is valid. */
    fun validate(spec: ApiSpec): List<String> = buildList {
        checkDeclarations(spec)
        for (module in spec.modules) {
            checkModule(spec, module)
        }
    }

    /** Throws when [spec] violates any invariant. */
    fun requireValid(spec: ApiSpec) {
        val problems = validate(spec)
        require(problems.isEmpty()) {
            "Invalid API spec '${spec.id}':\n" + problems.joinToString("\n") { "  - $it" }
        }
    }

    private fun MutableList<String>.checkDeclarations(spec: ApiSpec) {
        val all = spec.modules.flatMap { it.records } + spec.enums
        for (name in all.groupingBy { it.name }.eachCount().filterValues { it > 1 }.keys) {
            add("Type '$name' is declared more than once")
        }
        checkUnique(spec.modules.map { it.name }) { "Module '$it' is declared more than once" }

        for (enum in spec.enums) {
            if (enum.values.isEmpty()) add("Enum '${enum.name}' has no values")
            checkUnique(enum.values.map { it.name }) { "Enum '${enum.name}' declares value name '$it' more than once" }
            checkUnique(enum.values.map { it.wireValue }) { "Enum '${enum.name}' declares wire value '$it' more than once" }
        }

        for (record in spec.modules.flatMap { it.records }) {
            checkUnique(record.fields.map { it.name }) { "Record '${record.name}' declares field '$it' more than once" }
            checkUnique(record.fields.map { it.wireName }) { "Record '${record.name}' declares wire name '$it' more than once" }
            for (field in record.fields) {
                checkField(spec, record, field)
            }
        }
    }

    private fun MutableList<String>.checkField(spec: ApiSpec, record: RecordType, field: Field) {
        val where = "Field '${record.name}.${field.name}'"
        checkRef(spec, field.type, where)
        when (field.default) {
            DefaultValue.Null -> if (!field.nullable) add("$where defaults to null but isn't nullable")
            DefaultValue.EmptyList -> if (field.type !is TypeRef.ListOf) add("$where defaults to an empty list but isn't a list")
            DefaultValue.Zero -> {
                val kind = (field.type as? TypeRef.Primitive)?.kind
                if (kind == null || kind.isTemporal || kind == PrimitiveKind.JSON_OBJECT || kind == PrimitiveKind.BINARY) {
                    add("$where defaults to zero but isn't a scalar primitive")
                }
            }
            null -> Unit
        }
        when (val codec = field.codec) {
            is Codec.Temporal -> {
                val kind = (field.type as? TypeRef.Primitive)?.kind
                if (kind?.isTemporal != true) add("$where uses a temporal codec but isn't a date or time")
                if (codec.blankAsNull && !field.nullable) add("$where decodes blanks to null but isn't nullable")
            }
            null -> Unit
        }
    }

    private fun MutableList<String>.checkModule(spec: ApiSpec, module: ApiModule) {
        checkUnique(module.operations.map { it.name }) { "Module '${module.name}' declares operation '$it' more than once" }
        for (operation in module.operations) {
            val where = "Operation '${module.name}.${operation.name}'"
            checkUnique(operation.parameters.map { it.name }) { "$where declares parameter '$it' more than once" }
            for (parameter in operation.parameters) {
                checkRef(spec, parameter.type, "$where parameter '${parameter.name}'")
            }
            when (val body = operation.body) {
                is RequestBody.Json -> {
                    checkRef(spec, body.type, "$where body")
                    if (operation.formParameters.isNotEmpty()) add("$where sends both a JSON body and form parameters")
                }
                null -> Unit
            }
            when (val response = operation.response) {
                is ResponseBody.Json -> checkRef(spec, response.type, "$where response")
                ResponseBody.None, ResponseBody.Binary -> Unit
            }

            val placeholders = PLACEHOLDER.findAll(operation.path).map { it.groupValues[1] }.toSet()
            val pathParameters = operation.pathParameters.map { it.wireName }.toSet()
            for (missing in placeholders - pathParameters) add("$where has no path parameter for '{$missing}'")
            for (unused in pathParameters - placeholders) add("$where declares path parameter '$unused' missing from its path")
        }
    }

    private fun MutableList<String>.checkRef(spec: ApiSpec, type: TypeRef, where: String) {
        when (type) {
            is TypeRef.Named -> if (type.name !in spec.declarations) add("$where references unknown type '${type.name}'")
            is TypeRef.ListOf -> checkRef(spec, type.element, where)
            is TypeRef.Primitive -> Unit
        }
    }

    private inline fun MutableList<String>.checkUnique(names: List<String>, message: (String) -> String) {
        names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach { add(message(it)) }
    }

    private val PLACEHOLDER = Regex("""\{([^}]+)}""")
}
