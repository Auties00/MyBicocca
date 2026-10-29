import { m } from '#lib/paraglide/messages.js';
import { mergeResults, type PaletteItem } from '#lib/shell/palette.svelte.ts';
import type { SearchMemory } from './history.ts';
import {
	allTokensPrefix,
	highlight,
	matchScore,
	prepareText,
	type Match,
	type PreparedQuery,
	type PreparedText
} from './match.ts';

/** A row of the index, with its texts folded once. */
export interface SearchDoc {
	item: PaletteItem;
	text: PreparedText;
	/** Other names it is found by, scoring below its title: codes, synonyms, its course. */
	aliases: readonly PreparedText[];
	/** The aliases as written, to say which one a row was found by. */
	names: readonly string[];
}

export function searchDoc(
	item: PaletteItem,
	aliases: readonly (string | null | undefined)[] = []
): SearchDoc {
	const names = aliases.flatMap((alias) => alias?.trim() || []);
	return { item, text: prepareText(item.title), aliases: names.map(prepareText), names };
}

/** "tasse, pagamento, pagopa": a message's comma-separated synonyms. */
export function aliasList(text: string): string[] {
	return text.split(',').map((word) => word.trim());
}

export function itemsById(docs: readonly SearchDoc[]): ReadonlyMap<string, PaletteItem> {
	return new Map(docs.map((doc) => [doc.item.id, doc.item]));
}

/** Rows found by name (not by a fuzzy tier): with this many, fuzzy matches only add noise. */
const CROWDED = 3;

const fuzzy = (match: Match) => match.tier === 'abbreviation' || match.tier === 'typo';

/**
 * The index's answer to a query, as Android's `GlobalSearchUseCase` ranks it: every doc scored,
 * then boosted by the student's history (clamped at 1), then capped and ordered as the palette
 * merges (`mergeResults`). `fixed` rows come scored already (a day named in the query).
 * Abbreviations and typos drop out of a query that names three rows outright, unless the student
 * picked them before. The rows kept get the parts of their titles their tier matched, or, found
 * only by an alias the row doesn't show, that alias.
 */
export function rank(
	query: PreparedQuery,
	docs: Iterable<SearchDoc>,
	memory: SearchMemory,
	fixed: readonly PaletteItem[] = []
): PaletteItem[] {
	const matches: { doc: SearchDoc; match: Match }[] = [];
	let named = 0;
	for (const doc of docs) {
		const match = matchScore(query, doc.text, doc.aliases);
		if (match === null) continue;
		matches.push({ doc, match });
		if (!fuzzy(match)) named++;
	}
	const found = new Map<string, { doc?: SearchDoc; match: Match }>();
	const hits = fixed.map((item) => {
		const match = matchScore(query, prepareText(item.title));
		if (match) found.set(item.id, { match });
		return boosted(item, item.score ?? 0, memory);
	});
	for (const { doc, match } of matches) {
		if (fuzzy(match) && named >= CROWDED && memory.pick(doc.item.id) === 0) continue;
		hits.push(boosted(doc.item, match.score, memory));
		found.set(doc.item.id, { doc, match });
	}
	return mergeResults([hits]).map((item) => {
		const why = found.get(item.id);
		if (why === undefined) return item;
		if (why.doc && why.match.alias !== undefined)
			return explained(item, query, why.doc, why.match.alias);
		const ranges = highlight(query, item.title, why.match.tier);
		return ranges.length > 0 ? { ...item, highlight: ranges } : item;
	});
}

function boosted(item: PaletteItem, score: number, memory: SearchMemory): PaletteItem {
	const boost = memory.recency(item.title) + memory.pick(item.id);
	return { ...item, score: Math.min(1, score + boost) };
}

/**
 * "Corrisponde a «orario» · …", unless the row already shows the words the query found, in its
 * group and subtitle ("compiti analisi": an assignment under Compiti, of Analisi 1).
 */
function explained(
	item: PaletteItem,
	query: PreparedQuery,
	doc: SearchDoc,
	alias: number
): PaletteItem {
	if (allTokensPrefix(query, prepareText(`${item.group} ${item.subtitle ?? ''}`))) return item;
	const reason = m.search_matched_alias({ alias: doc.names[alias] });
	return { ...item, subtitle: item.subtitle ? `${reason} · ${item.subtitle}` : reason };
}
