/**
 * Jaro-Winkler similarity in 0..1: prefix-weighted, as people type the first letters right and
 * slip in the middle of a word.
 */
export function jaroWinkler(a: string, b: string): number {
	if (a === b) return 1;
	if (a.length === 0 || b.length === 0) return 0;
	const similarity = jaro(a, b);
	if (similarity === 0) return 0;
	const maxPrefix = Math.min(4, a.length, b.length);
	let prefix = 0;
	while (prefix < maxPrefix && a[prefix] === b[prefix]) prefix++;
	return similarity + prefix * 0.1 * (1 - similarity);
}

/**
 * Damerau-Levenshtein distance (optimal string alignment) capped at `max`: -1 as soon as it is
 * provably above. An adjacent transposition ("hte") counts as one edit.
 */
export function damerauLevenshtein(a: string, b: string, max: number): number {
	if (a === b) return 0;
	if (max <= 0) return -1;
	if (Math.abs(a.length - b.length) > max) return -1;
	let prevPrev: Int32Array<ArrayBuffer> | undefined;
	let prev = new Int32Array(b.length + 1);
	for (let j = 0; j <= b.length; j++) prev[j] = j;
	let current = new Int32Array(b.length + 1);
	for (let i = 1; i <= a.length; i++) {
		current[0] = i;
		let rowMin = i;
		for (let j = 1; j <= b.length; j++) {
			const cost = a[i - 1] === b[j - 1] ? 0 : 1;
			let distance = Math.min(prev[j] + 1, current[j - 1] + 1, prev[j - 1] + cost);
			if (prevPrev && i > 1 && j > 1 && a[i - 1] === b[j - 2] && a[i - 2] === b[j - 1]) {
				distance = Math.min(distance, prevPrev[j - 2] + 1);
			}
			current[j] = distance;
			if (distance < rowMin) rowMin = distance;
		}
		if (rowMin > max) return -1;
		const recycled = prevPrev ?? new Int32Array(b.length + 1);
		prevPrev = prev;
		prev = current;
		current = recycled;
	}
	return prev[b.length] <= max ? prev[b.length] : -1;
}

function jaro(a: string, b: string): number {
	const window = Math.max(0, Math.floor(Math.max(a.length, b.length) / 2) - 1);
	const aMatched = new Uint8Array(a.length);
	const bMatched = new Uint8Array(b.length);
	let matches = 0;
	for (let i = 0; i < a.length; i++) {
		const from = Math.max(0, i - window);
		const to = Math.min(b.length, i + window + 1);
		for (let j = from; j < to; j++) {
			if (!bMatched[j] && a[i] === b[j]) {
				aMatched[i] = 1;
				bMatched[j] = 1;
				matches++;
				break;
			}
		}
	}
	if (matches === 0) return 0;
	let transpositions = 0;
	let j = 0;
	for (let i = 0; i < a.length; i++) {
		if (!aMatched[i]) continue;
		while (!bMatched[j]) j++;
		if (a[i] !== b[j]) transpositions++;
		j++;
	}
	// Kotlin's `transpositions / 2` divides integers.
	const m = matches;
	return (m / a.length + m / b.length + (m - Math.floor(transpositions / 2)) / m) / 3;
}
