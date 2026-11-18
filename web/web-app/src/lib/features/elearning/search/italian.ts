/**
 * Light Italian stemmer (Lucene's ItalianLightStemmer rules): only the final inflectional vowel
 * goes, with a preceding `i` or `h`, so "matematiche" and "matematica" share a stem. Tokens under
 * six characters are too ambiguous to stem.
 */
export function stemItalian(token: string): string {
	if (token.length < 6) return token;
	const last = token[token.length - 1];
	const beforeLast = token[token.length - 2];
	switch (last) {
		case 'e':
		case 'i':
			return token.slice(0, beforeLast === 'i' || beforeLast === 'h' ? -2 : -1);
		case 'a':
		case 'o':
			return token.slice(0, beforeLast === 'i' ? -2 : -1);
		default:
			return token;
	}
}

/** Collapses runs of one letter: doubled consonants are the commonest Italian misspelling. */
export function degeminate(token: string): string {
	let result = '';
	let previous = '';
	for (const char of token) {
		if (char !== previous) result += char;
		previous = char;
	}
	return result;
}

/** Italian and English function words, ignored by acronyms and the typo tier. */
export const STOP_WORDS: ReadonlySet<string> = new Set([
	'il',
	'lo',
	'la',
	'i',
	'gli',
	'le',
	'l',
	'del',
	'dello',
	'della',
	'dei',
	'degli',
	'delle',
	'al',
	'allo',
	'alla',
	'ai',
	'agli',
	'alle',
	'dal',
	'dallo',
	'dalla',
	'dai',
	'dagli',
	'dalle',
	'nel',
	'nello',
	'nella',
	'nei',
	'negli',
	'nelle',
	'sul',
	'sullo',
	'sulla',
	'sui',
	'sugli',
	'sulle',
	'col',
	'coi',
	'di',
	'a',
	'ad',
	'da',
	'in',
	'con',
	'su',
	'per',
	'tra',
	'fra',
	'e',
	'ed',
	'o',
	'od',
	'un',
	'uno',
	'una',
	'che',
	'non',
	'of',
	'the',
	'and',
	'for',
	'on',
	'to',
	'an',
	'or',
	'by',
	'with'
]);
