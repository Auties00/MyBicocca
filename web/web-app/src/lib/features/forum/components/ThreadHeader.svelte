<script lang="ts">
	import { online } from 'svelte/reactivity/window';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArrowBendUpLeft from '~icons/ph/arrow-bend-up-left';
	import IconBell from '~icons/ph/bell';
	import IconBellSlash from '~icons/ph/bell-slash';
	import IconLock from '~icons/ph/lock-simple';
	import IconPushPin from '~icons/ph/push-pin-fill';
	import IconStar from '~icons/ph/star';
	import IconStarFill from '~icons/ph/star-fill';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { coursePath } from '#lib/features/course/links.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { discussionId as idOf, PENDING_LINE } from '../forum.ts';
	import { getForum } from '../forumState.svelte.ts';
	import PostTime from './PostTime.svelte';

	interface Props {
		discussionId: number;
		subject: string;
		/** Who opened the discussion and when, from its posts or the forum's list. */
		starter: { name: string; at: number } | undefined;
		replies: number;
		locked: boolean;
		/** Deleted or moved: only the subject is left to show. */
		gone: boolean;
		canReply: boolean;
		/** Whether the student follows it, when this visit knows. */
		following: boolean | undefined;
		followBusy: boolean;
		refreshing: boolean;
		onstar: () => void;
		onfollow: () => void;
		onrefresh: () => void;
		/** Replies to the opening post; missing until the posts load. */
		onreply: (() => void) | undefined;
	}

	let {
		discussionId,
		subject,
		starter,
		replies,
		locked,
		gone,
		canReply,
		following,
		followBusy,
		refreshing,
		onstar,
		onfollow,
		onrefresh,
		onreply
	}: Props = $props();

	const forum = getForum();
	/* Two panes: the forum's header stays above, so the thread titles itself. */
	const wide = $derived(forum.wide);
	const summary = $derived(forum.discussion(discussionId));
	const forumHref = $derived(coursePath(forum.courseId, 'forum', forum.id));
	const offline = $derived(online.current === false);

	/* The forum keeps its own order (pinned first, then by activity), which J and K follow too. */
	const steps = $derived.by(() => {
		const ids = forum.discussions.map(idOf);
		const index = ids.indexOf(discussionId);
		if (index < 0) return undefined;
		const href = (id: number | undefined) =>
			id === undefined ? undefined : coursePath(forum.courseId, 'forum', forum.id, id);
		return { previous: href(ids[index - 1]), next: href(ids[index + 1]) };
	});
</script>

{#snippet flags()}
	{#if summary?.pinned || locked}
		<p class="flags">
			{#if summary?.pinned}
				<span class="flag"><IconPushPin aria-hidden="true" />{m.forum_pinned()}</span>
			{/if}
			{#if locked}
				<span class="flag"><IconLock aria-hidden="true" />{m.forum_locked()}</span>
			{/if}
		</p>
	{/if}
{/snippet}

{#snippet details()}
	{#if starter}
		<p class="byline">
			{m.forum_thread_started({ name: starter.name })}
			<PostTime at={starter.at} />
			· {replies === 0 ? m.forum_no_replies() : m.forum_reply_count({ count: replies })}
		</p>
		<div class="tools">
			{#if summary?.canFavourite}
				<IconButton
					icon={summary.starred ? IconStarFill : IconStar}
					label={m.forum_star()}
					pressed={summary.starred}
					variant="outline"
					shortcut={['s']}
					disabled={offline}
					onclick={onstar}
				/>
			{/if}
			{#if forum.canFollow}
				<Button
					variant="outline"
					icon={following ? IconBellSlash : IconBell}
					loading={followBusy}
					disabled={offline}
					onclick={onfollow}
				>
					{following ? m.forum_unfollow() : m.forum_follow()}
				</Button>
			{/if}
			<IconButton
				icon={IconArrowClockwise}
				label={m.forum_refresh_thread()}
				variant="outline"
				loading={refreshing}
				onclick={onrefresh}
			/>
			{#if canReply}
				<span class="spacer"></span>
				<Button
					variant="solid"
					icon={IconArrowBendUpLeft}
					aria-keyshortcuts="R"
					disabled={onreply === undefined}
					onclick={onreply}
				>
					{m.forum_reply()}
				</Button>
			{/if}
		</div>
		{#if offline && (summary?.canFavourite || forum.canFollow)}
			<p class="offline" role="status">
				<IconWifiSlash aria-hidden="true" />{m.forum_offline_tools()}
			</p>
		{/if}
	{:else}
		<!-- In the byline's and the tools' own boxes, so the lines take their final height. -->
		<p class="byline" aria-hidden="true"><Skeleton variant="text" width="44%" /></p>
		<div class="tools" aria-hidden="true">
			<Skeleton width="220px" height="var(--control-h)" radius="var(--r-pill)" />
		</div>
	{/if}
{/snippet}

{#if wide}
	<DetailHeader
		id="thread-{discussionId}-title"
		title={starter === undefined && !gone ? '' : subject}
		loading={{ eyebrow: false, subtitle: false }}
		{steps}
	>
		{#if summary?.pinned || locked || !gone}
			<div class="wide-meta">
				{@render flags()}
				{#if !gone}{@render details()}{/if}
			</div>
		{/if}
	</DetailHeader>
{:else}
	<PageHeader
		eyebrow={forum.name ?? PENDING_LINE}
		title={subject}
		back={{ href: forumHref, label: forum.name ?? m.forum_back() }}
	>
		{#snippet meta()}
			<div class="narrow-meta">
				{@render flags()}
				{#if !gone}{@render details()}{/if}
			</div>
		{/snippet}
	</PageHeader>
{/if}

<style>
	.flags {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
	}

	.flag {
		display: inline-flex;
		align-items: center;
		gap: 5px;
		padding: 3px 10px 3px 8px;
		border-radius: var(--r-pill);
		background: var(--course-container);
		color: var(--course-ink);
		font: var(--type-label);
		font-weight: 600;
	}

	.flag :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.byline {
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.875rem;
	}

	.tools {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px;
		margin-block-start: 4px;
	}

	.spacer {
		flex: 1;
	}

	.offline {
		display: flex;
		align-items: center;
		gap: 6px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.offline :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		flex-shrink: 0;
	}

	.narrow-meta,
	.wide-meta {
		display: grid;
		gap: 8px;
		inline-size: 100%;
	}
</style>
