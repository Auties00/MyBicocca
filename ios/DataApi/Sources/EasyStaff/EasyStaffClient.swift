import DataApiRuntime
import Foundation

/// Sends the generated Agenda Web and EasyBadge requests and reads their responses: JSON, the
/// JSON a script assigns to a variable (as `combo.php` declares the options of its forms), or
/// an HTML page read by one of ``EasyStaffScrapers``.
///
/// Responses are read whatever their content type, since Agenda Web answers JSON as `text/html`.
public final class EasyStaffClient: Sendable {
    public static let defaultConfiguration = RestConfiguration(baseURL: URL(string: EasyStaffServer.main.rawValue)!)

    public let configuration: RestConfiguration

    public init(configuration: RestConfiguration = EasyStaffClient.defaultConfiguration) {
        self.configuration = configuration
    }

    public func json<T: Decodable>(_ request: RestRequest) async throws -> T {
        try await execute(request).decode(T.self, envelope: request.envelope)
    }

    public func jsonList<T: Decodable>(_ request: RestRequest) async throws -> [T] {
        try await execute(request).decode([T].self, envelope: request.envelope)
    }

    public func script<T: Decodable>(_ request: RestRequest, variable: String) async throws -> T {
        try await execute(request).script(T.self, variable: variable)
    }

    public func html<T>(_ request: RestRequest, scrape: (ScrapeNode) -> T?) async throws -> T {
        try await execute(request).scrape(scrape)
    }

    private func execute(_ request: RestRequest) async throws -> RestResponse {
        let response = try await configuration.send(request)
        guard response.isSuccess else { throw EasyStaffError(status: response.status) }
        return response
    }
}

extension EasyStaffApi {
    public init(configuration: RestConfiguration = EasyStaffClient.defaultConfiguration) {
        self.init(client: EasyStaffClient(configuration: configuration))
    }
}

/// A request Agenda Web or EasyBadge answered with an error status.
public struct EasyStaffError: Error, CustomStringConvertible {
    public let status: Int

    public var description: String { "EasyStaff request failed with status \(status)" }
}
