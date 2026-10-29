import type {
	MoodleAddDiscussionPostResponse,
	MoodleDiscussionSummary,
	MoodleForumPost,
	MoodleOption
} from '@mybicocca/data-api/elearning';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/** What the composer sends. */
export interface Draft {
	subject: string;
	/** The message as HTML (see `editorHtml`). */
	message: string;
	/** New files to attach. */
	files: readonly File[];
	/** Whether replies to the discussion reach the student by e-mail. */
	subscribe: boolean;
}

export type UploadState = 'waiting' | 'uploading' | 'done' | 'failed';

/** Reports each new file's upload as it goes, by its index in `Draft.files`. */
export type UploadProgress = (index: number, state: UploadState) => void;

/**
 * Uploads `files` one after the other into a draft area of the student's (a new one when
 * `itemId` is 0), straight to Moodle, and returns the area the post names.
 */
async function upload(
	scope: SessionScope,
	files: readonly File[],
	itemId: number,
	progress: UploadProgress
): Promise<number> {
	let area = itemId;
	for (const [index, file] of files.entries()) {
		progress(index, 'uploading');
		try {
			const [stored] = await scope.api.moodle((moodle, auth) =>
				moodle.files.uploadFile(
					auth.token,
					{ fileName: file.name, contentType: file.type || undefined, bytes: file },
					{ itemId: area }
				)
			);
			area = stored?.itemId ?? area;
			progress(index, 'done');
		} catch (error) {
			progress(index, 'failed');
			throw error;
		}
	}
	return area;
}

function postOptions(draft: Draft, attachments: number | undefined): MoodleOption[] {
	const options: MoodleOption[] = [
		{ name: 'discussionsubscribe', value: draft.subscribe ? '1' : '0' }
	];
	if (attachments !== undefined)
		options.push({ name: 'attachmentsid', value: String(attachments) });
	return options;
}

/** Starts a discussion and returns its id. */
export async function createDiscussion(
	scope: SessionScope,
	forumId: number,
	draft: Draft,
	groupId: number | undefined,
	progress: UploadProgress
): Promise<number> {
	const attachments =
		draft.files.length > 0 ? await upload(scope, draft.files, 0, progress) : undefined;
	const created = await scope.api.moodle((moodle, auth) =>
		moodle.forum.addDiscussion(forumId, draft.subject.trim(), draft.message, auth, {
			groupId,
			options: postOptions(draft, attachments)
		})
	);
	return created.discussionId;
}

/** Replies to `parentId`; Moodle answers with the new post. */
export async function reply(
	scope: SessionScope,
	parentId: number,
	draft: Draft,
	progress: UploadProgress
): Promise<MoodleAddDiscussionPostResponse> {
	const attachments =
		draft.files.length > 0 ? await upload(scope, draft.files, 0, progress) : undefined;
	return scope.api.moodle((moodle, auth) =>
		moodle.forum.addDiscussionPost(parentId, draft.subject.trim(), draft.message, auth, {
			options: postOptions(draft, attachments)
		})
	);
}

/**
 * Saves an edit. When the attachments change, the post's kept files are copied into a draft area
 * (an empty one when none is kept: Moodle copies them all when given no names), the new ones are
 * added there, and the post is saved with that area; otherwise its files are left alone.
 */
export async function editPost(
	scope: SessionScope,
	post: MoodleForumPost,
	draft: Draft,
	kept: readonly string[],
	progress: UploadProgress
): Promise<void> {
	const changed = draft.files.length > 0 || kept.length < post.attachments.length;
	let attachments: number | undefined;
	if (changed) {
		const keep = post.attachments
			.filter((file) => kept.includes(file.fileName))
			.map((file) => ({ fileName: file.fileName, filePath: file.filePath || '/' }));
		const area = await scope.api.moodle(async (moodle, auth) =>
			keep.length > 0
				? (
						await moodle.forum.prepareDraftAreaForPost(post.id, 'attachment', auth, {
							filesToKeep: keep
						})
					).draftItemId
				: (await moodle.draftArea.getUnusedDraftItemid(auth)).itemId
		);
		attachments = await upload(scope, draft.files, area, progress);
	}
	await scope.api.moodle((moodle, auth) =>
		moodle.forum.updateDiscussionPost(post.id, auth, {
			subject: draft.subject.trim(),
			message: draft.message,
			options:
				attachments === undefined
					? undefined
					: [{ name: 'attachmentsid', value: String(attachments) }]
		})
	);
}

/** Deletes a post; the opening post takes the whole discussion with it. */
export async function deletePost(scope: SessionScope, postId: number): Promise<void> {
	await scope.api.moodle((moodle, auth) => moodle.forum.deletePost(postId, auth));
}

export function setSubscription(
	scope: SessionScope,
	forumId: number,
	discussionId: number,
	subscribed: boolean
): Promise<MoodleDiscussionSummary> {
	return scope.api.moodle((moodle, auth) =>
		moodle.forum.setSubscriptionState(forumId, discussionId, subscribed, auth)
	);
}

export function setStarred(
	scope: SessionScope,
	discussionId: number,
	starred: boolean
): Promise<MoodleDiscussionSummary> {
	return scope.api.moodle((moodle, auth) =>
		moodle.forum.toggleFavouriteState(discussionId, starred, auth)
	);
}

/** Logs the visit as Moodle's own pages do (activity logs, completion on view); best effort. */
export function markViewed(scope: SessionScope, discussionId: number): void {
	scope.api
		.moodle((moodle, auth) => moodle.forum.viewForumDiscussion(discussionId, auth))
		.catch(() => undefined);
}
