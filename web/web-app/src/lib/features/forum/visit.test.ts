import { describe, expect, it } from 'vitest';
import { PREFETCH_WINDOW_MS, ThreadVisit, type PostsSnapshot } from './visit.ts';

const OPENED = 1_000_000;

const posts = [
	{ id: 1, unread: false },
	{ id: 2, unread: true },
	{ id: 3, unread: null },
	{ id: 4, unread: true }
];

function snapshot(fields: Partial<PostsSnapshot>): PostsSnapshot {
	return { posts, refreshing: false, failed: false, updatedAt: OPENED + 500, ...fields };
}

describe('ThreadVisit', () => {
	it('waits past posts cached on an earlier visit for the reload, then logs once', () => {
		const visit = new ThreadVisit(false, OPENED);
		expect(visit.observe(snapshot({ updatedAt: OPENED - 60_000 }))).toBeUndefined();
		expect(
			visit.observe(snapshot({ updatedAt: OPENED - 60_000, refreshing: true }))
		).toBeUndefined();
		expect(visit.observe(snapshot({}))).toEqual(new Set([2, 4]));
		expect(visit.observe(snapshot({ refreshing: true }))).toBeUndefined();
		expect(visit.observe(snapshot({}))).toBeUndefined();
	});

	it('takes posts prefetched just before the thread opened as loaded for it', () => {
		const prefetched = OPENED - PREFETCH_WINDOW_MS + 1;
		expect(new ThreadVisit(false, OPENED).observe(snapshot({ updatedAt: prefetched }))).toEqual(
			new Set([2, 4])
		);
		const older = OPENED - PREFETCH_WINDOW_MS - 1;
		expect(new ThreadVisit(false, OPENED).observe(snapshot({ updatedAt: older }))).toBeUndefined();
	});

	it('keeps waiting through a failed reload, and logs when a retry succeeds', () => {
		const visit = new ThreadVisit(false, OPENED);
		const cached = OPENED - 60_000;
		visit.observe(snapshot({ updatedAt: cached, refreshing: true }));
		expect(visit.observe(snapshot({ updatedAt: cached, failed: true }))).toBeUndefined();
		visit.observe(snapshot({ updatedAt: cached, refreshing: true, failed: true }));
		expect(visit.observe(snapshot({}))).toEqual(new Set([2, 4]));
	});

	it('waits for a cold load to bring the posts', () => {
		const visit = new ThreadVisit(false, OPENED);
		expect(visit.observe(snapshot({ posts: undefined, updatedAt: undefined }))).toBeUndefined();
		expect(visit.observe(snapshot({}))).toEqual(new Set([2, 4]));
	});

	it('never logs again once this visit has', () => {
		expect(new ThreadVisit(true, OPENED).observe(snapshot({}))).toBeUndefined();
	});
});
