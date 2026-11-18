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

    /// Decodes this value as `T`.
    public func decode<T: Decodable>(as type: T.Type) throws -> T {
        try JSONDecoder().decode(T.self, from: JSONEncoder().encode(self))
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
        case .string("true"), .string("1"), .number(1): self = true
        case .string("false"), .string("0"), .number(0): self = false
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

    /// A string where blank strings, and any of `alsoNull`, mean no value.
    public func decodeBlankAsNull(forKey key: Key, alsoNull: Set<String> = []) -> String? {
        guard let text = decodeLenient(String.self, forKey: key) else { return nil }
        return text.trimmingCharacters(in: .whitespaces).isEmpty || alsoNull.contains(text) ? nil : text
    }

    /// Like ``decodeLenient(_:forKey:)``, also reading the value from an array of single-key
    /// objects, merged into one object.
    public func decodeFolded<T: Decodable>(_ type: T.Type, forKey key: Key) -> T? {
        if let value = decodeLenient(T.self, forKey: key) {
            return value
        }
        guard case .array(let items)? = try? decode(JSONValue.self, forKey: key) else { return nil }
        var merged: [String: JSONValue] = [:]
        for case .object(let object) in items {
            merged.merge(object) { _, last in last }
        }
        return try? JSONValue.object(merged).decode(as: T.self)
    }

    /// Like ``decodeLenient(_:forKey:)``, also reading the value from a JSON document inside a
    /// string; a blank string means no value.
    public func decodeEmbedded<T: Decodable>(_ type: T.Type, forKey key: Key) -> T? {
        if let value = decodeLenient(T.self, forKey: key) {
            return value
        }
        guard let text = try? decode(String.self, forKey: key), !text.trimmingCharacters(in: .whitespaces).isEmpty else { return nil }
        return try? JSONDecoder().decode(T.self, from: Data(text.utf8))
    }

    /// `value`, or an error naming `key` when there is none.
    public func required<T>(_ value: T?, forKey key: Key) throws -> T {
        guard let value else {
            throw DecodingError.valueNotFound(T.self, DecodingError.Context(codingPath: codingPath + [key], debugDescription: "No usable value for '\(key.stringValue)'"))
        }
        return value
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

extension KeyedDecodingContainer {
    /// A date and time sent as seconds since the Unix epoch, read in the device's time zone;
    /// nil when the key is missing or holds no number.
    public func decodeEpochSeconds(forKey key: Key) -> LocalDateTime? {
        decodeLenient(Int64.self, forKey: key).map { LocalDateTime(epochSeconds: $0) }
    }

    /// A list of strings sent as one string joining them with `separator`; the elements are
    /// trimmed and blank ones are left out.
    public func decodeJoined(forKey key: Key, separator: String) -> [String]? {
        decodeLenient(String.self, forKey: key).map { text in
            text.components(separatedBy: separator).map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
        }
    }

    /// Like ``decodeLenient(_:forKey:)``, also reading the list from an object whose values are
    /// its elements.
    public func decodeObjectValues<T: Decodable>(_ type: T.Type, forKey key: Key) -> [T]? {
        if let value = decodeLenient([T].self, forKey: key) {
            return value
        }
        guard case .object(let object)? = try? decode(JSONValue.self, forKey: key) else { return nil }
        return try? JSONValue.array(Array(object.values)).decode(as: [T].self)
    }
}

extension KeyedEncodingContainer {
    /// Writes a date or time with `pattern`; no value is written as an empty string.
    public mutating func encodeTemporal<T: TemporalValue>(_ value: T?, forKey key: Key, pattern: String) throws {
        try encode(value.map { $0.format(pattern: TemporalPattern(pattern)) } ?? "", forKey: key)
    }

    /// Writes a date and time as seconds since the Unix epoch, in the device's time zone.
    public mutating func encodeEpochSeconds(_ value: LocalDateTime?, forKey key: Key) throws {
        try encodeIfPresent(value?.epochSeconds, forKey: key)
    }
}

extension LocalDateTime {
    /// The date and time `epochSeconds` seconds after the Unix epoch, in the device's time zone.
    public init(epochSeconds: Int64) {
        let parts = Calendar.current.dateComponents(in: .current, from: Date(timeIntervalSince1970: TimeInterval(epochSeconds)))
        self.init(
            date: LocalDate(year: parts.year ?? 1970, month: parts.month ?? 1, day: parts.day ?? 1),
            hour: parts.hour ?? 0,
            minute: parts.minute ?? 0,
            second: parts.second ?? 0
        )
    }

    /// Seconds since the Unix epoch, reading this date and time in the device's time zone.
    public var epochSeconds: Int64 {
        var parts = DateComponents(year: date.year, month: date.month, day: date.day, hour: hour, minute: minute, second: second)
        parts.timeZone = .current
        return Int64(Calendar.current.date(from: parts)?.timeIntervalSince1970 ?? 0)
    }
}

/// An enumeration of string or integer values. Conforming types get `Codable` from the wire
/// value, which an integer enum keeps as its decimal text.
///
/// A generated enum is open when its `init(wireValue:)` never fails: unknown values then land
/// in an `unknown(String)` case instead of failing to decode.
public protocol WireEnum: Codable, Hashable, Sendable, QueryValueConvertible, WireMapKey {
    init?(wireValue: String)
    var wireValue: String { get }

    /// Whether a value may also arrive wrapped as `{"value": "…"}`.
    static var acceptsWrappedValue: Bool { get }

    /// Whether values are written as JSON integers rather than strings.
    static var isInteger: Bool { get }
}

extension WireEnum {
    public static var acceptsWrappedValue: Bool { false }

    public static var isInteger: Bool { false }

    public var queryValue: String { wireValue }

    public init?(wireKey: String) {
        self.init(wireValue: wireKey)
    }

    public var wireKey: String { wireValue }

    /// Reads a string, or a number as its text.
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let raw: String
        if Self.acceptsWrappedValue, let wrapped = try? container.decode(WrappedValue.self) {
            raw = wrapped.value
        } else if let text = String(lenient: try container.decode(JSONValue.self)) {
            raw = text
        } else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "Expected a \(Self.self) value")
        }
        guard let value = Self(wireValue: raw) else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "Unknown \(Self.self) value '\(raw)'")
        }
        self = value
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        if Self.isInteger, let number = Int64(wireValue) {
            try container.encode(number)
        } else {
            try container.encode(wireValue)
        }
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
