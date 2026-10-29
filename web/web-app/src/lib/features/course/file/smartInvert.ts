/** Below this saturation a pixel is grey and inverts fully; from `COLOUR` on it keeps its colour. */
const GREY = 0.05;
const COLOUR = 0.2;

/**
 * Darkens a rendered page in place, as Android's viewer does for dark mode: greys invert onto a
 * warm dark paper (white becomes rgb(24 26 30), black a warm light grey), colours stay as they
 * are, and the saturations between blend smoothly, so figures and highlights keep their hues.
 * Saturation is the spread between a pixel's channels, so near-black anti-aliasing counts as grey.
 */
export function smartInvert(pixels: Uint8ClampedArray): void {
	for (let index = 0; index < pixels.length; index += 4) {
		const r = pixels[index];
		const g = pixels[index + 1];
		const b = pixels[index + 2];
		const high = r > g ? (r > b ? r : b) : g > b ? g : b;
		const low = r < g ? (r < b ? r : b) : g < b ? g : b;
		const saturation = (high - low) / 255;
		if (saturation >= COLOUR) continue;
		let inverted = 1;
		if (saturation > GREY) {
			const t = (saturation - GREY) / (COLOUR - GREY);
			inverted = 1 - t * t * (3 - 2 * t);
		}
		pixels[index] = r + (230.01 - 1.808 * r) * inverted;
		pixels[index + 1] = g + (220.065 - 1.761 * g) * inverted;
		pixels[index + 2] = b + (199.92 - 1.667 * b) * inverted;
	}
}
