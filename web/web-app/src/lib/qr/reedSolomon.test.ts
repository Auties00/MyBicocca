import { describe, expect, it } from 'vitest';
import { correctErrors } from './reedSolomon.ts';

/* ISO/IEC 18004 annex I: "01234567" as version 1-M, 16 data codewords then 10 for correction. */
const BLOCK = [
	16, 32, 12, 86, 97, 128, 236, 17, 236, 17, 236, 17, 236, 17, 236, 17, 165, 36, 212, 193, 237, 54,
	199, 135, 44, 85
];

function damaged(positions: number[]): Uint8Array {
	const block = Uint8Array.from(BLOCK);
	for (const [i, position] of positions.entries()) block[position] ^= 0x5a + 17 * i;
	return block;
}

describe('correctErrors', () => {
	it('leaves an intact block as it is', () => {
		const block = Uint8Array.from(BLOCK);
		expect(correctErrors(block, 10)).toBe(true);
		expect(Array.from(block)).toEqual(BLOCK);
	});

	it.each([[[0]], [[25]], [[3, 17]], [[0, 9, 18]], [[1, 2, 3, 4, 5]], [[0, 7, 15, 16, 25]]])(
		'repairs up to five wrong codewords (%j) with ten for correction',
		(positions) => {
			const block = damaged(positions);
			expect(correctErrors(block, 10)).toBe(true);
			expect(Array.from(block)).toEqual(BLOCK);
		}
	);

	it.each([[[0, 1, 2, 3, 4, 5]], [[2, 6, 10, 14, 19, 23, 24]]])(
		'gives up on more (%j) rather than inventing a block',
		(positions) => {
			expect(correctErrors(damaged(positions), 10)).toBe(false);
		}
	);
});
