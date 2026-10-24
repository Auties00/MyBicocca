import Foundation

/// A calendar date without a time zone, such as an exam day.
public struct LocalDate: Hashable, Comparable, Sendable, CustomStringConvertible {
    public var year: Int
    public var month: Int
    public var day: Int

    public init(year: Int, month: Int, day: Int) {
        self.year = year
        self.month = month
        self.day = day
    }

    public static func < (lhs: LocalDate, rhs: LocalDate) -> Bool {
        (lhs.year, lhs.month, lhs.day) < (rhs.year, rhs.month, rhs.day)
    }

    /// The ISO 8601 form, `yyyy-MM-dd`.
    public var description: String { format(pattern: .isoDate) }
}

/// A date and time without a time zone.
public struct LocalDateTime: Hashable, Comparable, Sendable, CustomStringConvertible {
    public var date: LocalDate
    public var hour: Int
    public var minute: Int
    public var second: Int

    public init(date: LocalDate, hour: Int, minute: Int, second: Int = 0) {
        self.date = date
        self.hour = hour
        self.minute = minute
        self.second = second
    }

    public static func < (lhs: LocalDateTime, rhs: LocalDateTime) -> Bool {
        (lhs.date, lhs.hour, lhs.minute, lhs.second) < (rhs.date, rhs.hour, rhs.minute, rhs.second)
    }

    /// The ISO 8601 form, `yyyy-MM-dd'T'HH:mm:ss`.
    public var description: String { format(pattern: .isoDateTime) }
}

/// A time of day without a time zone, such as an opening hour.
public struct LocalTime: Hashable, Comparable, Sendable, CustomStringConvertible {
    public var hour: Int
    public var minute: Int
    public var second: Int

    public init(hour: Int, minute: Int, second: Int = 0) {
        self.hour = hour
        self.minute = minute
        self.second = second
    }

    public static func < (lhs: LocalTime, rhs: LocalTime) -> Bool {
        (lhs.hour, lhs.minute, lhs.second) < (rhs.hour, rhs.minute, rhs.second)
    }

    /// The ISO 8601 form, `HH:mm:ss`.
    public var description: String { format(pattern: .isoTime) }
}

/// A date or time value that a ``TemporalPattern`` can parse and format.
public protocol TemporalValue: Sendable {
    init?(fields: TemporalFields)
    var fields: TemporalFields { get }
}

extension TemporalValue {
    public static func parse(_ text: String, pattern: TemporalPattern) -> Self? {
        pattern.parse(text).flatMap(Self.init(fields:))
    }

    public func format(pattern: TemporalPattern) -> String {
        pattern.format(fields)
    }
}

/// The components a ``TemporalPattern`` reads and writes.
public struct TemporalFields: Sendable {
    public var year = 1970
    public var month = 1
    public var day = 1
    public var hour = 0
    public var minute = 0
    public var second = 0
}

extension LocalDate: TemporalValue {
    public init?(fields: TemporalFields) {
        guard (1...12).contains(fields.month), (1...31).contains(fields.day) else { return nil }
        self.init(year: fields.year, month: fields.month, day: fields.day)
    }

    public var fields: TemporalFields {
        TemporalFields(year: year, month: month, day: day)
    }
}

extension LocalTime: TemporalValue {
    public init?(fields: TemporalFields) {
        guard (0...23).contains(fields.hour), (0...59).contains(fields.minute), (0...59).contains(fields.second) else { return nil }
        self.init(hour: fields.hour, minute: fields.minute, second: fields.second)
    }

    public var fields: TemporalFields {
        TemporalFields(hour: hour, minute: minute, second: second)
    }
}

extension LocalDateTime: TemporalValue {
    public init?(fields: TemporalFields) {
        guard let date = LocalDate(fields: fields),
              (0...23).contains(fields.hour), (0...59).contains(fields.minute), (0...59).contains(fields.second)
        else { return nil }
        self.init(date: date, hour: fields.hour, minute: fields.minute, second: fields.second)
    }

    public var fields: TemporalFields {
        TemporalFields(year: date.year, month: date.month, day: date.day, hour: hour, minute: minute, second: second)
    }
}

/// A date/time pattern with the Unicode letters `yyyy`, `MM`, `dd`, `HH`, `mm` and `ss`; any
/// other character, or text in single quotes, is taken literally (`dd/MM/yyyy HH:mm:ss`).
public struct TemporalPattern: Sendable, Hashable {
    enum Token: Sendable, Hashable {
        case number(field: Field, width: Int)
        case literal(String)
    }

    enum Field: Sendable, Hashable {
        case year, month, day, hour, minute, second
    }

    public static let isoDate = TemporalPattern("yyyy-MM-dd")
    public static let isoDateTime = TemporalPattern("yyyy-MM-dd'T'HH:mm:ss")
    public static let isoTime = TemporalPattern("HH:mm:ss")

    let tokens: [Token]

    public init(_ pattern: String) {
        var tokens: [Token] = []
        var characters = Array(pattern)[...]
        while let first = characters.first {
            if first == "'" {
                characters = characters.dropFirst()
                let literal = characters.prefix { $0 != "'" }
                tokens.append(.literal(String(literal)))
                characters = characters.dropFirst(literal.count + 1)
            } else if let field = TemporalPattern.field(for: first) {
                let run = characters.prefix { $0 == first }
                tokens.append(.number(field: field, width: run.count))
                characters = characters.dropFirst(run.count)
            } else {
                tokens.append(.literal(String(first)))
                characters = characters.dropFirst()
            }
        }
        self.tokens = tokens
    }

    private static func field(for letter: Character) -> Field? {
        switch letter {
        case "y": return .year
        case "M": return .month
        case "d": return .day
        case "H": return .hour
        case "m": return .minute
        case "s": return .second
        default: return nil
        }
    }

    func parse(_ text: String) -> TemporalFields? {
        var fields = TemporalFields()
        var remaining = Substring(text)
        for token in tokens {
            switch token {
            case .literal(let literal):
                guard remaining.hasPrefix(literal) else { return nil }
                remaining = remaining.dropFirst(literal.count)
            case .number(let field, let width):
                // A single letter accepts one or two digits; longer runs set the maximum width.
                let digits = remaining.prefix(max(width, 2)).prefix { $0.isASCII && $0.isNumber }
                guard !digits.isEmpty, let value = Int(digits) else { return nil }
                remaining = remaining.dropFirst(digits.count)
                switch field {
                case .year: fields.year = value
                case .month: fields.month = value
                case .day: fields.day = value
                case .hour: fields.hour = value
                case .minute: fields.minute = value
                case .second: fields.second = value
                }
            }
        }
        return remaining.isEmpty ? fields : nil
    }

    func format(_ fields: TemporalFields) -> String {
        tokens.map { token in
            switch token {
            case .literal(let literal):
                return literal
            case .number(let field, let width):
                let value: Int
                switch field {
                case .year: value = fields.year
                case .month: value = fields.month
                case .day: value = fields.day
                case .hour: value = fields.hour
                case .minute: value = fields.minute
                case .second: value = fields.second
                }
                let digits = String(value)
                return String(repeating: "0", count: max(0, width - digits.count)) + digits
            }
        }.joined()
    }
}

extension LocalDate: Codable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let text = try container.decode(String.self)
        guard let value = LocalDate.parse(text, pattern: .isoDate) else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "'\(text)' isn't an ISO date")
        }
        self = value
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encode(description)
    }
}

extension LocalTime: Codable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let text = try container.decode(String.self)
        guard let value = LocalTime.parse(text, pattern: .isoTime) else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "'\(text)' isn't an ISO time")
        }
        self = value
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encode(description)
    }
}

extension LocalDateTime: Codable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let text = try container.decode(String.self)
        guard let value = LocalDateTime.parse(text, pattern: .isoDateTime) else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "'\(text)' isn't an ISO date-time")
        }
        self = value
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encode(description)
    }
}
