import type { FinderPatterns, Pattern } from './finder.ts';
import { BitMatrix, distance, type Point } from './matrix.ts';

/**
 * A plane projective map, row-major: (x, y) goes to ((ax + by + c) / w, (dx + ey + f) / w) with
 * w = gx + hy + i. A code seen at an angle is a square seen through one.
 */
export type Homography = readonly [
	number,
	number,
	number,
	number,
	number,
	number,
	number,
	number,
	number
];

export function project([a, b, c, d, e, f, g, h, i]: Homography, x: number, y: number): Point {
	const w = g * x + h * y + i;
	return { x: (a * x + b * y + c) / w, y: (d * x + e * y + f) / w };
}

/** Maps the unit square's corners (0,0), (1,0), (1,1), (0,1) to `corners`, in that order. */
function fromSquare(corners: readonly Point[]): Homography {
	const [p0, p1, p2, p3] = corners;
	const sx = p0.x - p1.x + p2.x - p3.x;
	const sy = p0.y - p1.y + p2.y - p3.y;
	if (sx === 0 && sy === 0) {
		return [p1.x - p0.x, p2.x - p1.x, p0.x, p1.y - p0.y, p2.y - p1.y, p0.y, 0, 0, 1];
	}
	const dx1 = p1.x - p2.x;
	const dx2 = p3.x - p2.x;
	const dy1 = p1.y - p2.y;
	const dy2 = p3.y - p2.y;
	const det = dx1 * dy2 - dx2 * dy1;
	const g = (sx * dy2 - dx2 * sy) / det;
	const h = (dx1 * sy - sx * dy1) / det;
	return [
		p1.x - p0.x + g * p1.x,
		p3.x - p0.x + h * p3.x,
		p0.x,
		p1.y - p0.y + g * p1.y,
		p3.y - p0.y + h * p3.y,
		p0.y,
		g,
		h,
		1
	];
}

/** The inverse up to scale, which a projective map doesn't mind. */
function adjugate([a, b, c, d, e, f, g, h, i]: Homography): Homography {
	return [
		e * i - f * h,
		c * h - b * i,
		b * f - c * e,
		f * g - d * i,
		a * i - c * g,
		c * d - a * f,
		d * h - e * g,
		b * g - a * h,
		a * e - b * d
	];
}

/** `n`, then `m`. */
function compose(m: Homography, n: Homography): Homography {
	const cell = (row: number, column: number) =>
		m[row * 3] * n[column] + m[row * 3 + 1] * n[3 + column] + m[row * 3 + 2] * n[6 + column];
	return [
		cell(0, 0),
		cell(0, 1),
		cell(0, 2),
		cell(1, 0),
		cell(1, 1),
		cell(1, 2),
		cell(2, 0),
		cell(2, 1),
		cell(2, 2)
	];
}

/** The map taking each of four points to its counterpart. */
export function quadToQuad(from: readonly Point[], to: readonly Point[]): Homography {
	return compose(fromSquare(to), adjugate(fromSquare(from)));
}

/**
 * Length of the dark, light, dark runs from (fromX, fromY) towards (toX, toY), a Bresenham walk:
 * from a finder's centre that's 3.5 modules, whatever the angle. NaN if the walk ends first.
 */
function runLength(
	image: BitMatrix,
	fromX: number,
	fromY: number,
	toX: number,
	toY: number
): number {
	const steep = Math.abs(toY - fromY) > Math.abs(toX - fromX);
	if (steep) [fromX, fromY, toX, toY] = [fromY, fromX, toY, toX];
	const dx = Math.abs(toX - fromX);
	const dy = Math.abs(toY - fromY);
	const xStep = fromX < toX ? 1 : -1;
	const yStep = fromY < toY ? 1 : -1;
	let error = -dx / 2;
	let state = 0;
	for (let x = fromX, y = fromY; x !== toX + xStep; x += xStep) {
		const dark = steep ? image.get(y, x) : image.get(x, y);
		/* Dark, then light, then dark again: each change of colour moves on a run. */
		if ((state === 1) === dark) {
			if (state === 2) return Math.hypot(x - fromX, y - fromY);
			state++;
		}
		error += dy;
		if (error > 0) {
			if (y === toY) break;
			y += yStep;
			error -= dx;
		}
	}
	return state === 2 ? Math.hypot(toX + xStep - fromX, toY - fromY) : NaN;
}

/** The same runs counted both ways from a centre, clipped to the image: a finder's 7 modules. */
function runLengthBothWays(image: BitMatrix, from: Point, to: Point): number {
	const fromX = Math.floor(from.x);
	const fromY = Math.floor(from.y);
	const toX = Math.floor(to.x);
	const toY = Math.floor(to.y);
	const forward = runLength(image, fromX, fromY, toX, toY);
	let scale = 1;
	let otherX = fromX - (toX - fromX);
	if (otherX < 0) {
		scale = fromX / (fromX - otherX);
		otherX = 0;
	} else if (otherX >= image.width) {
		scale = (image.width - 1 - fromX) / (otherX - fromX);
		otherX = image.width - 1;
	}
	let otherY = Math.floor(fromY - (toY - fromY) * scale);
	scale = 1;
	if (otherY < 0) {
		scale = fromY / (fromY - otherY);
		otherY = 0;
	} else if (otherY >= image.height) {
		scale = (image.height - 1 - fromY) / (otherY - fromY);
		otherY = image.height - 1;
	}
	otherX = Math.floor(fromX + (otherX - fromX) * scale);
	/* The centre pixel is counted by both walks. */
	return forward + runLength(image, fromX, fromY, otherX, otherY) - 1;
}

function moduleSizeAlong(image: BitMatrix, a: Point, b: Point): number {
	const one = runLengthBothWays(image, a, b);
	const other = runLengthBothWays(image, b, a);
	if (Number.isNaN(one)) return other / 7;
	if (Number.isNaN(other)) return one / 7;
	return (one + other) / 14;
}

/** Modules per side, from the finders' spacing: always 4 × version + 17, so 1 more than a multiple of 4. */
function dimensionFrom({ topLeft, topRight, bottomLeft }: FinderPatterns, moduleSize: number) {
	const across = Math.round(distance(topLeft, topRight) / moduleSize);
	const down = Math.round(distance(topLeft, bottomLeft) / moduleSize);
	const dimension = Math.floor((across + down) / 2) + 7;
	switch (dimension & 3) {
		case 0:
			return dimension + 1;
		case 2:
			return dimension - 1;
		case 3:
			return undefined;
		default:
			return dimension;
	}
}

/**
 * Looks for the alignment pattern (a dark module ringed light, ringed dark: 1:1:1 across its
 * middle) in a square around where the finders predict it, rows from the middle outwards.
 */
function findAlignment(image: BitMatrix, moduleSize: number, around: Point, reach: number) {
	const allowance = Math.floor(reach * moduleSize);
	const left = Math.max(0, Math.floor(around.x) - allowance);
	const right = Math.min(image.width - 1, Math.floor(around.x) + allowance);
	const top = Math.max(0, Math.floor(around.y) - allowance);
	const bottom = Math.min(image.height - 1, Math.floor(around.y) + allowance);
	if (right - left < moduleSize * 3 || bottom - top < moduleSize * 3) return undefined;

	const isRatio = (counts: readonly number[]) =>
		counts.every((count) => Math.abs(moduleSize - count) < moduleSize / 2);
	const found: Pattern[] = [];

	/* Confirms a candidate by crossing it vertically; returns it once seen twice. */
	const consider = (counts: readonly number[], y: number, endX: number): Pattern | undefined => {
		const total = counts[0] + counts[1] + counts[2];
		const x = endX - counts[2] - counts[1] / 2;
		const column = Math.floor(x);
		const maxCount = 2 * counts[1];
		const vertical = [0, 0, 0];
		let i = y;
		while (i >= 0 && image.get(column, i) && vertical[1] <= maxCount) {
			vertical[1]++;
			i--;
		}
		if (i < 0 || vertical[1] > maxCount) return undefined;
		while (i >= 0 && !image.get(column, i) && vertical[0] <= maxCount) {
			vertical[0]++;
			i--;
		}
		if (vertical[0] > maxCount) return undefined;
		i = y + 1;
		while (i < image.height && image.get(column, i) && vertical[1] <= maxCount) {
			vertical[1]++;
			i++;
		}
		if (i === image.height || vertical[1] > maxCount) return undefined;
		while (i < image.height && !image.get(column, i) && vertical[2] <= maxCount) {
			vertical[2]++;
			i++;
		}
		if (vertical[2] > maxCount) return undefined;
		const verticalTotal = vertical[0] + vertical[1] + vertical[2];
		if (5 * Math.abs(verticalTotal - total) >= 2 * total || !isRatio(vertical)) return undefined;
		const centre = { x, y: i - vertical[2] - vertical[1] / 2, moduleSize: total / 3 };
		const twin = found.find(
			(other) =>
				Math.abs(centre.y - other.y) <= centre.moduleSize &&
				Math.abs(centre.x - other.x) <= centre.moduleSize
		);
		if (twin) return { x: (twin.x + centre.x) / 2, y: (twin.y + centre.y) / 2, moduleSize };
		found.push(centre);
		return undefined;
	};

	const middle = top + ((bottom - top) >> 1);
	for (let step = 0; step < bottom - top; step++) {
		const y = middle + (step % 2 === 0 ? (step + 1) >> 1 : -((step + 1) >> 1));
		const counts = [0, 0, 0];
		let x = left;
		/* A light run cut by the square's edge can't be measured. */
		while (x < right && !image.get(x, y)) x++;
		let state = 0;
		for (; x < right; x++) {
			if (image.get(x, y)) {
				if (state === 1) {
					counts[1]++;
				} else if (state === 2) {
					if (isRatio(counts)) {
						const confirmed = consider(counts, y, x);
						if (confirmed) return confirmed;
					}
					counts[0] = counts[2];
					counts[1] = 1;
					counts[2] = 0;
					state = 1;
				} else {
					counts[++state]++;
				}
			} else {
				if (state === 1) state++;
				counts[state]++;
			}
		}
		if (isRatio(counts)) {
			const confirmed = consider(counts, y, right);
			if (confirmed) return confirmed;
		}
	}
	return found[0];
}

/**
 * Reads the code's modules off the image: its size from the finders' spacing, then each module's
 * centre through the perspective the finders (and, from version 2, the alignment pattern near the
 * fourth corner) describe.
 */
export function sampleGrid(image: BitMatrix, finders: FinderPatterns): BitMatrix | undefined {
	const { topLeft, topRight, bottomLeft } = finders;
	const moduleSize =
		(moduleSizeAlong(image, topLeft, topRight) + moduleSizeAlong(image, topLeft, bottomLeft)) / 2;
	if (!(moduleSize >= 1)) return undefined;
	const dimension = dimensionFrom(finders, moduleSize);
	if (dimension === undefined || dimension < 21 || dimension > 177) return undefined;

	const corner = {
		x: topRight.x - topLeft.x + bottomLeft.x,
		y: topRight.y - topLeft.y + bottomLeft.y
	};
	const last = dimension - 3.5;
	let bottomRight: Point = corner;
	let bottomRightModule = last;
	if (dimension > 21) {
		/* The alignment pattern sits 3 modules in from the corner, diagonally. */
		const inset = 1 - 3 / (dimension - 7);
		const expected = {
			x: topLeft.x + inset * (corner.x - topLeft.x),
			y: topLeft.y + inset * (corner.y - topLeft.y)
		};
		for (let reach = 4; reach <= 16; reach *= 2) {
			const alignment = findAlignment(image, moduleSize, expected, reach);
			if (alignment) {
				bottomRight = alignment;
				bottomRightModule = last - 3;
				break;
			}
		}
	}

	const transform = quadToQuad(
		[
			{ x: 3.5, y: 3.5 },
			{ x: last, y: 3.5 },
			{ x: bottomRightModule, y: bottomRightModule },
			{ x: 3.5, y: last }
		],
		[topLeft, topRight, bottomRight, bottomLeft]
	);
	const modules = new BitMatrix(dimension);
	for (let y = 0; y < dimension; y++) {
		for (let x = 0; x < dimension; x++) {
			const at = project(transform, x + 0.5, y + 0.5);
			if (image.get(Math.floor(at.x), Math.floor(at.y))) modules.set(x, y);
		}
	}
	return modules;
}
