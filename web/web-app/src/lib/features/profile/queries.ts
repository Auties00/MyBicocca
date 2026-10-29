import type {
	Esse3TranscriptRow,
	Esse3TranscriptStats,
	Esse3TranscriptTest
} from '@mybicocca/data-api/esse3';
import { toDataError } from '#lib/data/errors.ts';
import { accountKey, careerKey } from '#lib/data/keys.ts';
import { cache, policies, type QueryOptions, type QueryPolicy } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { prerequisiteStatus, type PrerequisiteStatus } from './attempts.ts';

/** The record book's freshness on Android (`StalePolicy`, 10 minutes). */
const TRANSCRIPT = policies.fresh(10);

/** The Esse3 photo rarely changes: Android downloads it once and keeps it. */
const PHOTO: QueryPolicy = {
	staleMs: 7 * 24 * 60 * 60_000,
	persist: true,
	encrypt: true,
	maxAgeMs: 90 * 24 * 60 * 60_000
};

/** The prerequisite checks, one request per pending row: kept an hour. */
const PREREQUISITES = policies.fresh(60);

/** Esse3 is slow and shared: the prerequisite fan-out sends at most this many requests at once. */
const FAN_OUT = 3;

/** Every row of the career's record book (libretto); the calendar reads it too. */
export function recordBookRowsQuery(scope: SessionScope): QueryOptions<Esse3TranscriptRow[]> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, 'esse3.transcript.getRecordBookRows', matId),
		load: () => scope.api.esse3.transcript.getRecordBookRows(matId),
		policy: TRANSCRIPT
	};
}

/**
 * The record book's statistics: credits, exam counts and Esse3's averages. Some careers send no
 * averages inline, so `/medie` fills them in; its failure only leaves them out. Read them with
 * `careerStats`: the Today page shows the weighted average and the credits from here.
 */
export function careerStatsQuery(scope: SessionScope): QueryOptions<Esse3TranscriptStats> {
	const matId = scope.career.enrollmentTraitId;
	const { transcript } = scope.api.esse3;
	return {
		key: careerKey(scope, 'profile.stats', matId),
		load: async () => {
			const [stats, averages] = await Promise.all([
				transcript.getRecordBookStats(matId),
				transcript.getRecordBookAverages(matId).catch(() => [])
			]);
			return stats.averages.length > 0 ? stats : { ...stats, averages };
		},
		policy: TRANSCRIPT
	};
}

/** The student's photo from Esse3, or null when there is none. */
export function personPhotoQuery(scope: SessionScope): QueryOptions<Uint8Array | null> {
	const { personId } = scope.account;
	return {
		key: accountKey(scope.account, 'esse3.personalData.getPersonPhoto', personId),
		load: async () => {
			try {
				return await scope.api.esse3.personalData.getPersonPhoto(personId);
			} catch (error) {
				if (toDataError(error).kind === 'not_found') return null;
				throw error;
			}
		},
		policy: PHOTO
	};
}

/** Esse3's prerequisite check for one row; failures (422 on passed rows) read as unknown. */
async function checkPrerequisites(
	scope: SessionScope,
	activityChoiceId: number
): Promise<PrerequisiteStatus> {
	try {
		const check = await scope.api.esse3.transcript.getCheckProposalRecordBookRow(
			scope.career.enrollmentTraitId,
			activityChoiceId
		);
		return prerequisiteStatus(check.outcome);
	} catch {
		return 'unknown';
	}
}

/** Runs `task` over `items` with at most `limit` running at once. */
async function pool<T, R>(
	items: readonly T[],
	limit: number,
	task: (item: T) => Promise<R>
): Promise<R[]> {
	const results: R[] = new Array(items.length);
	let next = 0;
	const worker = async () => {
		while (next < items.length) {
			const index = next++;
			results[index] = await task(items[index]);
		}
	};
	await Promise.all(Array.from({ length: Math.min(limit, items.length) }, worker));
	return results;
}

/**
 * The prerequisite status of each pending row, by `adsceId`, for the libretto's warnings. Each
 * check goes through its row's own query, so a course opened from the list asks Esse3 nothing new.
 */
export function prerequisitesQuery(
	scope: SessionScope,
	pendingIds: readonly number[]
): QueryOptions<Record<number, PrerequisiteStatus>> {
	const ids = [...pendingIds].sort((a, b) => a - b);
	return {
		key: careerKey(scope, 'profile.prerequisites', scope.career.enrollmentTraitId, ids.join(',')),
		load: async () => {
			const statuses = await pool(ids, FAN_OUT, (id) => {
				const row = rowPrerequisitesQuery(scope, id);
				return cache.fetch(row.key, row.load, PREREQUISITES);
			});
			return Object.fromEntries(ids.map((id, index) => [id, statuses[index]]));
		},
		policy: PREREQUISITES
	};
}

/** The attempts ("prove") of one row: fetched live, as on Android. */
export function rowTestsQuery(
	scope: SessionScope,
	activityChoiceId: number
): QueryOptions<Esse3TranscriptTest[]> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, 'esse3.transcript.getRecordBookRowTests', matId, activityChoiceId),
		load: () => scope.api.esse3.transcript.getRecordBookRowTests(matId, activityChoiceId)
	};
}

/** One row as Esse3 has it now: its count of bookable exam calls is live. */
export function rowQuery(
	scope: SessionScope,
	activityChoiceId: number
): QueryOptions<Esse3TranscriptRow> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, 'esse3.transcript.getRecordBookRow', matId, activityChoiceId),
		load: () => scope.api.esse3.transcript.getRecordBookRow(matId, activityChoiceId)
	};
}

/** The prerequisite check of one row, for its detail. */
export function rowPrerequisitesQuery(
	scope: SessionScope,
	activityChoiceId: number
): QueryOptions<PrerequisiteStatus> {
	return {
		key: careerKey(
			scope,
			'esse3.transcript.getCheckProposalRecordBookRow',
			scope.career.enrollmentTraitId,
			activityChoiceId
		),
		load: () => checkPrerequisites(scope, activityChoiceId),
		policy: PREREQUISITES
	};
}
