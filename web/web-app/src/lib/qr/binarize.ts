import { BitMatrix } from './matrix.ts';

const BLOCK = 8;

/** A block whose pixels span no more than this is flat: all background, or all ink. */
const FLAT = 24;

/** Gray levels of an RGBA image, 0 for black to 255 for white. */
export function luminance(rgba: Uint8ClampedArray, width: number, height: number): Uint8Array {
	const gray = new Uint8Array(width * height);
	for (let i = 0, p = 0; i < gray.length; i++, p += 4) {
		gray[i] = (rgba[p] * 77 + rgba[p + 1] * 150 + rgba[p + 2] * 29) >> 8;
	}
	return gray;
}

/**
 * Dark and light pixels by a local threshold, ZXing's hybrid binarizer: each 8×8 block's mean,
 * averaged over the 5×5 blocks around it, so uneven light (a projector's hot spot, a shadow
 * across a screen) doesn't swallow modules.
 */
export function binarize(gray: Uint8Array, width: number, height: number): BitMatrix {
	const columns = Math.ceil(width / BLOCK);
	const rows = Math.ceil(height / BLOCK);
	const levels = new Float64Array(columns * rows);

	for (let row = 0; row < rows; row++) {
		for (let column = 0; column < columns; column++) {
			let sum = 0;
			let count = 0;
			let min = 255;
			let max = 0;
			for (let y = row * BLOCK; y < Math.min(height, row * BLOCK + BLOCK); y++) {
				for (let x = column * BLOCK; x < Math.min(width, column * BLOCK + BLOCK); x++) {
					const value = gray[y * width + x];
					sum += value;
					count++;
					if (value < min) min = value;
					if (value > max) max = value;
				}
			}
			let level = sum / count;
			/*
			 * A flat block's mean would split noise into ink and paper. It is taken for paper, unless
			 * it is darker than the blocks already seen above and before it, which every code is
			 * framed by: then it is ink, and their level holds.
			 */
			if (max - min <= FLAT) {
				level = min / 2;
				if (row > 0 && column > 0) {
					const neighbours =
						(levels[(row - 1) * columns + column] +
							2 * levels[row * columns + column - 1] +
							levels[(row - 1) * columns + column - 1]) /
						4;
					if (min < neighbours) level = neighbours;
				}
			}
			levels[row * columns + column] = level;
		}
	}

	const image = new BitMatrix(width, height);
	for (let row = 0; row < rows; row++) {
		for (let column = 0; column < columns; column++) {
			let sum = 0;
			for (let dy = -2; dy <= 2; dy++) {
				const r = Math.min(rows - 1, Math.max(0, row + dy));
				for (let dx = -2; dx <= 2; dx++) {
					sum += levels[r * columns + Math.min(columns - 1, Math.max(0, column + dx))];
				}
			}
			const threshold = sum / 25;
			for (let y = row * BLOCK; y < Math.min(height, row * BLOCK + BLOCK); y++) {
				for (let x = column * BLOCK; x < Math.min(width, column * BLOCK + BLOCK); x++) {
					if (gray[y * width + x] <= threshold) image.set(x, y);
				}
			}
		}
	}
	return image;
}
