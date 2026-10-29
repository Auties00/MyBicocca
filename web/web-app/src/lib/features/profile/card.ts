/** Width over height of the student card, as on Android. */
export const CARD_ASPECT = 1.6111112;

export const CHIP_COUNT = 10;

/**
 * Java's `String.hashCode` (s[0]·31^(n−1) + … + s[n−1], in 32-bit two's complement), so the web
 * picks the chip artwork the Android app picks for the same account id.
 */
export function javaHashCode(text: string): number {
	let hash = 0;
	for (let index = 0; index < text.length; index++) {
		hash = (Math.imul(31, hash) + text.charCodeAt(index)) | 0;
	}
	return hash;
}

/** The chip artwork of an account, 0-based: Kotlin's `hashCode().rem(n)`, made non-negative. */
export function chipIndex(accountId: string, count = CHIP_COUNT): number {
	const remainder = javaHashCode(accountId) % count;
	return remainder < 0 ? remainder + count : remainder;
}

/** Kotlin's `round`: halves go to the even neighbour. */
function roundHalfEven(value: number): number {
	const floor = Math.floor(value);
	const fraction = value - floor;
	if (fraction > 0.5) return floor + 1;
	if (fraction < 0.5) return floor;
	return floor % 2 === 0 ? floor : floor + 1;
}

const STEP = 180;

/** The face nearest to `rotation` (degrees, unbounded): a multiple of 180. */
export function nearestFace(rotation: number): number {
	return roundHalfEven(rotation / STEP) * STEP;
}

/** Which side of the card faces the viewer at `rotation`. */
export function faceAt(rotation: number): 'front' | 'back' {
	const angle = ((rotation % 360) + 360) % 360;
	return angle > 90 && angle < 270 ? 'back' : 'front';
}

/** A gesture shorter than this is a flick. */
const FLICK_MS = 100;

/**
 * Where the card settles when a drag ends: a flick (under 100 ms, over 5% of the width) turns it
 * one more face in its direction; any other drag snaps to the nearest face, so a drag past half
 * the width (90°) flips it.
 */
export function releaseTarget(
	rotation: number,
	totalDrag: number,
	width: number,
	durationMs: number
): number {
	const base = nearestFace(rotation);
	const flick = durationMs >= 0 && durationMs < FLICK_MS;
	if (flick && width > 0 && Math.abs(totalDrag) > width * 0.05) {
		return base + STEP * Math.sign(totalDrag);
	}
	return base;
}

/** A tap turns the card toward the tapped half: the left half backwards, the right forwards. */
export function tapTarget(rotation: number, left: boolean): number {
	return (roundHalfEven(rotation / STEP) + (left ? -1 : 1)) * STEP;
}

/** Degrees a horizontal drag of `dx` pixels turns a card `width` wide. */
export function dragRotation(dx: number, width: number): number {
	return width > 0 ? (dx / width) * STEP : 0;
}

/** Whether turning from `previous` to `next` crossed an edge-on angle (90° or 270°). */
export function crossesEdge(previous: number, next: number): boolean {
	const low = Math.min(previous, next);
	const high = Math.max(previous, next);
	// Edges sit at 90 + 180k: count them in (low, high].
	const edges = (value: number) => Math.floor((value - 90) / STEP);
	return low !== high && edges(high) !== edges(low);
}

/** The tilt about the X axis that follows a pointer `y` pixels down a card `height` tall. */
export function tiltAt(y: number, height: number): number {
	if (height <= 0) return 0;
	return -((y - height / 2) / (height / 2)) * 10;
}

/**
 * The iridescent foil that follows the pointer: cyan, clear, magenta along a line through the
 * pointer (`x`, `y` as fractions of the card) whose angle turns with `x`, spanning half the card
 * each way, as Android draws it. Returned as a CSS `linear-gradient()`.
 */
export function foilGradient(x: number, y: number, width: number, height: number): string {
	const angle = (x - 0.5) * Math.PI * 2;
	const halfX = Math.cos(angle) * width * 0.5;
	const halfY = Math.sin(angle) * height * 0.5;
	const span = Math.hypot(halfX, halfY) || 1;
	const ux = halfX / span;
	const uy = halfY / span;
	// CSS gradient angles start at the top and turn clockwise.
	const css = (Math.atan2(ux, -uy) * 180) / Math.PI;
	const length = Math.abs(width * ux) + Math.abs(height * uy) || 1;
	const along = (x - 0.5) * width * ux + (y - 0.5) * height * uy;
	const at = (offset: number) => `${(((length / 2 + along + offset) / length) * 100).toFixed(2)}%`;
	return (
		`linear-gradient(${css.toFixed(2)}deg, rgb(0 255 255 / 0.12) ${at(-span)}, ` +
		`transparent ${at(0)}, rgb(255 0 255 / 0.12) ${at(span)})`
	);
}

/** "MARCO ROSSI" → "Marco Rossi", as the signature on the back reads. */
export function titleCase(name: string): string {
	return name
		.trim()
		.split(/\s+/)
		.map((word) => word.charAt(0).toLocaleUpperCase('it') + word.slice(1).toLocaleLowerCase('it'))
		.join(' ');
}

/** The campus address for a username, which may already be one. */
export function institutionalEmail(username: string): string {
	return username.includes('@') ? username : `${username}@campus.unimib.it`;
}
