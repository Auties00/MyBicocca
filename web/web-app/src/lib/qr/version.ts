import { BitMatrix } from './matrix.ts';

export type ErrorLevel = 'L' | 'M' | 'Q' | 'H';

/* ISO/IEC 18004 table 9, indexed by version (index 0 unused). */
const ECC_PER_BLOCK: Record<ErrorLevel, readonly number[]> = {
	L: [
		0, 7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30,
		30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30
	],
	M: [
		0, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28,
		28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28
	],
	Q: [
		0, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30,
		30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30
	],
	H: [
		0, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30,
		30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30
	]
};

const BLOCKS: Record<ErrorLevel, readonly number[]> = {
	L: [
		0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12, 12, 13, 14,
		15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25
	],
	M: [
		0, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25,
		26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49
	],
	Q: [
		0, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34,
		34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68
	],
	H: [
		0, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37,
		40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81
	]
};

export function dimensionOf(version: number): number {
	return 17 + 4 * version;
}

/** Rows (and columns) of the alignment patterns' centres: evenly spaced from the far edge back to 6. */
export function alignmentCentres(version: number): number[] {
	if (version === 1) return [];
	const count = Math.floor(version / 7) + 2;
	const step = version === 32 ? 26 : Math.ceil((version * 4 + 4) / (count * 2 - 2)) * 2;
	const centres = [6];
	for (let at = dimensionOf(version) - 7; centres.length < count; at -= step) {
		centres.splice(1, 0, at);
	}
	return centres;
}

/** The modules left for codewords once the function patterns are drawn. */
function dataModules(version: number): number {
	let modules = (16 * version + 128) * version + 64;
	if (version >= 2) {
		const count = Math.floor(version / 7) + 2;
		modules -= (25 * count - 10) * count - 55;
		if (version >= 7) modules -= 36;
	}
	return modules;
}

/** Every codeword the symbol holds, data and error correction. */
export function codewordCount(version: number): number {
	return dataModules(version) >> 3;
}

/** The modules that aren't codewords: finders, separators, timing, alignment, format and version. */
export function functionModules(version: number): BitMatrix {
	const size = dimensionOf(version);
	const matrix = new BitMatrix(size);
	matrix.fill(0, 0, 9, 9);
	matrix.fill(size - 8, 0, 8, 9);
	matrix.fill(0, size - 8, 9, 8);
	const centres = alignmentCentres(version);
	const last = centres.length - 1;
	for (const [i, row] of centres.entries()) {
		for (const [j, column] of centres.entries()) {
			/* None where a finder pattern stands. */
			if ((i === 0 && (j === 0 || j === last)) || (i === last && j === 0)) continue;
			matrix.fill(column - 2, row - 2, 5, 5);
		}
	}
	matrix.fill(6, 9, 1, size - 17);
	matrix.fill(9, 6, size - 17, 1);
	if (version >= 7) {
		matrix.fill(size - 11, 0, 3, 6);
		matrix.fill(0, size - 11, 6, 3);
	}
	return matrix;
}

export interface Block {
	/** Data codewords, then error correction ones. */
	codewords: Uint8Array;
	dataLength: number;
}

/**
 * Splits the codewords read off the symbol into their blocks. The symbol interleaves them, a
 * codeword of each block in turn; the later blocks may hold one more data codeword.
 */
export function deinterleave(codewords: Uint8Array, version: number, level: ErrorLevel): Block[] {
	const eccLength = ECC_PER_BLOCK[level][version];
	const count = BLOCKS[level][version];
	const shortCount = count - (codewords.length % count);
	const shortLength = Math.floor(codewords.length / count);
	const shortData = shortLength - eccLength;
	const blocks = Array.from({ length: count }, (_, j) => ({
		codewords: new Uint8Array(j < shortCount ? shortLength : shortLength + 1),
		dataLength: j < shortCount ? shortData : shortData + 1
	}));
	let next = 0;
	for (let i = 0; i < shortData; i++) {
		for (const block of blocks) block.codewords[i] = codewords[next++];
	}
	for (const block of blocks.slice(shortCount)) block.codewords[shortData] = codewords[next++];
	for (let i = 0; i < eccLength; i++) {
		for (const block of blocks) block.codewords[block.dataLength + i] = codewords[next++];
	}
	return blocks;
}
