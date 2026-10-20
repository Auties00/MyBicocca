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

/** Booleans, also when sent as `"true"`/`"false"` or `1`/`0`. */
export function decodeBoolean(json: unknown): boolean | undefined {
  if (typeof json === "boolean") return json;
  if (json === "true" || json === 1) return true;
  if (json === "false" || json === 0) return false;
  return undefined;
}

export function decodeJsonObject(json: unknown): JsonObject | undefined {
  return asJsonObject(json) as JsonObject | undefined;
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
