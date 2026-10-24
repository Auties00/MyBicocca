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
extension LocalTime: QueryValueConvertible { public var queryValue: String { description } }

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

/// Credentials that authenticate a request through headers or form fields.
public protocol RequestAuth: Sendable {
    var headers: [String: String] { get }
    var form: [String: String] { get }
}

extension RequestAuth {
    public var form: [String: String] { [:] }
}

/// An empty JSON object, `{}`, for servers that require a body but read nothing from it.
public struct EmptyObject: Encodable, Sendable {
    public init() {}

    public func encode(to encoder: Encoder) throws {
        _ = encoder.container(keyedBy: EmptyKey.self)
    }

    private enum EmptyKey: CodingKey {}
}

/// A file sent as a field of a multipart form, held in memory.
public struct FileUpload: Hashable, Sendable {
    /// The name the server stores the file under.
    public var fileName: String
    /// The file's media type, or nil to let the server infer it.
    public var contentType: String?
    public var bytes: Data

    public init(fileName: String, contentType: String? = nil, bytes: Data) {
        self.fileName = fileName
        self.contentType = contentType
        self.bytes = bytes
    }
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
    /// Fields sent as `application/x-www-form-urlencoded`, or with ``multipart``, if the request has a form body.
    public var form: [QueryItem]?
    /// Files sent as fields of a multipart form.
    public var files: [(String, FileUpload)]
    /// Whether the form is sent as `multipart/form-data`, as files need.
    public var multipart: Bool
    /// Headers, including those of the credentials.
    public var headers: [QueryItem]
    /// Cookies, with their values not yet percent-encoded.
    public var cookies: [QueryItem]
    /// JSON body, if the request has one.
    public var body: Data?
    /// The base URL to send the request to, for APIs with several; nil for the client's own.
    public var server: String?
    /// Keys of the objects the response value is nested in, outermost first.
    public var envelope: [String]

    /// - Parameters:
    ///   - query: Query parameters; those with no value are left out.
    ///   - form: Form fields; those with no value are left out.
    ///   - headers: Headers; those with no value are left out.
    ///   - cookies: Cookies; those with no value are left out.
    ///   - files: Files sent as fields of a multipart form; those with no value are left out.
    ///   - multipart: Whether the form is sent as `multipart/form-data`.
    ///   - body: A value sent as JSON.
    ///   - auth: Credentials whose headers and form fields are added.
    public init(
        method: HTTPMethod,
        path: String,
        query: [(String, (any QueryValueConvertible)?)] = [],
        form: [(String, (any QueryValueConvertible)?)]? = nil,
        headers: [(String, (any QueryValueConvertible)?)] = [],
        cookies: [(String, (any QueryValueConvertible)?)] = [],
        files: [(String, FileUpload?)] = [],
        multipart: Bool = false,
        body: (any Encodable)? = nil,
        auth: (any RequestAuth)? = nil,
        server: String? = nil,
        envelope: [String] = []
    ) throws {
        self.method = method
        self.path = path
        self.query = RestRequest.items(query)
        let authForm = (auth?.form ?? [:]).sorted { $0.key < $1.key }.map { QueryItem(name: $0.key, value: $0.value) }
        self.form = form.map(RestRequest.items).map { $0 + authForm } ?? (authForm.isEmpty ? nil : authForm)
        self.headers = RestRequest.items(headers) + (auth?.headers ?? [:]).sorted { $0.key < $1.key }.map { QueryItem(name: $0.key, value: $0.value) }
        self.cookies = RestRequest.items(cookies)
        self.files = files.compactMap { name, file in file.map { (name, $0) } }
        self.multipart = multipart
        self.body = try body.map { try JSONEncoder().encode($0) }
        self.server = server
        self.envelope = envelope
    }

    /// `value` as PHP-style bracketed fields named after `name` (`name[0][key]=value`), as PHP
    /// servers read lists and records. Booleans are written `1` and `0`; nulls are left out.
    public static func brackets(_ name: String, _ value: (some Encodable)?) throws -> [(String, (any QueryValueConvertible)?)] {
        guard let value else { return [] }
        var fields: [(String, (any QueryValueConvertible)?)] = []
        func append(_ name: String, _ json: JSONValue) {
            switch json {
            case .null: break
            case .bool(let bool): fields.append((name, bool ? "1" : "0"))
            case .number, .string: fields.append((name, String(lenient: json)))
            case .array(let items): items.enumerated().forEach { append("\(name)[\($0.offset)]", $0.element) }
            case .object(let object): object.sorted { $0.key < $1.key }.forEach { append("\(name)[\($0.key)]", $0.value) }
            }
        }
        append(name, try JSONDecoder().decode(JSONValue.self, from: JSONEncoder().encode(value)))
        return fields
    }

    /// One field per element of `values`, each named `name`, as PHP reads a name ending in `[]`.
    public static func repeated(_ name: String, _ values: [some QueryValueConvertible]?) -> [(String, (any QueryValueConvertible)?)] {
        (values ?? []).map { (name, $0) }
    }

    /// `value` as JSON text, for a parameter written as JSON; nil when there is no value.
    public static func json(_ value: (some Encodable)?) throws -> String? {
        try value.map { String(decoding: try JSONEncoder().encode($0), as: UTF8.self) }
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
    /// The URL that answered, after redirects; relative links of an HTML body resolve against it.
    public let url: URL?

    public init(status: Int, body: Data, url: URL? = nil) {
        self.status = status
        self.body = body
        self.url = url
    }

    public var isSuccess: Bool { (200..<400).contains(status) }

    /// The body as UTF-8 text, such as a playlist.
    public var text: String { String(decoding: body, as: UTF8.self) }

    /// Decodes the body as `T`, after unwrapping it from the objects named by `envelope`.
    public func decode<T: Decodable>(_ type: T.Type, envelope: [String] = []) throws -> T {
        let decoder = JSONDecoder()
        guard !envelope.isEmpty else {
            return try decoder.decode(T.self, from: body)
        }
        decoder.userInfo[EnvelopeKeys.userInfoKey] = envelope
        return try decoder.decode(Enveloped<T>.self, from: body).value
    }
}

/// Decodes a value nested in the objects named by the decoder's envelope keys.
private struct Enveloped<T: Decodable>: Decodable {
    let value: T

    init(from decoder: Decoder) throws {
        let keys = decoder.userInfo[EnvelopeKeys.userInfoKey] as? [String] ?? []
        var container = try decoder.container(keyedBy: EnvelopeKeys.self)
        for key in keys.dropLast() {
            container = try container.nestedContainer(keyedBy: EnvelopeKeys.self, forKey: EnvelopeKeys(key))
        }
        value = try container.decode(T.self, forKey: EnvelopeKeys(keys.last ?? ""))
    }
}

private struct EnvelopeKeys: CodingKey {
    static let userInfoKey = CodingUserInfoKey(rawValue: "DataApiRuntime.envelope")!

    let stringValue: String
    var intValue: Int? { nil }

    init(_ stringValue: String) { self.stringValue = stringValue }
    init?(stringValue: String) { self.stringValue = stringValue }
    init?(intValue: Int) { nil }
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
        return RestResponse(status: http.statusCode, body: data, url: http.url)
    }

    func urlRequest(for request: RestRequest) throws -> URLRequest {
        let root = request.server ?? baseURL.absoluteString
        let base = root.hasSuffix("/") ? String(root.dropLast()) : root
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
        for header in request.headers {
            urlRequest.setValue(header.value, forHTTPHeaderField: header.name)
        }
        if !request.cookies.isEmpty {
            setCookies(request.cookies, for: url, on: &urlRequest)
        }
        if request.multipart {
            let boundary = "DataApi-\(UUID().uuidString)"
            urlRequest.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
            urlRequest.httpBody = RestConfiguration.multipartBody(fields: request.form ?? [], files: request.files, boundary: boundary)
        } else if let form = request.form {
            urlRequest.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
            urlRequest.httpBody = Data(form.map { "\(RestConfiguration.formEncode($0.name))=\(RestConfiguration.formEncode($0.value))" }.joined(separator: "&").utf8)
        } else if let body = request.body {
            urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
            urlRequest.httpBody = body
        }
        return urlRequest
    }

    /// Adds `cookies` to the session's cookie storage, which then sends them with the stored
    /// ones; a session without storage sends them as a `Cookie` header.
    private func setCookies(_ cookies: [QueryItem], for url: URL, on urlRequest: inout URLRequest) {
        let encoded = cookies.map { QueryItem(name: $0.name, value: RestConfiguration.formEncode($0.value)) }
        guard let storage = session.configuration.httpCookieStorage, let host = url.host else {
            urlRequest.setValue(encoded.map { "\($0.name)=\($0.value)" }.joined(separator: "; "), forHTTPHeaderField: "Cookie")
            return
        }
        for cookie in encoded {
            let properties: [HTTPCookiePropertyKey: Any] = [.name: cookie.name, .value: cookie.value, .domain: host, .path: url.path]
            if let cookie = HTTPCookie(properties: properties) {
                storage.setCookie(cookie)
            }
        }
    }

    /// `fields` and `files` as the parts of a `multipart/form-data` body.
    static func multipartBody(fields: [QueryItem], files: [(String, FileUpload)], boundary: String) -> Data {
        var body = Data()
        func append(_ text: String) { body.append(Data(text.utf8)) }
        for field in fields {
            append("--\(boundary)\r\nContent-Disposition: form-data; name=\"\(field.name)\"\r\n\r\n\(field.value)\r\n")
        }
        for (name, file) in files {
            append("--\(boundary)\r\nContent-Disposition: form-data; name=\"\(name)\"; filename=\"\(file.fileName)\"\r\n")
            append("Content-Type: \(file.contentType ?? "application/octet-stream")\r\n\r\n")
            body.append(file.bytes)
            append("\r\n")
        }
        append("--\(boundary)--\r\n")
        return body
    }

    private static let formCharacters = CharacterSet.alphanumerics.union(CharacterSet(charactersIn: "-._~"))

    private static func formEncode(_ value: String) -> String {
        value.addingPercentEncoding(withAllowedCharacters: formCharacters) ?? value
    }
}
