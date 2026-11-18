class BitReader {
	readonly #bytes: Uint8Array;
	#offset = 0;

	constructor(bytes: Uint8Array) {
		this.#bytes = bytes;
	}

	get remaining(): number {
		return this.#bytes.length * 8 - this.#offset;
	}

	/** The next `count` bits as a number, most significant first; undefined past the end. */
	read(count: number): number | undefined {
		if (count > this.remaining) return undefined;
		let value = 0;
		for (let i = 0; i < count; i++, this.#offset++) {
			const bit = (this.#bytes[this.#offset >> 3] >> (7 - (this.#offset & 7))) & 1;
			value = value * 2 + bit;
		}
		return value;
	}
}

const ALPHANUMERIC = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:';

/* Mode indicators, the 4 bits opening each segment. */
const END = 0b0000;
const NUMERIC = 0b0001;
const ALPHANUMERIC_MODE = 0b0010;
const BYTE = 0b0100;
const ECI = 0b0111;
const KANJI = 0b1000;

/** Bits of each mode's character count, for versions 1–9, 10–26 and 27–40. */
const COUNT_BITS = new Map<number, readonly number[]>([
	[NUMERIC, [10, 12, 14]],
	[ALPHANUMERIC_MODE, [9, 11, 13]],
	[BYTE, [8, 16, 16]],
	[KANJI, [8, 10, 12]]
]);

/** ECI assignment numbers of the character sets a byte segment may declare. */
const ECI_CHARSETS: Record<number, string> = {
	1: 'iso-8859-1',
	3: 'iso-8859-1',
	20: 'shift_jis',
	26: 'utf-8'
};

/**
 * Bytes in the declared character set, else UTF-8 (which every link and most encoders use),
 * else ISO 8859-1, the standard's default.
 */
function text(bytes: Uint8Array, charset: string | undefined): string {
	if (charset) return new TextDecoder(charset).decode(bytes);
	try {
		return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
	} catch {
		return new TextDecoder('iso-8859-1').decode(bytes);
	}
}

function digits(value: number | undefined, length: number): string | undefined {
	return value === undefined || value >= 10 ** length
		? undefined
		: String(value).padStart(length, '0');
}

/** A segment's characters, or undefined when its bits run out or break the mode's rules. */
function segment(reader: BitReader, mode: number, count: number, charset?: string) {
	let out = '';
	switch (mode) {
		case NUMERIC: {
			for (; count >= 3; count -= 3) {
				const group = digits(reader.read(10), 3);
				if (group === undefined) return undefined;
				out += group;
			}
			if (count > 0) {
				const group = digits(reader.read(count === 2 ? 7 : 4), count);
				if (group === undefined) return undefined;
				out += group;
			}
			return out;
		}
		case ALPHANUMERIC_MODE: {
			for (; count >= 2; count -= 2) {
				const pair = reader.read(11);
				if (pair === undefined || pair >= 45 * 45) return undefined;
				out += ALPHANUMERIC[Math.floor(pair / 45)] + ALPHANUMERIC[pair % 45];
			}
			if (count === 1) {
				const single = reader.read(6);
				if (single === undefined || single >= 45) return undefined;
				out += ALPHANUMERIC[single];
			}
			return out;
		}
		case BYTE: {
			const bytes = new Uint8Array(count);
			for (let i = 0; i < count; i++) {
				const byte = reader.read(8);
				if (byte === undefined) return undefined;
				bytes[i] = byte;
			}
			return text(bytes, charset);
		}
		default: {
			/* Each kanji is 13 bits: a Shift JIS pair with its lead byte folded in. */
			const bytes = new Uint8Array(2 * count);
			for (let i = 0; i < count; i++) {
				const value = reader.read(13);
				if (value === undefined) return undefined;
				let pair = (Math.floor(value / 0xc0) << 8) | (value % 0xc0);
				pair += pair < 0x1f00 ? 0x8140 : 0xc140;
				bytes[2 * i] = pair >> 8;
				bytes[2 * i + 1] = pair & 0xff;
			}
			return new TextDecoder('shift_jis').decode(bytes);
		}
	}
}

/** An ECI designator: 1, 2 or 3 bytes, told apart by the first one's leading bits. */
function eci(reader: BitReader): number | undefined {
	const first = reader.read(8);
	if (first === undefined) return undefined;
	if ((first & 0x80) === 0) return first;
	if ((first & 0xc0) === 0x80) {
		const next = reader.read(8);
		return next === undefined ? undefined : ((first & 0x3f) << 8) | next;
	}
	if ((first & 0xe0) === 0xc0) {
		const next = reader.read(16);
		return next === undefined ? undefined : ((first & 0x1f) << 16) | next;
	}
	return undefined;
}

/** The text the data codewords spell: segments of a mode and a count each, until the terminator. */
export function decodeSegments(data: Uint8Array, version: number): string | undefined {
	const reader = new BitReader(data);
	const size = version <= 9 ? 0 : version <= 26 ? 1 : 2;
	let out = '';
	let charset: string | undefined;
	while (reader.remaining >= 4) {
		const mode = reader.read(4);
		if (mode === END) break;
		if (mode === ECI) {
			const assignment = eci(reader);
			if (assignment === undefined) return undefined;
			charset = ECI_CHARSETS[assignment];
			continue;
		}
		const countBits = mode === undefined ? undefined : COUNT_BITS.get(mode);
		if (mode === undefined || countBits === undefined) return undefined;
		const count = reader.read(countBits[size]);
		if (count === undefined) return undefined;
		const part = segment(reader, mode, count, charset);
		if (part === undefined) return undefined;
		out += part;
	}
	return out;
}
