/**
 * QR codes for the demo portal's bookings, which the real portal sends as data URLs: byte mode,
 * error correction L, versions 1 to 4 (up to 78 bytes), mask 0. A port of the reference
 * algorithm (ISO/IEC 18004), kept to what a reservation code needs.
 */

/** Data and error-correction codewords of versions 1–4 at level L, each a single block. */
const VERSIONS = [
	{ version: 1, data: 19, ecc: 7 },
	{ version: 2, data: 34, ecc: 10 },
	{ version: 3, data: 55, ecc: 15 },
	{ version: 4, data: 80, ecc: 20 }
] as const;

const EXP = new Uint8Array(512);
const LOG = new Uint8Array(256);
for (let index = 0, value = 1; index < 255; index++) {
	EXP[index] = value;
	LOG[value] = index;
	value <<= 1;
	if (value & 0x100) value ^= 0x11d;
}
for (let index = 255; index < 512; index++) EXP[index] = EXP[index - 255];

const multiply = (a: number, b: number) => (a === 0 || b === 0 ? 0 : EXP[LOG[a] + LOG[b]]);

function errorCorrection(data: readonly number[], degree: number): number[] {
	let generator = [1];
	for (let index = 0; index < degree; index++) {
		const next = new Array<number>(generator.length + 1).fill(0);
		generator.forEach((coefficient, at) => {
			next[at] ^= coefficient;
			next[at + 1] ^= multiply(coefficient, EXP[index]);
		});
		generator = next;
	}
	const remainder = new Array<number>(degree).fill(0);
	for (const byte of data) {
		const factor = byte ^ (remainder.shift() ?? 0);
		remainder.push(0);
		for (let index = 0; index < degree; index++)
			remainder[index] ^= multiply(generator[index + 1], factor);
	}
	return remainder;
}

function codewords(bytes: Uint8Array, capacity: number): number[] {
	const bits: number[] = [];
	const push = (value: number, length: number) => {
		for (let bit = length - 1; bit >= 0; bit--) bits.push((value >>> bit) & 1);
	};
	push(0b0100, 4);
	push(bytes.length, 8);
	for (const byte of bytes) push(byte, 8);
	push(0, Math.min(4, capacity * 8 - bits.length));
	while (bits.length % 8 !== 0) bits.push(0);
	const words: number[] = [];
	for (let index = 0; index < bits.length; index += 8) {
		words.push(bits.slice(index, index + 8).reduce((byte, bit) => (byte << 1) | bit, 0));
	}
	for (let pad = 0xec; words.length < capacity; pad ^= 0xec ^ 0x11) words.push(pad);
	return words;
}

/** The dark modules of the QR code of `text`, row by row. */
export function qrMatrix(text: string): boolean[][] {
	const bytes = new TextEncoder().encode(text);
	const spec = VERSIONS.find((item) => 12 + bytes.length * 8 <= item.data * 8);
	if (spec === undefined) throw new RangeError('Too long for the demo QR encoder');
	const size = 17 + 4 * spec.version;
	const dark = Array.from({ length: size }, () => new Array<boolean>(size).fill(false));
	const fixed = Array.from({ length: size }, () => new Array<boolean>(size).fill(false));
	const set = (row: number, column: number, value: boolean) => {
		dark[row][column] = value;
		fixed[row][column] = true;
	};

	for (const [top, left] of [
		[0, 0],
		[0, size - 7],
		[size - 7, 0]
	]) {
		for (let dr = -1; dr <= 7; dr++) {
			for (let dc = -1; dc <= 7; dc++) {
				const row = top + dr;
				const column = left + dc;
				if (row < 0 || column < 0 || row >= size || column >= size) continue;
				const ring = Math.max(Math.abs(dr - 3), Math.abs(dc - 3));
				set(row, column, ring !== 2 && ring !== 4);
			}
		}
	}
	for (let index = 8; index < size - 8; index++) {
		set(6, index, index % 2 === 0);
		set(index, 6, index % 2 === 0);
	}
	if (spec.version > 1) {
		const center = size - 7;
		for (let dr = -2; dr <= 2; dr++)
			for (let dc = -2; dc <= 2; dc++)
				set(center + dr, center + dc, Math.max(Math.abs(dr), Math.abs(dc)) !== 1);
	}

	// Format information: level L (01) and mask 0, with its BCH code, in both copies.
	const format = (() => {
		const data = 0b01 << 3;
		let remainder = data;
		for (let index = 0; index < 10; index++)
			remainder = (remainder << 1) ^ ((remainder >>> 9) * 0x537);
		return ((data << 10) | remainder) ^ 0x5412;
	})();
	const bit = (index: number) => ((format >>> index) & 1) === 1;
	for (let index = 0; index <= 5; index++) set(index, 8, bit(index));
	set(7, 8, bit(6));
	set(8, 8, bit(7));
	set(8, 7, bit(8));
	for (let index = 9; index < 15; index++) set(8, 14 - index, bit(index));
	for (let index = 0; index < 8; index++) set(8, size - 1 - index, bit(index));
	for (let index = 8; index < 15; index++) set(size - 15 + index, 8, bit(index));
	set(size - 8, 8, true);

	const data = codewords(bytes, spec.data);
	const all = [...data, ...errorCorrection(data, spec.ecc)];
	let position = 0;
	for (let right = size - 1; right >= 1; right -= 2) {
		if (right === 6) right = 5;
		const upward = ((right + 1) & 2) === 0;
		for (let step = 0; step < size; step++) {
			const row = upward ? size - 1 - step : step;
			for (const column of [right, right - 1]) {
				if (fixed[row][column]) continue;
				const value =
					position < all.length * 8 && ((all[position >>> 3] >>> (7 - (position & 7))) & 1) === 1;
				dark[row][column] = value !== ((row + column) % 2 === 0);
				position++;
			}
		}
	}
	return dark;
}

/** The QR code of `text` as an SVG data URL, black on white with the standard quiet zone. */
export function qrDataUrl(text: string): string {
	const matrix = qrMatrix(text);
	const quiet = 4;
	const side = matrix.length + quiet * 2;
	let path = '';
	matrix.forEach((row, y) =>
		row.forEach((on, x) => {
			if (on) path += `M${x + quiet} ${y + quiet}h1v1h-1z`;
		})
	);
	const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${side} ${side}" shape-rendering="crispEdges"><rect width="${side}" height="${side}" fill="#fff"/><path d="${path}" fill="#000"/></svg>`;
	return `data:image/svg+xml;base64,${btoa(svg)}`;
}
