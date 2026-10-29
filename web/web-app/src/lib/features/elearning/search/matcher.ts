import { foldPreservingIndexes, foldQuery, tokenSpans, type TokenSpan } from './fold.ts';
import { STOP_WORDS, degeminate, stemItalian } from './italian.ts';
import { jaroWinkler, damerauLevenshtein } from './similarity.ts';
import { subsequenceMatch } from './subsequence.ts';

/** `[start, end)` over the original text. */
export type Range = readonly [start: number, end: number];

/**
 * How a text matched, strongest first: the whole query as a substring, every query word a
 * prefix (or Italian stem) of its own word, the initials of the content words, an abbreviation
 * (subsequence), then words within a typo budget.
 */
export const Tier = {
	substring: 1,
	prefix: 2,
	acronym: 3,
	subsequence: 4,
	fuzzy: 5
} as const;

export type MatchTier = (typeof Tier)[keyof typeof Tier];

export interface Match {
	tier: MatchTier;
	/** Ascending, non-overlapping, merged. */
	ranges: Range[];
}

/** A text folded once, for matching it against many queries. */
export interface FoldedText {
	folded: string;
	tokens: TokenSpan[];
	/** The content words, for acronyms: stop words out. */
	content: TokenSpan[];
	/** Their initials; null under two content words. */
	acronym: string | null;
	/** Each word's light Italian stem. */
	stems: string[];
}

export function foldText(text: string): FoldedText {
	const folded = foldPreservingIndexes(text);
	const tokens = tokenSpans(folded);
	const content = tokens.filter((token) => !STOP_WORDS.has(token.text));
	const acronym = content.length < 2 ? null : content.map((token) => token.text[0]).join('');
	return {
		folded,
		tokens,
		content,
		acronym,
		stems: tokens.map((token) => stemItalian(token.text))
	};
}

/** Edit budget by word length: none under 4 characters, 1 up to 7, 2 from 8. */
export function typoBudget(length: number): number {
	if (length < 4) return 0;
	return length < 8 ? 1 : 2;
}

const MIN_TYPO_SIMILARITY = 0.72;
const MAX_RANKED_TYPOS = 3;
const RANK_STEP_DOWN = 0.18;

/**
 * One query word against one text word in the typo tier, 0..1, or null out of budget. Equal
 * degeminated forms ("sopratutto") are free; a wrong first letter costs a point of rank.
 */
export function typoTokenScore(
	query: string,
	token: string,
	budget: number,
	degeminatedQuery = degeminate(query)
): number | null {
	// Degeminating keeps the first letter: words that start apart can't be equal after it.
	const degeminatedHit = query[0] === token[0] && degeminatedQuery === degeminate(token);
	const distance = degeminatedHit ? 0 : damerauLevenshtein(query, token, budget);
	if (distance < 0) return null;
	const firstLetterPenalty = query[0] === token[0] ? 0 : 1;
	const similarity = jaroWinkler(query, token);
	if (similarity < MIN_TYPO_SIMILARITY) return null;
	const rank = Math.min(distance + firstLetterPenalty, MAX_RANKED_TYPOS);
	return (
		((similarity - MIN_TYPO_SIMILARITY) / (1 - MIN_TYPO_SIMILARITY)) * (1 - rank * RANK_STEP_DOWN)
	);
}

/**
 * Where a query matches a text, tier by tier (Android's `QueryHighlighter`): null means no
 * match, so it filters as well as it highlights. Build one per query and reuse it on every text:
 * the folded query, its words and the typo verdicts per word are kept.
 */
export class QueryMatcher {
	readonly #query: string;
	readonly #compact: string;
	readonly #tokens: TokenSpan[];
	/** Per query word: its stem from six characters (else null), and what the text must contain. */
	readonly #stems: (string | null)[];
	readonly #needles: string[];
	readonly #significant: TokenSpan[];
	readonly #degeminated: string[];
	readonly #typos: Map<string, number | null>[];

	constructor(query: string) {
		this.#query = foldQuery(query);
		this.#compact = this.#query.replaceAll(' ', '');
		this.#tokens = tokenSpans(this.#query);
		this.#stems = this.#tokens.map((token) =>
			token.text.length >= 6 ? stemItalian(token.text) : null
		);
		this.#needles = this.#tokens.map((token, i) => this.#stems[i] ?? token.text);
		const significant = this.#tokens.filter((token) => !STOP_WORDS.has(token.text));
		this.#significant = significant.length > 0 ? significant : this.#tokens;
		this.#degeminated = this.#significant.map((token) => degeminate(token.text));
		this.#typos = this.#significant.map(() => new Map());
	}

	get isEmpty(): boolean {
		return this.#query === '';
	}

	match(text: string): Match | null {
		return text === '' ? null : this.matchFolded(foldText(text));
	}

	/** The match, trying the tiers down to `weakest` only (all five by default). */
	matchFolded(text: FoldedText, weakest: MatchTier = Tier.fuzzy): Match | null {
		const { folded, tokens, content, acronym } = text;
		if (this.#query === '' || folded === '') return null;
		const direct = folded.indexOf(this.#query);
		if (direct >= 0) {
			return { tier: Tier.substring, ranges: [[direct, direct + this.#query.length]] };
		}
		if (tokens.length === 0 || this.#tokens.length === 0) return null;
		const prefix = this.#prefixRanges(text);
		if (prefix) return { tier: Tier.prefix, ranges: prefix };
		if (weakest <= Tier.prefix) return null;
		// "asd" lights the initial of each content word of "Algoritmi e Strutture Dati".
		if (acronym === this.#compact) {
			return {
				tier: Tier.acronym,
				ranges: content.map((token) => [token.start, token.start + 1] as const)
			};
		}
		const subsequence = subsequenceMatch(this.#compact, folded);
		if (subsequence) {
			return {
				tier: Tier.subsequence,
				ranges: merged(subsequence.positions.map((at) => [at, at + 1] as const))
			};
		}
		const fuzzy = this.#fuzzyRanges(tokens);
		return fuzzy ? { tier: Tier.fuzzy, ranges: fuzzy } : null;
	}

	/**
	 * Every query word the prefix of its own text word, or with its stem from six characters. A
	 * prefix hit lights its prefix; a stem hit the whole word, whose tail differs.
	 */
	#prefixRanges({ folded, tokens, stems }: FoldedText): Range[] | null {
		// A word starting with the query word, or sharing its stem, contains that stem.
		for (const needle of this.#needles) if (!folded.includes(needle)) return null;
		const claimed = new Uint8Array(tokens.length);
		const ranges: Range[] = [];
		for (const [q, query] of this.#tokens.entries()) {
			const stem = this.#stems[q];
			let index = -1;
			for (let i = 0; i < tokens.length; i++) {
				if (claimed[i]) continue;
				if (tokens[i].text.startsWith(query.text) || (stem !== null && stems[i] === stem)) {
					index = i;
					break;
				}
			}
			if (index < 0) return null;
			claimed[index] = 1;
			const token = tokens[index];
			const length = token.text.startsWith(query.text) ? query.text.length : token.text.length;
			ranges.push([token.start, token.start + length]);
		}
		return merged(ranges);
	}

	/** Each significant query word lands on its own text word; the whole word lights up. */
	#fuzzyRanges(tokens: TokenSpan[]): Range[] | null {
		const claimed = new Uint8Array(tokens.length);
		const ranges: Range[] = [];
		for (let q = 0; q < this.#significant.length; q++) {
			const query = this.#significant[q].text;
			const budget = typoBudget(query.length);
			if (budget === 0) return null;
			const verdicts = this.#typos[q];
			let best = -1;
			let bestScore = -1;
			for (let i = 0; i < tokens.length; i++) {
				if (claimed[i]) continue;
				const text = tokens[i].text;
				// Out of budget by length alone, and degeminating keeps first letters apart.
				if (Math.abs(text.length - query.length) > budget && text[0] !== query[0]) continue;
				let score = verdicts.get(text);
				if (score === undefined) {
					score = typoTokenScore(query, text, budget, this.#degeminated[q]);
					verdicts.set(text, score);
				}
				if (score !== null && score > bestScore) {
					bestScore = score;
					best = i;
				}
			}
			if (best < 0) return null;
			claimed[best] = 1;
			ranges.push([tokens[best].start, tokens[best].start + tokens[best].text.length]);
		}
		return merged(ranges);
	}
}

/** Sorted by start, overlapping or touching ranges joined. */
export function merged(ranges: readonly Range[]): Range[] {
	if (ranges.length <= 1) return [...ranges];
	const sorted = [...ranges].sort((a, b) => a[0] - b[0]);
	const out: Range[] = [];
	let [start, end] = sorted[0];
	for (const [nextStart, nextEnd] of sorted.slice(1)) {
		if (nextStart <= end) {
			end = Math.max(end, nextEnd);
		} else {
			out.push([start, end]);
			[start, end] = [nextStart, nextEnd];
		}
	}
	out.push([start, end]);
	return out;
}
