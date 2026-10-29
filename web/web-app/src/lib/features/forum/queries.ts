import type {
	MoodleCourseUserGroupsResponse,
	MoodleDiscussionPostsResponse,
	MoodleForum,
	MoodleForumDiscussion
} from '@mybicocca/data-api/elearning';
import { accountKey } from '#lib/data/keys.ts';
import { policies, type QueryKey, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/** Discussions per page, as Android and Moodle's own app load them. */
export const DISCUSSIONS_PER_PAGE = 25;

/** A course's forums (`mod_forum_get_forums_by_courses`), with their counts and permissions. */
export function courseForumsQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleForum[]> {
	return {
		key: accountKey(scope.account, 'moodle.forum.getForumsByCourses', courseId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.forum.getForumsByCourses(auth, { courseIds: [courseId] })
			),
		policy: policies.fresh(10)
	};
}

/** The discussions of a forum loaded so far, in Moodle's order, page by page. */
export interface DiscussionPages {
	discussions: MoodleForumDiscussion[];
	/** Pages loaded. */
	pages: number;
	/** Whether the last page came back short, so there is nothing more to load. */
	complete: boolean;
}

export function discussionsKey(scope: SessionScope, forumId: number): QueryKey {
	return accountKey(scope.account, 'moodle.forum.getForumDiscussions', forumId);
}

/** One page of a forum's discussions, in Moodle's listing order (pinned, starred, latest). */
export function loadDiscussionPage(
	scope: SessionScope,
	forumId: number,
	page: number
): Promise<MoodleForumDiscussion[]> {
	return scope.api.moodle(async (moodle, auth) => {
		const response = await moodle.forum.getForumDiscussions(forumId, auth, {
			page,
			perPage: DISCUSSIONS_PER_PAGE
		});
		return response.discussions;
	});
}

/**
 * A forum's first page of discussions, reloaded whenever a page shows it: new posts are what a
 * forum is read for. Later pages join it as the list asks for them.
 */
export function forumDiscussionsQuery(
	scope: SessionScope,
	forumId: number
): QueryOptions<DiscussionPages> {
	return {
		key: discussionsKey(scope, forumId),
		load: async () => {
			const discussions = await loadDiscussionPage(scope, forumId, 0);
			return {
				discussions,
				pages: 1,
				complete: discussions.length < DISCUSSIONS_PER_PAGE
			};
		},
		policy: policies.live
	};
}

export function postsKey(scope: SessionScope, discussionId: number): QueryKey {
	return accountKey(scope.account, 'moodle.forum.getDiscussionPosts', discussionId);
}

/** Every post of a discussion, oldest first, with the inline files of their messages. */
export function discussionPostsQuery(
	scope: SessionScope,
	discussionId: number
): QueryOptions<MoodleDiscussionPostsResponse> {
	return {
		key: postsKey(scope, discussionId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.forum.getDiscussionPosts(discussionId, auth, {
					sortBy: 'created',
					sortDirection: 'ASC',
					includeInlineAttachments: true
				})
			),
		policy: policies.live
	};
}

/** The course groups the student belongs to, for a new discussion in a group forum. */
export function courseGroupsQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleCourseUserGroupsResponse> {
	return {
		key: accountKey(scope.account, 'moodle.course.getCourseUserGroups', courseId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.course.getCourseUserGroups(auth, { courseId, userId: 0 })
			),
		policy: policies.fresh(60)
	};
}
