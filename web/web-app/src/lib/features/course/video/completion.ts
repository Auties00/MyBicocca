import { accountKey } from '#lib/data/keys.ts';
import { mutate } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/**
 * Ticks a recording done in Moodle once it is watched to 90 %, as the Android app does. Only
 * activities the student ticks by hand accept it; the course's contents reload either way, so the
 * badges follow.
 */
export async function markWatched(
	scope: SessionScope,
	courseId: number,
	cmId: number
): Promise<void> {
	await mutate(
		() =>
			scope.api.moodle((moodle, auth) =>
				moodle.completion.updateActivityCompletionStatusManually(cmId, true, auth)
			),
		{ invalidate: [accountKey(scope.account, 'moodle.course.getContents', courseId)] }
	);
}
