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

/** Credentials that authenticate a request through headers or form fields. */
export interface RequestAuth {
  readonly headers: Readonly<Record<string, string>>;
  readonly form?: Readonly<Record<string, string>>;
}

/**
 * `value` as PHP-style bracketed fields named after `name` (`name[0][key]=value`), as PHP
 * servers read lists and records. Booleans are written `1` and `0`; nulls are left out.
 */
export function brackets(name: string, value: JsonValue | undefined): Array<[string, QueryValue]> {
  if (value === undefined || value === null) return [];
  if (typeof value === "boolean") return [[name, value ? "1" : "0"]];
  if (typeof value !== "object") return [[name, value]];
  const entries = Array.isArray(value) ? value.map((item, index) => [String(index), item] as const) : Object.entries(value);
  return entries.flatMap(([key, item]) => brackets(`${name}[${key}]`, item));
}

/** One field per element of `values`, each named `name`, as PHP reads a name ending in `[]`. */
export function repeated(name: string, values: readonly QueryScalar[] | null | undefined): Array<[string, QueryValue]> {
  return (values ?? []).map((value) => [name, value]);
}

/** A file sent as a field of a multipart form. */
export interface FileUpload {
  /** The name the server stores the file under. */
  readonly fileName: string;
  /** The file's media type; the server infers it when absent. */
  readonly contentType?: string;
  readonly bytes: Uint8Array<ArrayBuffer> | Blob;
}

/** An HTTP request, relative to an API's base URL. */
export interface RestRequest {
  method: HttpMethod;
  /** Path appended to the base URL, starting with `/`, with path parameters already encoded. */
  path: string;
  query?: ReadonlyArray<readonly [string, QueryValue]>;
  /** Fields sent as `application/x-www-form-urlencoded`, or with `multipart`. */
  form?: ReadonlyArray<readonly [string, QueryValue]>;
  /** Files sent as fields of a multipart form; those with no value are left out. */
  files?: ReadonlyArray<readonly [string, FileUpload | null | undefined]>;
  /** Whether the form is sent as `multipart/form-data`, as files need. */
  multipart?: boolean;
  /** Headers; those with no value are left out. */
  headers?: ReadonlyArray<readonly [string, QueryValue]>;
  /**
   * Cookies, percent-encoded into a `Cookie` header; those with no value are left out.
   * Browsers forbid that header, so cookie parameters only reach the server outside one.
   */
  cookies?: ReadonlyArray<readonly [string, QueryValue]>;
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
  const cookies = (request.cookies ?? []).flatMap(([name, value]) =>
    value === null || value === undefined ? [] : [`${name}=${encodeURIComponent(formatQueryValue(value))}`],
  );
  if (cookies.length > 0) headers.set("Cookie", cookies.join("; "));
  let body: string | URLSearchParams | FormData | undefined;
  const authForm = Object.entries(request.auth?.form ?? {});
  if (request.multipart === true) {
    // fetch sets the multipart content type, with its boundary.
    body = new FormData();
    for (const [name, value] of [...(request.form ?? []), ...authForm]) {
      if (value !== null && value !== undefined) body.append(name, formatQueryValue(value));
    }
    for (const [name, file] of request.files ?? []) {
      if (file === null || file === undefined) continue;
      const blob = file.bytes instanceof Blob ? file.bytes : new Blob([file.bytes], file.contentType === undefined ? {} : { type: file.contentType });
      body.append(name, blob, file.fileName);
    }
  } else if (request.form !== undefined || authForm.length > 0) {
    body = new URLSearchParams();
    for (const [name, value] of [...(request.form ?? []), ...authForm]) {
      if (value !== null && value !== undefined) body.append(name, formatQueryValue(value));
    }
  } else if (request.body !== undefined) {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(request.body);
  }

  const send = configuration.fetch ?? fetch;
  return send(url, { method: request.method, headers, body });
}
