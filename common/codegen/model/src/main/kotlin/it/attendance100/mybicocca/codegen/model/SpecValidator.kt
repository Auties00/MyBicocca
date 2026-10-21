package it.attendance100.mybicocca.codegen.model

/**
 * Checks the invariants targets rely on, so that a reader bug fails generation with a clear
 * message instead of producing code that doesn't compile.
 */
object SpecValidator {

    /** Returns every violated invariant of [spec]; empty when the spec is valid. */
    fun validate(spec: ApiSpec): List<String> = buildList {
        checkDeclarations(spec)
        checkApiParameters(spec)
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
            for ((what, templates) in listOf("header" to scheme.headers, "form field" to scheme.form)) {
                for ((name, template) in templates) {
                    for (placeholder in PLACEHOLDER.findAll(template).map { it.groupValues[1] }) {
                        if (placeholder !in parameters) add("Auth scheme '${scheme.name}' $what '$name' uses unknown credential '{$placeholder}'")
                    }
                }
            }
        }

        for (enum in spec.enums) {
            if (enum.values.isEmpty()) add("Enum '${enum.name}' has no values")
            if (enum.integer) {
                for (value in enum.values.filter { it.wireValue.toLongOrNull() == null }) {
                    add("Integer enum '${enum.name}' has non-integer wire value '${value.wireValue}'")
                }
            }
            checkUnique(enum.values.map { it.name }) { "Enum '${enum.name}' declares value name '$it' more than once" }
            checkUnique(enum.values.map { it.wireValue }) { "Enum '${enum.name}' declares wire value '$it' more than once" }
        }

        for (record in spec.modules.flatMap { it.records }) {
            checkUnique(record.fields.map { it.name }) { "Record '${record.name}' declares field '$it' more than once" }
            checkUnique(record.fields.map { it.wireName }) { "Record '${record.name}' declares wire name '$it' more than once" }
            for (field in record.fields) {
                checkField(spec, record, field)
            }
            (record.encoding as? RecordEncoding.Delimited)?.let { checkDelimited(record, it) }
            checkScrapedRecord(spec, record)
        }
    }

    private fun MutableList<String>.checkScrapedRecord(spec: ApiSpec, record: RecordType) {
        if (record.scrape == null) {
            for (field in record.fields.filter { it.scrape != null }) {
                add("Field '${record.name}.${field.name}' has a scrape but its record isn't scraped")
            }
            return
        }
        if (record.encoding != RecordEncoding.Object) add("Scraped record '${record.name}' isn't an object record")
        for (field in record.fields) {
            val where = "Field '${record.name}.${field.name}'"
            val scrape = field.scrape
            if (scrape == null) {
                if (field.default == null) add("$where has neither a scrape nor a default")
            } else {
                checkScrape(spec, field.type, scrape, where)
            }
        }
    }

    /** Checks that [steps], read from an element, yield a value of [type]. */
    private fun MutableList<String>.checkScrape(spec: ApiSpec, type: TypeRef, steps: List<ScrapeStep>, where: String) {
        val expected = Scrapes.kindFor(type, spec)
        if (expected == null) {
            add("$where can't be scraped: only scraped records, scalars, enums and lists of them can")
            return
        }
        if (type is TypeRef.ListOf && !Scrapes.isRecordList(type.element, spec) && Scrapes.kindFor(type.element, spec) != ScrapeKind.TEXT) {
            add("$where is a scraped list whose elements are neither scraped records nor read from text")
        }
        val problems = mutableListOf<String>()
        when (val actual = Scrapes.output(steps, ScrapeKind.ELEMENT) { problems += it }) {
            null -> problems.forEach { add("$where: $it") }
            expected -> Unit
            else -> add("$where: the scrape yields $actual but the type needs $expected")
        }
        for (pattern in patterns(steps)) {
            runCatching { Regex(pattern) }.onFailure { add("$where has the invalid pattern '$pattern'") }
        }
    }

    private fun patterns(steps: List<ScrapeStep>): List<String> = steps.flatMap { step ->
        when (step) {
            is ScrapeStep.Regex -> listOf(step.pattern)
            is ScrapeStep.Where -> patterns(step.test)
            is ScrapeStep.FirstOf -> step.alternatives.flatMap(::patterns)
            else -> emptyList()
        }
    }

    private fun MutableList<String>.checkDelimited(record: RecordType, encoding: RecordEncoding.Delimited) {
        if (encoding.separator.isEmpty()) add("Delimited record '${record.name}' has an empty separator")
        if (record.fields.isEmpty()) add("Delimited record '${record.name}' has no fields")
        for (field in record.fields) {
            val where = "Field '${record.name}.${field.name}' of a delimited record"
            val kind = (field.type as? TypeRef.Primitive)?.kind
            if (kind == null || !(kind.isScalar || kind.isTemporal)) add("$where isn't a scalar or temporal primitive")
            if (field.nullable) add("$where is nullable")
            if (field.codec != null && field.codec !is Codec.Temporal) add("$where has a non-temporal codec")
        }
    }

    private fun MutableList<String>.checkApiParameters(spec: ApiSpec) {
        checkUnique(spec.parameters.map { it.name }) { "API parameter '$it' is declared more than once" }
        for (parameter in spec.parameters) {
            val where = "API parameter '${parameter.name}'"
            checkRef(spec, parameter.type, where)
            if (!parameter.required) add("$where isn't required")
            if (parameter.location == ParameterLocation.FORM) add("$where is a form parameter")
            if (parameter.encoding != ParameterEncoding.PLAIN) add("$where isn't plainly encoded")
            if (!isPlain(spec, parameter.type)) add("$where can't be written as plain text")
        }
    }

    private fun MutableList<String>.checkField(spec: ApiSpec, record: RecordType, field: Field) {
        val where = "Field '${record.name}.${field.name}'"
        checkRef(spec, field.type, where)
        when (field.default) {
            DefaultValue.Null -> if (!field.nullable) add("$where defaults to null but isn't nullable")
            DefaultValue.EmptyList -> if (field.type !is TypeRef.ListOf) add("$where defaults to an empty list but isn't a list")
            DefaultValue.EmptyMap -> if (field.type !is TypeRef.MapOf) add("$where defaults to an empty map but isn't a map")
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
            is Codec.BlankAsNull -> {
                if (field.type != TypeRef.STRING) add("$where decodes blanks to null but isn't a string")
                if (!field.nullable) add("$where decodes blanks to null but isn't nullable")
            }
            Codec.FoldedObject -> if (!isObjectRecord(spec, field.type)) add("$where may arrive folded but isn't an object record")
            Codec.EmbeddedJson -> {
                if (!isObjectRecord(spec, field.type)) add("$where may arrive embedded but isn't an object record")
                if (!field.nullable) add("$where may arrive as a blank string but isn't nullable")
            }
            Codec.EpochSeconds -> if ((field.type as? TypeRef.Primitive)?.kind != PrimitiveKind.LOCAL_DATE_TIME) {
                add("$where is sent as epoch seconds but isn't a date and time")
            }
            is Codec.Joined -> {
                if (field.type != TypeRef.ListOf(TypeRef.STRING)) add("$where is sent joined but isn't a list of strings")
                if (codec.separator.isEmpty()) add("$where is joined with an empty separator")
            }
            Codec.ObjectValues -> if (field.type !is TypeRef.ListOf) add("$where may arrive as an object's values but isn't a list")
            is Codec.EmbeddedHtml -> {
                if (field.type !is TypeRef.ListOf && !field.nullable) add("$where is read from embedded HTML but isn't nullable")
                checkScrape(spec, field.type, codec.scrape, where)
            }
            null -> Unit
        }
    }

    private fun isObjectRecord(spec: ApiSpec, type: TypeRef): Boolean =
        type is TypeRef.Named && (spec.declarations[type.name] as? RecordType)?.encoding == RecordEncoding.Object

    /** Whether values of [type] have a plain text form: scalars, dates, enums and lists of them. */
    private fun isPlain(spec: ApiSpec, type: TypeRef): Boolean = when (type) {
        is TypeRef.Primitive -> type.kind.isScalar || type.kind.isTemporal
        is TypeRef.Named -> spec.declarations[type.name] is EnumType
        is TypeRef.ListOf -> type.element !is TypeRef.ListOf && isPlain(spec, type.element)
        is TypeRef.MapOf -> false
    }

    private fun MutableList<String>.checkModule(spec: ApiSpec, module: ApiModule) {
        checkUnique(module.operations.map { it.name }) { "Module '${module.name}' declares operation '$it' more than once" }
        for (operation in module.operations) {
            val where = "Operation '${module.name}.${operation.name}'"
            checkUnique(operation.parameters.map { it.name }) { "$where declares parameter '$it' more than once" }
            for (name in operation.parameters.map { it.name }.intersect(spec.parameters.map { it.name }.toSet())) {
                add("$where parameter '$name' clashes with an API parameter")
            }
            for (parameter in operation.parameters) {
                val what = "$where parameter '${parameter.name}'"
                checkRef(spec, parameter.type, what)
                when (parameter.encoding) {
                    ParameterEncoding.PLAIN -> {
                        parameter.default?.let { checkLiteral(spec, parameter.type, it, what) }
                        if (!isPlain(spec, parameter.type)) add("$what can't be written as plain text; encode it as JSON")
                    }
                    ParameterEncoding.JSON -> if (parameter.location == ParameterLocation.PATH) add("$what is a JSON-encoded path parameter")
                    ParameterEncoding.BRACKETS -> {
                        if (parameter.location != ParameterLocation.QUERY && parameter.location != ParameterLocation.FORM) {
                            add("$what is bracket-encoded but isn't a query or form parameter")
                        }
                        if (parameter.default != null) add("$what is bracket-encoded and has a default")
                    }
                    ParameterEncoding.REPEATED -> {
                        if (parameter.location != ParameterLocation.QUERY && parameter.location != ParameterLocation.FORM) {
                            add("$what is repeated but isn't a query or form parameter")
                        }
                        val type = parameter.type
                        if (type !is TypeRef.ListOf || type.element is TypeRef.ListOf || !isPlain(spec, type.element)) {
                            add("$what is repeated but isn't a list of plain values")
                        }
                        if (parameter.default != null) add("$what is repeated and has a default")
                    }
                }
                if (parameter.location == ParameterLocation.PATH && !parameter.required) add("$what isn't required")
                if (parameter.pattern != null) {
                    if ((parameter.type as? TypeRef.Primitive)?.kind?.isTemporal != true) add("$what has a pattern but isn't a date or time")
                    if (parameter.encoding != ParameterEncoding.PLAIN) add("$what has a pattern but isn't plainly encoded")
                    if (parameter.location !in setOf(ParameterLocation.QUERY, ParameterLocation.FORM, ParameterLocation.HEADER)) {
                        add("$what has a pattern but isn't a query, form or header parameter")
                    }
                }
            }
            operation.server?.let { server ->
                if (spec.servers.none { it.name == server }) add("$where targets unknown server '$server'")
            }
            for ((header, template) in operation.headers) {
                for (placeholder in PLACEHOLDER.findAll(template).map { it.groupValues[1] }) {
                    if (spec.parameters.none { it.name == placeholder }) add("$where header '$header' uses unknown API parameter '{$placeholder}'")
                }
            }
            operation.auth?.let { auth ->
                if (auth.schemes.isEmpty()) add("$where accepts no auth scheme")
                for (scheme in auth.schemes) {
                    if (spec.authSchemes.none { it.name == scheme }) add("$where accepts unknown auth scheme '$scheme'")
                }
                val sendsForm = spec.authSchemes.any { it.name in auth.schemes && it.form.isNotEmpty() }
                if (sendsForm && operation.body != null) add("$where sends a body but accepts credentials sent as form fields")
            }
            val sendsForm = operation.formParameters.isNotEmpty() || operation.form.isNotEmpty()
            when (val body = operation.body) {
                is RequestBody.Json -> {
                    checkRef(spec, body.type, "$where body")
                    if (sendsForm) add("$where sends both a JSON body and form fields")
                }
                RequestBody.EmptyObject -> if (sendsForm) add("$where sends both a JSON body and form fields")
                null -> Unit
            }
            when (val response = operation.response) {
                is ResponseBody.Json -> checkRef(spec, response.type, "$where response")
                is ResponseBody.ScriptVariable -> {
                    checkRef(spec, response.type, "$where response")
                    if (!SCRIPT_IDENTIFIER.matches(response.variable)) add("$where reads '${response.variable}', which isn't a script variable or property path")
                }
                is ResponseBody.Html -> {
                    checkRef(spec, response.type, "$where response")
                    checkScrape(spec, response.type, response.scrape, "$where response")
                }
                ResponseBody.None, ResponseBody.Binary -> Unit
            }

            val placeholders = PLACEHOLDER.findAll(operation.path).map { it.groupValues[1] }.toSet()
            val pathParameters = operation.pathParameters.map { it.wireName }.toSet()
            val apiPathParameters = spec.parameters.filter { it.location == ParameterLocation.PATH }.map { it.wireName }.toSet()
            for (missing in placeholders - pathParameters - apiPathParameters) add("$where has no path parameter for '{$missing}'")
            for (unused in pathParameters - placeholders) add("$where declares path parameter '$unused' missing from its path")
        }
    }

    private fun MutableList<String>.checkRef(spec: ApiSpec, type: TypeRef, where: String) {
        when (type) {
            is TypeRef.Named -> if (type.name !in spec.declarations) add("$where references unknown type '${type.name}'")
            is TypeRef.ListOf -> checkRef(spec, type.element, where)
            is TypeRef.MapOf -> {
                checkRef(spec, type.key, where)
                if (!spec.isMapKey(type.key)) add("$where has map keys that have no string form")
                checkRef(spec, type.value, where)
            }
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
            is TypeRef.ListOf, is TypeRef.MapOf -> false
        }
        if (!valid) add("$where defaults to '${literal.value}', which isn't a value of its type")
    }

    private inline fun MutableList<String>.checkUnique(names: List<String>, message: (String) -> String) {
        names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach { add(message(it)) }
    }

    private val PLACEHOLDER = Regex("""\{([^}]+)}""")
    /** A variable, or a property path such as `window.data_to_show`. */
    private val SCRIPT_IDENTIFIER = Regex("""[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*)*""")
}
