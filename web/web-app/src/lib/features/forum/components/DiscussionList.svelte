<script lang="ts">
	import type { Attachment } from 'svelte/attachments';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconChatsDuotone from '~icons/ph/chats-duotone';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { coursePath } from '#lib/features/course/links.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { discussionId } from '../forum.ts';
	import { getForum } from '../forumState.svelte.ts';
	import DiscussionRow from './DiscussionRow.svelte';
	import DiscussionsSkeleton from './DiscussionsSkeleton.svelte';

	interface Props {
		/** The open discussion, highlighted in the list. */
		selected: number | undefined;
	}

	let { selected }: Props = $props();

	const forum = getForum();
	const pages = $derived(forum.pages);
	const canCreate = $derived(forum.forum?.canCreateDiscussions === true);
	/* Beside an open discussion that failed the same way, the thread's notice speaks for both. */
	const deferred = $derived(
		forum.wide &&
			selected !== undefined &&
			pages.error !== undefined &&
			forum.threadProblem?.kind === pages.error.kind
	);
	const updatedAt = $derived(pages.updatedAt ? new Date(pages.updatedAt) : undefined);
	const total = $derived(forum.total);

	/* The next page loads as the end of the list comes near, whichever pane scrolls. */
	const nearEnd: Attachment<HTMLElement> = (sentinel) => {
		const observer = new IntersectionObserver(
			([entry]) => {
				if (entry?.isIntersecting && forum.moreError === undefined) void forum.loadMore();
			},
			{ rootMargin: '400px 0px' }
		);
		observer.observe(sentinel);
		return () => observer.disconnect();
	};
</script>

<div class="list">
	<div class="toolbar">
		<!-- Focus lands here when the open discussion is deleted: the count confirms it. -->
		<p class="count" id="forum-{forum.id}-count" tabindex="-1" aria-live="polite">
			{#if pages.data !== undefined}
				{#if total > 0}
					{m.forum_discussion_count({ count: total })}
				{:else}
					<span class="sr-only">{m.forum_discussions_none()}</span>
				{/if}
			{/if}
		</p>
		<IconButton
			icon={IconArrowClockwise}
			label={m.forum_refresh()}
			size="sm"
			loading={pages.refreshing}
			onclick={forum.refresh}
		/>
	</div>

	{#if pages.error && pages.data && !deferred}
		<ErrorState
			variant="stale"
			message={errorMessage(pages.error)}
			{updatedAt}
			onretry={pages.refresh}
			retrying={pages.refreshing}
		/>
	{/if}

	{#if pages.status === 'error' && pages.error && pages.data === undefined}
		<ErrorState
			title={m.forum_discussions_failed()}
			message={errorMessage(pages.error)}
			onretry={pages.refresh}
			retrying={pages.refreshing}
		/>
	{:else if pages.data === undefined}
		<DiscussionsSkeleton />
	{:else if forum.discussions.length === 0}
		<EmptyState
			icon={IconChatsDuotone}
			title={m.forum_discussions_empty_title()}
			body={canCreate
				? m.forum_discussions_empty_can_create()
				: m.forum_discussions_empty_readonly()}
		/>
	{:else}
		<ol class="rows">
			{#each forum.discussions as discussion (discussionId(discussion))}
				{@const id = discussionId(discussion)}
				<li>
					<DiscussionRow
						{discussion}
						href={coursePath(forum.courseId, 'forum', forum.id, id)}
						selected={selected === id}
						onopen={() => (forum.arrival = forum.wide ? id : undefined)}
					/>
				</li>
			{/each}
		</ol>
		{#if !pages.data.complete}
			<div class="more" {@attach nearEnd}>
				{#if forum.moreError}
					<p class="more-error" role="alert">{errorMessage(forum.moreError)}</p>
				{/if}
				<Button variant="ghost" size="sm" loading={forum.loadingMore} onclick={forum.loadMore}>
					{forum.moreError ? m.common_retry() : m.forum_discussions_more()}
				</Button>
			</div>
		{/if}
	{/if}
</div>

<style>
	.list {
		display: grid;
		gap: var(--sp-3);
		align-content: start;
	}

	.toolbar {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 8px;
		min-block-size: 36px;
	}

	.count {
		color: var(--text-muted);
		font: var(--type-ui);
		border-radius: var(--r-xs);
	}

	.count:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.rows {
		display: grid;
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.rows > li {
		content-visibility: auto;
		contain-intrinsic-size: auto 118px;
	}

	.more {
		display: grid;
		justify-items: center;
		gap: 6px;
		padding-block: var(--sp-2);
	}

	.more-error {
		color: var(--danger);
		font: var(--type-meta);
		text-align: center;
	}
</style>
