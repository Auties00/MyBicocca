import { test } from "vitest";
import { ApiFailure, ScrapeError } from "__RUNTIME__";
import { LiveFingerprint, LiveJournal, LiveReport, LiveShapes, type Observed } from "./support.js";

export { LiveFingerprint } from "./support.js";

/**
 * How live tests run, from the environment:
 *
 * | Variable          | Meaning                                                                  |
 * |-------------------|--------------------------------------------------------------------------|
 * | `LIVE_TESTS`      | `1` to run them; otherwise every case is skipped                         |
 * | `LIVE_WRITES`     | `1` to run the reversible cases, which change data and undo it           |
 * | `LIVE_SEED`       | Which values cases pick; by default it changes every day                 |
 * | `LIVE_REQUIRED`   | `1` to fail, rather than skip, the cases of an API whose account is missing |
 * | `LIVE_REPORT_DIR` | Where the report goes (default `test-results/live`)                      |
 * | `LIVE_JOURNAL`    | The journal of changes not undone yet (default `test-results/live-journal.json`) |
 * | `LIVE_FINGERPRINT_TEXT` | `1` to also report the canonical form of results, values included, to compare platforms locally |
 */
export const LiveSettings = {
  get enabled(): boolean {
    return env("LIVE_TESTS") === "1";
  },
  get writes(): boolean {
    return env("LIVE_WRITES") === "1";
  },
  get required(): boolean {
    return env("LIVE_REQUIRED") === "1";
  },
  seed: Number(env("LIVE_SEED") ?? Math.floor(Date.now() / 86_400_000)),
  get reportDir(): string {
    return env("LIVE_REPORT_DIR") ?? "test-results/live";
  },
  get journal(): string {
    return env("LIVE_JOURNAL") ?? "test-results/live-journal.json";
  },
  platform: "typescript",
};

export function env(name: string): string | undefined {
  const value = process.env[name];
  return value === undefined || value === "" ? undefined : value;
}

/** Thrown to skip a case: a value it needs doesn't exist, the request budget is used up, writes are off. */
export class LiveSkip extends Error {
  override readonly name = "LiveSkip";
}

/** Thrown to end a case that passed early: the server answered with an error the case allows. */
export class LiveEnd extends Error {
  override readonly name = "LiveEnd";
}

/** The live tests of one API, as its generated `<Prefix>Live` describes them. */
export interface LiveApi<A> {
  readonly id: string;
  /** The environment variables the account needs. */
  readonly env: readonly string[];
  readonly requestsPerSecond: number;
  readonly maxRequests: number;
  /** What the API's responses look like (see the generator's `Shapes`). */
  readonly shapes: string;
  /** The operations no case calls, with the reason. */
  readonly skips: Readonly<Record<string, string>>;
  /** The API, sending its requests with `fetch`; `anonymous` leaves out the account's headers. */
  create(fetch: typeof globalThis.fetch, anonymous: boolean): A;
  /** Undoes the change of the write case `caseId` from the values its journal entry saved; false when it can't. */
  replay?(live: LiveCase<A>, caseId: string, values: Readonly<Record<string, string>>): Promise<boolean>;
}

/** The state of an API's live tests during a run: its clients, sources, budget and report. */
export class LiveRun<A> {
  readonly report: LiveReport;
  readonly checker: LiveShapes;
  private readonly sources = new Map<string, Promise<unknown>>();
  private requests = 0;
  private lastRequest = 0;
  /** Where the responses of the current call go. */
  capture: Observed[] | undefined;
  journalReplayed = false;
  private cachedApi: A | undefined;
  private cachedAnonymous: A | undefined;

  constructor(readonly live: LiveApi<A>) {
    this.report = new LiveReport(live.id);
    this.checker = new LiveShapes(live.shapes, this.report);
    this.report.start(live.skips);
  }

  get api(): A {
    return (this.cachedApi ??= this.live.create(this.fetcher(), false));
  }

  get anonymous(): A {
    return (this.cachedAnonymous ??= this.live.create(this.fetcher(), true));
  }

  get missingEnv(): string[] {
    return this.live.env.filter((name) => env(name) === undefined);
  }

  async source<T>(id: string, compute: () => Promise<T>): Promise<T> {
    let result = this.sources.get(id);
    if (result === undefined) {
      result = compute();
      this.sources.set(id, result);
    }
    try {
      return (await result) as T;
    } catch (error) {
      throw new LiveSkip(`the call providing its values (${id}) failed: ${message(error)}`);
    }
  }

  /** A `fetch` that throttles requests, keeps cookies as a browser would and records responses. */
  private fetcher(): typeof globalThis.fetch {
    const cookies = new Map<string, Map<string, string>>();
    return async (input, init) => {
      if (this.requests >= this.live.maxRequests) throw new LiveSkip(`the run used up its budget of ${this.live.maxRequests} requests to ${this.live.id}`);
      const interval = 1000 / this.live.requestsPerSecond;
      const wait = this.lastRequest + interval - Date.now();
      if (wait > 0) await new Promise((resolve) => setTimeout(resolve, wait));
      this.lastRequest = Date.now();
      this.requests++;

      const url = new URL(input instanceof Request ? input.url : input.toString());
      const headers = new Headers(init?.headers);
      const jar = cookies.get(url.host);
      if (jar !== undefined && jar.size > 0) {
        const stored = Array.from(jar, ([name, value]) => `${name}=${value}`).join("; ");
        headers.set("Cookie", headers.has("Cookie") ? `${headers.get("Cookie")}; ${stored}` : stored);
      }
      const response = await fetch(input, { ...init, headers });
      for (const cookie of response.headers.getSetCookie()) {
        const [pair] = cookie.split(";");
        const separator = pair!.indexOf("=");
        if (separator > 0) {
          const host = cookies.get(url.host) ?? new Map<string, string>();
          host.set(pair!.slice(0, separator).trim(), pair!.slice(separator + 1).trim());
          cookies.set(url.host, host);
        }
      }
      const type = response.headers.get("Content-Type");
      const textual = type === null || ["json", "text", "javascript", "html", "xml"].some((kind) => type.includes(kind));
      this.capture?.push({ status: response.status, contentType: type, body: textual ? await response.clone().text() : undefined });
      return response;
    };
  }

  /** Undoes the changes earlier runs left in the journal, as far as they can be. */
  async replayJournal(): Promise<void> {
    for (const entry of LiveJournal.entries(this.live.id)) {
      const live = new LiveCase(this, entry.case, 0);
      let replayed = false;
      let failure: string | undefined;
      try {
        replayed = (await this.live.replay?.(live, entry.case, entry.values)) ?? false;
      } catch (error) {
        failure = message(error);
      }
      if (replayed) {
        LiveJournal.remove(entry.api, entry.case);
        this.report.event("journal", entry.case, "undone a change an earlier run left");
      } else {
        this.report.event("journal", entry.case, `a change an earlier run left is still there: ${failure ?? "it can't be undone automatically"}`);
      }
    }
  }
}

const runs = new Map<string, LiveRun<unknown>>();

function runOf<A>(live: LiveApi<A>): LiveRun<A> {
  let run = runs.get(live.id);
  if (run === undefined) {
    run = new LiveRun(live as LiveApi<unknown>);
    runs.set(live.id, run);
  }
  return run as LiveRun<A>;
}

/** Options of a call: what its response looks like, and the errors that end the case early. */
export interface CallOptions {
  readonly envelope?: readonly string[];
  readonly variable?: string;
  readonly errors?: readonly string[];
}

/**
 * What a case's code runs in: the API, the values it picks, its calls and their checks. `index`
 * is the case's number among its operation's combinations, which with the run's seed picks the
 * values of {@link choose}.
 */
export class LiveCase<A> {
  fingerprinted: string | undefined;

  constructor(
    readonly run: LiveRun<A>,
    readonly id: string,
    readonly index: number,
  ) {}

  get api(): A {
    return this.run.api;
  }

  get anonymousApi(): A {
    return this.run.anonymous;
  }

  /** One of `candidates`, which differs between cases and runs; skips the case when there is none. */
  choose<T>(name: string, candidates: readonly (T | null | undefined)[]): T {
    const values = distinct(candidates.filter((value): value is T => value !== null && value !== undefined && !Number.isNaN(value)));
    if (values.length === 0) throw new LiveSkip(`no value for ${name}`);
    const index = (((this.index + LiveSettings.seed) % values.length) + values.length) % values.length;
    return values[index]!;
  }

  /** The first of `candidates`; fails the case when there is none. */
  first<T>(name: string, candidates: readonly (T | null | undefined)[]): T {
    const value = candidates.find((candidate) => candidate !== null && candidate !== undefined);
    if (value === undefined || value === null) throw new Error(`${name} has no value`);
    return value;
  }

  resolve(template: string): string {
    return LiveTemplates.resolve(template);
  }

  /** `json` decoded with `decode`, which a value from `tests.yaml` must fit. */
  decode<T>(decode: (json: unknown) => T | undefined, json: string): T {
    const value = decode(JSON.parse(json));
    if (value === undefined) throw new Error(`tests.yaml gives a value that doesn't decode: ${json}`);
    return value;
  }

  /** The result of a call other calls take values from, made once per run. */
  source<T>(id: string, shape: string | undefined, options: CallOptions, compute: (live: LiveCase<A>) => Promise<T>): Promise<T> {
    return this.run.source(id, () => {
      const inner = new LiveCase(this.run, id, 0);
      return inner.call(shape, options, () => compute(inner));
    });
  }

  /**
   * Makes the call of `block`, then checks its JSON response against `shape`: a field the spec
   * doesn't know, a value of another type, an enum value it doesn't list. When the server answers
   * with one of `errors` (statuses or error codes), the case ends, passing.
   */
  async call<T>(shape: string | undefined, options: CallOptions, block: () => Promise<T>): Promise<T> {
    const previous = this.run.capture;
    const observed: Observed[] = [];
    this.run.capture = observed;
    try {
      const result = await block();
      const response = observed.at(-1);
      if (shape !== undefined && response !== undefined) this.run.checker.check(shape, response, options.envelope ?? [], options.variable);
      return result;
    } catch (error) {
      const failure = apiFailure(error);
      if (failure !== undefined && (options.errors ?? []).some((allowed) => allowed === String(failure.status) || allowed === failure.code)) {
        this.run.report.event("expected-error", this.id, `${failure.status} ${failure.code ?? ""}`.trim());
        throw new LiveEnd();
      }
      throw error;
    } finally {
      this.run.capture = previous;
    }
  }

  /**
   * Makes a call the server must refuse with the API's error: with an id it doesn't know
   * (`acceptsEmpty`, which also accepts an empty result) or invalid credentials.
   */
  async refused(acceptsEmpty: boolean, block: () => Promise<unknown>): Promise<void> {
    let result: unknown;
    try {
      result = await this.call(undefined, {}, block);
    } catch (error) {
      if (apiFailure(error) !== undefined) return;
      if (acceptsEmpty && error instanceof ScrapeError) return;
      throw error;
    }
    if (acceptsEmpty) {
      if (!isEmpty(result)) this.run.report.finding("accepts-unknown-id", this.id, "", "answered with data");
      return;
    }
    throw new Error(`The server accepted the call: ${describe(result)}`);
  }

  /** Records the canonical form of the case's result, which every platform must agree on. */
  fingerprint(write: (fingerprint: LiveFingerprint) => void): void {
    const fingerprint = new LiveFingerprint();
    write(fingerprint);
    this.fingerprinted = fingerprint.hash();
    // The canonical text holds the result's values, so only a local run asking for it gets it.
    if (env("LIVE_FINGERPRINT_TEXT") === "1") this.run.report.event("fingerprint-text", this.id, fingerprint.text());
  }

  expectNonEmpty(path: string, values: readonly unknown[]): void {
    if (!values.some((value) => value !== null && value !== undefined)) throw new Error(`Expected a value at '${path}'`);
  }

  expectContains<T>(path: string, values: readonly T[], expected: T): void {
    if (!values.some((value) => same(value, expected))) throw new Error(`Expected '${path}' to contain the expected value, among ${values.length}`);
  }

  expectNotContains<T>(path: string, values: readonly T[], expected: T): void {
    if (values.some((value) => same(value, expected))) throw new Error(`Expected '${path}' to no longer contain the value`);
  }

  expectEquals<T>(path: string, values: readonly T[], expected: T): void {
    if (values.length === 0 || values.some((value) => !same(value, expected))) throw new Error(`Expected every value at '${path}' to be the expected one`);
  }

  expectMatches(path: string, values: readonly unknown[], regex: string): void {
    const pattern = new RegExp(regex);
    if (values.length === 0 || values.some((value) => value === null || value === undefined || !pattern.test(String(value)))) {
      throw new Error(`Expected every value at '${path}' to match /${regex}/`);
    }
  }

  expectCount(path: string, values: readonly unknown[], min: number | null, max: number | null): void {
    const count = values.length;
    if ((min !== null && count < min) || (max !== null && count > max)) throw new Error(`Expected between ${min ?? 0} and ${max ?? "any"} values at '${path}', got ${count}`);
  }

  /** Notes in the journal a change to undo, with the values its undo needs, so that a later run can undo it if this one can't. */
  journal(values: Readonly<Record<string, string>>): void {
    LiveJournal.add({ api: this.run.live.id, case: this.id, values });
  }

  unjournal(): void {
    LiveJournal.remove(this.run.live.id, this.id);
  }
}

/**
 * Registers a case with vitest: it runs with the API's {@link LiveRun}, is skipped when live tests
 * are off, the account is missing, or it changes data and writes are off, and its outcome goes to
 * the report.
 */
export function liveCase<A>(live: LiveApi<A>, id: string, options: { effect?: string; index?: number }, body: (live: LiveCase<A>) => Promise<void>): void {
  test(id, async (context) => {
    if (!LiveSettings.enabled) return context.skip("Live tests run with LIVE_TESTS=1");
    const run = runOf(live);
    const missing = run.missingEnv;
    if (missing.length > 0) {
      const text = `Set ${missing.join(", ")} to run the live tests of ${live.id}`;
      if (LiveSettings.required) throw new Error(text);
      return context.skip(text);
    }
    if (options.effect === "reversible" && !LiveSettings.writes) return context.skip("Writes are off; set LIVE_WRITES=1");
    const live_ = new LiveCase(run, id, options.index ?? 0);
    const start = Date.now();
    try {
      if (LiveSettings.writes && !run.journalReplayed) {
        run.journalReplayed = true;
        await run.replayJournal();
      }
      await body(live_);
      run.report.case(id, "passed", undefined, live_.fingerprinted, Date.now() - start);
    } catch (error) {
      if (error instanceof LiveEnd) {
        run.report.case(id, "passed", undefined, undefined, Date.now() - start);
        return;
      }
      if (error instanceof LiveSkip) {
        run.report.case(id, "skipped", error.message, undefined, Date.now() - start);
        return context.skip(error.message);
      }
      run.report.case(id, "failed", `${error instanceof Error ? error.name : "Error"}: ${message(error)}`, undefined, Date.now() - start);
      throw error;
    }
  }, 300_000);
}

/**
 * The placeholders of text values in `tests.yaml`: `{env:NAME}`, `{today}`, `{today+N}`,
 * `{today-N}` (ISO dates, or in the pattern after a colon: `{today:yyyy}`) and `{base64:…}`,
 * whose content may hold placeholders too.
 */
export const LiveTemplates = {
  resolve(template: string): string {
    return parse(template, 0, false)[0];
  },
};

function parse(text: string, start: number, nested: boolean): [string, number] {
  let out = "";
  let index = start;
  while (index < text.length) {
    const char = text[index]!;
    if (char === "{") {
      const [value, next] = placeholder(text, index + 1);
      out += value;
      index = next;
    } else if (char === "}") {
      if (nested) return [out, index + 1];
      throw new Error(`Unbalanced '}' in '${text}'`);
    } else {
      out += char;
      index++;
    }
  }
  return [out, index];
}

function placeholder(text: string, start: number): [string, number] {
  if (text.startsWith("base64:", start)) {
    const [inner, next] = parse(text, start + "base64:".length, true);
    return [Buffer.from(inner, "utf8").toString("base64"), next];
  }
  const end = text.indexOf("}", start);
  const content = text.slice(start, end);
  if (content.startsWith("env:")) return [env(content.slice(4)) ?? "", end + 1];
  const today = /^today(?:([+-])(\d+))?(?::(.+))?$/.exec(content);
  if (today === null) throw new Error(`Unknown placeholder '{${content}}'`);
  const days = Number(today[2] ?? 0) * (today[1] === "-" ? -1 : 1);
  const now = new Date();
  const date = new Date(now.getFullYear(), now.getMonth(), now.getDate() + days);
  const pad = (value: number, width: number) => String(value).padStart(width, "0");
  const fields: Record<string, string> = { yyyy: pad(date.getFullYear(), 4), MM: pad(date.getMonth() + 1, 2), dd: pad(date.getDate(), 2) };
  const pattern = today[3] ?? "yyyy-MM-dd";
  return [pattern.replace(/yyyy|MM|dd/g, (letters) => fields[letters]!), end + 1];
}

/** Whether `value` is present, for filtering the values of a path. */
export function isPresent<T>(value: T): value is NonNullable<T> {
  return value !== null && value !== undefined && !(typeof value === "number" && Number.isNaN(value));
}

function apiFailure(error: unknown): { status: number; code: string | undefined } | undefined {
  if (error instanceof ApiFailure) return error;
  const cause = (error as { failure?: unknown } | null)?.failure;
  if (cause instanceof ApiFailure) return cause;
  const status = (error as { status?: unknown } | null)?.status;
  return typeof status === "number" ? { status, code: (error as { code?: string }).code } : undefined;
}

function distinct<T>(values: readonly T[]): T[] {
  const seen = new Set<string>();
  return values.filter((value) => {
    const key = typeof value === "object" ? JSON.stringify(value) : `${typeof value}:${String(value)}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function same(a: unknown, b: unknown): boolean {
  if (a === b) return true;
  if (typeof a === "object" && a !== null && typeof b === "object" && b !== null) return JSON.stringify(a) === JSON.stringify(b);
  return false;
}

function isEmpty(value: unknown): boolean {
  if (value === undefined || value === null) return true;
  if (Array.isArray(value)) return value.length === 0;
  if (value instanceof Map) return value.size === 0;
  return false;
}

function describe(value: unknown): string {
  if (Array.isArray(value)) return `${value.length} items`;
  return value === undefined || value === null ? "no content" : `a ${typeof value}`;
}

function message(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
