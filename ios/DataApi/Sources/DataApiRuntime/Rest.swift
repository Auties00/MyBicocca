import Foundation

/// A value that can be sent as a query, form or path parameter.
public protocol QueryValueConvertible {
    var queryValue: String { get }
}

extension String: QueryValueConvertible { public var queryValue: String { self } }
extension Int: QueryValueConvertible { public var queryValue: String { String(self) } }
extension Int64: QueryValueConvertible { public var queryValue: String { String(self) } }
extension Double: QueryValueConvertible { public var queryValue: String { String(self) } }
extension Float: QueryValueConvertible { public var queryValue: String { String(self) } }
extension Bool: QueryValueConvertible { public var queryValue: String { self ? "true" : "false" } }
extension LocalDate: QueryValueConvertible { public var queryValue: String { description } }
extension LocalDateTime: QueryValueConvertible { public var queryValue: String { description } }

/// Lists are sent as one comma-separated value.
extension Array: QueryValueConvertible where Element: QueryValueConvertible {
    public var queryValue: String { map(\.queryValue).joined(separator: ",") }
}

public enum HTTPMethod: String, Sendable {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case patch = "PATCH"
    case delete = "DELETE"
}

/// A name/value pair of a query string or form body.
public struct QueryItem: Hashable, Sendable {
    public var name: String
    public var value: String

    public init(name: String, value: String) {
        self.name = name
        self.value = value
    }
}

/// An HTTP request, relative to an API's base URL.
public struct RestRequest: Sendable {
    public var method: HTTPMethod
    /// Path appended to the base URL, starting with `/`, with path parameters already encoded.
    public var path: String
    public var query: [QueryItem]
    /// Fields sent as `application/x-www-form-urlencoded`, if the request has a form body.
    public var form: [QueryItem]?
    /// JSON body, if the request has one.
    public var body: Data?

    /// - Parameters:
    ///   - query: Query parameters; those with no value are left out.
    ///   - form: Form fields; those with no value are left out.
    ///   - body: A value sent as JSON.
    public init(
        method: HTTPMethod,
        path: String,
        query: [(String, (any QueryValueConvertible)?)] = [],
        form: [(String, (any QueryValueConvertible)?)]? = nil,
        body: (any Encodable)? = nil
    ) throws {
        self.method = method
        self.path = path
        self.query = RestRequest.items(query)
        self.form = form.map(RestRequest.items)
        self.body = try body.map { try JSONEncoder().encode($0) }
    }

    /// Percent-encodes a value for use as one path segment.
    public static func pathSegment(_ value: some QueryValueConvertible) -> String {
        let raw = value.queryValue
        return raw.addingPercentEncoding(withAllowedCharacters: segmentCharacters) ?? raw
    }

    private static let segmentCharacters = CharacterSet.urlPathAllowed.subtracting(CharacterSet(charactersIn: "/"))

    private static func items(_ pairs: [(String, (any QueryValueConvertible)?)]) -> [QueryItem] {
        pairs.compactMap { name, value in value.map { QueryItem(name: name, value: $0.queryValue) } }
    }
}

/// A raw HTTP response.
public struct RestResponse: Sendable {
    public let status: Int
    public let body: Data

    public var isSuccess: Bool { (200..<400).contains(status) }
}

/// Where and how an API client sends its requests.
public struct RestConfiguration: Sendable {
    public var baseURL: URL
    public var session: URLSession
    /// Headers sent with every request, such as `Authorization`.
    public var headers: [String: String]

    public init(baseURL: URL, session: URLSession = .shared, headers: [String: String] = [:]) {
        self.baseURL = baseURL
        self.session = session
        self.headers = headers
    }

    /// Sends `request` and returns the response whatever its status.
    public func send(_ request: RestRequest) async throws -> RestResponse {
        let (data, response) = try await session.data(for: urlRequest(for: request))
        guard let http = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }
        return RestResponse(status: http.statusCode, body: data)
    }

    func urlRequest(for request: RestRequest) throws -> URLRequest {
        let base = baseURL.absoluteString.hasSuffix("/") ? String(baseURL.absoluteString.dropLast()) : baseURL.absoluteString
        guard var components = URLComponents(string: base + request.path) else {
            throw URLError(.badURL)
        }
        if !request.query.isEmpty {
            components.queryItems = request.query.map { URLQueryItem(name: $0.name, value: $0.value) }
        }
        guard let url = components.url else {
            throw URLError(.badURL)
        }

        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = request.method.rawValue
        urlRequest.setValue("application/json", forHTTPHeaderField: "Accept")
        for (name, value) in headers {
            urlRequest.setValue(value, forHTTPHeaderField: name)
        }
        if let form = request.form {
            urlRequest.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
            urlRequest.httpBody = Data(form.map { "\(RestConfiguration.formEncode($0.name))=\(RestConfiguration.formEncode($0.value))" }.joined(separator: "&").utf8)
        } else if let body = request.body {
            urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
            urlRequest.httpBody = body
        }
        return urlRequest
    }

    private static let formCharacters = CharacterSet.alphanumerics.union(CharacterSet(charactersIn: "-._~"))

    private static func formEncode(_ value: String) -> String {
        value.addingPercentEncoding(withAllowedCharacters: formCharacters) ?? value
    }
}
