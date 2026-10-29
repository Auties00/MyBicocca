import { accountKey } from '#lib/data/keys.ts';
import { cache, prefetch, setQueryData, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { PlaylistItem } from '../content.ts';

/** Where the student got to in one lecture recording, kept on this device. */
export interface VideoProgress {
	cmId: number;
	courseId: number;
	/** Seconds. */
	position: number;
	/** Seconds; 0 until the player knows it. */
	duration: number;
	/** Watched to 90 %: for good, whatever is watched after. */
	completed: boolean;
	/** Epoch milliseconds of the last save. */
	updatedAt: number;
	title: string;
	sectionTitle: string | null;
	courseTitle: string;
	/** The Kaltura entry, for the thumbnail. */
	entryId: string | null;
}

export type ProgressMap = Readonly<Record<string, VideoProgress>>;

export type ProgressUpdate = Omit<VideoProgress, 'completed' | 'updatedAt'> & {
	completed?: boolean;
};

/** A recording counts as watched past this share of its length. */
const COMPLETION_RATIO = 0.9;

/** Below this share a recording counts as not started. */
const STARTED_RATIO = 0.01;

const YEAR_MS = 365 * 24 * 60 * 60_000;

/**
 * Every recording's progress of the account, by cmId: what Today's "continue watching" and the
 * course pages read. Nothing loads it from a server: it lives in the encrypted query store, so
 * signing out wipes it, and a load only returns what is kept.
 */
export function videoProgressQuery(scope: SessionScope): QueryOptions<ProgressMap> {
	const key = accountKey(scope.account, 'course.videoProgress');
	return {
		key,
		load: async () => {
			const kept = cache.peek<ProgressMap>(key).data;
			if (kept !== undefined) return kept;
			return __DEMO__ ? (await import('./demoProgress.ts')).demoProgress() : {};
		},
		policy: { staleMs: Number.POSITIVE_INFINITY, persist: true, encrypt: true, maxAgeMs: YEAR_MS }
	};
}

/**
 * The progress after `update`: a duration the player doesn't know yet keeps the one kept, and
 * reaching 90 % marks the recording watched, one way.
 */
export function mergeProgress(
	previous: VideoProgress | undefined,
	update: ProgressUpdate,
	now: number
): VideoProgress {
	const duration = update.duration > 0 ? update.duration : (previous?.duration ?? 0);
	return {
		...update,
		duration,
		completed:
			previous?.completed === true ||
			update.completed === true ||
			(duration > 0 && update.position >= COMPLETION_RATIO * duration),
		entryId: update.entryId ?? previous?.entryId ?? null,
		updatedAt: now
	};
}

/** Saves a recording's progress, returning what was kept before and what is kept now. */
export async function saveProgress(
	scope: SessionScope,
	update: ProgressUpdate
): Promise<{ previous: VideoProgress | undefined; saved: VideoProgress }> {
	const options = videoProgressQuery(scope);
	await prefetch(options);
	let previous: VideoProgress | undefined;
	let saved: VideoProgress | undefined;
	setQueryData<ProgressMap>(options.key, (current = {}) => {
		previous = current[update.cmId];
		saved = mergeProgress(previous, update, Date.now());
		return { ...current, [update.cmId]: saved };
	});
	if (saved === undefined) throw new Error('Progress was not saved');
	return { previous, saved };
}

export function progressRatio(progress: VideoProgress | undefined): number {
	if (progress === undefined || progress.duration <= 0) return 0;
	return Math.min(1, progress.position / progress.duration);
}

/** Started and not finished. */
export function isInProgress(progress: VideoProgress | undefined): progress is VideoProgress {
	return progress !== undefined && !progress.completed && progressRatio(progress) > STARTED_RATIO;
}

/** The recording to pick up again: the latest one started and not finished, of any course. */
export function continueWatching(
	progress: ProgressMap,
	courseId?: number
): VideoProgress | undefined {
	return Object.values(progress)
		.filter((item) => courseId === undefined || item.courseId === courseId)
		.filter(isInProgress)
		.sort((a, b) => b.updatedAt - a.updatedAt)[0];
}

/**
 * The course page's suggestion: the recording last started here and not finished; else the
 * first, in course order, that neither Moodle nor this device counts as watched; else the first.
 */
export function pickRecording(
	playlist: readonly PlaylistItem[],
	progress: ProgressMap
): PlaylistItem | undefined {
	const updated = (item: PlaylistItem) => progress[item.cmId]?.updatedAt ?? 0;
	const started = playlist
		.filter((item) => isInProgress(progress[item.cmId]))
		.sort((a, b) => updated(b) - updated(a))[0];
	return (
		started ??
		playlist.find((item) => !item.completed && progress[item.cmId]?.completed !== true) ??
		playlist[0]
	);
}
