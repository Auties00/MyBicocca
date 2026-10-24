import Foundation

/// A value with one string form, so that it can key a ``WireMap``.
public protocol WireMapKey: Hashable, Sendable {
    init?(wireKey: String)
    var wireKey: String { get }
}

extension String: WireMapKey {
    public init?(wireKey: String) { self = wireKey }
    public var wireKey: String { self }
}

extension Int: WireMapKey {
    public init?(wireKey: String) { self.init(wireKey) }
    public var wireKey: String { String(self) }
}

extension Int64: WireMapKey {
    public init?(wireKey: String) { self.init(wireKey) }
    public var wireKey: String { String(self) }
}

extension LocalDate: WireMapKey {
    public init?(wireKey: String) {
        guard let value = LocalDate.parse(wireKey, pattern: .isoDate) else { return nil }
        self = value
    }

    public var wireKey: String { description }
}

/// A JSON object read as a map, whose keys are any ``WireMapKey``.
///
/// Also reads the forms PHP and Laravel write: `[]` for an empty map, and an array of
/// single-key objects, merged.
public struct WireMap<Key: WireMapKey, Value: Hashable & Sendable>: Hashable, Sendable {
    public var entries: [Key: Value]

    public init(_ entries: [Key: Value] = [:]) {
        self.entries = entries
    }

    public subscript(key: Key) -> Value? {
        get { entries[key] }
        set { entries[key] = newValue }
    }

    public var keys: Dictionary<Key, Value>.Keys { entries.keys }
    public var values: Dictionary<Key, Value>.Values { entries.values }
    public var count: Int { entries.count }
    public var isEmpty: Bool { entries.isEmpty }
}

extension WireMap: ExpressibleByDictionaryLiteral {
    public init(dictionaryLiteral elements: (Key, Value)...) {
        self.init(Dictionary(elements) { _, last in last })
    }
}

extension WireMap: Sequence {
    public func makeIterator() -> Dictionary<Key, Value>.Iterator {
        entries.makeIterator()
    }
}

extension WireMap: Decodable where Value: Decodable {
    public init(from decoder: Decoder) throws {
        var entries: [Key: Value] = [:]
        if let object = try? decoder.container(keyedBy: AnyCodingKey.self) {
            try Self.read(object, into: &entries)
        } else {
            var array = try decoder.unkeyedContainer()
            while !array.isAtEnd {
                try Self.read(array.nestedContainer(keyedBy: AnyCodingKey.self), into: &entries)
            }
        }
        self.entries = entries
    }

    private static func read(_ container: KeyedDecodingContainer<AnyCodingKey>, into entries: inout [Key: Value]) throws {
        for codingKey in container.allKeys {
            guard let key = Key(wireKey: codingKey.stringValue) else {
                throw DecodingError.dataCorruptedError(forKey: codingKey, in: container, debugDescription: "Invalid \(Key.self) key '\(codingKey.stringValue)'")
            }
            entries[key] = try container.decode(Value.self, forKey: codingKey)
        }
    }
}

extension WireMap: Encodable where Value: Encodable {
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: AnyCodingKey.self)
        for (key, value) in entries {
            try container.encode(value, forKey: AnyCodingKey(key.wireKey))
        }
    }
}

/// A coding key with any name.
struct AnyCodingKey: CodingKey {
    let stringValue: String
    var intValue: Int? { nil }

    init(_ stringValue: String) { self.stringValue = stringValue }
    init?(stringValue: String) { self.stringValue = stringValue }
    init?(intValue: Int) { nil }
}
