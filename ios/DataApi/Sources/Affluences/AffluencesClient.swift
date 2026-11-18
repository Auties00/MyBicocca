import DataApiRuntime
import Foundation

/// Sends the generated Affluences operations' requests and decodes responses and errors.
///
/// Every operation names the server it targets, so the configuration's base URL is only a
/// fallback.
public final class AffluencesClient: Sendable {
    /// The app API rejects clients it doesn't recognise, so requests carry a browser's agent.
    public static let userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36"

    public static let defaultConfiguration = RestConfiguration(
        baseURL: URL(string: AffluencesServer.appV4.rawValue)!,
        headers: ["User-Agent": userAgent, "Accept-Language": "it"]
    )

    public let configuration: RestConfiguration

    public init(configuration: RestConfiguration = AffluencesClient.defaultConfiguration) {
        self.configuration = configuration
    }

    public func json<T: Decodable>(_ request: RestRequest) async throws -> T {
        try await execute(request).decode(T.self, envelope: request.envelope)
    }

    public func jsonList<T: Decodable>(_ request: RestRequest) async throws -> [T] {
        try await execute(request).decode([T].self, envelope: request.envelope)
    }

    public func data(_ request: RestRequest) async throws -> Data {
        try await execute(request).body
    }

    public func send(_ request: RestRequest) async throws {
        _ = try await execute(request)
    }

    private func execute(_ request: RestRequest) async throws -> RestResponse {
        let response = try await configuration.send(request)
        guard response.isSuccess else { throw AffluencesError(response: response) }
        return response
    }
}

extension AffluencesApi {
    public init(configuration: RestConfiguration = AffluencesClient.defaultConfiguration) {
        self.init(client: AffluencesClient(configuration: configuration))
    }
}

/// A request Affluences rejected. Match on ``code`` (or ``status``), never on the localised
/// ``message``.
public struct AffluencesError: Error, CustomStringConvertible {
    /// Polling an email validation the user hasn't completed yet.
    public static let notValidatedYet = "does_not_exist"

    public let status: Int
    public let code: String?
    public let message: String

    /// Reads either error envelope: `{"error": {"code", "message"}}` from the app API, or
    /// `{"error": "…", "errorMessage" | "error_message": "…"}` from the reservation API.
    init(response: RestResponse) {
        status = response.status
        let envelope = (try? JSONDecoder().decode(JSONValue.self, from: response.body)).flatMap { value -> [String: JSONValue]? in
            if case .object(let object) = value { return object } else { return nil }
        }
        var code: String?
        var message: String?
        switch envelope?["error"] {
        case .object(let error):
            if case .string(let value) = error["code"] { code = value }
            if case .string(let value) = error["message"] { message = value }
        case .string(let value):
            code = value
            if case .string(let text) = envelope?["errorMessage"] ?? envelope?["error_message"] { message = text }
        default:
            break
        }
        self.code = code
        self.message = message ?? code ?? "Request failed with status \(response.status)"
    }

    public var description: String {
        "Affluences request failed with status \(status)" + (code.map { " (\($0))" } ?? "") + ": \(message)"
    }
}
