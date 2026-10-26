import Foundation

/// A failed request, before an API's client turns it into its own error type.
public struct ApiFailure: Error, Sendable {
    public let status: Int
    /// The machine-readable error code the body carried, if any.
    public let code: String?
    /// The human-readable message the body carried, else the code, else a description of the status.
    public let message: String

    public init(status: Int, code: String?, message: String) {
        self.status = status
        self.code = code
        self.message = message
    }
}

/// Where a failed response's JSON body holds the error's code and message: JSON pointers
/// (`/error/code`), tried in order, skipping those holding no string or number.
public struct ErrorFormat: Sendable {
    /// Pointers that, holding a value other than `false` or `null`, mark a body received with a
    /// successful status as an error.
    public var detect: [String]
    public var code: [String]
    public var message: [String]

    public init(detect: [String] = [], code: [String] = [], message: [String] = []) {
        self.detect = detect
        self.code = code
        self.message = message
    }

    /// Whether `body`, received with a successful status, reports an error.
    public func detects(_ body: JSONValue?) -> Bool {
        detect.contains { pointer in
            switch ErrorFormat.value(at: pointer, in: body) {
            case nil, .null?, .bool(false)?: return false
            default: return true
            }
        }
    }

    public func code(_ body: JSONValue?) -> String? {
        code.lazy.compactMap { ErrorFormat.text(at: $0, in: body) }.first
    }

    public func message(_ body: JSONValue?) -> String? {
        message.lazy.compactMap { ErrorFormat.text(at: $0, in: body) }.first
    }

    /// The string or number at `pointer`, if there is one.
    private static func text(at pointer: String, in body: JSONValue?) -> String? {
        switch value(at: pointer, in: body) {
        case .string(let string)?: return string
        case .number(let number)?: return String(lenient: .number(number))
        default: return nil
        }
    }

    private static func value(at pointer: String, in body: JSONValue?) -> JSONValue? {
        var current = body
        for key in pointer.split(separator: "/", omittingEmptySubsequences: true) {
            guard case .object(let object)? = current else { return nil }
            current = object[key.replacingOccurrences(of: "~1", with: "/").replacingOccurrences(of: "~0", with: "~")]
        }
        return current
    }
}

/// A request sent to recover from a failure, to the server of the failed request. `{name}`
/// placeholders in ``path`` and ``headers`` are replaced by the client's API parameters.
public struct RetryRequest: Sendable {
    public var method: HTTPMethod
    public var path: String
    public var headers: [String: String]

    public init(method: HTTPMethod, path: String, headers: [String: String] = [:]) {
        self.method = method
        self.path = path
        self.headers = headers
    }
}

/// A failure to recover from by sending the request again, once.
public struct RetryRule: Sendable {
    /// The status of the failure, or nil for any error status.
    public var status: Int?
    /// The error code of the failure, or nil for any.
    public var code: String?
    /// A request to send first, such as one handing out a fresh token.
    public var before: RetryRequest?
    /// How long to wait before trying again.
    public var delaySeconds: UInt64?

    public init(status: Int? = nil, code: String? = nil, before: RetryRequest? = nil, delaySeconds: UInt64? = nil) {
        self.status = status
        self.code = code
        self.before = before
        self.delaySeconds = delaySeconds
    }
}

/// How an API's client reads failures and recovers from them, as its spec describes it.
public struct RestPolicy: Sendable {
    /// Statuses with which a list operation answers an empty list.
    public var emptyStatuses: Set<Int>
    public var errors: ErrorFormat
    /// Failures to recover from by sending the request again, tried in order.
    public var retries: [RetryRule]
    /// Whether the client keeps responses in a ``ResponseCache``.
    public var caches: Bool

    public init(emptyStatuses: Set<Int> = [], errors: ErrorFormat = ErrorFormat(), retries: [RetryRule] = [], caches: Bool = false) {
        self.emptyStatuses = emptyStatuses
        self.errors = errors
        self.retries = retries
        self.caches = caches
    }
}

/// The responses a client keeps when its API's spec says `caches`, the same on every platform:
/// - a successful GET read whole, not a download, is kept when its `Cache-Control` has
///   `max-age=N` and neither `no-store` nor `no-cache`, and answers the same URL for N seconds;
/// - any request other than a GET empties it once answered, whatever the answer, since it may
///   have changed what the kept responses show;
/// - it keeps at most `capacity` responses, dropping the oldest first.
///
/// The platform's own HTTP cache isn't used, since each platform's follows other rules.
public final class ResponseCache: @unchecked Sendable {
    private struct Entry {
        let response: RestResponse
        let expires: Date
    }

    private let capacity: Int
    private let lock = NSLock()
    private var entries: [String: Entry] = [:]
    /// The URLs of `entries`, oldest first.
    private var order: [String] = []

    public init(capacity: Int = 200) {
        self.capacity = capacity
    }

    /// The response kept for `url`, if it is still fresh.
    public func get(_ url: String) -> RestResponse? {
        lock.lock()
        defer { lock.unlock() }
        guard let entry = entries[url] else { return nil }
        if entry.expires > Date() { return entry.response }
        entries[url] = nil
        order.removeAll { $0 == url }
        return nil
    }

    /// Keeps `response` for `url`, if its headers allow it.
    public func put(_ url: String, _ response: RestResponse) {
        guard let seconds = Self.maxAge(response.cacheControl) else { return }
        lock.lock()
        defer { lock.unlock() }
        if entries[url] != nil { order.removeAll { $0 == url } }
        entries[url] = Entry(response: response, expires: Date().addingTimeInterval(TimeInterval(seconds)))
        order.append(url)
        while order.count > capacity { entries[order.removeFirst()] = nil }
    }

    public func clear() {
        lock.lock()
        defer { lock.unlock() }
        entries.removeAll()
        order.removeAll()
    }

    /// How many seconds `Cache-Control` lets a response be kept: its `max-age`, unless it forbids keeping it.
    public static func maxAge(_ cacheControl: String?) -> Int? {
        guard let cacheControl else { return nil }
        let directives = cacheControl.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces).lowercased() }
        if directives.contains("no-store") || directives.contains("no-cache") { return nil }
        guard let directive = directives.first(where: { $0.hasPrefix("max-age=") }),
              let seconds = Int(directive.dropFirst("max-age=".count)), seconds > 0 else { return nil }
        return seconds
    }
}

/// Sends requests as a ``RestPolicy`` says, and throws an ``ApiFailure`` for a response with an
/// error status, or with a JSON body the policy detects as an error.
public struct RestClient: Sendable {
    public let configuration: RestConfiguration
    public let policy: RestPolicy
    /// The API parameters, by name, for the placeholders of retry requests.
    public let parameters: [String: String]
    private let cache: ResponseCache?

    public init(configuration: RestConfiguration, policy: RestPolicy, parameters: [String: String] = [:]) {
        self.configuration = configuration
        self.policy = policy
        self.parameters = parameters
        cache = policy.caches ? ResponseCache() : nil
    }

    /// Sends `request`, recovering from the failures the policy's retry rules match.
    ///
    /// - Parameters:
    ///   - readsJSON: Whether the operation reads a JSON body, which is then inspected for an
    ///     error; other bodies are only when they are JSON.
    ///   - allowEmpty: Whether to return nil for one of the policy's empty statuses.
    ///   - stores: Whether a GET may be answered from the client's ``ResponseCache``, and leave
    ///     its response there: not for downloads.
    public func execute(_ request: RestRequest, readsJSON: Bool, allowEmpty: Bool = false, stores: Bool = true) async throws -> RestResponse? {
        var key: String?
        if let cache, stores, request.method == .get {
            key = try configuration.urlRequest(for: request).url?.absoluteString
            if let key, let kept = cache.get(key) { return kept }
        }
        var request = request
        // Downloads, text and pages take whatever the server sends, which a JSON Accept would refuse.
        if !readsJSON { request.headers.insert(QueryItem(name: "Accept", value: "*/*"), at: 0) }
        var response = try await configuration.send(request)
        for rule in policy.retries {
            guard !response.isSuccess else { break }
            if let status = rule.status, status != response.status { continue }
            if let code = rule.code, code != policy.errors.code(json(response)) { continue }
            if let before = rule.before {
                let headers: [(String, (any QueryValueConvertible)?)] = before.headers
                    .sorted { $0.key < $1.key }
                    .map { ($0.key, fill($0.value)) }
                _ = try await configuration.send(try RestRequest(method: before.method, path: fill(before.path), headers: headers, server: request.server))
            }
            if let delay = rule.delaySeconds {
                try await Task.sleep(nanoseconds: delay * 1_000_000_000)
            }
            response = try await configuration.send(request)
        }
        // What the request changed may show in the kept responses.
        if request.method != .get { cache?.clear() }
        if allowEmpty && policy.emptyStatuses.contains(response.status) { return nil }
        guard response.isSuccess else { throw failure(response) }
        if !policy.errors.detect.isEmpty && (readsJSON || response.isJSON), policy.errors.detects(json(response)) {
            throw failure(response)
        }
        if let key { cache?.put(key, response) }
        return response
    }

    private func failure(_ response: RestResponse) -> ApiFailure {
        let body = json(response)
        let code = policy.errors.code(body)
        return ApiFailure(status: response.status, code: code, message: policy.errors.message(body) ?? code ?? "Request failed with status \(response.status)")
    }

    private func json(_ response: RestResponse) -> JSONValue? {
        try? JSONDecoder().decode(JSONValue.self, from: response.body)
    }

    private func fill(_ template: String) -> String {
        var result = template
        for (name, value) in parameters {
            result = result.replacingOccurrences(of: "{\(name)}", with: value)
        }
        return result
    }
}

extension RestResponse {
    /// Whether the body looks like a JSON object, as some servers report errors.
    var isJSON: Bool {
        body.first.map { $0 == UInt8(ascii: "{") } ?? false
    }
}
