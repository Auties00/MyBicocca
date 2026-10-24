import Foundation

/// An arbitrary JSON value, for fields an API leaves untyped.
public enum JSONValue: Hashable, Sendable, Codable {
    case null
    case bool(Bool)
    case number(Double)
    case string(String)
    case array([JSONValue])
    case object([String: JSONValue])

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if container.decodeNil() {
            self = .null
        } else if let value = try? container.decode(Bool.self) {
            self = .bool(value)
        } else if let value = try? container.decode(Double.self) {
            self = .number(value)
        } else if let value = try? container.decode(String.self) {
            self = .string(value)
        } else if let value = try? container.decode([JSONValue].self) {
            self = .array(value)
        } else {
            self = .object(try container.decode([String: JSONValue].self))
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch self {
        case .null: try container.encodeNil()
        case .bool(let value): try container.encode(value)
        case .number(let value): try container.encode(value)
        case .string(let value): try container.encode(value)
        case .array(let value): try container.encode(value)
        case .object(let value): try container.encode(value)
        }
    }
}

/// An arbitrary JSON object.
public typealias JSONObject = [String: JSONValue]

/// A scalar that may also arrive as a JSON string (`"12"`, `"true"`), or a string that may
/// arrive as a number, as lenient servers send them.
public protocol LenientScalar {
    init?(lenient value: JSONValue)
}

extension String: LenientScalar {
    public init?(lenient value: JSONValue) {
        switch value {
        case .string(let string): self = string
        case .number(let number): self = number.rounded() == number ? String(Int64(number)) : String(number)
        case .bool(let bool): self = String(bool)
        default: return nil
        }
    }
}

extension Int: LenientScalar {
    public init?(lenient value: JSONValue) {
        guard let number = Double(lenient: value), let exact = Int(exactly: number) else { return nil }
        self = exact
    }
}

extension Int64: LenientScalar {
    public init?(lenient value: JSONValue) {
        guard let number = Double(lenient: value), let exact = Int64(exactly: number) else { return nil }
        self = exact
    }
}

extension Double: LenientScalar {
    public init?(lenient value: JSONValue) {
        switch value {
        case .number(let number): self = number
        case .string(let string): guard let number = Double(string) else { return nil }; self = number
        default: return nil
        }
    }
}

extension Float: LenientScalar {
    public init?(lenient value: JSONValue) {
        guard let number = Double(lenient: value) else { return nil }
        self = Float(number)
    }
}

extension Bool: LenientScalar {
    public init?(lenient value: JSONValue) {
        switch value {
        case .bool(let bool): self = bool
        case .string("true"), .number(1): self = true
        case .string("false"), .number(0): self = false
        default: return nil
        }
    }
}

extension KeyedDecodingContainer {
    /// The value for `key`, or nil when the key is missing, null or holds an unusable value.
    /// Scalars sent as strings (and the reverse) are converted; see ``LenientScalar``.
    public func decodeLenient<T: Decodable>(_ type: T.Type, forKey key: Key) -> T? {
        guard contains(key), (try? decodeNil(forKey: key)) == false else { return nil }
        if let value = try? decode(T.self, forKey: key) {
            return value
        }
        guard let scalar = T.self as? LenientScalar.Type, let raw = try? decode(JSONValue.self, forKey: key) else {
            return nil
        }
        return scalar.init(lenient: raw) as? T
    }

    /// Like ``decodeLenient(_:forKey:)``, but throws when there is no usable value.
    public func decodeRequired<T: Decodable>(_ type: T.Type, forKey key: Key) throws -> T {
        if let value = decodeLenient(type, forKey: key) {
            return value
        }
        // Decode strictly to throw the precise reason.
        return try decode(T.self, forKey: key)
    }

    /// A date or time written with `pattern`; nil when the key is missing or null, and for
    /// blank strings (or the literal `null`) when `blankAsNull`.
    public func decodeTemporal<T: TemporalValue>(_ type: T.Type, forKey key: Key, pattern: String, blankAsNull: Bool) throws -> T? {
        guard contains(key), try !decodeNil(forKey: key) else { return nil }
        let text = try decode(String.self, forKey: key)
        if blankAsNull, text.trimmingCharacters(in: .whitespaces).isEmpty || text == "null" {
            return nil
        }
        guard let value = T.parse(text, pattern: TemporalPattern(pattern)) else {
            throw DecodingError.dataCorruptedError(forKey: key, in: self, debugDescription: "'\(text)' doesn't match '\(pattern)'")
        }
        return value
    }

    /// Like ``decodeTemporal(_:forKey:pattern:blankAsNull:)``, but throws when there is no value.
    public func decodeTemporalRequired<T: TemporalValue>(_ type: T.Type, forKey key: Key, pattern: String, blankAsNull: Bool) throws -> T {
        guard let value = try decodeTemporal(type, forKey: key, pattern: pattern, blankAsNull: blankAsNull) else {
            throw DecodingError.valueNotFound(T.self, DecodingError.Context(codingPath: codingPath + [key], debugDescription: "No value for '\(key.stringValue)'"))
        }
        return value
    }
}

extension KeyedEncodingContainer {
    /// Writes a date or time with `pattern`; no value is written as an empty string.
    public mutating func encodeTemporal<T: TemporalValue>(_ value: T?, forKey key: Key, pattern: String) throws {
        try encode(value.map { $0.format(pattern: TemporalPattern(pattern)) } ?? "", forKey: key)
    }
}

/// An enumeration of string values. Conforming types get `Codable` from the wire value.
///
/// A generated enum is open when its `init(wireValue:)` never fails: unknown values then land
/// in an `unknown(String)` case instead of failing to decode.
public protocol WireEnum: Codable, Hashable, Sendable, QueryValueConvertible {
    init?(wireValue: String)
    var wireValue: String { get }

    /// Whether a value may also arrive wrapped as `{"value": "…"}`.
    static var acceptsWrappedValue: Bool { get }
}

extension WireEnum {
    public static var acceptsWrappedValue: Bool { false }

    public var queryValue: String { wireValue }

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let raw: String
        if Self.acceptsWrappedValue, let wrapped = try? container.decode(WrappedValue.self) {
            raw = wrapped.value
        } else {
            raw = try container.decode(String.self)
        }
        guard let value = Self(wireValue: raw) else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "Unknown \(Self.self) value '\(raw)'")
        }
        self = value
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encode(wireValue)
    }
}

private struct WrappedValue: Decodable {
    let value: String

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        value = container.decodeLenient(String.self, forKey: .value) ?? ""
    }

    private enum CodingKeys: String, CodingKey {
        case value
    }
}
