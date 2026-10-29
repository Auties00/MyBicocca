import { createContext } from 'svelte';
import { SvelteMap } from 'svelte/reactivity';
import type { MoodleForum, MoodleForumDiscussion } from '@mybicocca/data-api/elearning';
import { toDataError, type DataError } from '#lib/data/errors.ts';
import { query, setQueryData, type Query } from '#lib/data/query.svelte.ts';
import { session, type SessionScope } from '#lib/data/session.svelte.ts';
import { stripMlang } from '#lib/features/elearning/mlang.ts';
import { getLocale } from '#lib/paraglide/runtime.js';
import { compareDiscussions, discussionId, subscriptionMode } from './forum.ts';
import {
	courseForumsQuery,
	discussionsKey,
	DISCUSSIONS_PER_PAGE,
	forumDiscussionsQuery,
	loadDiscussionPage,
	type DiscussionPages
} from './queries.ts';
import { setStarred, setSubscription } from './writes.ts';
import { expanded } from '#lib/ui/media.ts';

/**
 * Whether the student follows a discussion, as far as this visit knows: no Moodle read reports
 * it, only the answers of the writes that change it (following, starring, posting).
 */
const following = new SvelteMap<string, boolean>();

const followKey = (scope: SessionScope, discussion: number) => `${scope.account.id}/${discussion}`;

export function knownFollowing(scope: SessionScope, discussion: number): boolean | undefined {
	return following.get(followKey(scope, discussion));
}

export function rememberFollowing(scope: SessionScope, discussion: number, value: boolean): void {
	following.set(followKey(scope, discussion), value);
}

/** What a composer holds before it is sent. */
export interface ComposerDraft {
	subject: string;
	html: string;
	files: File[];
	/** Names of the edited post's files to keep. */
	kept: string[];
	subscribe: boolean;
	group: string;
}

/**
 * A forum's page: the course's forums (for this one's name, type and permissions), its
 * discussions page by page, and the writes the list shows at once. The forum layout creates it;
 * the thread and the composer read it with `getForum()`.
 */
export class ForumState {
	readonly #courseId: () => number;
	readonly #id: () => number;
	readonly language = getLocale();

	readonly forums: Query<MoodleForum[]>;
	readonly pages: Query<DiscussionPages>;
	readonly forum: MoodleForum | undefined;
	readonly name: string | undefined;
	/** The discussions loaded so far, in Moodle's order. */
	readonly discussions: MoodleForumDiscussion[];
	/** How many discussions the forum has: the list once complete, Moodle's count before. */
	readonly total: number;

	loadingMore = $state(false);
	moreError = $state.raw<DataError>();
	/**
	 * A discussion opened beside the list, or just started: its title takes the focus once the
	 * navigation to it ends, so the keyboard lands in the thread rather than on the forum's heading.
	 */
	arrival = $state<number>();
	#threadProblem = $state.raw<() => DataError | undefined>();
	/**
	 * Unsent drafts, by composer. They live here, not in the composer, so a reply left half written
	 * is still there after a look at another discussion.
	 */
	readonly drafts = new SvelteMap<string, ComposerDraft>();
	/** Each opened discussion's posts that were unread when this visit first showed them. */
	readonly newPosts = new SvelteMap<number, ReadonlySet<number>>();
	/** Whether the list and the open discussion sit side by side, as the layout's ListDetail says. */
	twoPanes = $state<boolean>();

	/** Two panes, or, before the list has been laid out once, the size where they can be. */
	get wide(): boolean {
		return this.twoPanes ?? expanded.current;
	}

	constructor(courseId: () => number, id: () => number) {
		this.#courseId = courseId;
		this.#id = id;
		this.forums = query(() => {
			const scope = session.current;
			return scope && courseForumsQuery(scope, courseId());
		});
		this.pages = query(() => {
			const scope = session.current;
			return scope && forumDiscussionsQuery(scope, id());
		});
		this.forum = $derived(this.forums.data?.find((forum) => forum.id === id()));
		this.name = $derived(
			this.forum === undefined ? undefined : stripMlang(this.forum.name, this.language).trim()
		);
		this.discussions = $derived([...(this.pages.data?.discussions ?? [])].sort(compareDiscussions));
		this.total = $derived(
			this.pages.data?.complete
				? this.discussions.length
				: Math.max(this.forum?.numDiscussions ?? 0, this.discussions.length)
		);
	}

	get id(): number {
		return this.#id();
	}

	get courseId(): number {
		return this.#courseId();
	}

	/** Changes loaded discussions ahead of the server; nothing to change before the list loads. */
	#update(
		scope: SessionScope,
		forumId: number,
		change: (discussion: MoodleForumDiscussion) => MoodleForumDiscussion
	): void {
		const loaded = this.pages.data;
		if (loaded === undefined) return;
		setQueryData<DiscussionPages>(discussionsKey(scope, forumId), (current) => {
			const base = current ?? loaded;
			return { ...base, discussions: base.discussions.map(change) };
		});
	}

	get canFollow(): boolean {
		return this.forum !== undefined && subscriptionMode(this.forum) === 'optional';
	}

	discussion(id: number): MoodleForumDiscussion | undefined {
		return this.pages.data?.discussions.find((entry) => discussionId(entry) === id);
	}

	/** Why the open discussion's posts couldn't reload while older ones stay on screen. */
	get threadProblem(): DataError | undefined {
		return this.#threadProblem?.();
	}

	/** The open thread reports its load problem here; returns the call that withdraws it. */
	showThread(problem: () => DataError | undefined): () => void {
		this.#threadProblem = problem;
		return () => {
			if (this.#threadProblem === problem) this.#threadProblem = undefined;
		};
	}

	refresh = async (): Promise<void> => {
		await Promise.all([this.forums.refresh(), this.pages.refresh()]);
	};

	/** Adds the next page of discussions to the list. */
	loadMore = async (): Promise<void> => {
		const scope = session.current;
		const loaded = this.pages.data;
		if (scope === undefined || loaded === undefined || loaded.complete || this.loadingMore) return;
		const forumId = this.id;
		this.loadingMore = true;
		this.moreError = undefined;
		try {
			const next = await loadDiscussionPage(scope, forumId, loaded.pages);
			setQueryData<DiscussionPages>(discussionsKey(scope, forumId), (current) => {
				const base = current ?? loaded;
				const known = base.discussions.map(discussionId);
				return {
					discussions: [
						...base.discussions,
						...next.filter((entry) => !known.includes(discussionId(entry)))
					],
					pages: base.pages + 1,
					complete: next.length < DISCUSSIONS_PER_PAGE
				};
			});
		} catch (error) {
			this.moreError = toDataError(error);
		} finally {
			this.loadingMore = false;
		}
	};

	/** Takes a deleted discussion off the list at once, before the reload confirms it. */
	forget(id: number): void {
		const scope = session.current;
		const loaded = this.pages.data;
		if (scope === undefined || loaded === undefined) return;
		setQueryData<DiscussionPages>(discussionsKey(scope, this.id), (current) => {
			const base = current ?? loaded;
			return {
				...base,
				discussions: base.discussions.filter((entry) => discussionId(entry) !== id)
			};
		});
	}

	/** The discussion's unread posts were just shown. */
	markRead(id: number): void {
		const scope = session.current;
		if (scope === undefined) return;
		this.#update(scope, this.id, (entry) =>
			discussionId(entry) === id && entry.numUnread > 0 ? { ...entry, numUnread: 0 } : entry
		);
	}

	/** Stars or unstars at once; the list goes back if Moodle refuses. */
	star = async (id: number, starred: boolean): Promise<DataError | undefined> => {
		const scope = session.current;
		if (scope === undefined) return undefined;
		const forumId = this.id;
		const set = (value: boolean) =>
			this.#update(scope, forumId, (entry) =>
				discussionId(entry) === id ? { ...entry, starred: value } : entry
			);
		set(starred);
		try {
			const summary = await setStarred(scope, id, starred);
			rememberFollowing(scope, id, summary.userState.subscribed);
			return undefined;
		} catch (error) {
			set(!starred);
			return toDataError(error);
		}
	};

	follow = async (id: number, subscribed: boolean): Promise<DataError | undefined> => {
		const scope = session.current;
		if (scope === undefined) return undefined;
		try {
			const summary = await setSubscription(scope, this.id, id, subscribed);
			rememberFollowing(scope, id, summary.userState.subscribed);
			return undefined;
		} catch (error) {
			return toDataError(error);
		}
	};
}

export const [getForum, setForum] = createContext<ForumState>();
