<script lang="ts">
	import { onMount, tick, untrack } from 'svelte';
	import { SvelteSet } from 'svelte/reactivity';
	import { afterNavigate, goto } from '$app/navigation';
	import { page } from '$app/state';
	import type { ResolvedPathname } from '$app/types';
	import type { MoodleForumPost } from '@mybicocca/data-api/elearning';
	import IconChatsDuotone from '~icons/ph/chats-duotone';
	import IconLock from '~icons/ph/lock-simple';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { toDataError, type DataError } from '#lib/data/errors.ts';
	import { accountKey } from '#lib/data/keys.ts';
	import { mutate, query, setQueryData } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { coursePath } from '#lib/features/course/links.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import { isTypingTarget } from '#lib/ui/platform.ts';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import { buildThread, isGone, openingPost, type ThreadNode } from '../forum.ts';
	import { getForum, knownFollowing } from '../forumState.svelte.ts';
	import { discussionPostsQuery, discussionsKey, postsKey } from '../queries.ts';
	import { ThreadVisit } from '../visit.ts';
	import { deletePost, markViewed } from '../writes.ts';
	import AuthorAvatar from './AuthorAvatar.svelte';
	import Composer, { type ComposerResult } from './Composer.svelte';
	import PostCard from './PostCard.svelte';
	import ThreadHeader from './ThreadHeader.svelte';
	import ThreadSkeleton from './ThreadSkeleton.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		discussionId: number;
	}

	let { discussionId }: Props = $props();

	/** Replies indent up to this depth; deeper ones line up and say whom they answer. */
	const MAX_DEPTH = 4;

	const forum = getForum();
	const wide = $derived(forum.wide);
	const posts = query(() => {
		const scope = session.current;
		return scope && discussionPostsQuery(scope, discussionId);
	});

	/* Deleted elsewhere: what the cache still shows is no use. */
	const gone = $derived(isGone(posts.error));
	const summary = $derived(forum.discussion(discussionId));
	const list = $derived(posts.data?.posts ?? []);
	const roots = $derived(buildThread(list));
	const opening = $derived(openingPost(list));
	const byId = $derived(new Map(list.map((post) => [post.id, post])));
	const subject = $derived(
		summary?.subject || summary?.name || opening?.subject || m.forum_discussion_fallback()
	);
	const locked = $derived(summary?.locked ?? false);
	const canReply = $derived(
		!locked && !gone && (summary?.canReply ?? opening?.capabilities.reply ?? false)
	);
	const replies = $derived(
		posts.data === undefined ? (summary?.numReplies ?? 0) : Math.max(0, list.length - 1)
	);
	const starter = $derived(
		opening !== undefined
			? { name: opening.author.fullName ?? m.forum_author_unknown(), at: opening.timeCreated }
			: summary !== undefined
				? { name: summary.userFullName, at: summary.created }
				: undefined
	);
	const me = $derived(session.current?.account.learning?.userId);
	const following = $derived.by(() => {
		const scope = session.current;
		return scope && knownFollowing(scope, discussionId);
	});
	const forumHref = $derived(coursePath(forum.courseId, 'forum', forum.id));
	const threadHref = $derived(coursePath(forum.courseId, 'forum', forum.id, discussionId));

	const replyId = $derived(Number(page.url.searchParams.get('reply')) || undefined);
	const editId = $derived(Number(page.url.searchParams.get('edit')) || undefined);
	const replyTarget = $derived(replyId === undefined ? undefined : byId.get(replyId));
	const editTarget = $derived(
		editId === undefined
			? undefined
			: byId.get(editId)?.capabilities.edit
				? byId.get(editId)
				: undefined
	);

	const collapsed = new SvelteSet<number>();
	let highlight = $state<number>();
	let starError = $state.raw<DataError>();
	let followError = $state.raw<DataError>();
	let followBusy = $state(false);
	let deleting = $state.raw<MoodleForumPost>();
	let confirmDelete = $state(false);
	let composerPushed = false;
	/** The post just sent: it takes the focus once its composer has closed. */
	let sent: number | undefined;
	/** Set when the thread was opened beside the list: its title takes the focus once shown. */
	let titleFocus = $state(false);

	/* The thread is mounted per discussion: what an earlier mount of it found new is kept. */
	const visit = new ThreadVisit(untrack(() => forum.newPosts.has(discussionId)));
	const fresh = $derived(forum.newPosts.get(discussionId) ?? new Set<number>());
	$effect(() => {
		const found = visit.observe({
			posts: posts.data?.posts,
			refreshing: posts.refreshing,
			failed: posts.error !== undefined,
			updatedAt: posts.updatedAt
		});
		if (found === undefined) return;
		untrack(() => {
			forum.newPosts.set(discussionId, found);
			const scope = session.current;
			if (scope !== undefined) markViewed(scope, discussionId);
		});
	});

	/* The open discussion's posts are on screen: the list stops counting them as unread. */
	const listedUnread = $derived((summary?.numUnread ?? 0) > 0);
	$effect(() => {
		if (listedUnread) untrack(() => forum.markRead(discussionId));
	});

	/* Beside the list, a problem the list shares is told once, here, and Retry reloads both. */
	onMount(() =>
		forum.showThread(() => (posts.data === undefined || gone ? undefined : posts.error))
	);
	const sharedProblem = $derived(
		wide &&
			forum.pages.data !== undefined &&
			forum.pages.error !== undefined &&
			forum.pages.error.kind === posts.error?.kind
	);
	const updatedAt = $derived.by(() => {
		const times = [posts.updatedAt, sharedProblem ? forum.pages.updatedAt : undefined];
		const known = times.filter((time) => time !== undefined);
		return known.length === 0 ? undefined : new Date(Math.min(...known));
	});
	const retrying = $derived(posts.refreshing || (sharedProblem && forum.pages.refreshing));
	async function retry() {
		await Promise.all([posts.refresh(), sharedProblem ? forum.refresh() : undefined]);
	}

	$effect(() => {
		if (!titleFocus || starter === undefined) return;
		titleFocus = false;
		void tick().then(() =>
			document.getElementById(`thread-${discussionId}-title`)?.focus({ preventScroll: true })
		);
	});

	/* Focus after a navigation is decided here, once the page it leads to is on screen. */
	afterNavigate(({ from, to, type }) => {
		if (type === 'enter' || from === null || to === null) return;
		if (forum.arrival === discussionId) {
			forum.arrival = undefined;
			titleFocus = wide;
		}
		const samePage = from.url.pathname === to.url.pathname;
		const composing = to.url.searchParams.has('reply') || to.url.searchParams.has('edit');
		composerPushed = (type === 'goto' || type === 'link') && samePage && composing;
		if (!samePage || composing) return;
		/* A composer closed: the post it sent takes the focus, or else the post it was opened from. */
		const closed = Number(from.url.searchParams.get('reply') ?? from.url.searchParams.get('edit'));
		const arrived = sent;
		sent = undefined;
		if (arrived !== undefined) void focusPost(arrived, true);
		else if (closed > 0) void focusPost(closed, false);
	});

	/* The composer is part of the address, so Back closes it and a reload reopens it. */
	function composerHref(kind?: 'reply' | 'edit', postId?: number): ResolvedPathname {
		const query = kind === undefined ? '' : `?${kind}=${postId}`;
		return `${threadHref}${query}` as ResolvedPathname;
	}

	function openComposer(kind: 'reply' | 'edit', post: MoodleForumPost) {
		if (post.parentId !== null) collapsed.delete(post.parentId);
		void goto(composerHref(kind, post.id), { reset: false });
	}

	function closeComposer() {
		if (composerPushed) history.back();
		else void goto(composerHref(), { replace: true, reset: false });
	}

	async function focusPost(postId: number, reveal: boolean) {
		await tick();
		const element = document.getElementById(`post-${postId}`);
		if (element === null) return;
		if (reveal) {
			const reduced = reducedMotion.current;
			element.scrollIntoView({ block: 'nearest', behavior: reduced ? 'instant' : 'smooth' });
		}
		element.focus({ preventScroll: true });
	}

	function submitted(result: ComposerResult) {
		if (result.kind === 'new') return;
		const scope = session.current;
		if (result.kind === 'reply' && scope !== undefined && posts.data !== undefined) {
			const loaded = posts.data;
			setQueryData(postsKey(scope, discussionId), (current: typeof loaded | undefined) => {
				const base = current ?? loaded;
				return base.posts.some((post) => post.id === result.postId)
					? base
					: { ...base, posts: [...base.posts, result.post] };
			});
		}
		highlight = result.postId;
		toast.show({
			message: result.kind === 'reply' ? m.forum_reply_posted() : m.forum_post_saved(),
			tone: 'success'
		});
		sent = result.postId;
		closeComposer();
		setTimeout(() => {
			if (highlight === result.postId) highlight = undefined;
		}, 2600);
	}

	async function toggleStar() {
		if (summary === undefined) return;
		starError = await forum.star(discussionId, !summary.starred);
	}

	async function toggleFollow() {
		followBusy = true;
		const target = following !== true;
		followError = await forum.follow(discussionId, target);
		followBusy = false;
		if (followError === undefined) {
			toast.show({
				message: target ? m.forum_followed() : m.forum_unfollowed(),
				tone: 'success'
			});
		}
	}

	function askDelete(post: MoodleForumPost) {
		deleting = post;
		confirmDelete = true;
	}

	async function remove() {
		const scope = session.current;
		const target = deleting;
		if (scope === undefined || target === undefined) return;
		const whole = target.id === opening?.id;
		await mutate(() => deletePost(scope, target.id), {
			invalidate: [
				discussionsKey(scope, forum.id),
				accountKey(scope.account, 'moodle.forum.getForumsByCourses', forum.courseId),
				...(whole ? [] : [postsKey(scope, discussionId)])
			]
		});
		if (whole) {
			forum.forget(discussionId);
			toast.show({ message: m.forum_discussion_deleted(), tone: 'success' });
			// The dialog's opener went with the thread: the list's count, one lower, takes the focus.
			await goto(forumHref, { replace: true, reset: false });
			document.getElementById(`forum-${forum.id}-count`)?.focus();
			return;
		}
		const loaded = posts.data;
		if (loaded !== undefined) {
			setQueryData(postsKey(scope, discussionId), (current: typeof loaded | undefined) => {
				const base = current ?? loaded;
				return { ...base, posts: base.posts.filter((post) => post.id !== target.id) };
			});
		}
		toast.show({ message: m.forum_post_deleted(), tone: 'success' });
	}

	/* The dialog's opener went with the post: the post it replied to takes the focus. */
	function afterDelete(): HTMLElement | null {
		const parent = deleting?.parentId ?? opening?.id;
		return parent === undefined ? null : document.getElementById(`post-${parent}`);
	}

	registerShortcuts(() => [
		...(canReply ? [{ keys: [['r']], label: m.forum_keys_reply() }] : []),
		...(list.some((post) => post.capabilities.edit)
			? [{ keys: [['e']], label: m.forum_keys_edit() }]
			: []),
		...(summary?.canFavourite ? [{ keys: [['s']], label: m.forum_star() }] : [])
	]);

	/* r replies to the post in focus (else to the discussion), e edits it, s stars the discussion. */
	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.ctrlKey || event.metaKey || event.altKey) return;
		if (isTypingTarget(event.target) || document.querySelector('dialog:modal')) return;
		if (replyId !== undefined || editId !== undefined) return;
		const key = event.key.toLowerCase();
		const focused = (document.activeElement as HTMLElement | null)?.closest<HTMLElement>(
			'[data-post]'
		);
		const post = byId.get(Number(focused?.dataset.post)) ?? opening;
		if (key === 'r' && canReply && post !== undefined) {
			event.preventDefault();
			openComposer('reply', post);
		} else if (key === 'e' && post?.capabilities.edit === true && focused) {
			event.preventDefault();
			openComposer('edit', post);
		} else if (key === 's' && summary?.canFavourite) {
			event.preventDefault();
			void toggleStar();
		}
	}

	const deleteMessage = $derived(
		deleting !== undefined && deleting.id === opening?.id
			? replies > 0
				? m.forum_delete_discussion_body({ count: replies })
				: m.forum_delete_discussion_alone()
			: m.forum_delete_post_body()
	);
</script>

<svelte:window onkeydown={keydown} />

<svelte:head>
	<title>{subject} · {forum.name ?? m.forum_title_fallback()} · {m.app_name()}</title>
</svelte:head>

{#snippet header()}
	<ThreadHeader
		{discussionId}
		{subject}
		{starter}
		{replies}
		{locked}
		{gone}
		{canReply}
		{following}
		{followBusy}
		refreshing={posts.refreshing}
		onstar={toggleStar}
		onfollow={toggleFollow}
		onrefresh={posts.refresh}
		onreply={opening === undefined ? undefined : () => opening && openComposer('reply', opening)}
	/>
{/snippet}

{#snippet post(node: ThreadNode, depth: number, parentAuthor: string | undefined)}
	{@const current = node.post}
	{#if editTarget?.id === current.id}
		<Composer
			mode={{ kind: 'edit', post: current }}
			onsubmitted={submitted}
			oncancel={closeComposer}
		/>
	{:else}
		<PostCard
			post={current}
			courseId={forum.courseId}
			opening={current.id === opening?.id}
			mine={me !== undefined && current.author.id === me}
			fresh={fresh.has(current.id)}
			highlighted={highlight === current.id}
			replyingTo={depth > MAX_DEPTH ? parentAuthor : undefined}
			canReply={canReply && current.capabilities.reply}
			replies={node.children.length === 0
				? undefined
				: {
						count: node.descendants,
						open: !collapsed.has(current.id),
						controls: `replies-${current.id}`,
						ontoggle: () =>
							collapsed.has(current.id) ? collapsed.delete(current.id) : collapsed.add(current.id)
					}}
			onreply={() => openComposer('reply', current)}
			onedit={() => openComposer('edit', current)}
			ondelete={() => askDelete(current)}
		/>
	{/if}
	{#if replyTarget?.id === current.id && current.id !== opening?.id}
		<div class="inline-composer">
			<Composer
				mode={{ kind: 'reply', parent: current }}
				onsubmitted={submitted}
				oncancel={closeComposer}
			/>
		</div>
	{/if}
	{#if node.children.length > 0 && !collapsed.has(current.id)}
		<ol
			class={['replies', { flat: depth >= MAX_DEPTH }]}
			id="replies-{current.id}"
			aria-label={m.forum_replies_to({ name: current.author.fullName ?? '' })}
		>
			{#each node.children as child (child.post.id)}
				<li class="reply">
					{@render post(child, depth + 1, current.author.fullName ?? undefined)}
				</li>
			{/each}
		</ol>
	{/if}
{/snippet}

{#if !wide}{@render header()}{/if}

<div class={['thread', { stacked: !wide }]} data-discussion={discussionId}>
	{#if wide}{@render header()}{/if}

	{#if starError || followError}
		<Banner
			tone="danger"
			role="alert"
			title={m.forum_action_failed()}
			message={errorMessage((starError ?? followError) as DataError)}
			ondismiss={() => {
				starError = undefined;
				followError = undefined;
			}}
		/>
	{/if}

	{#if posts.error && posts.data && !gone}
		<ErrorState
			variant="stale"
			message={errorMessage(posts.error)}
			{updatedAt}
			onretry={retry}
			{retrying}
		/>
	{/if}

	{#if gone}
		<EmptyState
			size="page"
			icon={IconChatsDuotone}
			title={m.forum_thread_gone_title()}
			body={m.forum_thread_gone_body()}
		>
			{#snippet action()}
				<Button href={forumHref}>{m.forum_back_to_list()}</Button>
			{/snippet}
		</EmptyState>
	{:else if posts.status === 'error' && posts.error && posts.data === undefined}
		<ErrorState
			title={m.forum_thread_failed()}
			message={errorMessage(posts.error)}
			onretry={posts.refresh}
			retrying={posts.refreshing}
		/>
	{:else if posts.data === undefined}
		<ThreadSkeleton />
	{:else}
		<div class="posts">
			{#each roots as root (root.post.id)}
				{@render post(root, 0, undefined)}
			{/each}
		</div>

		{#if opening && replyTarget?.id === opening.id}
			<Composer
				mode={{ kind: 'reply', parent: opening }}
				onsubmitted={submitted}
				oncancel={closeComposer}
			/>
		{:else if canReply && opening}
			<button
				type="button"
				class="reply-box"
				onclick={() => opening && openComposer('reply', opening)}
			>
				{#if session.current}
					<AuthorAvatar name={session.current.account.displayName} size={32} />
				{/if}
				<span>{m.forum_reply_prompt()}</span>
			</button>
		{:else if locked}
			<p class="closed"><IconLock aria-hidden="true" />{m.forum_locked_notice()}</p>
		{/if}
	{/if}
</div>

<ConfirmDialog
	bind:open={confirmDelete}
	title={deleting?.id === opening?.id
		? m.forum_delete_discussion_title()
		: m.forum_delete_post_title()}
	message={deleteMessage}
	confirmLabel={m.forum_delete()}
	tone="danger"
	onconfirm={remove}
	returnFocus={afterDelete}
	errorMessage={(error) => errorMessage(toDataError(error))}
/>

<style>
	.thread {
		display: grid;
		gap: var(--sp-4);
		max-inline-size: 780px;
		min-inline-size: 0;
		container: thread / inline-size;
	}

	.stacked {
		margin-block-start: var(--sp-5);
	}

	.posts {
		display: grid;
		gap: 0;
		min-inline-size: 0;
	}

	.replies {
		--indent: 28px;
		display: grid;
		gap: 10px;
		margin: 10px 0 0;
		padding: 0 0 0 var(--indent);
		list-style: none;
	}

	@container thread (width < 520px) {
		.replies {
			--indent: 18px;
		}
	}

	.replies.flat {
		--indent: 0px;
	}

	.reply {
		position: relative;
		min-inline-size: 0;
	}

	/* The rail down the left of a parent's replies, with an elbow into each reply. */
	.reply::before,
	.reply::after {
		content: '';
		position: absolute;
		inset-inline-start: calc(var(--indent) / -2 - 1px);
		pointer-events: none;
	}

	.reply::before {
		inset-block: -10px 0;
		border-inline-start: 2px solid var(--hairline-strong);
	}

	.reply:last-child::before {
		display: none;
	}

	.reply::after {
		inset-block-start: -10px;
		block-size: 42px;
		inline-size: calc(var(--indent) / 2 - 2px);
		border-inline-start: 2px solid var(--hairline-strong);
		border-block-end: 2px solid var(--hairline-strong);
		border-end-start-radius: 12px;
	}

	.flat > .reply::before,
	.flat > .reply::after {
		display: none;
	}

	.inline-composer {
		margin-block-start: 10px;
	}

	.reply-box {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 56px;
		margin-block-start: var(--sp-2);
		padding: 10px 16px 10px 12px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text-muted);
		font: var(--type-body);
		text-align: start;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.reply-box:hover {
			box-shadow: inset 0 0 0 1px var(--border-strong);
			color: var(--text);
		}
	}

	.closed {
		display: flex;
		align-items: center;
		gap: 8px;
		padding: 12px 16px;
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.closed :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
	}
</style>
