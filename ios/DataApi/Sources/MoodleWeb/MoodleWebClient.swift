import DataApiRuntime
import Foundation

/// Fetches the Moodle pages the generated modules read and reads them with ``MoodleWebScrapers``.
///
/// The catalog pages are read as an anonymous visitor, so give the catalog a configuration whose
/// session sends no cookies; the media pages need the user's `MoodleSession` cookie.
public final class MoodleWebClient: Sendable {
    public static let defaultConfiguration = RestConfiguration(baseURL: URL(string: MoodleWebServer.main.rawValue)!)

    public let configuration: RestConfiguration

    public init(configuration: RestConfiguration = MoodleWebClient.defaultConfiguration) {
        self.configuration = configuration
    }

    public func html<T>(_ request: RestRequest, scrape: (ScrapeNode) -> T?) async throws -> T {
        let response = try await configuration.send(request)
        guard response.isSuccess else { throw MoodleWebError(status: response.status) }
        return try response.scrape(scrape)
    }
}

extension MoodleWebApi {
    public init(configuration: RestConfiguration = MoodleWebClient.defaultConfiguration) {
        self.init(client: MoodleWebClient(configuration: configuration))
    }
}

/// A page Moodle answered with an error status.
public struct MoodleWebError: Error, CustomStringConvertible {
    public let status: Int

    public var description: String { "Moodle page request failed with status \(status)" }
}
