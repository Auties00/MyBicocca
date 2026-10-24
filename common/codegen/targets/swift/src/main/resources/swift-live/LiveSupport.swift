import DataApiRuntime
import Foundation

/// The report of an API's live tests: one JSON object per line, in `<platform>-<api>.jsonl` under
/// ``LiveSettings/reportDir``. It holds outcomes, findings and fingerprints, never values or
/// credentials, so that it can be shared.
final class LiveReport: @unchecked Sendable {
    private let api: String
    private let file: URL
    private let lock = NSLock()
    private var findings = Set<String>()
    private var seen = Set<String>()

    init(api: String) {
        self.api = api
        file = URL(fileURLWithPath: LiveSettings.reportDir).appendingPathComponent("\(LiveSettings.platform)-\(api).jsonl")
    }

    func start(skips: [(String, String)]) {
        try? FileManager.default.createDirectory(at: file.deletingLastPathComponent(), withIntermediateDirectories: true)
        FileManager.default.createFile(atPath: file.path, contents: Data())
        write("run", ["seed": LiveSettings.seed, "writes": LiveSettings.writes])
        for (operation, reason) in skips { write("skip", ["id": operation, "reason": reason]) }
    }

    func `case`(_ id: String, status: String, reason: String?, fingerprint: String?, ms: Int) {
        var body: [String: Any] = ["id": id, "status": status, "ms": ms]
        if let reason { body["reason"] = reason }
        if let fingerprint { body["fingerprint"] = fingerprint }
        write("case", body)
    }

    /// Something a response did that the spec doesn't say, reported once per kind and place.
    func finding(_ kind: String, record: String, field: String, detail: String) {
        let isNew = lock.withLockedValue { findings.insert("\(kind)|\(record)|\(field)").inserted }
        if isNew { write("finding", ["kind": kind, "record": record, "field": field, "detail": detail]) }
    }

    /// A field of a record that held a value.
    func noteSeen(record: String, field: String) {
        let isNew = lock.withLockedValue { seen.insert("\(record)|\(field)").inserted }
        if isNew { write("seen", ["record": record, "field": field]) }
    }

    func event(_ kind: String, id: String, detail: String) {
        write(kind, ["id": id, "detail": detail])
    }

    private func write(_ type: String, _ body: [String: Any]) {
        var line = body
        line["type"] = type
        line["platform"] = LiveSettings.platform
        line["api"] = api
        guard var data = try? JSONSerialization.data(withJSONObject: line, options: [.sortedKeys]) else { return }
        data.append(0x0A)
        lock.withLockedValue {
            if let handle = try? FileHandle(forWritingTo: file) {
                handle.seekToEndOfFile()
                handle.write(data)
                handle.closeFile()
            }
        }
    }
}

extension NSLock {
    /// Runs `body` holding the lock.
    func withLockedValue<T>(_ body: () throws -> T) rethrows -> T {
        lock()
        defer { unlock() }
        return try body()
    }
}

/// A change a write case made and hasn't undone yet.
struct JournalEntry: Codable, Sendable {
    let api: String
    let `case`: String
    let values: [String: String]
    var platform: String = LiveSettings.platform
}

/// The changes write cases made and haven't undone yet, in ``LiveSettings/journal``, so that a run
/// interrupted between a change and its undo is cleaned up by the next one.
enum LiveJournal {
    private static let lock = NSLock()

    static func entries(api: String) -> [JournalEntry] {
        lock.withLockedValue { read().filter { $0.api == api } }
    }

    static func add(_ entry: JournalEntry) {
        lock.withLockedValue { write(read().filter { $0.api != entry.api || $0.case != entry.case } + [entry]) }
    }

    static func remove(api: String, case id: String) {
        lock.withLockedValue { write(read().filter { $0.api != api || $0.case != id }) }
    }

    private static var file: URL { URL(fileURLWithPath: LiveSettings.journal) }

    private static func read() -> [JournalEntry] {
        guard let data = try? Data(contentsOf: file), !data.isEmpty else { return [] }
        return (try? JSONDecoder().decode([JournalEntry].self, from: data)) ?? []
    }

    private static func write(_ entries: [JournalEntry]) {
        try? FileManager.default.createDirectory(at: file.deletingLastPathComponent(), withIntermediateDirectories: true)
        try? JSONEncoder().encode(entries).write(to: file)
    }
}

/// Checks responses against what the spec says they look like (the generator's `Shapes`),
/// reporting fields the spec doesn't know, values of another type than declared, required fields
/// that are missing and enum values it doesn't list, and noting the fields that held values.
final class LiveShapes: @unchecked Sendable {
    private let records: [String: [[Any]]]
    private let enums: [String: [String: Any]]
    private let report: LiveReport
    private let lock = NSLock()
    private var temporals: [String: NSRegularExpression] = [:]

    init(catalog: String, report: LiveReport) {
        let root = (try? JSONSerialization.jsonObject(with: Data(catalog.utf8))) as? [String: Any] ?? [:]
        records = root["records"] as? [String: [[Any]]] ?? [:]
        enums = root["enums"] as? [String: [String: Any]] ?? [:]
        self.report = report
    }

    func check(shape: String, response: Observed, envelope: [String], variable: String?) {
        guard let body = response.body else { return }
        let text: String
        if let variable {
            guard let data = Scrape.scriptVariable(body, variable) else { return }
            text = String(decoding: data, as: UTF8.self)
        } else {
            text = body
        }
        guard var value = try? JSONSerialization.jsonObject(with: Data(text.utf8), options: [.fragmentsAllowed]) else { return }
        for key in envelope {
            guard let object = value as? [String: Any], let inner = object[key] else { return }
            value = inner
        }
        guard let shape = (try? JSONSerialization.jsonObject(with: Data(shape.utf8))) as? [String: Any] else { return }
        visit(value, shape, record: "", field: "")
    }

    private func visit(_ value: Any, _ shape: [String: Any], record: String, field: String) {
        if value is NSNull { return }
        func mismatch(_ expected: String) {
            report.finding("type-mismatch", record: record, field: field, detail: "expected \(expected), got \(kind(of: value))")
        }
        let text = scalarText(value)
        switch shape["k"] as? String {
        case "string":
            if text == nil { mismatch("a string") }
        case "int":
            if let text, text.trimmingCharacters(in: .whitespaces).isEmpty || Int64(text.trimmingCharacters(in: .whitespaces)) != nil {} else { mismatch("an integer") }
        case "number", "epoch":
            if let text, text.trimmingCharacters(in: .whitespaces).isEmpty || Double(text) != nil {} else { mismatch("a number") }
        case "bool":
            if let text, ["true", "false", "0", "1"].contains(text.lowercased()) {} else { mismatch("a boolean") }
        case let kind? where ["date", "datetime", "time"].contains(kind):
            let pattern = shape["p"] as? String ?? ""
            guard let string = value as? String else { return mismatch("a \(kind) in \(pattern)") }
            if string.trimmingCharacters(in: .whitespaces).isEmpty || string == "null" {
                if shape["blank"] as? Bool != true { mismatch("a \(kind) in \(pattern)") }
            } else if temporal(pattern).firstMatch(in: string, range: NSRange(string.startIndex..., in: string)) == nil {
                report.finding("type-mismatch", record: record, field: field, detail: "expected a \(kind) in \(pattern)")
            }
        case "enum":
            let name = shape["n"] as? String ?? ""
            let enumeration = enums[name] ?? [:]
            let raw = (enumeration["wrapped"] as? Bool == true ? (value as? [String: Any])?["value"] : nil) ?? value
            let values = enumeration["values"] as? [String] ?? []
            if let content = scalarText(raw) {
                if !values.contains(content) { report.finding("unknown-enum", record: name, field: field, detail: "'\(content)' isn't one of its values") }
            } else {
                mismatch("a value of \(name)")
            }
        case "record":
            self.record(value, shape, record: record, field: field)
        case "list":
            let element = shape["e"] as? [String: Any] ?? [:]
            if let array = value as? [Any] {
                array.prefix(Self.maxElements).forEach { visit($0, element, record: record, field: field) }
            } else if let object = value as? [String: Any], shape["objectValues"] as? Bool == true {
                object.values.prefix(Self.maxElements).forEach { visit($0, element, record: record, field: field) }
            } else if !(text != nil && shape["joined"] as? Bool == true) {
                mismatch("a list")
            }
        case "map":
            let element = shape["v"] as? [String: Any] ?? [:]
            if let object = value as? [String: Any] {
                object.values.prefix(Self.maxElements).forEach { visit($0, element, record: record, field: field) }
            } else if let array = value as? [Any] {
                array.compactMap { $0 as? [String: Any] }.flatMap { $0.values }.prefix(Self.maxElements).forEach { visit($0, element, record: record, field: field) }
            } else {
                mismatch("an object")
            }
        case "json":
            if !(value is [String: Any]) { mismatch("an object") }
        default:
            break
        }
    }

    private func record(_ value: Any, _ shape: [String: Any], record: String, field: String) {
        let name = shape["n"] as? String ?? ""
        var object: [String: Any]
        if let dictionary = value as? [String: Any] {
            object = dictionary
        } else if let array = value as? [Any], shape["folded"] as? Bool == true {
            object = array.compactMap { $0 as? [String: Any] }.reduce(into: [:]) { merged, item in merged.merge(item) { _, new in new } }
        } else if let string = value as? String, shape["embedded"] as? Bool == true {
            if string.trimmingCharacters(in: .whitespaces).isEmpty { return }
            guard let parsed = (try? JSONSerialization.jsonObject(with: Data(string.utf8))) as? [String: Any] else {
                return report.finding("type-mismatch", record: record, field: field, detail: "expected JSON of \(name) in a string")
            }
            object = parsed
        } else if let array = value as? [Any], array.isEmpty {
            return
        } else {
            return report.finding("type-mismatch", record: record, field: field, detail: "expected an object of \(name), got \(kind(of: value))")
        }
        guard let fields = records[name] else { return }
        let declared = Set(fields.compactMap { $0.first as? String })
        for key in object.keys where !declared.contains(key) {
            report.finding("unknown-key", record: name, field: key, detail: "the spec doesn't declare it")
        }
        for definition in fields {
            guard definition.count == 3, let wire = definition[0] as? String, let fieldShape = definition[1] as? [String: Any] else { continue }
            let optional = definition[2] as? Bool ?? false
            if let item = object[wire] {
                if item is NSNull {
                    if !optional { report.finding("missing-key", record: name, field: wire, detail: "a required field is null") }
                } else {
                    if !((item as? String)?.trimmingCharacters(in: .whitespaces).isEmpty ?? false) { report.noteSeen(record: name, field: wire) }
                    visit(item, fieldShape, record: name, field: wire)
                }
            } else if !optional {
                report.finding("missing-key", record: name, field: wire, detail: "a required field is missing")
            }
        }
    }

    /// A regex matching the start of a date or time written in `pattern` (Unicode date syntax).
    private func temporal(_ pattern: String) -> NSRegularExpression {
        lock.withLockedValue {
            if let regex = temporals[pattern] { return regex }
            var source = "^"
            let characters = Array(pattern)
            var index = 0
            while index < characters.count {
                let char = characters[index]
                var end = index
                while end < characters.count && characters[end] == char { end += 1 }
                let count = end - index
                if char == "'" {
                    let close = characters[(index + 1)...].firstIndex(of: "'") ?? characters.count
                    source += NSRegularExpression.escapedPattern(for: String(characters[(index + 1)..<close]))
                    end = close + 1
                } else if char == "y" {
                    source += count == 2 ? "\\d{2}" : "\\d{4}"
                } else if char == "S" {
                    source += "\\d+"
                } else if char.isLetter {
                    source += "\\d{1,2}"
                } else {
                    source += NSRegularExpression.escapedPattern(for: String(characters[index..<end]))
                }
                index = end
            }
            let regex = (try? NSRegularExpression(pattern: source)) ?? NSRegularExpression()
            temporals[pattern] = regex
            return regex
        }
    }

    private func scalarText(_ value: Any) -> String? {
        if let string = value as? String { return string }
        if let number = value as? NSNumber {
            if CFGetTypeID(number) == CFBooleanGetTypeID() { return number.boolValue ? "true" : "false" }
            return number.stringValue
        }
        return nil
    }

    private func kind(of value: Any) -> String {
        switch value {
        case is [String: Any]: return "an object"
        case is [Any]: return "a list"
        case is NSNull: return "null"
        case is String: return "a string"
        case let number as NSNumber: return CFGetTypeID(number) == CFBooleanGetTypeID() ? "a boolean" : "a number"
        default: return "a value"
        }
    }

    private static let maxElements = 50
}

/// The canonical form of a decoded result, the same on every platform, and its hash: atoms end
/// with `;` (`~` for no value, `"…"` text with `\` and `"` escaped, integers, numbers as
/// millionths rounded down after adding a half, `t`/`f`, dates in ISO 8601, `b<size>` for bytes,
/// JSON with sorted keys); lists are `[…]`, records `(…)` with their fields in declaration order,
/// maps `{key=value…}` sorted by key. The hash is the 64-bit FNV-1a of the UTF-8 text, in hex.
final class LiveFingerprint {
    private(set) var text = ""

    func `nil`() {
        text += "~;"
    }

    func str(_ value: String?) {
        atom(value) { "\"" + escape($0) + "\"" }
    }

    func int(_ value: Int?) {
        atom(value) { String($0) }
    }

    func int(_ value: Int64?) {
        atom(value) { String($0) }
    }

    func num(_ value: Double?) {
        atom(value) { millionths($0) }
    }

    func num(_ value: Float?) {
        atom(value) { millionths(Double($0)) }
    }

    func bool(_ value: Bool?) {
        atom(value) { $0 ? "t" : "f" }
    }

    func date(_ value: LocalDate?) {
        atom(value) { String(format: "%04d-%02d-%02d", $0.year, $0.month, $0.day) }
    }

    func dateTime(_ value: LocalDateTime?) {
        atom(value) { String(format: "%04d-%02d-%02dT%02d:%02d:%02d", $0.date.year, $0.date.month, $0.date.day, $0.hour, $0.minute, $0.second) }
    }

    func time(_ value: LocalTime?) {
        atom(value) { String(format: "%02d:%02d:%02d", $0.hour, $0.minute, $0.second) }
    }

    func bytes(_ size: Int) {
        text += "b\(size);"
    }

    func json(_ value: JSONValue?) {
        guard let value, value != .null else { return self.nil() }
        text += canonical(value) + ";"
    }

    func json(_ value: JSONObject?) {
        guard let value else { return self.nil() }
        json(JSONValue.object(value))
    }

    func list<T>(_ value: [T]?, _ each: (T) -> Void) {
        guard let value else { return self.nil() }
        text += "["
        value.forEach(each)
        text += "]"
    }

    func obj(_ fields: () -> Void) {
        text += "("
        fields()
        text += ")"
    }

    func map<K: Hashable, V>(_ value: [K: V]?, _ key: (LiveFingerprint, K) -> Void, _ each: (V) -> Void) {
        guard let value else { return self.nil() }
        let entries = value.map { entry -> (String, V) in
            let keyText = LiveFingerprint()
            key(keyText, entry.key)
            return (keyText.text, entry.value)
        }.sorted { Array($0.0.utf16).lexicographicallyPrecedes(Array($1.0.utf16)) }
        text += "{"
        for (key, value) in entries {
            text += key + "="
            each(value)
        }
        text += "}"
    }

    func hash() -> String {
        var hash: UInt64 = 0xcbf2_9ce4_8422_2325
        for byte in text.utf8 {
            hash ^= UInt64(byte)
            hash = hash &* 0x0000_0100_0000_01b3
        }
        let hex = String(hash, radix: 16)
        return String(repeating: "0", count: 16 - hex.count) + hex
    }

    private func atom<T>(_ value: T?, _ render: (T) -> String) {
        guard let value else { return self.nil() }
        text += render(value) + ";"
    }

    private func escape(_ text: String) -> String {
        text.replacingOccurrences(of: "\\", with: "\\\\").replacingOccurrences(of: "\"", with: "\\\"")
    }

    private func millionths(_ value: Double) -> String {
        String(Int64((value * 1_000_000 + 0.5).rounded(.down)))
    }

    private func canonical(_ value: JSONValue) -> String {
        switch value {
        case .null: return "null"
        case .bool(let flag): return flag ? "true" : "false"
        case .number(let number):
            if number == number.rounded(.down) && abs(number) < 9.007199254740992e15 { return String(Int64(number)) }
            return millionths(number)
        case .string(let string): return "\"" + escape(string) + "\""
        case .array(let items): return "[" + items.map(canonical).joined(separator: ",") + "]"
        case .object(let object):
            let keys = object.keys.sorted { Array($0.utf16).lexicographicallyPrecedes(Array($1.utf16)) }
            return "{" + keys.map { "\"" + escape($0) + "\":" + canonical(object[$0]!) }.joined(separator: ",") + "}"
        }
    }
}
