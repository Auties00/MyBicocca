import type { MoodleSubmissionStatus } from '@mybicocca/data-api/elearning';
import { SvelteMap } from 'svelte/reactivity';
import type { DataError } from '#lib/data/errors.ts';
import { cache, policies } from '#lib/data/query.svelte.ts';
import { keyString, type QuerySnapshot } from '#lib/data/queryCache.ts';
import { session } from '#lib/data/session.svelte.ts';
import { isWithdrawn, statusQueue, submissionStatusQuery } from './queries.ts';

/** One status as a list row reads it: what is cached, and whether its load failed. */
export interface StatusEntry {
	data: MoodleSubmissionStatus | undefined;
	/** A failure to show; none while loading. */
	error: DataError | undefined;
	refreshing: boolean;
}

/**
 * The submission statuses of a list of assignments, each shared with the assignment's own page.
 * Cached ones show at once; the rest load through the shared queue, a few at a time, and those
 * still waiting when the list goes away never load. Create it while a component initializes.
 */
export class SubmissionStatuses {
	readonly #snapshots = new SvelteMap<number, QuerySnapshot<MoodleSubmissionStatus>>();

	constructor(ids: () => readonly number[]) {
		$effect(() => {
			const scope = session.current;
			if (scope === undefined) return;
			const stops = ids().map((id) => {
				const options = submissionStatusQuery(scope, id);
				const queued = keyString(options.key);
				const release = statusQueue.want(queued);
				const stop = cache.observe(
					options.key,
					() => statusQueue.run(queued, options.load),
					options.policy ?? policies.live,
					(snapshot) => this.#snapshots.set(id, snapshot)
				);
				return () => {
					stop();
					release();
				};
			});
			return () => stops.forEach((stop) => stop());
		});
	}

	get(id: number): StatusEntry {
		const snapshot = this.#snapshots.get(id);
		return {
			data: snapshot?.data,
			error: isWithdrawn(snapshot?.error) ? undefined : snapshot?.error,
			refreshing: snapshot?.refreshing ?? false
		};
	}

	/** Loads again the statuses of `ids` that failed. */
	retry = async (ids: readonly number[]): Promise<void> => {
		const scope = session.current;
		if (scope === undefined) return;
		await Promise.all(ids.map((id) => cache.refresh(submissionStatusQuery(scope, id).key)));
	};
}
