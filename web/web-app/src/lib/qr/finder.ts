import { type BitMatrix, type Point } from './matrix.ts';

/** A pattern's centre, and its module size as measured across it. */
export interface Pattern extends Point {
	readonly moduleSize: number;
}

/** The three finder patterns, named by the corner they mark on an upright code. */
export interface FinderPatterns {
	topLeft: Pattern;
	topRight: Pattern;
	bottomLeft: Pattern;
}

interface Candidate {
	x: number;
	y: number;
	moduleSize: number;
	/** Scan lines that crossed it: one alone is often noise. */
	count: number;
}

/** The largest code looked for spans a quarter of the image: version 20, 97 modules. */
const MAX_MODULES = 97;

/** Candidates kept for the search of the best three, which tries every triple. */
const MAX_CANDIDATES = 12;

/**
 * Whether five runs (dark, light, dark, light, dark) read 1:1:3:1:1, each within `tolerance` of a
 * module.
 */
function isFinderRatio(counts: readonly number[], tolerance = 0.5): boolean {
	const total = counts[0] + counts[1] + counts[2] + counts[3] + counts[4];
	if (total < 7) return false;
	const module = total / 7;
	const variance = module * tolerance;
	return (
		Math.abs(module - counts[0]) < variance &&
		Math.abs(module - counts[1]) < variance &&
		Math.abs(3 * module - counts[2]) < 3 * variance &&
		Math.abs(module - counts[3]) < variance &&
		Math.abs(module - counts[4]) < variance
	);
}

/** The middle of the third run, counted back from where the fifth ends. */
function centreFromEnd(counts: readonly number[], end: number): number {
	return end - counts[4] - counts[3] - counts[2] / 2;
}

/**
 * Counts the five runs through a pixel along a line (`dark(k)` reads the k-th pixel from it, k
 * negative behind), each light or outer run capped at `maxCount`, and returns where the pattern's
 * centre lies on the line as an offset from that pixel, or NaN when the runs are not a finder's.
 * The centre run always ends: outside the image reads as light.
 */
function crossCheck(
	dark: (k: number) => boolean,
	maxCount: number,
	{ expectedTotal, tolerance = 0.5 }: { expectedTotal?: number; tolerance?: number } = {}
): number {
	const counts = [0, 0, 0, 0, 0];
	let k = 0;
	while (dark(k)) {
		counts[2]++;
		k--;
	}
	while (!dark(k) && counts[1] <= maxCount) {
		counts[1]++;
		k--;
	}
	if (counts[1] > maxCount) return NaN;
	while (dark(k) && counts[0] <= maxCount) {
		counts[0]++;
		k--;
	}
	if (counts[0] > maxCount) return NaN;
	k = 1;
	while (dark(k)) {
		counts[2]++;
		k++;
	}
	while (!dark(k) && counts[3] < maxCount) {
		counts[3]++;
		k++;
	}
	if (counts[3] >= maxCount) return NaN;
	while (dark(k) && counts[4] < maxCount) {
		counts[4]++;
		k++;
	}
	if (counts[4] >= maxCount) return NaN;
	/* A pattern-like stretch much larger or smaller than the one first seen is something else. */
	const total = counts[0] + counts[1] + counts[2] + counts[3] + counts[4];
	if (expectedTotal !== undefined && 5 * Math.abs(total - expectedTotal) >= 2 * expectedTotal) {
		return NaN;
	}
	return isFinderRatio(counts, tolerance) ? centreFromEnd(counts, k) : NaN;
}

class FinderSearch {
	readonly candidates: Candidate[] = [];
	readonly image: BitMatrix;

	constructor(image: BitMatrix) {
		this.image = image;
	}

	/**
	 * Confirms a pattern seen on row `y`, ending before `endX`, by crossing it up and down, then
	 * across through the centre found, then diagonally.
	 */
	consider(counts: readonly number[], y: number, endX: number): boolean {
		const { image } = this;
		const total = counts[0] + counts[1] + counts[2] + counts[3] + counts[4];
		const across = { expectedTotal: total };
		const startX = Math.floor(centreFromEnd(counts, endX));
		const centreY = y + crossCheck((k) => image.get(startX, y + k), counts[2], across);
		if (Number.isNaN(centreY)) return false;
		const row = Math.floor(centreY);
		const centreX = startX + crossCheck((k) => image.get(startX + k, row), counts[2], across);
		if (Number.isNaN(centreX)) return false;
		const column = Math.floor(centreX);
		const diagonal = (k: number) => image.get(column + k, row + k);
		if (Number.isNaN(crossCheck(diagonal, 2 * total, { tolerance: 0.75 }))) return false;
		this.#merge(centreX, centreY, total / 7);
		return true;
	}

	#merge(x: number, y: number, moduleSize: number): void {
		for (const candidate of this.candidates) {
			const near =
				Math.abs(y - candidate.y) <= moduleSize && Math.abs(x - candidate.x) <= moduleSize;
			if (!near) continue;
			const difference = Math.abs(moduleSize - candidate.moduleSize);
			if (difference > 1 && difference > candidate.moduleSize) continue;
			const count = candidate.count + 1;
			candidate.x = (candidate.count * candidate.x + x) / count;
			candidate.y = (candidate.count * candidate.y + y) / count;
			candidate.moduleSize = (candidate.count * candidate.moduleSize + moduleSize) / count;
			candidate.count = count;
			return;
		}
		this.candidates.push({ x, y, moduleSize, count: 1 });
	}
}

/** Scans rows for the 1:1:3:1:1 runs a finder pattern draws across any line through its centre. */
function scan(image: BitMatrix): Candidate[] {
	const search = new FinderSearch(image);
	const skip = Math.max(3, Math.floor((3 * image.height) / (4 * MAX_MODULES)));
	for (let y = skip - 1; y < image.height; y += skip) {
		let counts = [0, 0, 0, 0, 0];
		let state = 0;
		for (let x = 0; x < image.width; x++) {
			if (image.get(x, y)) {
				if (state % 2 === 1) state++;
				counts[state]++;
			} else if (state % 2 === 1) {
				counts[state]++;
			} else if (state < 4) {
				state++;
				counts[state]++;
			} else if (isFinderRatio(counts) && search.consider(counts, y, x)) {
				counts = [0, 0, 0, 0, 0];
				state = 0;
			} else {
				counts = [counts[2], counts[3], counts[4], 1, 0];
				state = 3;
			}
		}
		if (isFinderRatio(counts)) search.consider(counts, y, image.width);
	}
	return search.candidates;
}

function squaredDistance(a: Point, b: Point): number {
	return (a.x - b.x) ** 2 + (a.y - b.y) ** 2;
}

/**
 * The three candidates closest to an isosceles right triangle with similar modules, the shape the
 * finder patterns make whatever the code's angle.
 */
function bestTriple(candidates: Candidate[]): [Candidate, Candidate, Candidate] | undefined {
	const confirmed = candidates.filter((candidate) => candidate.count >= 2);
	const pool = (confirmed.length >= 3 ? confirmed : candidates)
		.toSorted((a, b) => b.count - a.count)
		.slice(0, MAX_CANDIDATES)
		.sort((a, b) => a.moduleSize - b.moduleSize);
	let best: [Candidate, Candidate, Candidate] | undefined;
	let distortion = Infinity;
	for (let i = 0; i < pool.length - 2; i++) {
		for (let j = i + 1; j < pool.length - 1; j++) {
			const ij = squaredDistance(pool[i], pool[j]);
			for (let k = j + 1; k < pool.length; k++) {
				if (pool[k].moduleSize > pool[i].moduleSize * 1.4) continue;
				const [a, b, c] = [
					ij,
					squaredDistance(pool[j], pool[k]),
					squaredDistance(pool[i], pool[k])
				].sort((p, q) => p - q);
				/* An isosceles right triangle has c = a + b with a = b. */
				const d = Math.abs(c - 2 * b) + Math.abs(c - 2 * a);
				if (d < distortion) {
					distortion = d;
					best = [pool[i], pool[j], pool[k]];
				}
			}
		}
	}
	return best;
}

/**
 * Names the patterns: the top-left one faces the longest side, and the turn from it to the
 * other two tells the top-right from the bottom-left whatever the rotation.
 */
function orient([p, q, r]: [Candidate, Candidate, Candidate]): FinderPatterns {
	const pq = squaredDistance(p, q);
	const qr = squaredDistance(q, r);
	const pr = squaredDistance(p, r);
	const [a, topLeft, c] =
		qr >= pq && qr >= pr ? [q, p, r] : pr >= qr && pr >= pq ? [p, q, r] : [p, r, q];
	const turn = (c.x - topLeft.x) * (a.y - topLeft.y) - (c.y - topLeft.y) * (a.x - topLeft.x);
	return turn < 0
		? { topLeft, topRight: a, bottomLeft: c }
		: { topLeft, topRight: c, bottomLeft: a };
}

export function findFinderPatterns(image: BitMatrix): FinderPatterns | undefined {
	const candidates = scan(image);
	if (candidates.length < 3) return undefined;
	const triple = bestTriple(candidates);
	return triple && orient(triple);
}
