import type {
	MoodleForum,
	MoodleForumDiscussion,
	MoodleForumPost
} from '@mybicocca/data-api/elearning';
import type { DataError } from '#lib/data/errors.ts';

/** A line of text not known yet: it keeps the line's height so nothing below moves when it comes. */
export const PENDING_LINE = String.fromCharCode(0xa0);

/** Moodle's forum types, with every unknown one (plugins, future types) as `other`. */
export type ForumKind = 'news' | 'general' | 'qanda' | 'eachuser' | 'single' | 'blog' | 'other';

const KINDS = new Set<string>(['news', 'general', 'qanda', 'eachuser', 'single', 'blog']);

export function forumKind(forum: Pick<MoodleForum, 'type'>): ForumKind {
	return KINDS.has(forum.type) ? (forum.type as ForumKind) : 'other';
}

/**
 * The forum the Forums tab features: the teachers' announcements, where exam rooms, results and
 * cancellations are posted. Moodle creates one news forum per course.
 */
export function announcementsForum(forums: readonly MoodleForum[]): MoodleForum | undefined {
	return forums.find((forum) => forumKind(forum) === 'news');
}

/**
 * The other forums, busiest first: teachers often create topic forums and leave them empty, and
 * those sink to the bottom.
 */
export function otherForums(
	forums: readonly MoodleForum[],
	featured: MoodleForum | undefined
): MoodleForum[] {
	return forums
		.filter((forum) => forum !== featured)
		.sort((a, b) => (b.numDiscussions ?? 0) - (a.numDiscussions ?? 0));
}

/** When a discussion last changed: its latest post, else its opening post. In epoch seconds. */
export function lastActivity(discussion: MoodleForumDiscussion): number {
	return discussion.timeModified || discussion.modified || discussion.created;
}

/**
 * Moodle's own listing order, so a page of results and the list agree: pinned first, then
 * starred, then by latest activity.
 */
export function compareDiscussions(a: MoodleForumDiscussion, b: MoodleForumDiscussion): number {
	return (
		Number(b.pinned) - Number(a.pinned) ||
		Number(b.starred) - Number(a.starred) ||
		lastActivity(b) - lastActivity(a) ||
		b.discussion - a.discussion
	);
}

/** The most recent announcements, whether pinned or not: "latest" means latest activity. */
export function latestDiscussions(
	discussions: readonly MoodleForumDiscussion[],
	count: number
): MoodleForumDiscussion[] {
	return [...discussions]
		.sort((a, b) => lastActivity(b) - lastActivity(a) || b.discussion - a.discussion)
		.slice(0, count);
}

/**
 * Whether the opening post carries files. Moodle sends the `attachment` flag as a boolean, which
 * arrives as the text "false" when there are none.
 */
export function hasAttachments(discussion: MoodleForumDiscussion): boolean {
	if (discussion.attachments.length > 0) return true;
	return !['', '0', 'false'].includes(discussion.attachment.trim().toLowerCase());
}

/** A discussion's id: `discussion`, which older Moodle answers leave at 0 in favour of `id`. */
export function discussionId(discussion: MoodleForumDiscussion): number {
	return discussion.discussion || discussion.id;
}

/** One post of a thread, with the replies it received in the order they were written. */
export interface ThreadNode {
	post: MoodleForumPost;
	children: ThreadNode[];
	/** Every reply below it, at any depth. */
	descendants: number;
}

/**
 * The thread as a tree: posts whose parent isn't in the list (the opening post, or a reply whose
 * parent was deleted for good) are roots, and each post's replies are in the order written.
 */
export function buildThread(posts: readonly MoodleForumPost[]): ThreadNode[] {
	const byTime = [...posts].sort((a, b) => a.timeCreated - b.timeCreated || a.id - b.id);
	const nodes = new Map<number, ThreadNode>();
	for (const post of byTime) nodes.set(post.id, { post, children: [], descendants: 0 });
	const roots: ThreadNode[] = [];
	for (const post of byTime) {
		const node = nodes.get(post.id) as ThreadNode;
		const parent = post.parentId === null ? undefined : nodes.get(post.parentId);
		if (parent === undefined || parent === node) roots.push(node);
		else parent.children.push(node);
	}
	const count = (node: ThreadNode): number =>
		(node.descendants = node.children.reduce((sum, child) => sum + 1 + count(child), 0));
	roots.forEach(count);
	return roots;
}

/** The opening post: the first one without a parent, else the oldest. */
export function openingPost(posts: readonly MoodleForumPost[]): MoodleForumPost | undefined {
	return (
		posts.find((post) => post.parentId === null || !post.hasParent) ??
		[...posts].sort((a, b) => a.timeCreated - b.timeCreated)[0]
	);
}

/**
 * A reply's subject as Moodle writes it ("Ri: …" in Italian), else "Re: subject", never stacking
 * the prefix.
 */
export function replySubject(post: Pick<MoodleForumPost, 'subject' | 'replySubject'>): string {
	if (post.replySubject.trim() !== '') return post.replySubject;
	const subject = post.subject.trim();
	return /^(re|ri):/i.test(subject) ? subject : `Re: ${subject}`;
}

/** The limits a forum sets on a post's attachments. */
export interface AttachmentLimits {
	/** How many files a post may carry; 0 means none. */
	maxFiles: number;
	/** The largest file in bytes; 0 means no limit of the forum's own. */
	maxBytes: number;
}

/**
 * The forum's limits: its own, else the site's for this user (Moodle's 0 means "the course or site
 * limit", which the student's upload limit reflects).
 */
export function attachmentLimits(
	forum: Pick<MoodleForum, 'maxAttachments' | 'maxBytes'>,
	siteMaxBytes: number | undefined
): AttachmentLimits {
	const site = siteMaxBytes !== undefined && siteMaxBytes > 0 ? siteMaxBytes : 0;
	const own = forum.maxBytes > 0 ? forum.maxBytes : 0;
	return {
		maxFiles: Math.max(0, forum.maxAttachments),
		maxBytes: own > 0 && site > 0 ? Math.min(own, site) : own || site
	};
}

export type AttachmentProblem =
	{ kind: 'too_many'; max: number } | { kind: 'too_big'; name: string; max: number };

/**
 * Which of `adding` fit next to the `kept` files: files over the size limit are refused, and the
 * rest are taken in order until the post is full.
 */
export function admitFiles<F extends { name: string; size: number }>(
	adding: readonly F[],
	kept: number,
	limits: AttachmentLimits
): { accepted: F[]; problems: AttachmentProblem[] } {
	const accepted: F[] = [];
	const problems: AttachmentProblem[] = [];
	let room = limits.maxFiles - kept;
	let full = false;
	for (const file of adding) {
		if (limits.maxBytes > 0 && file.size > limits.maxBytes) {
			problems.push({ kind: 'too_big', name: file.name, max: limits.maxBytes });
		} else if (room <= 0) {
			full = true;
		} else {
			accepted.push(file);
			room--;
		}
	}
	if (full) problems.push({ kind: 'too_many', max: limits.maxFiles });
	return { accepted, problems };
}

/**
 * Whether the student may follow discussions here: not where Moodle forces the subscription (news
 * forums by default) nor where the teacher switched it off.
 */
export function subscriptionMode(
	forum: Pick<MoodleForum, 'forceSubscribe'>
): 'optional' | 'forced' | 'disabled' {
	if (forum.forceSubscribe === 1) return 'forced';
	if (forum.forceSubscribe === 3) return 'disabled';
	return 'optional';
}

/**
 * Whether a failure means the discussion is gone (deleted, or moved out of reach): Moodle answers
 * a missing record with an exception, not a 404.
 */
export function isGone(error: DataError | undefined): boolean {
	if (error === undefined) return false;
	if (error.kind === 'not_found') return true;
	return (
		error.kind === 'rejected' &&
		/invalidrecord|unable to find|invalid(?:discussion|post)id/i.test(
			`${error.code ?? ''} ${error.message}`
		)
	);
}
