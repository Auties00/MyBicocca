/** Lower-case, accent-free, single-spaced: "Università" and "universita" match. */
export function normalize(text: string): string {
	return text
		.normalize('NFD')
		.replace(/\p{Diacritic}/gu, '')
		.toLowerCase()
		.replace(/\s+/g, ' ')
		.trim();
}

/**
 * Scores a candidate for a query: exact 1, title prefix 0.9, every query word a prefix of a title
 * word 0.8, of a keyword 0.65, contained in the subtitle 0.4; 0 for no match.
 */
export function matchScore(
	query: string,
	candidate: { title: string; subtitle?: string; keywords?: readonly string[] }
): number {
	const q = normalize(query);
	if (!q) return 0;
	const title = normalize(candidate.title);
	if (title === q) return 1;
	if (title.startsWith(q)) return 0.9;
	const words = q.split(' ');
	const prefixOf = (text: string) => {
		const tokens = text.split(/[\s/'’·,-]+/);
		return words.every((w) => tokens.some((t) => t.startsWith(w)));
	};
	if (prefixOf(title)) return 0.8;
	if (candidate.keywords?.some((k) => prefixOf(normalize(k)))) return 0.65;
	if (candidate.subtitle && normalize(candidate.subtitle).includes(q)) return 0.4;
	return 0;
}
