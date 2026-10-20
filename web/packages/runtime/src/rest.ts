import type { JsonValue } from "./coding.js";
import type { LocalDate, LocalDateTime, LocalTime } from "./temporal.js";

export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type QueryScalar = string | number | boolean | LocalDate | LocalDateTime | LocalTime;

/**
 * A value sent as a query, form or path parameter; `null` and `undefined` are left out and
 * lists are sent as one comma-separated value.
 */
export type QueryValue = QueryScalar | readonly QueryScalar[] | null | undefined;

function formatQueryValue(value: NonNullable<QueryValue>): string {
  return Array.isArray(value) ? value.map(String).join(",") : String(value);
}

/** Credentials that authenticate a request through headers. */
export interface RequestAuth {
  readonly headers: Readonly<Record<string, string>>;
}

/** An HTTP request, relative to an API's base URL. */
export interface RestRequest {
  method: HttpMethod;
  /** Path appended to the base URL, starting with `/`, with path parameters already encoded. */
  path: string;
  query?: ReadonlyArray<readonly [string, QueryValue]>;
  /** Fields sent as `application/x-www-form-urlencoded`. */
  form?: ReadonlyArray<readonly [string, QueryValue]>;
  /** Headers; those with no value are left out. */
  headers?: ReadonlyArray<readonly [string, QueryValue]>;
  /** A value sent as JSON. */
  body?: JsonValue;
  /** Credentials whose headers are added. */
  auth?: RequestAuth | null;
  /** The base URL to send the request to, for APIs with several; the client's own by default. */
  server?: string;
  /** Keys of the objects the response value is nested in, outermost first. */
  envelope?: readonly string[];
}

/** Percent-encodes a value for use as one path segment. */
export function pathSegment(value: NonNullable<QueryValue>): string {
  return encodeURIComponent(formatQueryValue(value));
}

/** Where and how an API client sends its requests. */
export interface RestConfiguration {
  baseUrl: string;
  /** Headers sent with every request, such as `Authorization`. */
  headers?: Readonly<Record<string, string>>;
  /** The `fetch` implementation; the global one by default. */
  fetch?: typeof fetch;
}

/** Sends `request` and returns the response whatever its status. */
export function sendRequest(configuration: RestConfiguration, request: RestRequest): Promise<Response> {
  const url = new URL((request.server ?? configuration.baseUrl).replace(/\/$/, "") + request.path);
  for (const [name, value] of request.query ?? []) {
    if (value !== null && value !== undefined) url.searchParams.append(name, formatQueryValue(value));
  }

  const headers = new Headers({ Accept: "application/json", ...configuration.headers, ...request.auth?.headers });
  for (const [name, value] of request.headers ?? []) {
    if (value !== null && value !== undefined) headers.set(name, formatQueryValue(value));
  }
  let body: string | URLSearchParams | undefined;
  if (request.form !== undefined) {
    body = new URLSearchParams();
    for (const [name, value] of request.form) {
      if (value !== null && value !== undefined) body.append(name, formatQueryValue(value));
    }
  } else if (request.body !== undefined) {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(request.body);
  }

  const send = configuration.fetch ?? fetch;
  return send(url, { method: request.method, headers, body });
}
