/** Articles, prepositions and conjunctions that stay lowercase unless they open the title. */
const MINOR = new Set(
	[
		'di a da in con su per tra fra e ed o od',
		'il lo la i gli le un uno una',
		'del dello della dei degli delle',
		'al allo alla ai agli alle',
		'dal dallo dalla dai dagli dalle',
		'nel nello nella nei negli nelle',
		'sul sullo sulla sui sugli sulle'
	].flatMap((words) => words.split(' '))
);

/** Roman numerals of more than one letter: single letters are ordinary words ("i", "e"). */
const ROMAN = new Set(['ii', 'iii', 'iv', 'vi', 'vii', 'viii', 'ix', 'xi', 'xii']);

/** Elided articles and prepositions before an apostrophe: "l'Informatica", "dell'Ambiente". */
const ELIDED = new Set(['l', 'un', 'd', 'dell', 'all', 'dall', 'nell', 'sull']);

const ACCENTED: Record<string, string> = { a: 'à', e: 'è', i: 'ì', o: 'ò', u: 'ù' };

/** The first character in upper case, the rest as written: "lunedì 2" → "Lunedì 2". */
export function capitalize(text: string): string {
	return text.charAt(0).toLocaleUpperCase('it-IT') + text.slice(1);
}

/** A word's first letter in upper case, past any leading punctuation: "(a)" → "(A)". */
function capitalizeWord(word: string): string {
	const index = word.search(/\p{L}/u);
	if (index < 0) return word;
	return word.slice(0, index) + capitalize(word.slice(index));
}

/** Whether `word`, lower-cased, stands for a roman numeral at that place. */
const isRoman = (word: string, first: boolean, last: boolean) =>
	ROMAN.has(word) || (last && !first && /^[ivx]$/.test(word));

function caseWord(word: string, first: boolean, last: boolean): string {
	// A lone "i" is the article, except at the end, where no article can stand: "Fisica I".
	if (isRoman(word, first, last)) {
		return word.toLocaleUpperCase('it-IT');
	}
	const elision = /^(\p{L}+)['’](.+)$/u.exec(word);
	if (elision && ELIDED.has(elision[1])) {
		const article = first ? capitalizeWord(elision[1]) : elision[1];
		return `${article}'${capitalizeWord(elision[2])}`;
	}
	if (!first && MINOR.has(word)) return word;
	return capitalizeWord(word);
}

/**
 * A person's name as Esse3 stores it ("MARIA D'ANGELO-ROSSI"), capitalized part by part
 * ("Maria D'Angelo-Rossi"): unlike titles, names keep every word, particle and elision capital.
 */
export function personName(text: string): string {
	return text
		.trim()
		.toLocaleLowerCase('it-IT')
		.replace(
			/(^|[\s'’-])(\p{L})/gu,
			(_, before: string, letter: string) => before + letter.toLocaleUpperCase('it-IT')
		);
}

/** Lower-case words, Esse3's final apostrophes turned back into accents. */
function lowerWords(text: string): string[] {
	return text
		.toLocaleLowerCase('it-IT')
		.trim()
		.split(/\s+/)
		.filter((word) => word !== '')
		.map((word) => word.replace(/([aeiou])['’]$/u, (_, vowel: string) => ACCENTED[vowel]));
}

/**
 * An Esse3 name, which comes in capitals ("ALGORITMI E STRUTTURE DATI"), in Italian title case
 * ("Algoritmi e Strutture Dati"). Esse3 writes final accents as an apostrophe
 * ("AFFIDABILITA'"), which becomes the accented vowel.
 */
export function italianTitleCase(text: string): string {
	const words = lowerWords(text);
	return words
		.map((word, index) => caseWord(word, index === 0, index === words.length - 1))
		.join(' ');
}

/** A programme name as the app shows it: "INFORMATICA" becomes "Informatica", mixed case stays. */
export function careerTitle(career: { description: string }): string {
	return /\p{Ll}/u.test(career.description)
		? career.description
		: italianTitleCase(career.description);
}

/**
 * A short label in sentence case: one in capitals ("II APPELLO") is lowered but for its roman
 * numerals ("II appello"); one a person typed in mixed case ("compitino") only gets a capital.
 */
export function sentenceCase(text: string): string {
	const trimmed = text.trim().replace(/\s+/g, ' ');
	if (/\p{Ll}/u.test(trimmed)) return capitalizeWord(trimmed);
	const words = lowerWords(trimmed);
	return capitalizeWord(
		words
			.map((word, index) =>
				isRoman(word, index === 0, index === words.length - 1)
					? word.toLocaleUpperCase('it-IT')
					: word
			)
			.join(' ')
	);
}
