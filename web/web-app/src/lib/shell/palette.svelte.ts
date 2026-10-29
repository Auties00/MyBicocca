import { untrack } from 'svelte';
import type { ChipTone, Href, Icon } from '#lib/ui/types.ts';

/** `[start, end)` over a row's title. */
export type TitleRange = readonly [start: number, end: number];

/** One row of the command palette. */
export interface PaletteItem {
	/** Stable across sessions ('page:calendar', 'course:4812'): history is keyed by it. */
	id: string;
	title: string;
	subtitle?: string;
	icon?: Icon;
	/** Tint of the icon's bed; neutral by default. */
	tone?: ChipTone;
	/** Section the row is listed under ("Corsi", "Servizi"). */
	group: string;
	/** Where its group stands among groups whose rows score the same: lower first. */
	order?: number;
	/** Where it goes; or `run` for an action. One of the two is required. */
	href?: Href;
	run?: () => void;
	/** Runs without closing the palette (an undo, a setting toggled in place). */
	stay?: boolean;
	/**
	 * Offered whatever the query finds (search the catalog for it): listed last, never counted as
	 * a result, and under the empty state when nothing else matched.
	 */
	fallback?: boolean;
	/** A fallback left out once a row scores this much: the query named something already. */
	unlessScored?: number;
	/** Relevance 0–1 for the current query; rows from all providers are merged by it. */
	score?: number;
	/** The parts of `title` the query matched, drawn in bold. */
	highlight?: readonly TitleRange[];
	/** Key names (as `Kbd`) shown on the right. */
	shortcut?: readonly string[];
	/** Offered on a history row: takes it out of the history (✕, or Shift+Backspace). */
	remove?: () => void;
	/** Other words it is found by ("tema" for the appearance), for a provider that rescores it. */
	keywords?: readonly string[];
}

/** A line under the field about results a provider couldn't get. */
export interface PaletteNotice {
	message: string;
	retry?: () => void;
	/** Offline: what is missing comes back with the connection, not with a retry. */
	offline?: boolean;
}

/**
 * A source of palette results. The shell registers the static catalogue (pages, services,
 * settings, actions); the search feature registers its index of the session's data:
 *
 * ```ts
 * $effect(() => registerPaletteProvider({ id: 'search', search: (q, signal) => rank(q, signal) }));
 * ```
 */
export interface PaletteProvider {
	id: string;
	/** Results for a non-empty query, best first. Called after each pause in typing; honour `signal`. */
	search(
		query: string,
		signal: AbortSignal
	): readonly PaletteItem[] | Promise<readonly PaletteItem[]>;
	/** Rows to show before anything is typed (history, things to do), in their groups; reactive. */
	suggestions?(): readonly PaletteItem[];
	/** A row was opened, from any provider, for `query` ('' before typing). */
	opened?(item: PaletteItem, query: string): void;
	/** Whether results are still on their way (an index loading); reactive. */
	readonly loading?: boolean;
	/** Changes whenever the same query may find something else: the open query is searched again. */
	readonly version?: unknown;
	/** What it couldn't load (a service down, offline); reactive. */
	readonly notice?: PaletteNotice;
	/** Ids it ranks itself: other providers' copies of them are dropped. */
	owns?(id: string): boolean;
	/**
	 * Another provider's row scored and highlighted on this provider's scale, so one matcher ranks
	 * every row; null when the query misses it.
	 */
	rescore?(item: PaletteItem, query: string): PaletteItem | null;
}

class PaletteRegistry {
	providers = $state.raw<readonly PaletteProvider[]>([]);

	/* Untracked, so registering from an effect doesn't make that effect depend on the list. */
	register(provider: PaletteProvider): () => void {
		const current = untrack(() => this.providers);
		this.providers = [...current.filter((p) => p.id !== provider.id), provider];
		return () => {
			this.providers = untrack(() => this.providers).filter((p) => p !== provider);
		};
	}

	get loading(): boolean {
		return this.providers.some((provider) => provider.loading === true);
	}

	/** Read it where a search runs, so the search runs again when a provider's results change. */
	get version(): unknown[] {
		return this.providers.map((provider) => provider.version);
	}

	get notices(): PaletteNotice[] {
		return this.providers.flatMap((provider) => (provider.notice ? [provider.notice] : []));
	}

	suggestions(): PaletteItem[] {
		return this.providers.flatMap((provider) => provider.suggestions?.() ?? []);
	}

	opened(item: PaletteItem, query: string): void {
		for (const provider of untrack(() => this.providers)) provider.opened?.(item, query);
	}
}

export const paletteRegistry = new PaletteRegistry();

/** Adds a provider; returns the function that removes it (handy as an effect or onMount cleanup). */
export function registerPaletteProvider(provider: PaletteProvider): () => void {
	return paletteRegistry.register(provider);
}

/** As Android's unified search: no group floods the list, and thirty rows at most. */
export const MAX_PER_GROUP = 8;
export const MAX_RESULTS = 30;

/** `best`, with the fields it leaves out taken from `other`. */
function completed(best: PaletteItem, other: PaletteItem): PaletteItem {
	const set = Object.entries(best).filter(([, value]) => value !== undefined);
	return { ...other, ...Object.fromEntries(set) };
}

/**
 * Merges the providers' answers into the palette's list. Rows sharing an id are one row: the
 * best-scored copy, completed by the other's fields (its highlight, its group's order). Then by
 * score, the groups' order, the provider's and its own; the best eight of a group; thirty in all;
 * the fallbacks after them, those the best row's score doesn't rule out.
 */
export function mergeResults(lists: readonly (readonly PaletteItem[])[]): PaletteItem[] {
	const rows: { item: PaletteItem; provider: number; rank: number }[] = [];
	const byId: Record<string, (typeof rows)[number]> = Object.create(null);
	lists.forEach((list, provider) =>
		list.forEach((item, rank) => {
			const seen = byId[item.id];
			if (seen === undefined) rows.push((byId[item.id] = { item, provider, rank }));
			else if ((item.score ?? 0) > (seen.item.score ?? 0))
				Object.assign(seen, { item: completed(item, seen.item), provider, rank });
			else seen.item = completed(seen.item, item);
		})
	);
	const perGroup: Record<string, number> = Object.create(null);
	const ranked = rows
		.filter(({ item }) => !item.fallback)
		.sort(
			(a, b) =>
				(b.item.score ?? 0) - (a.item.score ?? 0) ||
				(a.item.order ?? Infinity) - (b.item.order ?? Infinity) ||
				a.provider - b.provider ||
				a.rank - b.rank
		)
		.filter(({ item }) => (perGroup[item.group] = (perGroup[item.group] ?? 0) + 1) <= MAX_PER_GROUP)
		.slice(0, MAX_RESULTS);
	const best = ranked[0]?.item.score ?? 0;
	const fallbacks = rows.filter(
		({ item }) => item.fallback && best < (item.unlessScored ?? Infinity)
	);
	return [...ranked, ...fallbacks].map(({ item }) => item);
}

/**
 * Queries every provider and merges their answers, each row scored by the provider that rescores
 * the others' when there is one; a provider that fails adds nothing.
 */
export async function searchPalette(query: string, signal: AbortSignal): Promise<PaletteItem[]> {
	const providers = untrack(() => paletteRegistry.providers);
	const lists = await Promise.all(
		providers.map(async (provider) => {
			try {
				const found = await provider.search(query, signal);
				const others = providers.filter((other) => other !== provider && other.owns);
				return others.length === 0
					? found
					: found.filter((item) => !others.some((other) => other.owns?.(item.id)));
			} catch {
				return [];
			}
		})
	);
	const scorer = providers.find((provider) => provider.rescore);
	const rescored = (item: PaletteItem) => scorer?.rescore?.(item, query) ?? [];
	return mergeResults(
		lists.map((list, at) => (!scorer || providers[at] === scorer ? list : list.flatMap(rescored)))
	);
}

const TOP_HIT_SCORE = 0.8;
const TOP_HIT_LEAD = 0.05;

/** The same name, ignoring case and accents: a course and its record-book row. */
function sameTitle(a: PaletteItem, b: PaletteItem): boolean {
	return a.title.localeCompare(b.title, undefined, { sensitivity: 'base' }) === 0;
}

/**
 * The row that answers the query outright: strong, and clearly ahead of the next (or alone). A
 * namesake in a later group (the record book's row of the course) doesn't count as the next.
 */
export function topHit(results: readonly PaletteItem[]): PaletteItem | undefined {
	const [best, ...rest] = results;
	if (best === undefined || best.fallback || (best.score ?? 0) < TOP_HIT_SCORE) return undefined;
	const next = rest.find(
		(item) =>
			!item.fallback &&
			!(sameTitle(item, best) && (item.order ?? Infinity) > (best.order ?? Infinity))
	);
	if (next !== undefined && (best.score ?? 0) - (next.score ?? 0) < TOP_HIT_LEAD) return undefined;
	return best;
}
