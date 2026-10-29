/**
 * A course's visual identity, ported from Android's organic shapes: three soft polar shapes
 * picked by course id, scattered behind its card (and the course page's hero) in one of five
 * hand-tuned layouts picked by a hash of its code. Only stability per course matters, not
 * Android's exact pixels.
 */

export type ShapeName = 'burst' | 'puffy' | 'cookie' | 'sunny' | 'cookie6' | 'cookie4';

const HERO_SHAPES: readonly ShapeName[] = [
	'burst',
	'puffy',
	'cookie',
	'sunny',
	'cookie6',
	'cookie4'
];

/** Large, medium and small shape of a course: `id mod 6`, two and four further along. */
export function courseShapes(courseId: number): [ShapeName, ShapeName, ShapeName] {
	const n = HERO_SHAPES.length;
	const base = ((courseId % n) + n) % n;
	return [HERO_SHAPES[base], HERO_SHAPES[(base + 2) % n], HERO_SHAPES[(base + 4) % n]];
}

/** Radius 0..1 at angle θ: lobed outlines, smoothed so petals never spike. */
const RADIUS: Record<ShapeName, (theta: number) => number> = {
	burst: lobes(15, 0.84),
	puffy: lobes(6, 0.85),
	cookie: lobes(12, 0.86),
	sunny: lobes(8, 0.78),
	cookie6: (theta) => 1 - (0.2 * (1 - Math.cos(6 * (theta - Math.PI / 2)))) / 2,
	cookie4: (theta) => 1 - (0.18 * (1 - Math.cos(4 * theta))) / 2
};

function lobes(count: number, inner: number): (theta: number) => number {
	return (theta) => inner + ((1 - inner) * (1 + Math.cos(count * (theta + Math.PI / 2)))) / 2;
}

const paths = new Map<ShapeName, string>();

/** The shape's outline in a 100 × 100 box. */
export function shapePath(name: ShapeName): string {
	let path = paths.get(name);
	if (path === undefined) {
		const samples = 120;
		const radius = RADIUS[name];
		const points: string[] = [];
		for (let i = 0; i < samples; i++) {
			const theta = (i / samples) * 2 * Math.PI - Math.PI / 2;
			const r = 50 * radius(theta);
			points.push(
				`${(50 + Math.cos(theta) * r).toFixed(1)} ${(50 + Math.sin(theta) * r).toFixed(1)}`
			);
		}
		path = `M${points.join('L')}Z`;
		paths.set(name, path);
	}
	return path;
}

/**
 * Where one decoration goes: its centre as fractions of the container's width and height, its
 * size as a fraction of the height, its rotation in degrees.
 */
export interface Placement {
	x: number;
	y: number;
	size: number;
	rotation: number;
}

type Anchor = readonly [x: number, y: number, size: number, rotation?: number];

/** Anchors bleed past the box on purpose, so shapes read as cut by the card's edge. */
const LAYOUTS: readonly (readonly Anchor[])[] = [
	[
		[0.07, 0.2, 1.5],
		[0.45, 2.5, 2.5],
		[1, 0.5, 1.5, 15]
	],
	[
		[0, 1.2, 1.8],
		[0.5, 0, 1.5],
		[0.975, 1.35, 1.75]
	],
	[
		[0, 2.5, 4],
		[0.67, 1.2, 1.3],
		[1, 0, 1.3]
	],
	[
		[1, 2.5, 4],
		[0.45, 0.35, 0.75, 35],
		[0.1, 0.8, 1.2]
	],
	[
		[0, 1, 1.3],
		[0.4, -0.1, 0.6],
		[0.8, 3, 3.8]
	]
];

/** FNV-1a. */
function hash(text: string): number {
	let value = 2166136261;
	for (let i = 0; i < text.length; i++) {
		value ^= text.charCodeAt(i);
		value = Math.imul(value, 16777619);
	}
	return value >>> 0;
}

/** Mulberry32: a tiny seeded generator, so the jitter is the same on every visit. */
function random(seed: number): () => number {
	let state = seed >>> 0;
	return () => {
		state = (state + 0x6d2b79f5) >>> 0;
		let t = state;
		t = Math.imul(t ^ (t >>> 15), t | 1);
		t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
		return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
	};
}

/**
 * `count` placements for the decorated box of `id` (a course code or title); `salt` gives a
 * second box of the same card its own arrangement. Rotations jitter ±15°.
 */
export function placementsFor(id: string, count: number, salt = 0): Placement[] {
	const seed = hash(id) + salt;
	const layout = LAYOUTS[seed % LAYOUTS.length];
	const next = random(seed * 31);
	return Array.from({ length: Math.max(0, count) }, (_, index) => {
		const [x, y, size, rotation = 0] = layout[index % layout.length];
		// Android centres a shape taller than its box on the box before offsetting it.
		const centre = size > 1 ? 0.5 + y - size / 2 : y;
		return { x, y: centre, size, rotation: rotation + next() * 30 - 15 };
	});
}
