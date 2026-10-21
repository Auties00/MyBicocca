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

    private enum Position: WireEnum {
        case user
        case unknown(String)

        static let isInteger = true

        init(wireValue: String) {
            self = wireValue == "1" ? .user : .unknown(wireValue)
        }

        var wireValue: String {
            switch self {
            case .user: return "1"
            case .unknown(let value): return value
            }
        }
    }

    func testReadsAndWritesIntegerValues() throws {
        let decoded = try JSONDecoder().decode([Position].self, from: Data(#"[1, "1", 100]"#.utf8))
        XCTAssertEqual(decoded, [.user, .user, .unknown("100")])
        XCTAssertEqual(String(decoding: try JSONEncoder().encode([Position.user]), as: UTF8.self), "[1]")
    }
}

final class WireMapTests: XCTestCase {
    func testReadsObjectsEmptyArraysAndArraysOfObjects() throws {
        let maps = try JSONDecoder().decode(
            [WireMap<LocalDate, Int>].self,
            from: Data(#"[{"2025-03-05": 1}, [], [{"2025-03-05": 1}, {"2025-03-06": 2}]]"#.utf8)
        )
        let day = LocalDate(year: 2025, month: 3, day: 5)
        XCTAssertEqual(maps[0][day], 1)
        XCTAssertTrue(maps[1].isEmpty)
        XCTAssertEqual(maps[2].count, 2)
    }

    func testWritesKeysInTheirStringForm() throws {
        let map: WireMap<LocalDate, Int> = [LocalDate(year: 2025, month: 3, day: 5): 1]
        XCTAssertEqual(String(decoding: try JSONEncoder().encode(map), as: UTF8.self), #"{"2025-03-05":1}"#)
    }
}

final class CodecTests: XCTestCase {
    private struct Settings: Codable, Hashable {
        var enabled: Bool
    }

    private struct Sample: Decodable {
        var note: String?
        var folded: Settings?
        var embedded: Settings?
        var flag: Bool?

        private enum CodingKeys: String, CodingKey {
            case note, folded, embedded, flag
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            note = container.decodeBlankAsNull(forKey: .note, alsoNull: ["0"])
            folded = container.decodeFolded(Settings.self, forKey: .folded)
            embedded = container.decodeEmbedded(Settings.self, forKey: .embedded)
            flag = container.decodeLenient(Bool.self, forKey: .flag)
        }
    }

    func testDecodesLaravelShapes() throws {
        let json = #"{"note": "0", "folded": [{"enabled": true}], "embedded": "{\"enabled\": true}", "flag": "1"}"#
        let sample = try JSONDecoder().decode(Sample.self, from: Data(json.utf8))
        XCTAssertNil(sample.note)
        XCTAssertEqual(sample.folded, Settings(enabled: true))
        XCTAssertEqual(sample.embedded, Settings(enabled: true))
        XCTAssertEqual(sample.flag, true)
    }

    func testTreatsBlankEmbeddedDocumentsAsNoValue() throws {
        let sample = try JSONDecoder().decode(Sample.self, from: Data(#"{"note": " ", "embedded": ""}"#.utf8))
        XCTAssertNil(sample.note)
        XCTAssertNil(sample.embedded)
    }
}

final class ScrapeTests: XCTestCase {
    private let page = """
    <html><body><h1 class="page">Rooms <span class="sr-only">(list)</span></h1>
    <div class="room">
      <h2>U6-22 con Podio<span>[U6-22]</span></h2>
      <div><span class="label">Capacity</span>120 seats</div>
      <div><span class="label">Notes</span>First line<br>Second<ul><li>Ramp</li></ul></div>
      <a class="site" href="/rooms/view.php?id=42">site</a>
      <a class="map" href="#map-1">map</a>
      <ul class="staff"><h4>Teachers</h4><li>Ada</li><li>Bob</li><h4>Tutors</h4><li>Cy</li></ul>
    </div>
    <div id="map-1"><iframe src="https://maps.example/embed"></iframe></div>
    </body></html>
    """

    private func room() throws -> ScrapeNode {
        let document = try Scrape.document(page, url: URL(string: "https://example.com/portal/index.php"))
        return try XCTUnwrap(Scrape.select(document, ".room"))
    }

    private func label(_ room: ScrapeNode, _ text: String) -> ScrapeNode? {
        Scrape.selectAll(room, ".label").filter { Scrape.equals(Scrape.text($0, exclude: nil, missing: []), text) == true }.first
    }

    func testReadsTextOwnTextAndUnwraps() throws {
        let room = try room()
        XCTAssertEqual(Scrape.ownText(Scrape.select(room, "h2"), missing: []), "U6-22 con Podio")
        XCTAssertEqual(Scrape.unwrap(Scrape.text(Scrape.select(room, "h2 span"), exclude: nil, missing: []), "[", "]"), "U6-22")
        XCTAssertEqual(Scrape.text(Scrape.select(Scrape.root(room), "h1.page"), exclude: ".sr-only", missing: []), "Rooms")
        XCTAssertNil(Scrape.text(Scrape.select(room, "h2"), exclude: nil, missing: ["U6-22 con Podio[U6-22]"]))
    }

    func testReadsLabelledValuesAndLines() throws {
        let room = try room()
        XCTAssertEqual(Scrape.int(Scrape.removeSuffix(Scrape.text(Scrape.lastSibling(label(room, "Capacity")), exclude: nil, missing: []), " seats")), 120)
        XCTAssertEqual(Scrape.lines(label(room, "Notes"), missing: []), "First line\nSecond\n• Ramp")
    }

    func testResolvesUrlsQueryParametersAndLinks() throws {
        let room = try room()
        let url = Scrape.url(Scrape.select(room, "a.site"), "href")
        XCTAssertEqual(url, "https://example.com/rooms/view.php?id=42")
        XCTAssertEqual(Scrape.int(Scrape.queryParameter(url, "id")), 42)
        XCTAssertEqual(Scrape.attribute(Scrape.select(Scrape.follow(Scrape.select(room, ".map"), "href"), "iframe"), "src", missing: []), "https://maps.example/embed")
    }

    func testGroupsSectionsAndMatchesPatterns() throws {
        let sections = Scrape.sections(Scrape.select(try room(), ".staff"), header: "h4")
        XCTAssertEqual(sections.map { Scrape.text(Scrape.select($0, "h4"), exclude: nil, missing: []) }, ["Teachers", "Tutors"])
        XCTAssertEqual(Scrape.selectAll(sections.first, "li").compactMap { Scrape.text($0, exclude: nil, missing: []) }, ["Ada", "Bob"])
        XCTAssertEqual(Scrape.regex("https://kaf/entryid/1_abc/x", "entryid/([\w-]+)"), "1_abc")
        XCTAssertEqual(Scrape.replace("Terra", ["terra": "0"]), "0")
    }

    func testReadsScriptVariables() throws {
        let script = "var elenco_sedi = [{\"id\": 1}];\nvar elenco_aule = {};"
        let json = try XCTUnwrap(Scrape.scriptVariable(script, "elenco_aule"))
        XCTAssertEqual(String(decoding: json, as: UTF8.self), "{}")
        XCTAssertNil(Scrape.scriptVariable(script, "elenco_tipi"))
    }
}

final class PhpCodecTests: XCTestCase {
    private struct Lesson: Decodable {
        var start: LocalDateTime?
        var teachers: [String]
        var owners: [String]

        private enum CodingKeys: String, CodingKey {
            case start, teachers, owners
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            start = container.decodeEpochSeconds(forKey: .start)
            teachers = container.decodeJoined(forKey: .teachers, separator: ",") ?? []
            owners = container.decodeObjectValues(String.self, forKey: .owners) ?? []
        }
    }

    func testReadsEpochSecondsJoinedListsAndObjectValues() throws {
        let json = #"{"start": "1700000000", "teachers": "Ada, Bob ,", "owners": {"a": "x"}}"#
        let lesson = try JSONDecoder().decode(Lesson.self, from: Data(json.utf8))
        XCTAssertEqual(lesson.start?.epochSeconds, 1_700_000_000)
        XCTAssertEqual(lesson.teachers, ["Ada", "Bob"])
        XCTAssertEqual(lesson.owners, ["x"])
    }

    func testReadsValuesOutOfHtmlFragments() {
        let fragment = #"<iframe src="https://maps.example/embed"></iframe>"#
        XCTAssertEqual(Scrape.attribute(Scrape.select(Scrape.fragment(fragment), "iframe"), "src", missing: []), "https://maps.example/embed")
    }
}
