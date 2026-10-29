import { untrack } from 'svelte';
import { today } from '#lib/data/clock.svelte.ts';
import type { DataError } from '#lib/data/errors.ts';
import { network } from '#lib/data/network.svelte.ts';
import { cache, policies, type QueryOptions, type QueryPolicy } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { clock } from '#lib/features/calendar/clock.svelte.ts';
import { lessonsQuery } from '#lib/features/calendar/queries.ts';
import { TurnoChoices } from '#lib/features/calendar/turni.svelte.ts';
import { toEnrolledCourses } from '#lib/features/elearning/courses.ts';
import { courseDeadlinesQuery, enrolledCoursesQuery } from '#lib/features/elearning/queries.ts';
import { bookingsQuery, examCallsQuery } from '#lib/features/exams/queries.ts';
import { roomsCatalogQuery } from '#lib/features/map/queries.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { recordBookQuestionnairesQuery } from '#lib/features/questionnaires/queries.ts';
import { buildDeadlines, currentPlanId } from '#lib/features/services/deadlines.ts';
import {
	chargesQuery,
	invoicesQuery,
	planHeadersQuery,
	publishedResultsQuery,
	studentPlanQuery
} from '#lib/features/services/sources.ts';
import { addMonths, monthOf, toIsoDate } from '#lib/format/days.ts';
import { cached, intlLocale } from '#lib/format/locale.ts';
import { m } from '#lib/paraglide/messages.js';
import { getLocale } from '#lib/paraglide/runtime.js';
import { MAX_PER_GROUP, type PaletteItem, type PaletteNotice } from '#lib/shell/palette.svelte.ts';
import { category } from './categories.ts';
import { searchMemory, type HistoryEntry } from './history.ts';
import { prepareQuery } from './match.ts';
import { itemsById, rank, type SearchDoc } from './rank.ts';
import { dayRow, lessonDocs } from './sources/calendar.ts';
import { careerDocs } from './sources/career.ts';
import { catalogDocs, shellDoc } from './sources/catalog.ts';
import { assignmentDocs, catalogRow, courseDocs } from './sources/elearning.ts';
import { mapRow, placeDocs } from './sources/places.ts';
import { examDocs, questionnaireDocs, todoDocs } from './sources/todo.ts';

/** Past this many, what failed is too long a list to name. */
const NAMED_PARTS = 3;

/** A row found this well, by its name or one of its others, answers a query without the catalog. */
const NAMED = 0.75;

/**
 * One feature query the index reads. Search never asks a server for what a page has loaded: it
 * takes the cache's data, however old (pages keep it fresh), or the copy kept in IndexedDB. A
 * source with a `label` is what search is mostly for, one request: it is fetched while the palette
 * is open when nothing is kept, and named when that fails.
 */
class Source<T> {
	data = $state.raw<T>();
	/** Being loaded, with nothing to show yet. */
	waiting = $state(false);
	/** Why it has nothing to show. */
	error = $state.raw<DataError>();
	#key: QueryOptions<T>['key'] | undefined;
	#stop: (() => void) | undefined;
	/** Bumped by every reset, so a stored copy read for another career is dropped. */
	#generation = 0;

	constructor(
		readonly options: (scope: SessionScope) => QueryOptions<T> | undefined,
		readonly label?: () => string
	) {}

	/** Resolves once whatever was kept has been read. */
	async open(scope: SessionScope): Promise<void> {
		const options = this.options(scope);
		if (options === undefined) return;
		const cached = cache.peek<T>(options.key).data;
		if (cached !== undefined) {
			this.data = cached;
			return;
		}
		if (this.label === undefined) return this.#readStored(options);
		if (this.#stop) return;
		this.#key = options.key;
		this.#stop = cache.observe(
			options.key,
			options.load,
			options.policy ?? policies.live,
			(snapshot) => {
				if (snapshot.data !== undefined) this.data = snapshot.data;
				const missing = snapshot.data === undefined;
				this.waiting = missing && (snapshot.status === 'pending' || snapshot.refreshing);
				this.error = missing && !snapshot.refreshing ? snapshot.error : undefined;
			}
		);
	}

	async #readStored(options: QueryOptions<T>): Promise<void> {
		const policy: QueryPolicy<T> = options.policy ?? policies.live;
		if (this.data !== undefined || policy.persist !== true) return;
		const generation = this.#generation;
		const stored = await cache.peekStored(options.key, policy);
		if (generation !== this.#generation) return;
		if (stored !== undefined && this.data === undefined) this.data = stored;
	}

	retry(): void {
		if (this.error !== undefined && this.#key !== undefined) void cache.refresh(this.#key);
	}

	close(): void {
		this.#stop?.();
		this.#stop = undefined;
		this.#key = undefined;
		this.waiting = false;
		this.error = undefined;
	}

	reset(): void {
		this.close();
		this.#generation++;
		this.data = undefined;
	}
}

function listOf(names: readonly string[]): string {
	const locale = intlLocale();
	return cached('search-list', locale, {}, () => new Intl.ListFormat(locale)).format(names);
}

/**
 * The session's search index: what the student can open, as docs folded for the matcher. Built
 * from the features' own queries, read when the palette opens; each part rebuilds only when its
 * data changes.
 */
export class SearchIndex {
	#scope: string | undefined;
	#turni = $state.raw<TurnoChoices>();
	/** Opens whose stored copies are still being read. */
	#reading = $state(0);
	#opened = $state(false);

	readonly #courses = new Source(enrolledCoursesQuery, m.search_part_courses);
	readonly #assignments = new Source(courseDeadlinesQuery, m.search_part_deadlines);
	readonly #transcript = new Source(recordBookRowsQuery, m.search_part_transcript);
	readonly #rooms = new Source(roomsCatalogQuery, m.search_part_rooms);
	readonly #questionnaires = new Source(
		recordBookQuestionnairesQuery,
		m.search_part_questionnaires
	);
	readonly #results = new Source(publishedResultsQuery, m.search_part_results);
	readonly #bookings = new Source(bookingsQuery, m.search_part_bookings);
	readonly #calls = new Source(examCallsQuery, m.search_part_calls);
	readonly #invoices = new Source(invoicesQuery);
	readonly #charges = new Source(chargesQuery);
	readonly #planHeaders = new Source(planHeadersQuery);
	readonly #plan = new Source((scope) => {
		const headers = this.#planHeaders.data;
		const planId = headers && currentPlanId(headers);
		return planId === null || planId === undefined ? undefined : studentPlanQuery(scope, planId);
	});
	readonly #lessons = [0, 1].map(
		(offset) =>
			new Source((scope) =>
				lessonsQuery(scope, addMonths(monthOf(toIsoDate(today.current)), offset))
			)
	);

	readonly #sources = [
		this.#courses,
		this.#assignments,
		this.#transcript,
		this.#rooms,
		this.#questionnaires,
		this.#results,
		this.#bookings,
		this.#calls,
		this.#invoices,
		this.#charges,
		this.#planHeaders,
		this.#plan,
		...this.#lessons
	];

	readonly #catalog = $derived(catalogDocs());

	readonly #enrolled = $derived(
		this.#courses.data && toEnrolledCourses(this.#courses.data, getLocale())
	);

	readonly #courseDocs = $derived(courseDocs(this.#enrolled ?? []));

	readonly #assignmentDocs = $derived(
		assignmentDocs(this.#assignments.data ?? [], this.#enrolled ?? [], today.current)
	);

	readonly #careerDocs = $derived(careerDocs(this.#transcript.data ?? []));

	readonly #placeDocs = $derived(placeDocs(this.#rooms.data));

	readonly #deadlines = $derived(
		buildDeadlines(
			{
				results: this.#results.data,
				invoices: this.#invoices.data,
				charges: this.#charges.data,
				bookings: this.#bookings.data,
				calls: this.#calls.data,
				plan: this.#plan.data
			},
			today.current
		)
	);

	readonly #todoDocs = $derived([
		...todoDocs(this.#deadlines),
		...questionnaireDocs(this.#questionnaires.data ?? [])
	]);

	readonly #examDocs = $derived(examDocs(this.#deadlines));

	readonly #lessonDocs = $derived(
		lessonDocs(
			this.#lessons.flatMap((source) => source.data ?? []),
			clock.now,
			(lesson) => this.#turni?.hides(lesson) === true
		)
	);

	/** Every doc, the things to do first. */
	readonly docs: SearchDoc[] = $derived([
		...this.#todoDocs,
		...this.#catalog,
		...this.#courseDocs,
		...this.#assignmentDocs,
		...this.#examDocs,
		...this.#lessonDocs,
		...this.#placeDocs,
		...this.#careerDocs
	]);

	/** The rows by id, for history entries to show as they are now. */
	readonly byId: ReadonlyMap<string, PaletteItem> = $derived(itemsById(this.docs));

	/** What the student has to do, the most urgent first: results, calls, fees, questionnaires. */
	readonly todo: PaletteItem[] = $derived(this.#todoDocs.map((doc) => doc.item));

	readonly loading: boolean = $derived(
		this.#reading > 0 || this.#sources.some((source) => source.waiting)
	);

	readonly #failed = $derived(this.#sources.filter((source) => source.error !== undefined));

	/** Everything read and nothing failed: a row missing from the index is gone. */
	readonly settled: boolean = $derived(this.#opened && !this.loading && this.#failed.length === 0);

	/** What couldn't be loaded, with a retry; offline, that only saved data is searched. */
	readonly notice: PaletteNotice | undefined = $derived.by(() => {
		if (this.#failed.length === 0) return undefined;
		if (!network.online || this.#failed.every((source) => source.error?.kind === 'offline'))
			return { message: m.search_notice_offline(), offline: true };
		const labels = this.#failed.map((source) => source.label?.() ?? '');
		const parts = labels.filter((label, at) => labels.indexOf(label) === at);
		return {
			message:
				parts.length > NAMED_PARTS
					? m.search_notice_failed_many()
					: m.search_notice_failed({ parts: listOf(parts) }),
			retry: () => this.retry()
		};
	});

	/** Reads the session's data, loading what search needs and nobody loaded. */
	open(scope: SessionScope): void {
		untrack(() => {
			const key = `${scope.account.id}/${scope.career.id}`;
			if (key !== this.#scope) {
				for (const source of this.#sources) source.reset();
				this.#scope = key;
			}
			this.#turni = new TurnoChoices(scope.career.id);
			this.#opened = true;
			this.#reading++;
			// The study plan is found through its headers.
			const first = this.#sources.filter((source) => source !== this.#plan);
			void Promise.all(first.map((source) => source.open(scope)))
				.then(() => this.#plan.open(scope))
				.finally(() => this.#reading--);
		});
	}

	/** Stops loading, keeping what was read for the next time. */
	close(): void {
		for (const source of this.#sources) source.close();
	}

	retry(): void {
		for (const source of this.#sources) source.retry();
	}

	/** Another provider's row, scored as the index scores its own; null when the query misses it. */
	rescore(item: PaletteItem, query: string, history: readonly HistoryEntry[]): PaletteItem | null {
		const prepared = prepareQuery(query);
		if (prepared === null) return null;
		return rank(prepared, [shellDoc(item)], searchMemory(history, query))[0] ?? null;
	}

	/**
	 * The ranked rows for `query`: a day it names first, the map's directory last when the places
	 * found fill their group, and the course catalog last unless a row of the palette's, from any
	 * provider, is named by it (only fuzzy matches, or none).
	 */
	search(query: string, history: readonly HistoryEntry[]): PaletteItem[] {
		const prepared = prepareQuery(query);
		if (prepared === null) return [];
		const day = dayRow(query, today.current);
		const found = rank(prepared, this.docs, searchMemory(history, query), day ? [day] : []);
		const places = category('places').group;
		const more =
			found.filter((item) => item.group === places).length >= MAX_PER_GROUP ? [mapRow(query)] : [];
		if (day || query.length < 3 || !/\p{L}/u.test(query)) return [...found, ...more];
		return [...found, ...more, { ...catalogRow(query), unlessScored: NAMED }];
	}
}
