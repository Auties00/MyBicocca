import { masked, readFormat, readVersion } from './format.ts';
import type { BitMatrix } from './matrix.ts';
import { correctErrors } from './reedSolomon.ts';
import { decodeSegments } from './segments.ts';
import { codewordCount, deinterleave, functionModules } from './version.ts';

/**
 * The codewords, read in the symbol's zigzag: two columns at a time from the right edge, up then
 * down, skipping the vertical timing column and every function module, unmasking as it goes.
 */
function readCodewords(modules: BitMatrix, version: number, mask: number): Uint8Array {
	const size = modules.width;
	const reserved = functionModules(version);
	const codewords = new Uint8Array(codewordCount(version));
	let next = 0;
	let byte = 0;
	let bits = 0;
	let upward = true;
	for (let right = size - 1; right > 0; right -= 2) {
		if (right === 6) right = 5;
		for (let count = 0; count < size; count++) {
			const y = upward ? size - 1 - count : count;
			for (let x = right; x > right - 2; x--) {
				if (reserved.get(x, y)) continue;
				byte = (byte << 1) | (modules.get(x, y) !== masked(mask, y, x) ? 1 : 0);
				if (++bits === 8) {
					/* Up to 7 remainder bits follow the last codeword. */
					if (next < codewords.length) codewords[next++] = byte;
					byte = 0;
					bits = 0;
				}
			}
		}
		upward = !upward;
	}
	return codewords;
}

/** The text of a QR code's modules, or undefined when they can't be read or repaired. */
export function readSymbol(modules: BitMatrix): string | undefined {
	const format = readFormat(modules);
	const version = readVersion(modules);
	if (format === undefined || version === undefined) return undefined;
	const blocks = deinterleave(readCodewords(modules, version, format.mask), version, format.level);
	const data: number[] = [];
	for (const block of blocks) {
		const eccLength = block.codewords.length - block.dataLength;
		if (!correctErrors(block.codewords, eccLength)) return undefined;
		data.push(...block.codewords.subarray(0, block.dataLength));
	}
	return decodeSegments(Uint8Array.from(data), version);
}
