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
        checkUnique(spec.servers.map { it.name }) { "Server '$it' is declared more than once" }
        checkUnique(spec.authSchemes.map { it.name }) { "Auth scheme '$it' is declared more than once" }
        for (scheme in spec.authSchemes) {
            val parameters = scheme.parameters.map { it.name }.toSet()
            for ((header, template) in scheme.headers) {
                for (placeholder in PLACEHOLDER.findAll(template).map { it.groupValues[1] }) {
                    if (placeholder !in parameters) add("Auth scheme '${scheme.name}' header '$header' uses unknown credential '{$placeholder}'")
                }
            }
        }

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
            DefaultValue.Zero -> if ((field.type as? TypeRef.Primitive)?.kind?.isScalar != true) {
                add("$where defaults to zero but isn't a scalar primitive")
            }
            is DefaultValue.Literal -> checkLiteral(spec, field.type, field.default, where)
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
                parameter.default?.let { checkLiteral(spec, parameter.type, it, "$where parameter '${parameter.name}'") }
                if (parameter.location == ParameterLocation.PATH && !parameter.required) add("$where path parameter '${parameter.name}' isn't required")
            }
            operation.server?.let { server ->
                if (spec.servers.none { it.name == server }) add("$where targets unknown server '$server'")
            }
            operation.auth?.let { auth ->
                if (auth.schemes.isEmpty()) add("$where accepts no auth scheme")
                for (scheme in auth.schemes) {
                    if (spec.authSchemes.none { it.name == scheme }) add("$where accepts unknown auth scheme '$scheme'")
                }
            }
            when (val body = operation.body) {
                is RequestBody.Json -> {
                    checkRef(spec, body.type, "$where body")
                    if (operation.formParameters.isNotEmpty()) add("$where sends both a JSON body and form parameters")
                }
                RequestBody.EmptyObject -> if (operation.formParameters.isNotEmpty()) add("$where sends both a JSON body and form parameters")
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

    private fun MutableList<String>.checkLiteral(spec: ApiSpec, type: TypeRef, literal: DefaultValue.Literal, where: String) {
        val valid = when (type) {
            is TypeRef.Primitive -> when (type.kind) {
                PrimitiveKind.STRING -> true
                PrimitiveKind.INT32, PrimitiveKind.INT64 -> literal.value.toLongOrNull() != null
                PrimitiveKind.FLOAT32, PrimitiveKind.FLOAT64 -> literal.value.toDoubleOrNull() != null
                PrimitiveKind.BOOLEAN -> literal.value == "true" || literal.value == "false"
                else -> false
            }
            is TypeRef.Named -> (spec.declarations[type.name] as? EnumType)?.values?.any { it.wireValue == literal.value } == true
            is TypeRef.ListOf -> false
        }
        if (!valid) add("$where defaults to '${literal.value}', which isn't a value of its type")
    }

    private inline fun MutableList<String>.checkUnique(names: List<String>, message: (String) -> String) {
        names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach { add(message(it)) }
    }

    private val PLACEHOLDER = Regex("""\{([^}]+)}""")
}
