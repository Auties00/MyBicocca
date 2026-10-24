import DataApiRuntime
import Foundation

extension EasyStaffPlanningApi {
    /// The Portale Planning API of `portal`.
    public init(portal: EasyStaffPlanningPortal, configuration: RestConfiguration = EasyStaffPlanningClient.defaultConfiguration) {
        self.init(portalId: portal.id, portalCode: portal.code, configuration: configuration)
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
