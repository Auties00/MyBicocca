import DataApiRuntime
import XCTest

final class TemporalPatternTests: XCTestCase {
    func testParsesAndFormatsEsse3Dates() {
        let pattern = TemporalPattern("dd/MM/yyyy")
        XCTAssertEqual(LocalDate.parse("05/03/2025", pattern: pattern), LocalDate(year: 2025, month: 3, day: 5))
        XCTAssertEqual(LocalDate(year: 2025, month: 3, day: 5).format(pattern: pattern), "05/03/2025")
        XCTAssertNil(LocalDate.parse("2025-03-05", pattern: pattern))
    }

    func testParsesDateTimesWithQuotedLiterals() {
        let value = LocalDateTime.parse("2025-03-05T14:07:09", pattern: .isoDateTime)
        XCTAssertEqual(value, LocalDateTime(date: LocalDate(year: 2025, month: 3, day: 5), hour: 14, minute: 7, second: 9))
    }
}

final class LenientDecodingTests: XCTestCase {
    private struct Sample: Decodable {
        var count: Int
        var name: String
        var day: LocalDate?

        private enum CodingKeys: String, CodingKey {
            case count, name, day
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            count = container.decodeLenient(Int.self, forKey: .count) ?? 0
            name = container.decodeLenient(String.self, forKey: .name) ?? ""
            day = try container.decodeTemporal(LocalDate.self, forKey: .day, pattern: "dd/MM/yyyy", blankAsNull: true)
        }
    }

    func testAcceptsNumbersAsStringsAndBlankDates() throws {
        let sample = try JSONDecoder().decode(Sample.self, from: Data(#"{"count": "12", "name": 7, "day": ""}"#.utf8))
        XCTAssertEqual(sample.count, 12)
        XCTAssertEqual(sample.name, "7")
        XCTAssertNil(sample.day)
    }

    func testFallsBackToDefaultsForMissingOrNullKeys() throws {
        let sample = try JSONDecoder().decode(Sample.self, from: Data(#"{"count": null}"#.utf8))
        XCTAssertEqual(sample.count, 0)
        XCTAssertEqual(sample.name, "")
    }
}

final class WireEnumTests: XCTestCase {
    private enum Status: WireEnum {
        case active
        case unknown(String)

        static let acceptsWrappedValue = true

        init(wireValue: String) {
            self = wireValue == "A" ? .active : .unknown(wireValue)
        }

        var wireValue: String {
            switch self {
            case .active: return "A"
            case .unknown(let value): return value
            }
        }
    }

    func testDecodesPlainWrappedAndUnknownValues() throws {
        let decoded = try JSONDecoder().decode([Status].self, from: Data(#"["A", {"value": "A"}, "Z"]"#.utf8))
        XCTAssertEqual(decoded, [.active, .active, .unknown("Z")])
    }
}
