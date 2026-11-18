import type { DataError } from '#lib/data/errors.ts';

/** The four sources the deadlines merge, as the UI names what is missing. */
export type DeadlineStream = 'results' | 'fees' | 'exams' | 'bookings';

export interface StreamState {
	name: DeadlineStream;
	/** Answered at least once, with data or with an error. */
	settled: boolean;
	/** Has data, fresh or from an earlier load. */
	loaded: boolean;
	error: DataError | undefined;
	updatedAt: number | undefined;
}

/** What a stream reads of each query it is made of. */
export interface SourceState {
	data: unknown;
	error: DataError | undefined;
	updatedAt: number | undefined;
}

/**
 * A stream from the query whose data it lists and those that only enrich it (the charges that
 * name the invoices). Any of them failing is the stream's failure, so it is reported and retried;
 * only the first decides whether the stream has data. `ready` holds the stream back for what
 * else it waits on.
 */
export function streamOf(
	name: DeadlineStream,
	[main, ...extras]: readonly [SourceState, ...SourceState[]],
	ready = true
): StreamState {
	const all = [main, ...extras];
	const times = all.flatMap((item) =>
		item.data !== undefined && item.updatedAt !== undefined ? [item.updatedAt] : []
	);
	return {
		name,
		settled: ready && all.every((item) => item.data !== undefined || item.error !== undefined),
		loaded: main.data !== undefined,
		error: all.find((item) => item.error !== undefined)?.error,
		updatedAt: times.length === 0 ? undefined : Math.min(...times)
	};
}

export interface StreamFailure {
	stream: DeadlineStream;
	error: DataError;
	/** Older data of the stream is still in the merge; otherwise the merge lacks the stream. */
	stale: boolean;
}

export interface MergeStatus {
	/** `pending` until every stream answered once; `error` when none of them has data. */
	status: 'pending' | 'success' | 'error';
	failures: StreamFailure[];
	/** The streams that failed with nothing to show. */
	missing: DeadlineStream[];
	/** Every stream is in the merge, so an empty list really means nothing is due. */
	complete: boolean;
	/** When the oldest stream in the merge was loaded. */
	updatedAt: number | undefined;
}

/**
 * How far the merge can be trusted. A merge waits for every stream, since a partial one would read
 * as "fewer deadlines", and only a complete one may say that nothing is due.
 */
export function mergeStatus(streams: readonly StreamState[]): MergeStatus {
	const failures = streams.flatMap((item) =>
		item.error === undefined ? [] : [{ stream: item.name, error: item.error, stale: item.loaded }]
	);
	const missing = failures.filter((failure) => !failure.stale).map((failure) => failure.stream);
	const status = streams.some((item) => !item.settled)
		? 'pending'
		: streams.some((item) => item.loaded)
			? 'success'
			: 'error';
	const times = streams.flatMap((item) =>
		item.loaded && item.updatedAt !== undefined ? [item.updatedAt] : []
	);
	return {
		status,
		failures,
		missing,
		complete: status === 'success' && missing.length === 0,
		updatedAt: times.length === 0 ? undefined : Math.min(...times)
	};
}
