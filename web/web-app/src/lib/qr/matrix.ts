/** A grid of dark and light cells: an image's pixels after thresholding, or a code's modules. */
export class BitMatrix {
	readonly width: number;
	readonly height: number;
	readonly bits: Uint8Array;

	constructor(width: number, height = width) {
		this.width = width;
		this.height = height;
		this.bits = new Uint8Array(width * height);
	}

	/** Whether the cell is dark; anything outside the grid reads as light, as a quiet zone. */
	get(x: number, y: number): boolean {
		return (
			x >= 0 && y >= 0 && x < this.width && y < this.height && this.bits[y * this.width + x] === 1
		);
	}

	set(x: number, y: number, dark = true): void {
		this.bits[y * this.width + x] = dark ? 1 : 0;
	}

	/** Marks a rectangle dark. */
	fill(left: number, top: number, width: number, height: number): void {
		for (let y = top; y < top + height; y++) {
			this.bits.fill(1, y * this.width + left, y * this.width + left + width);
		}
	}
}

export interface Point {
	readonly x: number;
	readonly y: number;
}

export function distance(a: Point, b: Point): number {
	return Math.hypot(a.x - b.x, a.y - b.y);
}
