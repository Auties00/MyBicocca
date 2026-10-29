import type { BitMatrix } from './matrix.ts';
import { dimensionOf, type ErrorLevel } from './version.ts';

/** XORed onto the format bits so they're never all light. */
const FORMAT_MASK = 0x5412;
const FORMAT_GENERATOR = 0x537;
const VERSION_GENERATOR = 0x1f25;

/** A BCH code corrects up to 3 wrong bits in these. */
const MAX_WRONG_BITS = 3;

/** The remainder of `value` divided by `generator`, as polynomials over GF(2). */
function remainder(value: number, generator: number): number {
	const degree = 31 - Math.clz32(generator);
	for (let bit = 31 - Math.clz32(value); bit >= degree; bit--) {
		if (value & (1 << bit)) value ^= generator << (bit - degree);
	}
	return value;
}

/* Every valid format word, indexed by its 5 data bits. */
const FORMAT_WORDS = Array.from(
	{ length: 32 },
	(_, data) => ((data << 10) | remainder(data << 10, FORMAT_GENERATOR)) ^ FORMAT_MASK
);

/* Every valid version word, for versions 7 to 40 (index 0 is version 7). */
const VERSION_WORDS = Array.from(
	{ length: 34 },
	(_, i) => ((i + 7) << 12) | remainder((i + 7) << 12, VERSION_GENERATOR)
);

function wrongBits(a: number, b: number): number {
	let bits = a ^ b;
	let count = 0;
	for (; bits !== 0; bits &= bits - 1) count++;
	return count;
}

/** The index of the valid word nearest to any of the copies read, if near enough. */
function nearest(copies: readonly number[], words: readonly number[]): number | undefined {
	let best: number | undefined;
	let fewest = MAX_WRONG_BITS + 1;
	for (const [index, word] of words.entries()) {
		for (const copy of copies) {
			const wrong = wrongBits(copy, word);
			if (wrong < fewest) {
				fewest = wrong;
				best = index;
			}
		}
	}
	return best;
}

export interface Format {
	level: ErrorLevel;
	/** Which of the eight data masks covers the codewords. */
	mask: number;
}

const LEVELS: readonly ErrorLevel[] = ['M', 'L', 'H', 'Q'];

/** Reads the format word, written twice: around the top-left finder, and split between the other two. */
export function readFormat(matrix: BitMatrix): Format | undefined {
	const size = matrix.width;
	let first = 0;
	const read = (x: number, y: number, word: number) => (word << 1) | (matrix.get(x, y) ? 1 : 0);
	for (let x = 0; x <= 5; x++) first = read(x, 8, first);
	first = read(7, 8, first);
	first = read(8, 8, first);
	first = read(8, 7, first);
	for (let y = 5; y >= 0; y--) first = read(8, y, first);
	let second = 0;
	for (let y = size - 1; y >= size - 7; y--) second = read(8, y, second);
	for (let x = size - 8; x < size; x++) second = read(x, 8, second);
	const data = nearest([first, second], FORMAT_WORDS);
	return data === undefined ? undefined : { level: LEVELS[(data >> 3) & 3], mask: data & 7 };
}

/**
 * The symbol's version: from its size up to 6, else from the version word written beside the
 * top-right and bottom-left finders, which has to agree with the size.
 */
export function readVersion(matrix: BitMatrix): number | undefined {
	const size = matrix.width;
	const provisional = (size - 17) / 4;
	if (provisional <= 6) return provisional;
	let topRight = 0;
	let bottomLeft = 0;
	for (let i = 5; i >= 0; i--) {
		for (let j = size - 9; j >= size - 11; j--) {
			topRight = (topRight << 1) | (matrix.get(j, i) ? 1 : 0);
			bottomLeft = (bottomLeft << 1) | (matrix.get(i, j) ? 1 : 0);
		}
	}
	const index = nearest([topRight, bottomLeft], VERSION_WORDS);
	if (index === undefined) return undefined;
	const version = index + 7;
	return dimensionOf(version) === size ? version : undefined;
}

/** Whether data mask `mask` flips the module at `row`, `column`. */
export function masked(mask: number, row: number, column: number): boolean {
	switch (mask) {
		case 0:
			return (row + column) % 2 === 0;
		case 1:
			return row % 2 === 0;
		case 2:
			return column % 3 === 0;
		case 3:
			return (row + column) % 3 === 0;
		case 4:
			return (Math.floor(row / 2) + Math.floor(column / 3)) % 2 === 0;
		case 5:
			return ((row * column) % 2) + ((row * column) % 3) === 0;
		case 6:
			return (((row * column) % 2) + ((row * column) % 3)) % 2 === 0;
		default:
			return (((row + column) % 2) + ((row * column) % 3)) % 2 === 0;
	}
}
