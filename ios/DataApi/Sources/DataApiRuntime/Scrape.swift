import Foundation
import SwiftSoup

/// A node of a parsed HTML page: an element, or text. Generated scrapers take and return these,
/// so API modules don't depend on SwiftSoup themselves.
public typealias ScrapeNode = SwiftSoup.Node

/// The steps generated scrapers compile to. Every helper accepts and returns optionals, so a
/// missing value travels to the end of a pipeline; reads return nil for blank text.
public enum Scrape {
    /// Parses `html` as a document whose relative links resolve against `url`.
    public static func document(_ html: String, url: URL?) throws -> ScrapeNode {
        try SwiftSoup.parse(html, url?.absoluteString ?? "")
    }

    /// The JSON a script assigns to `variable` (`var name = [...];`), or nil when it assigns none.
    public static func scriptVariable(_ script: String, _ variable: String) -> Data? {
        let prefix = "var \(variable) ="
        guard let line = script.split(whereSeparator: \.isNewline).first(where: { $0.hasPrefix(prefix) }) else { return nil }
        var json = line.dropFirst(prefix.count).trimmingCharacters(in: .whitespaces)
        if json.hasSuffix(";") { json.removeLast() }
        return Data(json.utf8)
    }

    public static func select(_ node: ScrapeNode?, _ selector: String) -> ScrapeNode? {
        try? (node as? Element)?.select(selector).first()
    }

    public static func selectAll(_ node: ScrapeNode?, _ selector: String) -> [ScrapeNode] {
        (try? (node as? Element)?.select(selector).array()) ?? []
    }

    public static func root(_ node: ScrapeNode?) -> ScrapeNode? {
        node?.ownerDocument()
    }

    public static func next(_ node: ScrapeNode?) -> ScrapeNode? {
        try? (node as? Element)?.nextElementSibling()
    }

    public static func lastSibling(_ node: ScrapeNode?) -> ScrapeNode? {
        node?.siblingNodes().last
    }

    /// The element of the whole document matched by the selector in the node's `attribute`.
    public static func follow(_ node: ScrapeNode?, _ attribute: String) -> ScrapeNode? {
        guard let selector = Scrape.attribute(node, attribute, missing: []) else { return nil }
        return try? node?.ownerDocument()?.select(selector).first()
    }

    /// The node's children grouped into sections, each starting with a child matching `header`.
    public static func sections(_ node: ScrapeNode?, header: String) -> [ScrapeNode] {
        guard let element = node as? Element, let headers = try? element.select(header) else { return [] }
        var sections: [Element] = []
        for child in element.children().array() {
            if headers.array().contains(where: { $0 === child }), let tag = try? Tag.valueOf("section") {
                sections.append(Element(tag, element.getBaseUri()))
            }
            if let section = sections.last, let copy = child.copy() as? Node {
                _ = try? section.appendChild(copy)
            }
        }
        return sections
    }

    public static func text(_ node: ScrapeNode?, exclude: String?, missing: [String]) -> String? {
        switch node {
        case let text as TextNode:
            return present(text.text(), missing)
        case let element as Element:
            guard let exclude else { return present(try? element.text(), missing) }
            guard let copy = element.copy() as? Element else { return nil }
            _ = try? copy.select(exclude).remove()
            return present(try? copy.text(), missing)
        default:
            return nil
        }
    }

    public static func ownText(_ node: ScrapeNode?, missing: [String]) -> String? {
        switch node {
        case let text as TextNode: return present(text.text(), missing)
        case let element as Element: return present(element.ownText(), missing)
        default: return nil
        }
    }

    public static func html(_ node: ScrapeNode?) -> String? {
        present(try? (node as? Element)?.html(), [])
    }

    public static func attribute(_ node: ScrapeNode?, _ name: String, missing: [String]) -> String? {
        guard let node, node.hasAttr(name) else { return nil }
        return present(try? node.attr(name), missing)
    }

    /// The node's `attribute` as an absolute URL, resolved against the page's.
    public static func url(_ node: ScrapeNode?, _ attribute: String) -> String? {
        present(try? node?.absUrl(attribute), [])
    }

    /// The node's siblings as text, keeping line breaks and writing list items as `• ` lines.
    public static func lines(_ node: ScrapeNode?, missing: [String]) -> String? {
        guard let siblings = node?.siblingNodes() else { return nil }
        var rendered = ""
        for sibling in siblings {
            switch sibling {
            case let text as TextNode:
                rendered += text.getWholeText()
            case let element as Element where element.tagName() == "br":
                rendered += "\n"
            case let element as Element where element.tagName() == "ul":
                for item in (try? element.select("li").array()) ?? [] {
                    rendered += "\n• " + ((try? item.text()) ?? "").trimmingCharacters(in: .whitespaces)
                }
            case let element as Element:
                rendered += ((try? element.text()) ?? "").trimmingCharacters(in: .whitespaces)
            default:
                break
            }
        }
        var text = rendered.components(separatedBy: "\n").map { $0.trimmingCharacters(in: .whitespaces) }.joined(separator: "\n")
        while text.contains("\n\n\n") { text = text.replacingOccurrences(of: "\n\n\n", with: "\n\n") }
        return present(text, missing)
    }

    /// The text `pattern` matches: its first group, or the whole match without groups.
    public static func regex(_ text: String?, _ pattern: String) -> String? {
        guard let text, let expression = try? NSRegularExpression(pattern: pattern) else { return nil }
        let range = NSRange(text.startIndex..., in: text)
        guard let match = expression.firstMatch(in: text, range: range) else { return nil }
        let group = match.numberOfRanges > 1 ? match.range(at: 1) : match.range
        guard let matched = Range(group, in: text) else { return nil }
        return present(String(text[matched]), [])
    }

    public static func removePrefix(_ text: String?, _ prefix: String) -> String? {
        guard let text else { return nil }
        return present(text.hasPrefix(prefix) ? String(text.dropFirst(prefix.count)) : text, [])
    }

    public static func removeSuffix(_ text: String?, _ suffix: String) -> String? {
        guard let text else { return nil }
        return present(text.hasSuffix(suffix) ? String(text.dropLast(suffix.count)) : text, [])
    }

    /// The text without `prefix` and `suffix`, when it has both.
    public static func unwrap(_ text: String?, _ prefix: String, _ suffix: String) -> String? {
        guard let text else { return nil }
        guard text.count >= prefix.count + suffix.count, text.hasPrefix(prefix), text.hasSuffix(suffix) else { return present(text, []) }
        return present(String(text.dropFirst(prefix.count).dropLast(suffix.count)), [])
    }

    public static func nullIf(_ text: String?, _ values: [String]) -> String? {
        guard let text, !values.contains(where: { $0.caseInsensitiveCompare(text) == .orderedSame }) else { return nil }
        return text
    }

    /// The value of the query parameter `name` of the URL `text` holds.
    public static func queryParameter(_ text: String?, _ name: String) -> String? {
        guard let text else { return nil }
        return present(URLComponents(string: text)?.queryItems?.first(where: { $0.name == name })?.value, [])
    }

    public static func replace(_ text: String?, _ replacements: [String: String]) -> String? {
        guard let text else { return nil }
        return replacements.first(where: { $0.key.caseInsensitiveCompare(text) == .orderedSame })?.value ?? text
    }

    public static func equals(_ text: String?, _ value: String) -> Bool? {
        text.map { $0.caseInsensitiveCompare(value) == .orderedSame }
    }

    public static func contains(_ text: String?, _ value: String) -> Bool? {
        text.map { $0.range(of: value, options: .caseInsensitive) != nil }
    }

    /// `nodes`, or nil when empty, so alternatives yielding elements can be chained with `??`.
    public static func nonEmpty(_ nodes: [ScrapeNode]) -> [ScrapeNode]? {
        nodes.isEmpty ? nil : nodes
    }

    public static func int(_ text: String?) -> Int? { text.flatMap { Int($0) } }

    public static func int64(_ text: String?) -> Int64? { text.flatMap { Int64($0) } }

    public static func float(_ text: String?) -> Float? { text.flatMap { Float($0) } }

    public static func double(_ text: String?) -> Double? { text.flatMap { Double($0) } }

    /// The text trimmed, or nil when blank or a text the page shows for a missing value.
    private static func present(_ text: String?, _ missing: [String]) -> String? {
        guard let text else { return nil }
        let value = text.replacingOccurrences(of: "\u{00a0}", with: " ").trimmingCharacters(in: .whitespacesAndNewlines)
        guard !value.isEmpty, !missing.contains(where: { $0.caseInsensitiveCompare(value) == .orderedSame }) else { return nil }
        return value
    }
}

extension RestResponse {
    /// Decodes the JSON a script response assigns to `variable`.
    public func script<T: Decodable>(_ type: T.Type, variable: String) throws -> T {
        guard let json = Scrape.scriptVariable(String(decoding: body, as: UTF8.self), variable) else {
            throw ScrapeError.missingVariable(variable)
        }
        return try JSONDecoder().decode(T.self, from: json)
    }

    /// Parses the response as an HTML page and reads a value out of it with `scrape`.
    public func scrape<T>(_ scrape: (ScrapeNode) -> T?) throws -> T {
        let document = try Scrape.document(String(decoding: body, as: UTF8.self), url: url)
        guard let value = scrape(document) else { throw ScrapeError.unreadablePage }
        return value
    }
}

/// A page that didn't hold what a scraper expected, usually because the site changed.
public enum ScrapeError: Error, Sendable {
    case missingVariable(String)
    case unreadablePage
}
