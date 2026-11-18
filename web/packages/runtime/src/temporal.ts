/** The components a {@link TemporalPattern} reads and writes. */
export interface TemporalFields {
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
  second: number;
}

/** A date or time type that patterns can parse into and format from. */
export interface TemporalType<T> {
  fromFields(fields: TemporalFields): T | undefined;
}

/** A value of a {@link TemporalType}. */
export interface TemporalValue {
  toFields(): TemporalFields;
}

/** A calendar date without a time zone, such as an exam day. */
export class LocalDate implements TemporalValue {
  constructor(
    readonly year: number,
    readonly month: number,
    readonly day: number,
  ) {}

  static fromFields(fields: TemporalFields): LocalDate | undefined {
    const { year, month, day } = fields;
    if (month < 1 || month > 12 || day < 1 || day > 31) return undefined;
    return new LocalDate(year, month, day);
  }

  /** Parses `text` written with `pattern` (ISO `yyyy-MM-dd` by default). */
  static parse(text: string, pattern: string | TemporalPattern = TemporalPattern.isoDate): LocalDate | undefined {
    return parseTemporal(text, LocalDate, pattern);
  }

  toFields(): TemporalFields {
    return { year: this.year, month: this.month, day: this.day, hour: 0, minute: 0, second: 0 };
  }

  equals(other: LocalDate): boolean {
    return this.year === other.year && this.month === other.month && this.day === other.day;
  }

  /** The ISO 8601 form, `yyyy-MM-dd`. */
  toString(): string {
    return TemporalPattern.isoDate.format(this.toFields());
  }

  toJSON(): string {
    return this.toString();
  }
}

/** A date and time without a time zone. */
export class LocalDateTime implements TemporalValue {
  constructor(
    readonly date: LocalDate,
    readonly hour: number,
    readonly minute: number,
    readonly second: number = 0,
  ) {}

  static fromFields(fields: TemporalFields): LocalDateTime | undefined {
    const date = LocalDate.fromFields(fields);
    const { hour, minute, second } = fields;
    if (date === undefined || hour > 23 || minute > 59 || second > 59) return undefined;
    return new LocalDateTime(date, hour, minute, second);
  }

  /** Parses `text` written with `pattern` (ISO `yyyy-MM-dd'T'HH:mm:ss` by default). */
  static parse(text: string, pattern: string | TemporalPattern = TemporalPattern.isoDateTime): LocalDateTime | undefined {
    return parseTemporal(text, LocalDateTime, pattern);
  }

  toFields(): TemporalFields {
    return { ...this.date.toFields(), hour: this.hour, minute: this.minute, second: this.second };
  }

  equals(other: LocalDateTime): boolean {
    return this.date.equals(other.date) && this.hour === other.hour && this.minute === other.minute && this.second === other.second;
  }

  /** The ISO 8601 form, `yyyy-MM-dd'T'HH:mm:ss`. */
  toString(): string {
    return TemporalPattern.isoDateTime.format(this.toFields());
  }

  toJSON(): string {
    return this.toString();
  }
}

type Field = keyof TemporalFields;

type Token = { kind: "number"; field: Field; width: number } | { kind: "literal"; text: string };

const FIELD_LETTERS: Record<string, Field> = {
  y: "year",
  M: "month",
  d: "day",
  H: "hour",
  m: "minute",
  s: "second",
};

/**
 * A date/time pattern with the Unicode letters `yyyy`, `MM`, `dd`, `HH`, `mm` and `ss`; any
 * other character, or text in single quotes, is taken literally (`dd/MM/yyyy HH:mm:ss`).
 */
export class TemporalPattern {
  static readonly isoDate = new TemporalPattern("yyyy-MM-dd");
  static readonly isoDateTime = new TemporalPattern("yyyy-MM-dd'T'HH:mm:ss");

  private static readonly cache = new Map<string, TemporalPattern>();

  private readonly tokens: Token[] = [];

  constructor(readonly pattern: string) {
    let index = 0;
    while (index < pattern.length) {
      const char = pattern[index]!;
      if (char === "'") {
        const end = pattern.indexOf("'", index + 1);
        const stop = end < 0 ? pattern.length : end;
        this.tokens.push({ kind: "literal", text: pattern.slice(index + 1, stop) });
        index = stop + 1;
        continue;
      }
      const field = FIELD_LETTERS[char];
      if (field === undefined) {
        this.tokens.push({ kind: "literal", text: char });
        index++;
        continue;
      }
      let end = index;
      while (pattern[end] === char) end++;
      this.tokens.push({ kind: "number", field, width: end - index });
      index = end;
    }
  }

  /** The pattern for `pattern`, parsed once. */
  static of(pattern: string | TemporalPattern): TemporalPattern {
    if (pattern instanceof TemporalPattern) return pattern;
    let parsed = TemporalPattern.cache.get(pattern);
    if (parsed === undefined) {
      parsed = new TemporalPattern(pattern);
      TemporalPattern.cache.set(pattern, parsed);
    }
    return parsed;
  }

  parse(text: string): TemporalFields | undefined {
    const fields: TemporalFields = { year: 1970, month: 1, day: 1, hour: 0, minute: 0, second: 0 };
    let position = 0;
    for (const token of this.tokens) {
      if (token.kind === "literal") {
        if (!text.startsWith(token.text, position)) return undefined;
        position += token.text.length;
        continue;
      }
      // A single letter accepts one or two digits; longer runs set the maximum width.
      const digits = /^\d+/.exec(text.slice(position, position + Math.max(token.width, 2)))?.[0];
      if (digits === undefined) return undefined;
      fields[token.field] = Number(digits);
      position += digits.length;
    }
    return position === text.length ? fields : undefined;
  }

  format(fields: TemporalFields): string {
    return this.tokens
      .map((token) => (token.kind === "literal" ? token.text : String(fields[token.field]).padStart(token.width, "0")))
      .join("");
  }
}

function parseTemporal<T>(text: string, type: TemporalType<T>, pattern: string | TemporalPattern): T | undefined {
  const fields = TemporalPattern.of(pattern).parse(text);
  return fields === undefined ? undefined : type.fromFields(fields);
}

/**
 * Decodes a date or time written with `pattern`; `undefined` when `json` isn't a matching
 * string, and for blank strings (or the literal `null`) when `blankAsNull`.
 */
export function decodeTemporal<T>(json: unknown, type: TemporalType<T>, pattern: string, blankAsNull: boolean): T | undefined {
  if (typeof json !== "string") return undefined;
  if (blankAsNull && (json.trim() === "" || json === "null")) return undefined;
  return parseTemporal(json, type, pattern);
}

/** Encodes a date or time with `pattern`; no value is written as an empty string. */
export function encodeTemporal(value: TemporalValue | null, pattern: string): string {
  return value === null ? "" : TemporalPattern.of(pattern).format(value.toFields());
}

/** Encodes a date or time in its ISO 8601 form. */
export function encodeIsoTemporal(value: LocalDate | LocalDateTime): string {
  return value.toString();
}

export function decodeLocalDate(json: unknown): LocalDate | undefined {
  return typeof json === "string" ? LocalDate.parse(json) : undefined;
}

export function decodeLocalDateTime(json: unknown): LocalDateTime | undefined {
  return typeof json === "string" ? LocalDateTime.parse(json) : undefined;
}
