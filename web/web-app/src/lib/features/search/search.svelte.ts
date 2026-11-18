import { network } from '#lib/data/network.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import { registerPaletteProvider, type PaletteNotice } from '#lib/shell/palette.svelte.ts';
import { shell } from '#lib/shell/shell.svelte.ts';
import { historyOf, keepHistories } from './history.svelte.ts';
import type { SearchIndex } from './index.svelte.ts';
import { recentRows } from './recent.ts';

/** Things to do shown before typing. */
const TODO_ROWS = 4;

/** Runs `task` once the page has settled: a couple of seconds in, when the main thread is idle. */
function whenSettled(task: () => void): () => void {
	let idle: number | undefined;
	const timer = setTimeout(() => {
		if ('requestIdleCallback' in window) idle = requestIdleCallback(task, { timeout: 5000 });
		else task();
	}, 2000);
	return () => {
		clearTimeout(timer);
		if (idle !== undefined) cancelIdleCallback(idle);
	};
}

/**
 * Android's unified search in the command palette, for the signed-in session: one ranked index of
 * the catalogue, the courses and their deadlines, the timetable, the campus, the record book and
 * what the student has to do, plus the account's history (recent picks, and the boosts they give).
 * The index's code loads once the page has settled, so the first search doesn't wait for it and
 * works offline; its data is read the first time the palette opens. Call it once while the
 * signed-in layout initializes.
 */
export function useSearch(): void {
	let index = $state.raw<SearchIndex>();
	let unavailable = $state(false);
	let loading: Promise<SearchIndex> | undefined;
	const history = $derived(session.current && historyOf(session.current.account.id));

	function load(): Promise<SearchIndex> {
		loading ??= import('./index.svelte.ts').then(
			(module) => {
				unavailable = false;
				return (index = new module.SearchIndex());
			},
			(error: unknown) => {
				loading = undefined;
				unavailable = true;
				throw error;
			}
		);
		return loading;
	}

	const prefetch = () => void load().catch(() => undefined);

	$effect(() => whenSettled(prefetch));

	$effect(() => {
		if (shell.paletteOpen || (unavailable && network.online)) prefetch();
	});

	$effect(() => {
		if (!shell.paletteOpen && history) history.cleared = undefined;
	});

	$effect(() => {
		const scope = session.current;
		if (index === undefined) return;
		if (scope && shell.paletteOpen) index.open(scope);
		else index.close();
	});

	$effect(() => {
		keepHistories(session.accounts.map((account) => account.id));
		void history?.load();
	});

	$effect(() =>
		registerPaletteProvider({
			id: 'search',
			async search(query, signal) {
				// Offline, the code that failed to arrive is fetched again once the connection is back.
				if (unavailable && !network.online) return [];
				const ready = await load();
				return signal.aborted ? [] : ready.search(query, history?.entries ?? []);
			},
			suggestions() {
				const recent = history
					? recentRows({
							entries: history.entries,
							cleared: history.cleared,
							byId: index?.byId,
							settled: index?.settled === true,
							remove: (id) => void history.remove(id),
							clear: () => void history.clear(),
							restore: () => history.restore()
						})
					: [];
				return [...recent, ...(index?.todo.slice(0, TODO_ROWS) ?? [])];
			},
			opened(item, query) {
				history?.record(item, query);
			},
			owns(id) {
				return index?.byId.has(id) === true;
			},
			rescore(item, query) {
				if (index) return index.rescore(item, query, history?.entries ?? []);
				// Without the index a row keeps its own provider's score, and what that misses stays out.
				return (item.score ?? 0) > 0 ? item : null;
			},
			get loading() {
				return shell.paletteOpen && (index ? index.loading : !unavailable);
			},
			get version() {
				return index?.docs;
			},
			get notice(): PaletteNotice | undefined {
				if (!unavailable) return index?.notice;
				if (!network.online) return { message: m.search_notice_offline(), offline: true };
				return { message: m.search_notice_unavailable(), retry: prefetch };
			}
		})
	);
}
