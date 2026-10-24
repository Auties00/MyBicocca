package it.attendance100.mybicocca.codegen.reader.openapi

import it.attendance100.mybicocca.codegen.model.AuthParameter
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.AuthScheme

/**
 * The `x-mb-*` extensions the reader understands, by where they appear:
 *
 * | Where                 | Extension            | Meaning                                                           |
 * |-----------------------|----------------------|-------------------------------------------------------------------|
 * | root                  | `x-mb-id`            | The API's id (e.g. `affluences`).                                 |
 * | root                  | `x-mb-prefix`        | Prefix of its declarations (e.g. `Affluences`).                   |
 * | server                | `x-mb-name`          | PascalCase name of the server.                                    |
 * | components            | `x-mb-auth-schemes`  | Auth schemes: `{Name: {description, parameters: [{name, description}], headers: {Header: "Bearer {param}"}}}`. |
 * | operation             | `x-mb-auth`          | `{schemes: [Name, …], required: true}`.                          |
 * | operation             | `x-mb-headers`       | Fixed headers, `{name: value}`.                                  |
 * | operation             | `x-mb-envelope`      | Keys the response value is nested in, e.g. `[data]`.             |
 * | operation             | `x-mb-empty-body`    | `true` to send `{}` as the body.                                 |
 * | schema                | `x-mb-module`        | Module the record belongs to, when not the first that uses it.   |
 * | enum schema           | `x-enum-varnames`    | PascalCase names of the values, in order.                         |
 * | enum schema           | `x-mb-open`          | `false` to reject unknown values (enums are open by default).    |
 * | enum schema           | `x-mb-wrapped`       | `true` when values may arrive as `{"value": "…"}`.               |
 * | property, parameter   | `x-mb-name`          | camelCase name, when not derivable from the wire name.           |
 * | string property       | `x-mb-pattern`       | Date/time pattern of a `date`, `date-time` or `time` value.      |
 * | string property       | `x-mb-blank-as-null` | `true` when blank strings mean no value (with `x-mb-pattern`).   |
 */
internal object Extensions {
    const val ID = "x-mb-id"
    const val PREFIX = "x-mb-prefix"
    const val NAME = "x-mb-name"
    const val AUTH_SCHEMES = "x-mb-auth-schemes"
    const val AUTH = "x-mb-auth"
    const val HEADERS = "x-mb-headers"
    const val ENVELOPE = "x-mb-envelope"
    const val EMPTY_BODY = "x-mb-empty-body"
    const val MODULE = "x-mb-module"
    const val ENUM_NAMES = "x-enum-varnames"
    const val OPEN = "x-mb-open"
    const val WRAPPED = "x-mb-wrapped"
    const val PATTERN = "x-mb-pattern"
    const val BLANK_AS_NULL = "x-mb-blank-as-null"

    fun authSchemes(raw: Any?): List<AuthScheme> {
        val schemes = raw as? Map<*, *> ?: return emptyList()
        return schemes.map { (name, definition) ->
            val scheme = definition as? Map<*, *> ?: error("Auth scheme '$name' must be an object")
            AuthScheme(
                name = name.toString(),
                parameters = (scheme["parameters"] as? List<*>).orEmpty().map { parameter ->
                    val map = parameter as? Map<*, *> ?: error("Parameters of auth scheme '$name' must be objects")
                    AuthParameter(map["name"]?.toString() ?: error("A parameter of auth scheme '$name' has no name"), map["description"]?.toString())
                },
                headers = stringMap(scheme["headers"], "headers of auth scheme '$name'"),
                description = scheme["description"]?.toString(),
            )
        }
    }

    fun authRequirement(raw: Any?): AuthRequirement? {
        val map = raw as? Map<*, *> ?: return null
        val schemes = (map["schemes"] as? List<*>)?.map { it.toString() } ?: error("$AUTH needs a list of schemes")
        return AuthRequirement(schemes, required = map["required"] as? Boolean ?: true)
    }

    fun stringMap(raw: Any?, what: String): Map<String, String> = when (raw) {
        null -> emptyMap()
        is Map<*, *> -> raw.entries.associate { (key, value) -> key.toString() to value.toString() }
        else -> error("Expected an object for $what")
    }

    fun stringList(raw: Any?, what: String): List<String> = when (raw) {
        null -> emptyList()
        is List<*> -> raw.map { it.toString() }
        else -> error("Expected a list for $what")
    }
}
