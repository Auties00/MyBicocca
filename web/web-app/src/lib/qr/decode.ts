import { binarize, luminance } from './binarize.ts';
import { findFinderPatterns } from './finder.ts';
import { sampleGrid } from './grid.ts';
import { readSymbol } from './read.ts';

/**
 * The text of the QR code in an RGBA image (a camera frame), or undefined when none reads: the
 * decoder for browsers without the Barcode Detection API (Safari, Firefox, Chrome on Windows and
 * Linux). Dark modules on a light ground, as every lecturer's screen shows them.
 */
export function decodeQr(
	rgba: Uint8ClampedArray,
	width: number,
	height: number
): string | undefined {
	const image = binarize(luminance(rgba, width, height), width, height);
	const finders = findFinderPatterns(image);
	const modules = finders && sampleGrid(image, finders);
	return modules && readSymbol(modules);
}
