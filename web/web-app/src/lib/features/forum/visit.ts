/** Posts loaded this shortly before a thread opened (on hover, by the route) were loaded to open it. */
export const PREFETCH_WINDOW_MS = 5_000;

/** A discussion's posts query at one moment. */
export interface PostsSnapshot {
	posts: readonly { id: number; unread: boolean | null }[] | undefined;
	refreshing: boolean;
	failed: boolean;
	/** When the posts were loaded, in epoch milliseconds. */
	updatedAt: number | undefined;
}

/**
 * One visit to a discussion. Logging the view marks the discussion read on Moodle, so it waits
 * for posts loaded for this visit: they say which posts are new. Posts cached earlier would still
 * flag what was read since.
 */
export class ThreadVisit {
	#phase: 'waiting' | 'loading' | 'logged';
	readonly #openedAt: number;

	/** `logged`: the view was already logged in this visit, by an earlier mount of the thread. */
	constructor(logged: boolean, openedAt = Date.now()) {
		this.#phase = logged ? 'logged' : 'waiting';
		this.#openedAt = openedAt;
	}

	/** Once posts loaded for this visit are in hand: the ones new to it, and the view is to be logged. */
	observe(snapshot: PostsSnapshot): ReadonlySet<number> | undefined {
		if (this.#phase === 'logged') return undefined;
		if (snapshot.refreshing) {
			this.#phase = 'loading';
			return undefined;
		}
		const forVisit =
			this.#phase === 'loading' || (snapshot.updatedAt ?? 0) >= this.#openedAt - PREFETCH_WINDOW_MS;
		if (!forVisit || snapshot.posts === undefined || snapshot.failed) return undefined;
		this.#phase = 'logged';
		return new Set(snapshot.posts.filter((post) => post.unread === true).map((post) => post.id));
	}
}
