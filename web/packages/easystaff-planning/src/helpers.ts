import { asJsonObject, decodeInteger, decodeString, type WireMap } from "@mybicocca/data-api-runtime";
import type {
  EasyStaffPlanningDaySchedule,
  EasyStaffPlanningFormField,
  EasyStaffPlanningReservationResult,
  EasyStaffPlanningSlotAvailability,
  EasyStaffPlanningTimeRange,
} from "./generated/index.js";

/** The university's Portale Planning portals, as the parameters of `createEasyStaffPlanningApi`. */
export const EasyStaffPlanningPortal = {
  /** The student information desks ("Sportelli Informativi"), which support anonymous booking. */
  InformationDesks: { portalId: 2, portalCode: "unimib-segreteria" },
  /** The legacy study rooms portal, whose mandatory login is blocked; it lists nothing. */
  StudyRooms: { portalId: 1, portalCode: "unimib-aulestudio" },
} as const;

/** An option of a booking form field: the value to send, and the label to show. */
export interface EasyStaffPlanningFieldOption {
  value: string;
  label: string;
}

/**
 * The options of a field, from its `values`: the labels of a select by value, a list of
 * values, or a single value (the privacy policy URL of the GDPR checkbox). Values without a
 * label are their own label. `undefined` when the field has none.
 */
export function fieldOptions(field: EasyStaffPlanningFormField): EasyStaffPlanningFieldOption[] | undefined {
  const values = field.values;
  if (values === null) return undefined;
  if (Array.isArray(values)) {
    return values.flatMap((item) => {
      const value = decodeString(item);
      return value === undefined ? [] : [{ value, label: value }];
    });
  }
  const labels = asJsonObject(values);
  if (labels !== undefined) {
    return Object.entries(labels).flatMap(([value, item]) => {
      const label = decodeString(item);
      return label === undefined ? [] : [{ value, label }];
    });
  }
  const value = decodeString(values);
  return value === undefined || value.trim() === "" ? undefined : [{ value, label: value }];
}

/** The entry identifier of a reservation, sent either bare or as the entry object. */
export function reservationEntryId(result: EasyStaffPlanningReservationResult): number | undefined {
  const entry = asJsonObject(result.entry);
  return decodeInteger(entry !== undefined ? entry["id"] : result.entry);
}

/** The slots of the requested day, which the backend nests under its date. */
export function daySlots(
  schedule: EasyStaffPlanningDaySchedule,
): WireMap<EasyStaffPlanningTimeRange, EasyStaffPlanningSlotAvailability> | undefined {
  return schedule.days.values().next().value;
}
