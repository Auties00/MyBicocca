/*
 * Android draws the questionnaire glyphs as expressive shapes (a six-sided cookie, a four-leaf
 * clover); these are their SVG counterparts, as closed outlines around the centre of a 40 square.
 */

const SIZE = 40;
const STEPS = 192;

function outline(radius: (angle: number) => number): string {
	const radii = Array.from({ length: STEPS }, (_, step) => radius((step / STEPS) * 2 * Math.PI));
	const scale = SIZE / 2 / Math.max(...radii);
	const points = radii.map((r, step) => {
		const angle = (step / STEPS) * 2 * Math.PI - Math.PI / 2;
		const x = SIZE / 2 + r * scale * Math.cos(angle);
		const y = SIZE / 2 + r * scale * Math.sin(angle);
		return `${x.toFixed(2)} ${y.toFixed(2)}`;
	});
	return `M${points.join('L')}Z`;
}

/** A wavy outline: `lobes` gentle bumps whose depth is `depth` of the radius. */
export function scallop(lobes: number, depth: number): string {
	return outline((angle) => 1 + depth * Math.cos(lobes * angle));
}

/** The nine-lobed badge of the summary and of the sent questionnaire. */
export const BADGE = scallop(9, 0.07);

/**
 * Four round leaves on the diagonals, as discs around the centre whose outline is blended where
 * two leaves meet, so the notches between them are soft rather than pinched.
 */
export function clover(): string {
	const offset = 10.5;
	const leaf = 9.5;
	const softness = 20;
	const centres = [1, 3, 5, 7].map((eighth) => (eighth * Math.PI) / 4);
	return outline((angle) => {
		const sum = centres.reduce((total, centre) => {
			const along = offset * Math.cos(angle - centre);
			const across = offset * offset - along * along;
			const reach = along + Math.sqrt(Math.max(0, leaf * leaf - across));
			return total + Math.max(0, reach) ** softness;
		}, 0);
		return sum ** (1 / softness);
	});
}
