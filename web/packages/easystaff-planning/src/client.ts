import {
  DecodeError,
  asJsonObject,
  decodeInteger,
  decodeString,
  sendRequest,
  unwrapEnvelope,
  type RestConfiguration,
  type RestRequest,
  type WireMap,
} from "@mybicocca/data-api-runtime";
import type {
  EasyStaffPlanningDaySchedule,
  EasyStaffPlanningFormField,
  EasyStaffPlanningReservationResult,
  EasyStaffPlanningSlotAvailability,
  EasyStaffPlanningTimeRange,
} from "./generated/index.js";

/** A request the backend rejected. Match on `key` (or `status`). */
export class EasyStaffPlanningError extends Error {
  override readonly name = "EasyStaffPlanningError";

  /** @param key The error key of a `{"message": "<key>"}` body, such as `not_found`. */
  constructor(
    readonly status: number,
    readonly key: string | undefined,
  ) {
    super(`Portale Planning request failed with status ${status}${key === undefined ? "" : `: ${key}`}`);
  }
}

const PORTAL_HEADER = "X-CLIENTE";
const INVALID_TOKEN = "unauthorized.invalid_token";
const TOO_FAST = "unauthorized.too_fast";

/** How long to wait before sending a booking again: the backend refuses guest tokens younger than about ten seconds. */
const GUEST_TOKEN_MIN_AGE_MS = 12_000;

/**
 * Sends the generated Portale Planning operations' requests and decodes responses and errors.
 *
 * Some endpoints, the booking form and the availability schedules among them, require the
 * `jwt_public` guest cookie, which the backend hands out on the service and area listings. A
 * request rejected with `unauthorized.invalid_token` fetches the service listing for a fresh
 * token and is sent once more. This relies on the `fetch` implementation keeping cookies.
 */
export class EasyStaffPlanningClient {
  readonly configuration: RestConfiguration;

  constructor(configuration: Partial<RestConfiguration> = {}) {
    this.configuration = {
      baseUrl: "https://gestioneorari.didattica.unimib.it/portaleplanningAPI/api",
      ...configuration,
      headers: { "X-APP-LOCALE": "it", ...configuration.headers },
    };
  }

  async json<T>(request: RestRequest, decode: (json: unknown) => T | undefined): Promise<T> {
    const value = decode(await this.body(request));
    if (value === undefined) throw new DecodeError(`Unexpected response to ${request.method} ${request.path}`);
    return value;
  }

  async jsonList<T>(request: RestRequest, decodeItem: (json: unknown) => T | undefined): Promise<T[]> {
    const json = await this.body(request);
    if (!Array.isArray(json)) throw new DecodeError(`Expected a list in response to ${request.method} ${request.path}`);
    return json.map((item, index) => {
      const value = decodeItem(item);
      if (value === undefined) throw new DecodeError(`Unexpected item ${index} in response to ${request.method} ${request.path}`);
      return value;
    });
  }

  async bytes(request: RestRequest): Promise<Uint8Array> {
    return new Uint8Array(await (await this.execute(request)).arrayBuffer());
  }

  async send(request: RestRequest): Promise<void> {
    await this.execute(request);
  }

  private async body(request: RestRequest): Promise<unknown> {
    return unwrapEnvelope(await (await this.execute(request)).json(), request.envelope ?? []);
  }

  private async execute(request: RestRequest): Promise<Response> {
    let response = await sendRequest(this.configuration, request);
    let key = response.status === 401 ? await errorKey(response.clone()) : undefined;
    const portal = request.headers?.find(([name]) => name === PORTAL_HEADER)?.[1];
    if (key === INVALID_TOKEN && portal !== undefined && portal !== null) {
      await sendRequest(this.configuration, {
        method: "GET",
        path: `/servizi/${encodeURIComponent(String(portal))}`,
        headers: [[PORTAL_HEADER, portal]],
        ...(request.server === undefined ? {} : { server: request.server }),
      });
      response = await sendRequest(this.configuration, request);
      key = response.status === 401 ? await errorKey(response.clone()) : undefined;
    }
    if (key === TOO_FAST) {
      await new Promise((resolve) => setTimeout(resolve, GUEST_TOKEN_MIN_AGE_MS));
      response = await sendRequest(this.configuration, request);
    }
    if (!response.ok) throw new EasyStaffPlanningError(response.status, await errorKey(response));
    return response;
  }
}

/** The key of a `{"message": "<key>"}` error body, if it is one. */
async function errorKey(response: Response): Promise<string | undefined> {
  const message = decodeString(asJsonObject(await response.json().catch(() => undefined))?.["message"]);
  return message === undefined || message === "" ? undefined : message;
}

/** The university's Portale Planning portals. */
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
