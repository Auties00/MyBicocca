/**
 * Lenient JSON decoders. Each returns `undefined` when the value is missing or unusable, so
 * generated code can fall back to a field's default with `??`, the way lenient servers need.
 */

export type JsonValue = null | boolean | number | string | JsonValue[] | JsonObject;

export interface JsonObject {
  [key: string]: JsonValue;
}

/** A response that doesn't have the shape the API declares. */
export class DecodeError extends Error {
  override readonly name = "DecodeError";
}

/** `json` as an object to read fields from, or `undefined` when it isn't one. */
export function asJsonObject(json: unknown): Readonly<Record<string, unknown>> | undefined {
  return typeof json === "object" && json !== null && !Array.isArray(json) ? (json as Record<string, unknown>) : undefined;
}

/** Strings, plus numbers and booleans in their string form. */
export function decodeString(json: unknown): string | undefined {
  if (typeof json === "string") return json;
  if (typeof json === "number" || typeof json === "boolean") return String(json);
  return undefined;
}

/** Integers, also when sent as strings. */
export function decodeInteger(json: unknown): number | undefined {
  const value = decodeNumber(json);
  return value !== undefined && Number.isInteger(value) ? value : undefined;
}

/** Numbers, also when sent as strings. */
export function decodeNumber(json: unknown): number | undefined {
  if (typeof json === "number") return json;
  if (typeof json === "string" && json.trim() !== "") {
    const value = Number(json);
    return Number.isNaN(value) ? undefined : value;
  }
  return undefined;
}

/** Booleans, also when sent as `"true"`/`"false"`, `1`/`0` or `"1"`/`"0"`. */
export function decodeBoolean(json: unknown): boolean | undefined {
  if (typeof json === "boolean") return json;
  if (json === "true" || json === 1 || json === "1") return true;
  if (json === "false" || json === 0 || json === "0") return false;
  return undefined;
}

/** Strings where blank ones, and any of `alsoNull`, mean no value (`null`). */
export function decodeBlankAsNull(json: unknown, alsoNull: readonly string[] = []): string | null | undefined {
  const text = decodeString(json);
  if (text === undefined) return undefined;
  return text.trim() === "" || alsoNull.includes(text) ? null : text;
}

/**
 * `json` as an object to read fields from, also merging an array of single-key objects into
 * one, as some servers send objects and maps; `undefined` for anything else.
 */
export function foldObjects(json: unknown): Readonly<Record<string, unknown>> | undefined {
  if (!Array.isArray(json)) return asJsonObject(json);
  const merged: Record<string, unknown> = {};
  for (const item of json) Object.assign(merged, asJsonObject(item));
  return merged;
}

/** A value decoded by `decode`, also from an array of single-key objects, merged. */
export function decodeFolded<T>(json: unknown, decode: (json: unknown) => T | undefined): T | undefined {
  return decode(foldObjects(json));
}

/** A value decoded by `decode`, also from a JSON document inside a string; blank strings are no value. */
export function decodeEmbedded<T>(json: unknown, decode: (json: unknown) => T | undefined): T | null | undefined {
  if (typeof json !== "string") return decode(json);
  if (json.trim() === "") return null;
  try {
    return decode(JSON.parse(json));
  } catch {
    return undefined;
  }
}

export function decodeJsonObject(json: unknown): JsonObject | undefined {
  return asJsonObject(json) as JsonObject | undefined;
}

/** Any JSON value; `undefined` only when it is missing. */
export function decodeJsonValue(json: unknown): JsonValue | undefined {
  return json === undefined ? undefined : (json as JsonValue);
}

/** The value nested in the objects named by `envelope`, outermost first. */
export function unwrapEnvelope(json: unknown, envelope: readonly string[]): unknown {
  let value = json;
  for (const key of envelope) {
    const object = asJsonObject(value);
    if (object === undefined) throw new DecodeError(`Expected an object containing "${key}"`);
    value = object[key];
  }
  return value;
}

/** A list whose every item decodes; `undefined` when `json` isn't a list or an item fails. */
export function decodeList<T>(json: unknown, decodeItem: (json: unknown) => T | undefined): T[] | undefined {
  if (!Array.isArray(json)) return undefined;
  const items: T[] = [];
  for (const item of json) {
    const decoded = decodeItem(item);
    if (decoded === undefined) return undefined;
    items.push(decoded);
  }
  return items;
}

/**
 * A map with string keys, whose every value decodes; `undefined` otherwise. Also reads PHP's
 * `[]` for an empty map and an array of single-key objects, merged.
 */
export function decodeRecordMap<V>(json: unknown, decodeValue: (json: unknown) => V | undefined): Record<string, V> | undefined {
  const object = foldObjects(json);
  if (object === undefined) return undefined;
  const map: Record<string, V> = {};
  for (const [key, item] of Object.entries(object)) {
    const value = decodeValue(item);
    if (value === undefined) return undefined;
    map[key] = value;
  }
  return map;
}

/**
 * A map whose keys aren't strings, such as dates. Keys are compared by their string form, so
 * `get` finds a key equal to the one stored rather than the same object.
 */
export class WireMap<K, V> implements Iterable<[K, V]> {
  private readonly byText = new Map<string, [K, V]>();

  /** @param keyText The string form of a key, as written on the wire. */
  constructor(
    readonly keyText: (key: K) => string,
    entries: Iterable<readonly [K, V]> = [],
  ) {
    for (const [key, value] of entries) this.set(key, value);
  }

  get size(): number {
    return this.byText.size;
  }

  get(key: K): V | undefined {
    return this.byText.get(this.keyText(key))?.[1];
  }

  has(key: K): boolean {
    return this.byText.has(this.keyText(key));
  }

  set(key: K, value: V): this {
    this.byText.set(this.keyText(key), [key, value]);
    return this;
  }

  keys(): IterableIterator<K> {
    return Array.from(this.byText.values(), ([key]) => key)[Symbol.iterator]();
  }

  values(): IterableIterator<V> {
    return Array.from(this.byText.values(), ([, value]) => value)[Symbol.iterator]();
  }

  entries(): IterableIterator<[K, V]> {
    return this.byText.values();
  }

  [Symbol.iterator](): IterableIterator<[K, V]> {
    return this.entries();
  }
}

/** Like {@link decodeRecordMap}, for keys that `decodeKey` reads from their string form. */
export function decodeWireMap<K, V>(
  json: unknown,
  decodeKey: (json: unknown) => K | undefined,
  keyText: (key: K) => string,
  decodeValue: (json: unknown) => V | undefined,
): WireMap<K, V> | undefined {
  const object = foldObjects(json);
  if (object === undefined) return undefined;
  const map = new WireMap<K, V>(keyText);
  for (const [text, item] of Object.entries(object)) {
    const key = decodeKey(text);
    const value = decodeValue(item);
    if (key === undefined || value === undefined) return undefined;
    map.set(key, value);
  }
  return map;
}

/** A {@link WireMap} as a JSON object, its values written by `encodeValue`. */
export function encodeWireMap<K, V>(map: WireMap<K, V>, encodeValue: (value: V) => JsonValue): JsonObject {
  const object: JsonObject = {};
  for (const [key, value] of map) object[map.keyText(key)] = encodeValue(value);
  return object;
}

/** `value`, or a {@link DecodeError} naming `path` when it is missing. */
export function required<T>(value: T | undefined, path: string): T {
  if (value === undefined) throw new DecodeError(`Missing or invalid value for ${path}`);
  return value;
}

/**
 * Decodes one of `values`. An open enum also accepts other strings; a value may arrive
 * wrapped as `{"value": "…"}` when `acceptsWrappedValue`.
 */
export function decodeWireEnum<T extends string>(
  json: unknown,
  values: readonly T[],
  options: { open: boolean; acceptsWrappedValue: boolean },
): T | undefined {
  const wrapped = options.acceptsWrappedValue ? asJsonObject(json) : undefined;
  const raw = wrapped !== undefined ? (decodeString(wrapped["value"]) ?? "") : decodeString(json);
  if (raw === undefined) return undefined;
  return options.open || (values as readonly string[]).includes(raw) ? (raw as T) : undefined;
}
