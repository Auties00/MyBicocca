package it.attendance100.mybicocca.codegen.reader.openapi

import it.attendance100.mybicocca.codegen.model.ClientSetting
import it.attendance100.mybicocca.codegen.model.ClientSpec
import it.attendance100.mybicocca.codegen.model.ErrorCode
import it.attendance100.mybicocca.codegen.model.ErrorFormat
import it.attendance100.mybicocca.codegen.model.HttpMethod
import it.attendance100.mybicocca.codegen.model.ParameterLocation
import it.attendance100.mybicocca.codegen.model.RetryRequest
import it.attendance100.mybicocca.codegen.model.RetryRule

/**
 * Reads the root `x-mb-client` extension into a [ClientSpec]. Every key is optional:
 *
 * ```yaml
 * x-mb-client:
 *   baseUrl: https://example.org/api     # for operations naming no server
 *   timeoutSeconds: 30
 *   keepsCookies: true
 *   caches: true
 *   sendsDefaults: true                  # spell out fields left at their defaults
 *   sendsNulls: false                    # leave out fields without a value
 *   headers: { Accept-Language: it }
 *   query: { format: json }
 *   settings:
 *     - { name: language, in: query, wireName: lang, default: en, description: … }
 *   emptyStatuses: [404]                 # list operations answer an empty list so
 *   error:
 *     detect: [/exception]               # failures reported with HTTP 200
 *     code: [/error/code, /error]        # JSON pointers, tried in order
 *     message: [/error/message]
 *     codes:
 *       NotFound: { value: not_found, description: … }
 *   retries:
 *     - when: { status: 401, code: invalid_token }
 *       before: { method: GET, path: "/tokens/{portalId}", headers: { X-Portal: "{portalId}" } }
 *       delaySeconds: 12
 *       description: …
 * ```
 */
internal object ClientSyntax {
    fun read(raw: Any?): ClientSpec {
        val client = raw as? Map<*, *> ?: return if (raw == null) ClientSpec.DEFAULT else error("${Extensions.CLIENT} must be an object")
        unknownKeys(client, CLIENT_KEYS, Extensions.CLIENT)
        return ClientSpec(
            baseUrl = client["baseUrl"]?.toString(),
            timeoutSeconds = client["timeoutSeconds"]?.let { int(it, "timeoutSeconds") },
            keepsCookies = client["keepsCookies"] as? Boolean ?: false,
            caches = client["caches"] as? Boolean ?: false,
            sendsDefaults = client["sendsDefaults"] as? Boolean ?: false,
            sendsNulls = client["sendsNulls"] as? Boolean ?: true,
            headers = Extensions.stringMap(client["headers"], "${Extensions.CLIENT}.headers"),
            query = Extensions.stringMap(client["query"], "${Extensions.CLIENT}.query"),
            settings = list(client["settings"], "settings").map(::setting),
            emptyStatuses = list(client["emptyStatuses"], "emptyStatuses").map { int(it, "emptyStatuses") }.toSet(),
            error = errorFormat(client["error"]),
            retries = list(client["retries"], "retries").map(::retry),
        )
    }

    private fun setting(raw: Any?): ClientSetting {
        val map = raw as? Map<*, *> ?: error("${Extensions.CLIENT}.settings must hold objects")
        val name = map["name"]?.toString() ?: error("A client setting has no name")
        return ClientSetting(
            name = name,
            location = when (map["in"]?.toString()) {
                "query" -> ParameterLocation.QUERY
                "header" -> ParameterLocation.HEADER
                else -> error("Client setting '$name' must be `in: query` or `in: header`")
            },
            wireName = map["wireName"]?.toString() ?: error("Client setting '$name' has no wireName"),
            default = map["default"]?.toString() ?: error("Client setting '$name' has no default"),
            description = map["description"]?.toString(),
        )
    }

    private fun errorFormat(raw: Any?): ErrorFormat {
        val map = raw as? Map<*, *> ?: return if (raw == null) ErrorFormat() else error("${Extensions.CLIENT}.error must be an object")
        unknownKeys(map, setOf("detect", "code", "message", "codes"), "${Extensions.CLIENT}.error")
        val codes = map["codes"] as? Map<*, *> ?: emptyMap<Any, Any>()
        return ErrorFormat(
            detect = Extensions.stringList(map["detect"], "error.detect"),
            code = Extensions.stringList(map["code"], "error.code"),
            message = Extensions.stringList(map["message"], "error.message"),
            codes = codes.map { (name, definition) ->
                when (definition) {
                    is Map<*, *> -> ErrorCode(
                        name = name.toString(),
                        value = definition["value"]?.toString() ?: error("Error code '$name' has no value"),
                        description = definition["description"]?.toString(),
                    )
                    else -> ErrorCode(name.toString(), definition.toString())
                }
            },
        )
    }

    private fun retry(raw: Any?): RetryRule {
        val map = raw as? Map<*, *> ?: error("${Extensions.CLIENT}.retries must hold objects")
        unknownKeys(map, setOf("when", "before", "delaySeconds", "description"), "a retry rule")
        val match = map["when"] as? Map<*, *> ?: error("A retry rule needs `when`")
        val before = map["before"]?.let { before ->
            val request = before as? Map<*, *> ?: error("A retry rule's `before` must be an object")
            RetryRequest(
                method = HttpMethod.valueOf((request["method"] ?: "GET").toString().uppercase()),
                path = request["path"]?.toString() ?: error("A retry rule's `before` needs a path"),
                headers = Extensions.stringMap(request["headers"], "the headers of a retry rule's `before`"),
            )
        }
        return RetryRule(
            status = match["status"]?.let { int(it, "a retry rule's status") },
            code = match["code"]?.toString(),
            before = before,
            delaySeconds = map["delaySeconds"]?.let { int(it, "delaySeconds") },
            description = map["description"]?.toString(),
        )
    }

    private fun list(raw: Any?, what: String): List<Any?> = when (raw) {
        null -> emptyList()
        is List<*> -> raw
        else -> error("${Extensions.CLIENT}.$what must be a list")
    }

    private fun int(raw: Any?, what: String): Int = (raw as? Number)?.toInt() ?: raw.toString().toIntOrNull() ?: error("$what must be a number")

    private fun unknownKeys(map: Map<*, *>, known: Set<String>, where: String) {
        val unknown = map.keys.map { it.toString() }.filter { it !in known }
        require(unknown.isEmpty()) { "Unknown keys in $where: $unknown" }
    }

    private val CLIENT_KEYS = setOf(
        "baseUrl", "timeoutSeconds", "keepsCookies", "caches", "sendsDefaults", "sendsNulls",
        "headers", "query", "settings", "emptyStatuses", "error", "retries",
    )
}
