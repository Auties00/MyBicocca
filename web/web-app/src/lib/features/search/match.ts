import {
	foldPreservingIndexes,
	foldQuery,
	isLetterOrDigit
} from '#lib/features/elearning/search/fold.ts';
import { STOP_WORDS, degeminate, stemItalian } from '#lib/features/elearning/search/italian.ts';
import { typoBudget, typoTokenScore } from '#lib/features/elearning/search/matcher.ts';
import { subsequenceMatch } from '#lib/features/elearning/search/subsequence.ts';
import type { TitleRange } from '#lib/shell/palette.svelte.ts';

/** A word of a folded text and where it starts. */
interface Word {
	start: number;
	text: string;
}

/** The runs of letters and digits: "Aula 2151 (Sala Lauree)" has "sala", not "(sala". */
function words(folded: string): Word[] {
	const out: Word[] = [];
	let start = -1;
	for (let at = 0; at <= folded.length; at++) {
		const inside = at < folded.length && isLetterOrDigit(folded[at]);
		if (inside && start < 0) start = at;
		else if (!inside && start >= 0) {
			out.push({ start, text: folded.slice(start, at) });
			start = -1;
		}
	}
	return out;
}

/** A text folded once (case, accents, spacing), for scoring it against every query. */
export interface PreparedText {
	norm: string;
	tokens: string[];
	/** Each token's light Italian stem. */
	stems: string[];
	/** How many words aren't function words. */
	contentWords: number;
	/** Initials of the content words; null under two of them. */
	acronym: string | null;
}

export interface PreparedQuery extends PreparedText {
	/** Without spaces, for acronyms typed spaced out ("a s d"). */
	compact: string;
	/** Per token, its stem from six characters; shorter tokens only match as prefixes. */
	queryStems: (string | null)[];
	/** The tokens the typo tier needs: function words out, unless nothing else is left. */
	significant: string[];
}

export type MatchTier = 'exact' | 'prefix' | 'acronym' | 'alias' | 'abbreviation' | 'typo';

export interface Match {
	score: number;
	tier: MatchTier;
	/** For the alias tier: which alias matched. */
	alias?: number;
}

export function prepareText(text: string): PreparedText {
	const norm = foldQuery(text);
	const tokens = words(norm).map((word) => word.text);
	const content = tokens.filter((word) => !STOP_WORDS.has(word));
	return {
		norm,
		tokens,
		stems: tokens.map(stemItalian),
		contentWords: content.length,
		acronym: content.length < 2 ? null : content.map((word) => word[0]).join('')
	};
}

/** Null for a query with nothing to match. */
export function prepareQuery(query: string): PreparedQuery | null {
	const text = prepareText(query);
	if (text.tokens.length === 0) return null;
	const significant = text.tokens.filter((token) => !STOP_WORDS.has(token));
	return {
		...text,
		compact: text.tokens.join(''),
		queryStems: text.tokens.map((token) => (token.length >= 6 ? stemItalian(token) : null)),
		significant: significant.length > 0 ? significant : text.tokens
	};
}

/** Every query token starts a token of the text, or shares its stem from six characters. */
export function allTokensPrefix(query: PreparedQuery, text: PreparedText): boolean {
	if (text.tokens.length === 0) return false;
	return query.tokens.every((token, q) => {
		const stem = query.queryStems[q];
		return text.tokens.some((word, i) => word.startsWith(token) || stem === text.stems[i]);
	});
}

/**
 * Whether `query` is the starts of some of the words, in order: "anmat" is AN·MAT of "Analisi
 * Matematica", "sisop" SIS·OP of "Sistemi Operativi".
 */
function wordStarts(query: string, tokens: readonly string[]): boolean {
	const failed = new Set<number>();
	const from = (at: number, word: number): boolean => {
		if (at === query.length) return true;
		const state = at * (tokens.length + 1) + word;
		if (failed.has(state)) return false;
		for (let w = word; w < tokens.length; w++) {
			const token = tokens[w];
			for (
				let length = 1;
				length <= token.length && token[length - 1] === query[at + length - 1];
				length++
			) {
				if (from(at + length, w + 1)) return true;
			}
		}
		failed.add(state);
		return false;
	};
	return from(0, 0);
}

function startsWord(text: string, index: number): boolean {
	return index === 0 || !isLetterOrDigit(text[index - 1]);
}

/** The share of matched characters in runs of two or more that start a word. */
function anchoredShare(positions: readonly number[], text: string): number {
	let anchored = 0;
	let start = 0;
	for (let i = 1; i <= positions.length; i++) {
		if (i < positions.length && positions[i] === positions[i - 1] + 1) continue;
		const length = i - start;
		if (length >= 2 && startsWord(text, positions[start])) anchored += length;
		start = i;
	}
	return anchored / positions.length;
}

/** Past this many words a title is a sentence, where scattered letters line up by chance. */
const ABBREVIATION_MAX_WORDS = 5;
const ABBREVIATION_ANCHORED = 0.75;

/**
 * Abbreviations of one word ("anmat"), mapped onto 0.55–0.72 by the subsequence's strength. Only
 * the starts of words count, or runs mostly made of them when no word is a typo of the query
 * ("aule" is "Aula" mistyped, not AUL·e of "Aula Tribunale"). Android's looser rule let "esami"
 * find "informativa privacy e consenso al trattamento dati" letter by letter.
 */
function abbreviationScore(query: PreparedQuery, text: PreparedText, typo: boolean): number | null {
	if (query.tokens.length !== 1 || query.norm.includes(' ')) return null;
	if (text.contentWords > ABBREVIATION_MAX_WORDS) return null;
	const match = subsequenceMatch(query.norm, text.norm);
	if (match === null) return null;
	if (
		!wordStarts(query.norm, text.tokens) &&
		(typo || anchoredShare(match.positions, text.norm) < ABBREVIATION_ANCHORED)
	)
		return null;
	const strength = Math.min(1, Math.max(0, (match.normalizedScore - 0.5) / 0.5));
	return 0.55 + 0.17 * strength;
}

/**
 * The words of `tokens` each significant query token is a typo of, best first, or null when one
 * has none. A typo keeps the first letter: "laboratorio" is no misspelling of "elaboratori".
 */
function typoWords(query: PreparedQuery, tokens: readonly string[]): [number, number][] | null {
	const found: [number, number][] = [];
	for (const token of query.significant) {
		const budget = typoBudget(token.length);
		if (budget === 0) return null;
		const degeminated = degeminate(token);
		let best = -1;
		let bestScore = -1;
		for (let i = 0; i < tokens.length; i++) {
			const word = tokens[i];
			if (word[0] !== token[0]) continue;
			const score = typoTokenScore(token, word, budget, degeminated);
			if (score !== null && score > bestScore) {
				bestScore = score;
				best = i;
			}
		}
		if (best < 0) return null;
		found.push([best, bestScore]);
	}
	return found;
}

/** Every significant query token within its typo budget of some text token: 0.5–0.7. */
function typoScore(query: PreparedQuery, text: PreparedText): number | null {
	const found = typoWords(query, text.tokens);
	if (found === null) return null;
	const total = found.reduce((sum, [, score]) => sum + score, 0);
	return 0.5 + (total / found.length) * 0.2;
}

/**
 * Android's `SearchMatcher`: how well a candidate answers a query, tier by tier, or null for no
 * match. Exact 1, every word a prefix 0.85, acronym 0.8, an alias exact 0.75 or by prefixes 0.7,
 * then the overlapping abbreviation (0.55–0.72) and typo (0.5–0.7) bands, whichever is higher.
 * Aliases (codes, synonyms, other names) never reach the fuzzy bands.
 */
export function matchScore(
	query: PreparedQuery,
	text: PreparedText,
	aliases: readonly PreparedText[] = []
): Match | null {
	if (text.norm === query.norm) return { score: 1, tier: 'exact' };
	if (allTokensPrefix(query, text)) return { score: 0.85, tier: 'prefix' };
	if (text.acronym !== null && text.acronym === query.compact)
		return { score: 0.8, tier: 'acronym' };
	const exact = aliases.findIndex((other) => other.norm === query.norm);
	if (exact >= 0) return { score: 0.75, tier: 'alias', alias: exact };
	const prefix = aliases.findIndex((other) => allTokensPrefix(query, other));
	if (prefix >= 0) return { score: 0.7, tier: 'alias', alias: prefix };
	const typo = typoScore(query, text);
	const abbreviation = abbreviationScore(query, text, typo !== null);
	if (abbreviation !== null && (typo === null || abbreviation >= typo))
		return { score: abbreviation, tier: 'abbreviation' };
	return typo === null ? null : { score: typo, tier: 'typo' };
}

/** Sorted, touching or overlapping ranges joined. */
function joined(ranges: TitleRange[]): TitleRange[] {
	ranges.sort((a, b) => a[0] - b[0]);
	const out: [number, number][] = [];
	for (const [start, end] of ranges) {
		const last = out.at(-1);
		if (last && start <= last[1]) last[1] = Math.max(last[1], end);
		else out.push([start, end]);
	}
	return out;
}

/** Each query token on its own word: the part it typed, or the whole word its stem met. */
function prefixRanges(query: PreparedQuery, spans: readonly Word[]): TitleRange[] {
	const stems = spans.map((span) => stemItalian(span.text));
	const claimed = new Set<number>();
	const ranges: TitleRange[] = [];
	query.tokens.forEach((token, q) => {
		const fits = (i: number) =>
			spans[i].text.startsWith(token) ||
			(query.queryStems[q] !== null && stems[i] === query.queryStems[q]);
		let at = spans.findIndex((_, i) => !claimed.has(i) && fits(i));
		if (at < 0) at = spans.findIndex((_, i) => fits(i));
		if (at < 0) return;
		claimed.add(at);
		const { start, text } = spans[at];
		ranges.push([start, start + (text.startsWith(token) ? token.length : text.length)]);
	});
	return ranges;
}

/**
 * The parts of `title` that made it match, drawn as the tier found them: the typed parts of its
 * words, the initials of an acronym, the letters of an abbreviation, the whole words a typo
 * stands for. None for an alias, which the title doesn't show.
 */
export function highlight(query: PreparedQuery, title: string, tier: MatchTier): TitleRange[] {
	const folded = foldPreservingIndexes(title);
	const spans = words(folded);
	if (spans.length === 0) return [];
	switch (tier) {
		case 'exact': {
			const last = spans[spans.length - 1];
			return [[spans[0].start, last.start + last.text.length]];
		}
		case 'prefix':
			return joined(prefixRanges(query, spans));
		case 'acronym':
			return spans
				.filter((span) => !STOP_WORDS.has(span.text))
				.map((span) => [span.start, span.start + 1]);
		case 'abbreviation': {
			const positions = subsequenceMatch(query.norm, folded)?.positions ?? [];
			return joined(positions.map((at) => [at, at + 1]));
		}
		case 'typo': {
			const found = typoWords(
				query,
				spans.map((span) => span.text)
			);
			return joined(
				(found ?? []).map(([i]) => [spans[i].start, spans[i].start + spans[i].text.length])
			);
		}
		case 'alias':
			return [];
	}
}
