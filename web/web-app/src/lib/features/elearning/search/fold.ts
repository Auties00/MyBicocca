/**
 * Search folding: case and diacritics are dropped one UTF-16 unit at a time, so every index of a
 * folded string points at the same character of the original and highlights land where they
 * should. Ported from Android's `core/search` (MatchHighlight, TextNormalizer).
 */

const COMBINING_MARK = /\p{Mn}/u;
const LETTER_OR_DIGIT = /[\p{L}\p{Nd}]/u;

/** Lowercase, diacritics stripped, still one unit long: "À" is "a", "ß" stays "ß". */
export function foldChar(char: string): string {
	const lower = char.toLowerCase();
	if (lower.length !== 1) return char;
	if (lower.charCodeAt(0) < 0x80) return lower;
	for (const part of lower.normalize('NFD')) {
		if (part.length === 1 && !COMBINING_MARK.test(part)) return part;
	}
	return lower;
}

/** Folds every character; the result has the length of `text`. */
export function foldPreservingIndexes(text: string): string {
	let folded = '';
	for (let index = 0; index < text.length; index++) folded += foldChar(text[index]);
	return folded;
}

const WHITESPACE = /\s/;

export function isWhitespace(char: string): boolean {
	const code = char.charCodeAt(0);
	if (code === 32 || (code >= 9 && code <= 13)) return true;
	return code >= 0x80 && WHITESPACE.test(char);
}

/** Whitespace, slashes, apostrophes and dashes split words. */
export function isTokenSeparator(char: string): boolean {
	return (
		char === '/' ||
		char === "'" ||
		char === '’' ||
		char === '–' ||
		char === '—' ||
		char === '-' ||
		isWhitespace(char)
	);
}

/** A word of a folded string and where it starts. */
export interface TokenSpan {
	readonly start: number;
	readonly text: string;
}

export function tokenSpans(folded: string): TokenSpan[] {
	const spans: TokenSpan[] = [];
	let index = 0;
	while (index < folded.length) {
		if (isTokenSeparator(folded[index])) {
			index++;
			continue;
		}
		const start = index;
		while (index < folded.length && !isTokenSeparator(folded[index])) index++;
		spans.push({ start, text: folded.slice(start, index) });
	}
	return spans;
}

/** Folded, trimmed, with runs of whitespace collapsed to one space. */
export function foldQuery(query: string): string {
	return foldPreservingIndexes(query).trim().replace(/\s+/g, ' ');
}

export function isLetterOrDigit(char: string): boolean {
	const code = char.charCodeAt(0);
	if ((code >= 48 && code <= 57) || (code >= 97 && code <= 122) || (code >= 65 && code <= 90))
		return true;
	return code >= 0x80 && LETTER_OR_DIGIT.test(char);
}

export function isDigit(char: string): boolean {
	const code = char.charCodeAt(0);
	return code >= 48 && code <= 57;
}
