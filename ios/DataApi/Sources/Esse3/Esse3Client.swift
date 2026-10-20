import DataApiRuntime
import Foundation

/// Sends the generated Esse3 operations' requests and decodes responses and errors.
public final class Esse3Client: Sendable {
    public static let defaultBaseURL = URL(string: "https://s3w.si.unimib.it/e3rest/api")!

    public let configuration: RestConfiguration

    /// - Parameter configuration: Where to send requests; set an `Authorization` header to
    ///   call operations that need a logged-in user.
    public init(configuration: RestConfiguration = RestConfiguration(baseURL: Esse3Client.defaultBaseURL)) {
        self.configuration = configuration
    }

    public func json<T: Decodable>(_ request: RestRequest, roles: Set<Esse3PermissionLevel>) async throws -> T {
        let response = try await execute(request, roles: roles)
        return try response.decode(T.self, envelope: request.envelope)
    }

    /// Like ``json(_:roles:)`` for list responses; Esse3 answers HTTP 404 for an empty list.
    public func jsonList<T: Decodable>(_ request: RestRequest, roles: Set<Esse3PermissionLevel>) async throws -> [T] {
        let response = try await configuration.send(request)
        if response.status == 404 {
            return []
        }
        try ensureSuccess(response, roles: roles)
        return try response.decode([T].self, envelope: request.envelope)
    }

    public func data(_ request: RestRequest, roles: Set<Esse3PermissionLevel>) async throws -> Data {
        try await execute(request, roles: roles).body
    }

    public func send(_ request: RestRequest, roles: Set<Esse3PermissionLevel>) async throws {
        _ = try await execute(request, roles: roles)
    }

    private func execute(_ request: RestRequest, roles: Set<Esse3PermissionLevel>) async throws -> RestResponse {
        let response = try await configuration.send(request)
        try ensureSuccess(response, roles: roles)
        return response
    }

    private func ensureSuccess(_ response: RestResponse, roles: Set<Esse3PermissionLevel>) throws {
        guard !response.isSuccess else { return }
        let details = (try? JSONDecoder().decode(Esse3ErrorResponse.self, from: response.body))
            ?? Esse3ErrorResponse(statusCode: response.status, returnCode: -1, errorMessage: "Request failed with status \(response.status)")
        throw Esse3Error(status: response.status, expectedRoles: roles, response: details)
    }
}

extension Esse3Api {
    public init(configuration: RestConfiguration = RestConfiguration(baseURL: Esse3Client.defaultBaseURL)) {
        self.init(client: Esse3Client(configuration: configuration))
    }
}
