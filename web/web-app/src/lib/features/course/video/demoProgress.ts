import { days } from '#lib/demo/dates.ts';
import { DemoCourses, DemoElearning } from '#lib/demo/persona.ts';
import { DEMO_LECTURE_SECONDS, demoEntryId } from '#lib/demo/fixtures/kaltura/playback.course.ts';
import { demoCourseTitle } from '#lib/demo/fixtures/moodle/course.course.ts';
import type { ProgressMap, VideoProgress } from './progress.ts';

/**
 * The demo student's viewing history, as on Android: each course's first two recordings watched
 * and the third left part-way, so "continue watching" has somewhere to go.
 */
export function demoProgress(): ProgressMap {
	const entries: VideoProgress[] = DemoCourses.all.flatMap((course) =>
		[1, 1, 0.4].map((share, week) => {
			const cmId = DemoElearning.cmId(course, 10 + week * 10 + 1);
			const topic = DemoElearning.topics.get(course)?.[week] ?? null;
			return {
				cmId,
				courseId: course.moodleCourseId,
				position: Math.round(DEMO_LECTURE_SECONDS * share),
				duration: DEMO_LECTURE_SECONDS,
				completed: share >= 1,
				updatedAt: days(week * 5 - 14, 18).getTime(),
				title: `Lezione ${week + 1}: registrazione`,
				sectionTitle: topic,
				courseTitle: demoCourseTitle(course),
				entryId: demoEntryId(cmId)
			};
		})
	);
	return Object.fromEntries(entries.map((entry) => [entry.cmId, entry]));
}
