import { asJsonObject } from "./coding.js";
import { requestUrl, sendRequest, type HttpMethod, type RestConfiguration, type RestRequest } from "./rest.js";

/** A failed request, before an API's client turns it into its own error type. */
export class ApiFailure extends Error {
  override readonly name = "ApiFailure";

  /**
   * @param code The machine-readable error code the body carried, if any.
   * @param message The human-readable message the body carried, else the code, else a
   *   description of the status.
   * @param serverMessage The human-readable message the body carried, if any.
   */
  constructor(
    readonly status: number,
    readonly code: string | undefined,
    message: string,
    readonly serverMessage?: string,
  ) {
    super(message);
  }
}

/**
 * Where a failed response's JSON body holds the error's code and message: JSON pointers
 * (`/error/code`), tried in order, skipping those holding no string or number.
 */
export interface ErrorFormat {
  /** Pointers that, holding a value other than `false` or `null`, mark a body received with a successful status as an error. */
  readonly detect?: readonly string[];
  readonly code?: readonly string[];
  readonly message?: readonly string[];
}

/**
 * A request sent to recover from a failure, to the server of the failed request. `{name}`
 * placeholders in `path` and `headers` are replaced by the client's API parameters.
 */
export interface RetryRequest {
  readonly method: HttpMethod;
  readonly path: string;
  readonly headers?: Readonly<Record<string, string>>;
}

/** A failure to recover from by sending the request again, once. */
export interface RetryRule {
  /** The status of the failure; any error status when absent. */
  readonly status?: number;
  /** The error code of the failure; any when absent. */
  readonly code?: string;
  /** A request to send first, such as one handing out a fresh token. */
  readonly before?: RetryRequest;
  readonly delaySeconds?: number;
}

/** How an API's client reads failures and recovers from them, as its spec describes it. */
export interface RestPolicy {
  /** Statuses with which a list operation answers an empty list. */
  readonly emptyStatuses?: readonly number[];
  readonly errors?: ErrorFormat;
  /** Failures to recover from by sending the request again, tried in order. */
  readonly retries?: readonly RetryRule[];
  /** Whether the client keeps responses in a {@link ResponseCache}. */
  readonly caches?: boolean;
}

/** A response kept by a {@link ResponseCache}. */
interface Kept {
  readonly url: string;
  readonly status: number;
  readonly headers: Array<[string, string]>;
  readonly body: ArrayBuffer;
  readonly expires: number;
}

/**
 * The responses a client keeps when its API's spec says `caches`, the same on every platform:
 * - a successful GET read whole, not a download, is kept when its `Cache-Control` has
 *   `max-age=N` and neither `no-store` nor `no-cache`, and answers the same URL for N seconds;
 * - any request other than a GET empties it once answered, whatever the answer, since it may
 *   have changed what the kept responses show;
 * - it keeps at most `capacity` responses, dropping the oldest first.
 *
 * The platform's own HTTP cache isn't used, since each platform's follows other rules.
 */
export class ResponseCache {
  private readonly entries = new Map<string, Kept>();

  constructor(
    private readonly capacity = 200,
    private readonly now: () => number = Date.now,
  ) {}

  /** The response kept for `url`, if it is still fresh, as a new `Response`. */
  get(url: string): Response | undefined {
    const kept = this.entries.get(url);
    if (kept === undefined) return undefined;
    if (kept.expires <= this.now()) {
      this.entries.delete(url);
      return undefined;
    }
    const response = new Response(kept.body.slice(0), { status: kept.status, headers: kept.headers });
    // Pages resolve their links against the URL that answered.
    Object.defineProperty(response, "url", { value: kept.url });
    return response;
  }

  /** Keeps `response` for `url`, if its headers allow it; the response stays readable. */
  async put(url: string, response: Response): Promise<void> {
    const seconds = maxAge(response.headers.get("Cache-Control"));
    if (seconds === undefined) return;
    const body = await response.clone().arrayBuffer();
    const headers: Array<[string, string]> = [];
    response.headers.forEach((value, name) => headers.push([name, value]));
    this.entries.delete(url);
    this.entries.set(url, { url: response.url, status: response.status, headers, body, expires: this.now() + seconds * 1000 });
    while (this.entries.size > this.capacity) this.entries.delete(this.entries.keys().next().value!);
  }

  clear(): void {
    this.entries.clear();
  }
}

/** How many seconds `Cache-Control` lets a response be kept: its `max-age`, unless it forbids keeping it. */
export function maxAge(cacheControl: string | null): number | undefined {
  if (cacheControl === null) return undefined;
  const directives = cacheControl.split(",").map((directive) => directive.trim().toLowerCase());
  if (directives.includes("no-store") || directives.includes("no-cache")) return undefined;
  const seconds = Number(directives.find((directive) => directive.startsWith("max-age="))?.slice("max-age=".length));
  return Number.isInteger(seconds) && seconds > 0 ? seconds : undefined;
}

/**
 * Sends requests as a {@link RestPolicy} says, and throws an {@link ApiFailure} for a response
 * with an error status, or with a JSON body the policy detects as an error.
 */
export class RestClient {
  private readonly cache: ResponseCache | undefined;

  /** @param parameters The API parameters, by name, for the placeholders of retry requests. */
  constructor(
    readonly configuration: RestConfiguration,
    readonly policy: RestPolicy,
    readonly parameters: Readonly<Record<string, string>> = {},
  ) {
    this.cache = policy.caches === true ? new ResponseCache() : undefined;
  }

  /**
   * Sends `request`, recovering from the failures the policy's retry rules match.
   *
   * @param readsJson Whether the operation reads a JSON body, which is then inspected for an
   *   error; other bodies are only when they are JSON.
   * @param allowEmpty Whether to return `undefined` for one of the policy's empty statuses.
   * @param stores Whether a GET may be answered from the client's {@link ResponseCache}, and
   *   leave its response there: not for downloads.
   */
  async execute(request: RestRequest, readsJson: boolean, allowEmpty = false, stores = true): Promise<Response | undefined> {
    const cache = request.method === "GET" && stores ? this.cache : undefined;
    const url = cache === undefined ? undefined : requestUrl(this.configuration, request).toString();
    const kept = url === undefined ? undefined : cache?.get(url);
    if (kept !== undefined) return kept;
    // Downloads, text and pages take whatever the server sends, which a JSON Accept would refuse.
    if (!readsJson) request = { ...request, headers: [["Accept", "*/*"], ...(request.headers ?? [])] };
    let response = await sendRequest(this.configuration, request);
    for (const rule of this.policy.retries ?? []) {
      if (response.status < 400) break;
      if (rule.status !== undefined && rule.status !== response.status) continue;
      if (rule.code !== undefined && rule.code !== code(this.policy.errors, await json(response))) continue;
      if (rule.before !== undefined) {
        await sendRequest(this.configuration, {
          method: rule.before.method,
          path: this.fill(rule.before.path),
          headers: Object.entries(rule.before.headers ?? {}).map(([name, value]) => [name, this.fill(value)] as const),
          ...(request.server === undefined ? {} : { server: request.server }),
        });
      }
      if (rule.delaySeconds !== undefined) {
        await new Promise((resolve) => setTimeout(resolve, rule.delaySeconds! * 1000));
      }
      response = await sendRequest(this.configuration, request);
    }
    // What the request changed may show in the kept responses.
    if (request.method !== "GET") this.cache?.clear();
    if (allowEmpty && (this.policy.emptyStatuses ?? []).includes(response.status)) return undefined;
    if (response.status >= 400) throw await this.failure(response);
    const detect = this.policy.errors?.detect ?? [];
    const isJson = response.headers.get("Content-Type")?.includes("json") ?? false;
    if (detect.length > 0 && (readsJson || isJson) && detects(detect, await json(response))) throw await this.failure(response);
    if (url !== undefined) await cache?.put(url, response);
    return response;
  }

  private async failure(response: Response): Promise<ApiFailure> {
    const body = await json(response);
    const errorCode = code(this.policy.errors, body);
    const serverMessage = first(this.policy.errors?.message, body);
    const message = serverMessage ?? errorCode ?? `Request failed with status ${response.status}`;
    return new ApiFailure(response.status, errorCode, message, serverMessage);
  }

  private fill(template: string): string {
    return template.replace(/\{([^}]+)}/g, (_, name: string) => {
      const value = this.parameters[name];
      if (value === undefined) throw new Error(`No API parameter '${name}'`);
      return value;
    });
  }
}

/** The body of `response` as JSON, if it is JSON; the response stays readable. */
async function json(response: Response): Promise<unknown> {
  try {
    const text = await response.clone().text();
    return text.trim() === "" ? undefined : JSON.parse(text);
  } catch {
    return undefined;
  }
}

function detects(pointers: readonly string[], body: unknown): boolean {
  return pointers.some((pointer) => {
    const value = at(pointer, body);
    return value !== undefined && value !== null && value !== false;
  });
}

function code(format: ErrorFormat | undefined, body: unknown): string | undefined {
  return first(format?.code, body);
}

/** The first string or number at one of `pointers`. */
function first(pointers: readonly string[] | undefined, body: unknown): string | undefined {
  for (const pointer of pointers ?? []) {
    const value = at(pointer, body);
    if (typeof value === "string" || typeof value === "number") return String(value);
  }
  return undefined;
}

function at(pointer: string, body: unknown): unknown {
  let current = body;
  for (const key of pointer.split("/").filter((part) => part !== "")) {
    current = asJsonObject(current)?.[key.replace(/~1/g, "/").replace(/~0/g, "~")];
  }
  return current;
}
