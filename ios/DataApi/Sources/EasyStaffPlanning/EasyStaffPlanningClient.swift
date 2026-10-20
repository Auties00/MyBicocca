import DataApiRuntime
import Foundation

/// Sends the generated Portale Planning operations' requests and decodes responses and errors.
///
/// Some endpoints, the booking form and the availability schedules among them, require the
/// `jwt_public` guest cookie, which the backend hands out on the service and area listings
/// and lets expire after twenty minutes. A request rejected with `unauthorized.invalid_token`
/// fetches the service listing for a fresh token and is sent once more. This relies on the
/// session keeping cookies, as `URLSession.shared` does.
public final class EasyStaffPlanningClient: Sendable {
    /// Origin of the official web front-end, without which booking writes are refused.
    public static let origin = "https://gestioneorari.didattica.unimib.it"

    public static let defaultConfiguration = RestConfiguration(
        baseURL: URL(string: EasyStaffPlanningServer.main.rawValue)!,
        headers: ["X-APP-LOCALE": "it", "Origin": origin]
    )

    private static let portalHeader = "X-CLIENTE"
    private static let invalidToken = "unauthorized.invalid_token"
    private static let tooFast = "unauthorized.too_fast"

    /// How long to wait before sending a booking again after `unauthorized.too_fast`: the
    /// backend refuses guest tokens younger than about ten seconds.
    private static let guestTokenMinAge: UInt64 = 12_000_000_000

    public let configuration: RestConfiguration

    public init(configuration: RestConfiguration = EasyStaffPlanningClient.defaultConfiguration) {
        self.configuration = configuration
    }

    public func json<T: Decodable>(_ request: RestRequest) async throws -> T {
        try await execute(request).decode(T.self, envelope: request.envelope)
    }

    public func jsonList<T: Decodable>(_ request: RestRequest) async throws -> [T] {
        try await execute(request).decode([T].self, envelope: request.envelope)
    }

    public func data(_ request: RestRequest) async throws -> Data {
        try await execute(request).body
    }

    public func send(_ request: RestRequest) async throws {
        _ = try await execute(request)
    }

    private func execute(_ request: RestRequest) async throws -> RestResponse {
        var response = try await configuration.send(request)
        var key = response.status == 401 ? EasyStaffPlanningError(response: response).key : nil
        if key == Self.invalidToken, let portal = request.headers.first(where: { $0.name == Self.portalHeader })?.value {
            let listing = try RestRequest(
                method: .get,
                path: "/servizi/\(RestRequest.pathSegment(portal))",
                headers: [(Self.portalHeader, portal)],
                server: request.server
            )
            _ = try await configuration.send(listing)
            response = try await configuration.send(request)
            key = response.status == 401 ? EasyStaffPlanningError(response: response).key : nil
        }
        if key == Self.tooFast {
            try await Task.sleep(nanoseconds: Self.guestTokenMinAge)
            response = try await configuration.send(request)
        }
        guard response.isSuccess else { throw EasyStaffPlanningError(response: response) }
        return response
    }
}

extension EasyStaffPlanningApi {
    public init(portal: EasyStaffPlanningPortal, configuration: RestConfiguration = EasyStaffPlanningClient.defaultConfiguration) {
        self.init(client: EasyStaffPlanningClient(configuration: configuration), portalId: portal.id, portalCode: portal.code)
    }
}

/// The university's Portale Planning portals.
public enum EasyStaffPlanningPortal: Sendable, CaseIterable {
    /// The student information desks ("Sportelli Informativi"), which support anonymous booking.
    case informationDesks
    /// The legacy study rooms portal, whose mandatory login is blocked; it lists nothing.
    case studyRooms

    public var id: Int {
        switch self {
        case .informationDesks: return 2
        case .studyRooms: return 1
        }
    }

    public var code: String {
        switch self {
        case .informationDesks: return "unimib-segreteria"
        case .studyRooms: return "unimib-aulestudio"
        }
    }
}

/// A request the backend rejected. Match on ``key`` (or ``status``).
public struct EasyStaffPlanningError: Error, CustomStringConvertible {
    public let status: Int
    /// The error key of a `{"message": "<key>"}` body, such as `not_found`.
    public let key: String?

    init(response: RestResponse) {
        status = response.status
        if case .object(let body)? = try? JSONDecoder().decode(JSONValue.self, from: response.body),
           case .string(let message)? = body["message"], !message.isEmpty {
            key = message
        } else {
            key = nil
        }
    }

    public var description: String {
        "Portale Planning request failed with status \(status)" + (key.map { ": \($0)" } ?? "")
    }
}

/// An option of a booking form field: the value to send, and the label to show.
public struct EasyStaffPlanningFieldOption: Hashable, Sendable {
    public var value: String
    public var label: String
}

extension EasyStaffPlanningFormField {
    /// The options of the field, from ``values``: the labels of a select by value, a list of
    /// values, or a single value (the privacy policy URL of the GDPR checkbox). Values without
    /// a label are their own label. A select's options are sorted by value, since JSON objects
    /// don't keep their order here. Nil when the field has none.
    public var options: [EasyStaffPlanningFieldOption]? {
        switch values {
        case .object(let labels)?:
            return labels.sorted { $0.key < $1.key }.compactMap { value, label in
                String(lenient: label).map { EasyStaffPlanningFieldOption(value: value, label: $0) }
            }
        case .array(let items)?:
            return items.compactMap(String.init(lenient:)).map { EasyStaffPlanningFieldOption(value: $0, label: $0) }
        case let value?:
            return String(lenient: value).flatMap { $0.trimmingCharacters(in: .whitespaces).isEmpty ? nil : [EasyStaffPlanningFieldOption(value: $0, label: $0)] }
        case nil:
            return nil
        }
    }
}

extension EasyStaffPlanningReservationResult {
    /// The entry identifier of the reservation, sent either bare or as the entry object.
    public var entryId: Int? {
        switch entry {
        case .object(let object)?: return object["id"].flatMap(Int.init(lenient:))
        case let value?: return Int(lenient: value)
        case nil: return nil
        }
    }
}

extension EasyStaffPlanningDaySchedule {
    /// The slots of the requested day, which the backend nests under its date.
    public var slots: WireMap<EasyStaffPlanningTimeRange, EasyStaffPlanningSlotAvailability> {
        days.values.first ?? [:]
    }
}
