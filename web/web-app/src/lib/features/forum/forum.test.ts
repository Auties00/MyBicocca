import { describe, expect, it } from 'vitest';
import type { MoodleForumDiscussion, MoodleForumPost } from '@mybicocca/data-api/elearning';
import {
	admitFiles,
	attachmentLimits,
	buildThread,
	compareDiscussions,
	hasAttachments,
	isGone,
	latestDiscussions,
	openingPost,
	replySubject,
	type ThreadNode
} from './forum.ts';

function post(id: number, parentId: number | null, timeCreated: number): MoodleForumPost {
	return {
		id,
		parentId,
		hasParent: parentId !== null,
		timeCreated,
		subject: `Post ${id}`,
		replySubject: ''
	} as MoodleForumPost;
}

function discussion(
	id: number,
	fields: Partial<Pick<MoodleForumDiscussion, 'pinned' | 'starred' | 'timeModified'>>
): MoodleForumDiscussion {
	return {
		id: id * 10,
		discussion: id,
		pinned: false,
		starred: false,
		timeModified: 0,
		modified: 0,
		created: 0,
		...fields
	} as MoodleForumDiscussion;
}

const shape = (nodes: ThreadNode[]): unknown =>
	nodes.map((node) => [node.post.id, node.descendants, shape(node.children)]);

describe('buildThread', () => {
	it('nests replies under their parents, in the order they were written', () => {
		const thread = buildThread([
			post(4, 1, 40),
			post(1, null, 10),
			post(3, 2, 30),
			post(2, 1, 20),
			post(5, 1, 25)
		]);
		expect(shape(thread)).toEqual([
			[
				1,
				4,
				[
					[2, 1, [[3, 0, []]]],
					[5, 0, []],
					[4, 0, []]
				]
			]
		]);
	});

	it('keeps a reply whose parent is gone as a root of its own', () => {
		const thread = buildThread([post(1, null, 10), post(7, 99, 20)]);
		expect(thread.map((node) => node.post.id)).toEqual([1, 7]);
	});
});

describe('openingPost', () => {
	it('is the post without a parent, else the oldest', () => {
		expect(openingPost([post(2, 1, 20), post(1, null, 10)])?.id).toBe(1);
		expect(openingPost([post(3, 9, 30), post(2, 9, 20)])?.id).toBe(2);
	});
});

describe('replySubject', () => {
	it('prefers Moodle’s own reply subject', () => {
		expect(replySubject({ subject: 'Esame', replySubject: 'Re: Esame' })).toBe('Re: Esame');
	});

	it('never stacks the prefix', () => {
		expect(replySubject({ subject: 'Re: Esame', replySubject: '' })).toBe('Re: Esame');
		expect(replySubject({ subject: 'Ri: Esame', replySubject: '' })).toBe('Ri: Esame');
		expect(replySubject({ subject: ' Esame ', replySubject: '' })).toBe('Re: Esame');
	});
});

describe('compareDiscussions', () => {
	it('lists pinned, then starred, then by latest activity', () => {
		const sorted = [
			discussion(1, { timeModified: 300 }),
			discussion(2, { timeModified: 100, starred: true }),
			discussion(3, { timeModified: 50, pinned: true }),
			discussion(4, { timeModified: 400 })
		].sort(compareDiscussions);
		expect(sorted.map((entry) => entry.discussion)).toEqual([3, 2, 4, 1]);
	});

	it('takes the latest announcements by activity alone, pinned or not', () => {
		const latest = latestDiscussions(
			[
				discussion(1, { timeModified: 300 }),
				discussion(2, { timeModified: 50, pinned: true }),
				discussion(3, { timeModified: 500 })
			],
			2
		);
		expect(latest.map((entry) => entry.discussion)).toEqual([3, 1]);
	});
});

describe('attachmentLimits', () => {
	it('takes the tighter of the forum’s and the site’s size limits', () => {
		expect(attachmentLimits({ maxAttachments: 3, maxBytes: 5_000 }, 2_000)).toEqual({
			maxFiles: 3,
			maxBytes: 2_000
		});
		expect(attachmentLimits({ maxAttachments: 9, maxBytes: 0 }, 8_000)).toEqual({
			maxFiles: 9,
			maxBytes: 8_000
		});
		expect(attachmentLimits({ maxAttachments: 1, maxBytes: 0 }, undefined).maxBytes).toBe(0);
	});
});

describe('admitFiles', () => {
	const limits = { maxFiles: 3, maxBytes: 100 };
	const file = (name: string, size: number) => ({ name, size });

	it('refuses files over the size limit and stops when the post is full', () => {
		const result = admitFiles(
			[file('a.pdf', 50), file('big.zip', 500), file('b.pdf', 10), file('c.pdf', 10)],
			1,
			limits
		);
		expect(result.accepted.map((entry) => entry.name)).toEqual(['a.pdf', 'b.pdf']);
		expect(result.problems).toEqual([
			{ kind: 'too_big', name: 'big.zip', max: 100 },
			{ kind: 'too_many', max: 3 }
		]);
	});

	it('takes any size when the forum sets no limit', () => {
		const result = admitFiles([file('huge.mp4', 10 ** 9)], 0, { maxFiles: 1, maxBytes: 0 });
		expect(result.accepted).toHaveLength(1);
		expect(result.problems).toEqual([]);
	});
});

describe('hasAttachments', () => {
	const flagged = (attachment: string, files = 0) =>
		({ attachment, attachments: Array.from({ length: files }) }) as MoodleForumDiscussion;

	it('reads Moodle’s boolean flag, which arrives as text', () => {
		expect(hasAttachments(flagged('false'))).toBe(false);
		expect(hasAttachments(flagged(''))).toBe(false);
		expect(hasAttachments(flagged('true'))).toBe(true);
		expect(hasAttachments(flagged('1'))).toBe(true);
		expect(hasAttachments(flagged('false', 2))).toBe(true);
	});
});

describe('isGone', () => {
	it('recognises a discussion Moodle can no longer find', () => {
		expect(
			isGone({
				kind: 'rejected',
				service: 'moodle',
				status: 200,
				code: 'invalidrecord',
				message: 'Impossibile trovare il record'
			})
		).toBe(true);
		expect(isGone({ kind: 'not_found', service: 'moodle' })).toBe(true);
		expect(isGone({ kind: 'offline' })).toBe(false);
		expect(
			isGone({ kind: 'rejected', service: 'moodle', status: 200, message: 'Accesso negato' })
		).toBe(false);
	});
});
