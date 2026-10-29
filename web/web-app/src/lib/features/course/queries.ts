import type {
	MoodleCourseSection,
	MoodleCoursesByFieldResponse,
	MoodleWebCourseInfo
} from '@mybicocca/data-api/elearning';
import { DataFailure } from '#lib/data/errors.ts';
import { accountKey, publicKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { CourseFile } from './content.ts';
import { pluginfilePath } from './files.ts';

const YEAR_MS = 365 * 24 * 60 * 60_000;

/**
 * A course's sections and activities (`core_course_get_contents`), with each activity's completion
 * (`completionData`). Every course page reads it; the quizzes, assignments and forums tabs use it
 * to group their activities by section.
 */
export function courseContentsQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleCourseSection[]> {
	return {
		key: accountKey(scope.account, 'moodle.course.getContents', courseId),
		load: () => scope.api.moodle((moodle, auth) => moodle.course.getContents(courseId, auth)),
		policy: policies.fresh(10)
	};
}

/** The course record by id: names, `idnumber`, teachers (`contacts`) and the custom fields. */
export function courseByIdQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleCoursesByFieldResponse> {
	return {
		key: accountKey(scope.account, 'moodle.course.getCoursesByField', 'id', courseId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.course.getCoursesByField(auth, { field: 'id', value: String(courseId) })
			),
		policy: policies.fresh(10)
	};
}

/**
 * The public course page: syllabus in each language, staff with e-mails, library links. It is the
 * same for everyone, so it is read as a visitor, without the Moodle session, and cached as public
 * data that survives signing out.
 */
export function courseInfoQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleWebCourseInfo> {
	return {
		key: publicKey('moodleWeb.catalog.getCourseInfo', courseId),
		load: async () => {
			const info = await scope.api.moodleWebPublic.catalog.getCourseInfo(courseId);
			if (info === null) throw new DataFailure({ kind: 'not_found', service: 'moodleWeb' });
			return info;
		},
		policy: policies.shared(60)
	};
}

/**
 * The Kaltura entry behind a video activity. Every load of the activity's page logs a view in
 * Moodle, and an activity never changes its entry, the same for every student: the answer is
 * kept for good, as public data.
 */
export function kalturaEntryQuery(scope: SessionScope, cmId: number): QueryOptions<string> {
	return {
		key: publicKey('moodleWeb.media.getKalturaPlayer', cmId),
		load: async () => {
			const entry = await scope.api.moodleWeb(async (web) => {
				const player = await web.media.getKalturaPlayer(cmId);
				return player.entryId;
			});
			if (entry === null) {
				throw new DataFailure({ kind: 'auth', service: 'moodleWeb', reason: 'session_expired' });
			}
			return entry;
		},
		policy: { staleMs: Number.POSITIVE_INFINITY, persist: true, maxAgeMs: YEAR_MS }
	};
}

/**
 * A course file's bytes, for this visit only. The token travels in the request alone: no address
 * the page shows ever carries it. A file the teacher replaces gets a new modification time, so
 * a new entry.
 */
export function fileQuery(
	scope: SessionScope,
	file: Pick<CourseFile, 'url' | 'modified'>
): QueryOptions<Uint8Array> {
	const path = pluginfilePath(file.url);
	return {
		key: accountKey(
			scope.account,
			'moodle.files.downloadFile',
			path ?? file.url,
			file.modified ?? 0
		),
		load: () => {
			if (path === null) throw new DataFailure({ kind: 'not_found', service: 'moodle' });
			return scope.api.moodle((moodle, auth) => moodle.files.downloadFile(path, auth.token));
		},
		policy: policies.memory(60)
	};
}
