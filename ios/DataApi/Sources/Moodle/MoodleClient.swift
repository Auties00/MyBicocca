import DataApiRuntime
import Foundation

/// Sends the generated Moodle web service calls and decodes their results and errors.
///
/// Moodle answers errors with HTTP 200 and an `{"exception" | "error", "errorcode", "message"}`
/// body, which becomes a ``MoodleError``. Calls also ask Moodle to filter text and to rewrite
/// file URLs so that they work with the token, as the Moodle mobile app does.
public final class MoodleClient: Sendable {
    public static let defaultConfiguration = RestConfiguration(baseURL: URL(string: "https://elearning.unimib.it")!)

    public let configuration: RestConfiguration

    /// The langpack Moodle formats strings in, such as `it`.
    public let language: String

    public init(configuration: RestConfiguration = MoodleClient.defaultConfiguration, language: String = "en") {
        self.configuration = configuration
        self.language = language
    }

    public func json<T: Decodable>(_ request: RestRequest) async throws -> T {
        try decode(T.self, try await execute(request))
    }

    public func jsonList<T: Decodable>(_ request: RestRequest) async throws -> [T] {
        try decode([T].self, try await execute(request))
    }

    public func send(_ request: RestRequest) async throws {
        _ = try decode(JSONValue.self, try await execute(request))
    }

    private func execute(_ request: RestRequest) async throws -> RestResponse {
        var request = request
        request.query += [
            QueryItem(name: "moodlewssettingfilter", value: "true"),
            QueryItem(name: "moodlewssettingfileurl", value: "true"),
            QueryItem(name: "moodlewssettinglang", value: language),
        ]
        let response = try await configuration.send(request)
        guard response.isSuccess else { throw MoodleError(code: nil, message: "Request failed with status \(response.status)") }
        return response
    }

    private func decode<T: Decodable>(_ type: T.Type, _ response: RestResponse) throws -> T {
        if let error = MoodleError(body: response.body) { throw error }
        return try response.decode(T.self)
    }
}

extension MoodleApi {
    public init(configuration: RestConfiguration = MoodleClient.defaultConfiguration, language: String = "en") {
        self.init(client: MoodleClient(configuration: configuration, language: language))
    }
}

/// A call Moodle rejected. Match on ``code``, such as `invalidtoken`.
public struct MoodleError: Error, CustomStringConvertible {
    public let code: String?
    public let message: String

    init(code: String?, message: String) {
        self.code = code
        self.message = message
    }

    /// The error a result reports, or nil when it is a result.
    init?(body: Data) {
        guard case .object(let object)? = try? JSONDecoder().decode(JSONValue.self, from: body) else { return nil }
        let failed = object["exception"] != nil || object["errorcode"] != nil || {
            switch object["error"] {
            case .bool(true)?, .string?: return true
            default: return false
            }
        }()
        guard failed else { return nil }
        if case .string(let code)? = object["errorcode"] { self.code = code } else { self.code = nil }
        switch (object["message"], object["error"]) {
        case (.string(let message)?, _), (_, .string(let message)?): self.message = message
        default: self.message = "unknown error"
        }
    }

    public var description: String {
        "Moodle call failed" + (code.map { " (\($0))" } ?? "") + ": \(message)"
    }
}
