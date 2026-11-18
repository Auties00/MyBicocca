package it.attendance100.mybicocca.codegen.reader.openapi

import it.attendance100.mybicocca.codegen.model.AuthParameter
import it.attendance100.mybicocca.codegen.model.AuthRequirement
import it.attendance100.mybicocca.codegen.model.AuthScheme
import it.attendance100.mybicocca.codegen.model.Effect
import it.attendance100.mybicocca.codegen.model.HttpMethod

/**
 * The `x-mb-*` extensions the reader understands, by where they appear:
 *
 * | Where                 | Extension            | Meaning                                                           |
 * |-----------------------|----------------------|-------------------------------------------------------------------|
 * | root                  | `x-mb-id`            | The API's id (e.g. `affluences`).                                 |
 * | root                  | `x-mb-prefix`        | Prefix of its declarations (e.g. `Affluences`).                   |
 * | root                  | `x-mb-parameters`    | API parameters, as `$ref`s to component parameters. Operations reference the path ones as usual; the reader drops those references. |
 * | root                  | `x-mb-headers`       | Headers every operation sends, before its own.                   |
 * | root                  | `x-mb-client`        | How the client sends requests and reads failures (see [ClientSyntax]). |
 * | server                | `x-mb-name`          | PascalCase name of the server.                                    |
 * | components            | `x-mb-auth-schemes`  | Auth schemes: `{Name: {description, parameters: [{name, description}], headers: {Header: "Bearer {param}"}}}`. |
 * | operation             | `x-mb-auth`          | `{schemes: [Name, …], required: true}`.                          |
 * | operation             | `x-mb-effect`        | What the operation does: `read`, `benign`, `reversible` or `irreversible` (see `Effect`); GETs default to `read`. |
 * | operation             | `x-mb-headers`       | Fixed headers, `{name: value}`; `{parameter}` in a value names an API parameter. |
 * | operation             | `x-mb-query`         | Fixed query parameters, `{name: value}`.                         |
 * | operation             | `x-mb-form`          | Fixed form fields, `{name: value}`; an empty value sends the field empty. |
 * | operation             | `x-mb-script-variable` | The response is a script; its value is the JSON this variable is assigned (`var name = …;`, `window.name = …;`). |
 * | operation             | `x-mb-scrape`        | How a `text/html` response is read (see [ScrapeSyntax]); the document itself when absent. |
 * | operation             | `x-mb-envelope`      | Keys the response value is nested in, e.g. `[data]`.             |
 * | operation             | `x-mb-empty-body`    | `true` to send `{}` as the body.                                 |
 * | operation             | `x-mb-path`          | The path to send, when it clashes with another operation's: OpenAPI forbids paths that differ only in parameter names. |
 * | schema                | `x-mb-module`        | Module the record belongs to, when not the first that uses it.   |
 * | object schema         | `x-mb-scrape`        | `{missing: [texts]}`: the record is read out of HTML, with these texts meaning no value. |
 * | object schema         | `x-mb-delimited`     | Separator of a record written as one string (`09:00-09:30`).      |
 * | map schema            | `x-mb-key`           | Key type: `integer`, `int64`, `date` or a schema name (default: strings). |
 * | enum schema           | `x-enum-varnames`    | PascalCase names of the values, in order.                         |
 * | enum schema           | `x-mb-open`          | `false` to reject unknown values (enums are open by default).    |
 * | enum schema           | `x-mb-wrapped`       | `true` when values may arrive as `{"value": "…"}`.               |
 * | property, parameter   | `x-mb-name`          | camelCase name, when not derivable from the wire name.           |
 * | property              | `x-mb-scrape`        | How a scraped record's property is read (see [ScrapeSyntax]).     |
 * | array parameter, form property | `x-mb-repeated` | `true` to send one field per element (`days[]=1&days[]=2`). |
 * | path parameter        | `x-mb-raw`           | `true` to send the value as given, slashes included: a path the server handed out. |
 * | string property, parameter | `x-mb-pattern`  | Date/time pattern of a `date`, `date-time` or `time` value.      |
 * | string property       | `x-mb-blank-as-null` | `true` when blank strings mean no value.                          |
 * | string property       | `x-mb-also-null`     | Other strings meaning no value, e.g. `["0"]` (with `x-mb-blank-as-null`). |
 * | `allOf` property      | `x-mb-folded`        | `true` when the record may arrive as an array of single-key objects. |
 * | `allOf` property      | `x-mb-embedded`      | `true` when the record may arrive as JSON inside a string.        |
 * | `date-time` property  | `x-mb-epoch-seconds` | `true` when sent as seconds since the Unix epoch.                 |
 * | array property        | `x-mb-joined`        | Separator of a list of strings sent as one string (`"Ada, Bob"`). |
 * | array property        | `x-mb-object-values` | `true` when the list may arrive as an object whose values are its elements. |
 * | property              | `x-mb-html`          | The value is an HTML fragment sent as a string, read with this scrape (see [ScrapeSyntax]). |
 *
 * A `multipart/form-data` body is a form whose `format: binary` properties are files. A
 * non-JSON, non-HTML response with a string schema, or of a text media type without one, is text;
 * one with `format: binary`, or any other, is bytes.
 *
 * Keys next to a `$ref` are lost, so a property referencing a record with a codec, or a
 * parameter with a default, wraps the reference: `allOf: [$ref: …]`. Parameters `in: cookie` are cookies, and a
 * parameter with `content: application/json` is written as JSON.
 */
internal object Extensions {
    const val ID = "x-mb-id"
    const val PREFIX = "x-mb-prefix"
    const val PARAMETERS = "x-mb-parameters"
    const val CLIENT = "x-mb-client"
    const val NAME = "x-mb-name"
    const val AUTH_SCHEMES = "x-mb-auth-schemes"
    const val AUTH = "x-mb-auth"
    const val EFFECT = "x-mb-effect"
    const val HEADERS = "x-mb-headers"
    const val ENVELOPE = "x-mb-envelope"
    const val EMPTY_BODY = "x-mb-empty-body"
    const val PATH = "x-mb-path"
    const val MODULE = "x-mb-module"
    const val DELIMITED = "x-mb-delimited"
    const val KEY = "x-mb-key"
    const val ENUM_NAMES = "x-enum-varnames"
    const val OPEN = "x-mb-open"
    const val WRAPPED = "x-mb-wrapped"
    const val PATTERN = "x-mb-pattern"
    const val BLANK_AS_NULL = "x-mb-blank-as-null"
    const val ALSO_NULL = "x-mb-also-null"
    const val FOLDED = "x-mb-folded"
    const val EMBEDDED = "x-mb-embedded"
    const val QUERY = "x-mb-query"
    const val FORM = "x-mb-form"
    const val SCRIPT_VARIABLE = "x-mb-script-variable"
    const val SCRAPE = "x-mb-scrape"
    const val REPEATED = "x-mb-repeated"
    const val RAW = "x-mb-raw"
    const val EPOCH_SECONDS = "x-mb-epoch-seconds"
    const val JOINED = "x-mb-joined"
    const val OBJECT_VALUES = "x-mb-object-values"
    const val HTML = "x-mb-html"

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

    /** The operation's `x-mb-effect`; GETs that don't declare one are reads. */
    fun effect(raw: Any?, method: HttpMethod, what: String): Effect? = when (raw) {
        null -> Effect.READ.takeIf { method == HttpMethod.GET }
        else -> Effect.fromWireName(raw.toString())
            ?: error("$EFFECT of $what must be one of ${Effect.entries.map { it.wireName }}, not '$raw'")
    }

    fun stringMap(raw: Any?, what: String): Map<String, String> = when (raw) {
        null -> emptyMap()
        is Map<*, *> -> raw.entries.associate { (key, value) -> key.toString() to value.toString() }
        else -> error("Expected an object for $what")
    }

    /** The names a list of `$ref`s points to. */
    fun refNames(raw: Any?, what: String): List<String> = when (raw) {
        null -> emptyList()
        is List<*> -> raw.map { item -> ((item as? Map<*, *>)?.get("\$ref") ?: error("Expected a \$ref in $what")).toString().substringAfterLast('/') }
        else -> error("Expected a list for $what")
    }

    fun stringList(raw: Any?, what: String): List<String> = when (raw) {
        null -> emptyList()
        is List<*> -> raw.map { it.toString() }
        else -> error("Expected a list for $what")
    }
}
