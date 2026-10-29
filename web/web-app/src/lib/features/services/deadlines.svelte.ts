import { today } from '#lib/data/clock.svelte.ts';
import type { DataError } from '#lib/data/errors.ts';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { bookingsQuery, examCallsQuery } from '#lib/features/exams/queries.ts';
import {
	buildDeadlines,
	currentPlanId,
	serviceStatus,
	summarize,
	type Deadline,
	type DeadlineSources,
	type DeadlineSummary,
	type ServiceStatus
} from './deadlines.ts';
import { provideShared, shared } from './shared.svelte.ts';
import {
	chargesQuery,
	invoicesQuery,
	planHeadersQuery,
	publishedResultsQuery,
	studentPlanQuery
} from './sources.ts';
import {
	mergeStatus,
	streamOf,
	type DeadlineStream,
	type MergeStatus,
	type StreamFailure
} from './streams.ts';

export type { DeadlineStream, StreamFailure } from './streams.ts';

const settled = (q: Query<unknown>) => q.data !== undefined || q.error !== undefined;

/**
 * The merged deadlines of the active career (spec §2.4), live: each source is its own cached
 * query shared with the page that owns it, so a booking or a payment there shows up here.
 * Create it while a component initializes, as `query`.
 *
 * Nothing is told before every stream answered once (see `mergeStatus`): `data`, `services` and
 * `summary` stay undefined until then, so no count contradicts another while the rest loads.
 */
export class DeadlinesQuery {
	readonly #results = query(() => session.current && publishedResultsQuery(session.current));
	readonly #invoices = query(() => session.current && invoicesQuery(session.current));
	readonly #charges = query(() => session.current && chargesQuery(session.current));
	readonly #bookings = query(() => session.current && bookingsQuery(session.current));
	readonly #calls = query(() => session.current && examCallsQuery(session.current));
	readonly #headers = query(() => session.current && planHeadersQuery(session.current));
	readonly #planId = $derived(
		this.#headers.data === undefined ? undefined : currentPlanId(this.#headers.data)
	);
	readonly #plan = query(() => {
		const scope = session.current;
		const planId = this.#planId;
		return scope && planId !== undefined && planId !== null && studentPlanQuery(scope, planId);
	});

	/** The study plan only narrows the bookable calls, so a failure or no plan settles it. */
	readonly #planSettled = $derived(
		this.#headers.error !== undefined ||
			this.#planId === null ||
			(this.#planId !== undefined && settled(this.#plan))
	);

	readonly #merge: MergeStatus = $derived(
		mergeStatus([
			streamOf('results', [this.#results]),
			streamOf('fees', [this.#invoices, this.#charges]),
			streamOf('exams', [this.#bookings]),
			streamOf('bookings', [this.#calls], this.#planSettled)
		])
	);

	readonly #sources: DeadlineSources = $derived({
		results: this.#results.data,
		invoices: this.#invoices.data,
		charges: this.#charges.data,
		bookings: this.#bookings.data,
		calls: this.#calls.data,
		plan: this.#plan.data ?? (this.#planId === null ? null : undefined)
	});

	readonly status: MergeStatus['status'] = $derived(this.#merge.status);

	/** Every stream is in the merge: only then may an empty list say that nothing is due. */
	readonly complete: boolean = $derived(this.#merge.complete);

	/** The streams the merge lacks, failed with nothing to show. */
	readonly missing: DeadlineStream[] = $derived(this.#merge.missing);

	readonly failures: StreamFailure[] = $derived(this.#merge.failures);

	readonly data: Deadline[] | undefined = $derived(
		this.#merge.status === 'success' ? buildDeadlines(this.#sources, today.current) : undefined
	);

	readonly summary: DeadlineSummary | undefined = $derived(
		this.data === undefined ? undefined : summarize(this.data)
	);

	/** Badge material for the hub; a missing stream counts nothing, so read `missing` too. */
	readonly services: ServiceStatus | undefined = $derived(
		this.data === undefined ? undefined : serviceStatus(this.#sources, this.data, today.current)
	);

	/** A refresh failed over data still shown. */
	readonly stale: boolean = $derived(this.failures.some((failure) => failure.stale));

	/** The first failure, for a one-line message. */
	readonly error: DataError | undefined = $derived(this.failures[0]?.error);

	readonly refreshing: boolean = $derived(this.#queries().some((item) => item.refreshing));

	/** When the oldest of the shown streams was loaded. */
	readonly updatedAt: number | undefined = $derived(this.#merge.updatedAt);

	/** Today as the merge sees it. */
	get today(): Date {
		return today.current;
	}

	#queries(): Query<unknown>[] {
		return [
			this.#results,
			this.#invoices,
			this.#charges,
			this.#bookings,
			this.#calls,
			this.#headers,
			this.#plan
		];
	}

	/** Reloads only what failed, as Android's Retry does. */
	retry = async (): Promise<void> => {
		await Promise.all(
			this.#queries()
				.filter((item) => item.error !== undefined)
				.map((item) => item.refresh())
		);
	};

	refresh = async (): Promise<void> => {
		await Promise.all(this.#queries().map((item) => item.refresh()));
	};
}

/** The deadlines of the active career; call it while a component initializes. */
export function deadlinesQuery(): DeadlinesQuery {
	return new DeadlinesQuery();
}

/** Lets the pages below share their deadlines; call it while the layout initializes. */
export const provideDeadlines: () => void = provideShared;

/** The deadlines a parent layout shares, or new ones; call it while a component initializes. */
export function useDeadlines(): DeadlinesQuery {
	return shared(deadlinesQuery);
}
