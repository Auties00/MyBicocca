<script lang="ts">
	import type { MoodleForumDiscussion } from '@mybicocca/data-api/elearning';
	import type { ResolvedPathname } from '$app/types';
	import IconChatCircle from '~icons/ph/chat-circle';
	import IconLock from '~icons/ph/lock-simple';
	import IconPaperclip from '~icons/ph/paperclip';
	import IconPushPin from '~icons/ph/push-pin-fill';
	import IconStar from '~icons/ph/star-fill';
	import { htmlToLine } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { discussionId, hasAttachments, lastActivity } from '../forum.ts';
	import AuthorAvatar from './AuthorAvatar.svelte';
	import PostTime from './PostTime.svelte';

	interface Props {
		discussion: MoodleForumDiscussion;
		href: ResolvedPathname;
		selected: boolean;
		onopen: () => void;
	}

	let { discussion, href, selected, onopen }: Props = $props();

	const preview = $derived(htmlToLine(discussion.message));
	const unread = $derived(discussion.numUnread);
	const latestBy = $derived(
		discussion.numReplies > 0 &&
			discussion.userModifiedFullName !== '' &&
			discussion.userModifiedFullName !== discussion.userFullName
			? discussion.userModifiedFullName
			: undefined
	);
	const byline = $derived(
		latestBy === undefined
			? discussion.userFullName
			: `${discussion.userFullName} · ${m.forum_latest_by({ name: latestBy })}`
	);
	const attachment = $derived(hasAttachments(discussion));
</script>

<a
	class={['row', { pinned: discussion.pinned, unread: unread > 0, selected }]}
	{href}
	aria-current={selected ? 'page' : undefined}
	data-discussion={discussionId(discussion)}
	onclick={onopen}
>
	<AuthorAvatar name={discussion.userFullName} size={40} />
	<span class="main">
		<span class="subject">
			{#if discussion.pinned}
				<IconPushPin class="flag" aria-hidden="true" />
				<span class="sr-only">{m.forum_pinned()}:</span>
			{/if}
			{discussion.subject || discussion.name}
		</span>
		<span class="when"><PostTime at={lastActivity(discussion)} /></span>
		{#if preview}<span class="preview">{preview}</span>{/if}
		<span class="who" title={byline}>{byline}</span>
		<span class="facts">
			{#if unread > 0}
				<span class="unread-badge">
					<span aria-hidden="true">{Math.min(unread, 99)}</span>
					<span class="sr-only">{m.forum_unread_posts({ count: unread })}</span>
				</span>
			{/if}
			{#if discussion.numReplies > 0}
				<span class="fact">
					<IconChatCircle aria-hidden="true" />
					<span aria-hidden="true">{discussion.numReplies}</span>
					<span class="sr-only">{m.forum_reply_count({ count: discussion.numReplies })}</span>
				</span>
			{/if}
			{#if attachment}
				<span class="fact">
					<IconPaperclip aria-hidden="true" />
					<span class="sr-only">{m.forum_has_attachments()}</span>
				</span>
			{/if}
			{#if discussion.locked}
				<span class="fact">
					<IconLock aria-hidden="true" />
					<span class="sr-only">{m.forum_locked()}</span>
				</span>
			{/if}
			{#if discussion.starred}
				<span class="fact star">
					<IconStar aria-hidden="true" />
					<span class="sr-only">{m.forum_starred()}</span>
				</span>
			{/if}
		</span>
	</span>
</a>

<style>
	.row {
		position: relative;
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 12px;
		padding: 14px 14px 12px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--inner-light);
		color: var(--text);
		text-decoration: none;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			box-shadow var(--dur-fast) var(--ease-out);
	}

	.pinned {
		background: var(--course-container);
	}

	@media (hover: hover) {
		/* The list rows' state layers, over the row's own fill. */
		.row:hover {
			background-image: linear-gradient(var(--state-hover), var(--state-hover));
			transition-duration: 0s;
		}

		.pinned:hover {
			background: var(--course-container-hover);
		}
	}

	.row:active {
		background-image: linear-gradient(var(--state-press), var(--state-press));
	}

	.selected,
	.selected:hover {
		box-shadow:
			inset 0 0 0 2px var(--course-bar),
			var(--inner-light);
	}

	.main {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		gap: 3px 10px;
		min-inline-size: 0;
	}

	.subject {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		font: var(--type-row-title);
		letter-spacing: var(--track-row-title);
		text-wrap: pretty;
	}

	.pinned .subject {
		color: var(--course-ink);
	}

	.subject :global(.flag) {
		display: inline-block;
		inline-size: 14px;
		block-size: 14px;
		margin-inline-end: 3px;
		vertical-align: -1px;
		color: var(--course-ink);
	}

	.unread .subject {
		font-weight: 700;
	}

	.when {
		padding-block-start: 1px;
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.75rem;
		white-space: nowrap;
	}

	.unread .when {
		color: var(--accent-text);
		font-weight: 600;
	}

	.unread-badge {
		display: grid;
		place-items: center;
		min-inline-size: 20px;
		block-size: 20px;
		padding-inline: 6px;
		border-radius: var(--r-pill);
		background: var(--accent);
		color: var(--on-accent);
		font: 650 0.75rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.preview {
		grid-column: 1 / -1;
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		color: var(--text-muted);
		font: var(--type-meta);
		font-size: 0.875rem;
	}

	.who {
		min-inline-size: 0;
		margin-block-start: 2px;
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.facts {
		display: inline-flex;
		align-items: center;
		justify-content: flex-end;
		gap: 8px;
		margin-block-start: 2px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.fact {
		display: inline-flex;
		align-items: center;
		gap: 3px;
		font-variant-numeric: tabular-nums;
	}

	.fact :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.star {
		color: var(--gold);
	}

	@media (prefers-reduced-motion: reduce) {
		.row {
			transition: none;
		}
	}
</style>
