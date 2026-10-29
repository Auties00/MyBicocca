/** Between the parts of a line; the no-break space keeps the dot beside the part before it. */
export const SEPARATOR = '\u00a0· ';

/** The parts that are there, joined into one line: "Year 2 · Active". */
export function joinParts(parts: readonly (string | null | undefined)[]): string {
	return parts.filter((part): part is string => !!part).join(SEPARATOR);
}

/* Short words that stay lower case inside a name; other capitalised words this short are acronyms. */
const SMALL_WORDS = new Set([
	...'a ad ai al con da dei del di e ed gli i il in la le lo nel per su sul un'.split(' '),
	...'and at by for of on the to'.split(' ')
]);

/* Names that keep their capital wherever they fall: places and institutions a programme names. */
const PROPER_NOUNS = new Set(
	'bicocca europa italia lombardia milano roma unione stato repubblica mediterraneo'.split(' ')
);

const ACRONYM_LENGTH = 3;

const capitalised = (word: string) => word.charAt(0).toLocaleUpperCase('it') + word.slice(1);

/**
 * Esse3 writes programme names in capitals ("SCIENZE E TECNOLOGIE INFORMATICHE"), which read
 * better in sentence case. Acronyms such as ICT or the class code L-31 keep their capitals, as do
 * places and institutions ("Unione europea"), and names already in mixed case are left as written.
 * Esse3's names are Italian, so is the casing.
 */
export function sentenceCase(text: string): string {
	if (text !== text.toLocaleUpperCase('it')) return text;
	let first = true;
	return text.replace(/\p{L}+/gu, (word, offset: number) => {
		const lower = word.toLocaleLowerCase('it');
		const elided = /['’]/.test(text.charAt(offset + word.length));
		const inside = /['’]/.test(text.charAt(offset - 1));
		const acronym = word.length <= ACRONYM_LENGTH && !elided && !inside && !SMALL_WORDS.has(lower);
		const leading = first;
		first = false;
		if (acronym) return word;
		return leading || PROPER_NOUNS.has(lower) ? capitalised(lower) : lower;
	});
}
