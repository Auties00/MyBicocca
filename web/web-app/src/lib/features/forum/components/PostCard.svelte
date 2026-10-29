<script lang="ts">
	import { online } from 'svelte/reactivity/window';
	import type { MoodleForumPost } from '@mybicocca/data-api/elearning';
	import IconArrowBendUpLeft from '~icons/ph/arrow-bend-up-left';
	import IconCaretDown from '~icons/ph/caret-down';
	import IconPencilSimple from '~icons/ph/pencil-simple';
	import IconTrash from '~icons/ph/trash';
	import TeacherHtml from '#lib/features/course/components/TeacherHtml.svelte';
	import type { CourseFile } from '#lib/features/course/content.ts';
	import { getFileOpener } from '#lib/features/course/file/opener.svelte.ts';
	import { classifyFile } from '#lib/features/course/files.ts';
	import { fileIcon } from '#lib/features/course/components/icons.ts';
	import { attachmentHref } from '#lib/features/course/links.ts';
	import { formatDateTime } from '#lib/format/dates.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import AuthorAvatar from './AuthorAvatar.svelte';
	import PostTime from './PostTime.svelte';

	interface Props {
		post: MoodleForumPost;
		courseId: number;
		/** The discussion's opening post, drawn larger. */
		opening?: boolean;
		mine?: boolean;
		/** Unread before this visit. */
		fresh?: boolean;
		/** Just posted or saved: drawn with a passing highlight. */
		highlighted?: boolean;
		/** Whose post this one answers, shown where the thread stops indenting. */
		replyingTo?: string;
		canReply: boolean;
		/** The replies below it, which the card can fold away. */
		replies?: { count: number; open: boolean; controls: string; ontoggle: () => void };
		onreply: () => void;
		onedit: () => void;
		ondelete: () => void;
	}

	let {
		post,
		courseId,
		opening = false,
		mine = false,
		fresh = false,
		highlighted = false,
		replyingTo,
		canReply,
		replies,
		onreply,
		onedit,
		ondelete
	}: Props = $props();

	const opener = getFileOpener();
	const edited = $derived(post.timeModified - post.timeCreated > 60);
	const author = $derived(post.author.fullName ?? m.forum_author_unknown());
	const headingId = $props.id();

	const files = $derived<CourseFile[]>(
		post.attachments.map((file) => ({
			url: file.url,
			name: file.fileName,
			folder: '/',
			mime: null,
			size: file.fileSize || null,
			modified: file.timeModified ? file.timeModified * 1000 : null
		}))
	);
</script>

<article
	class={['post', { opening, highlighted }]}
	id="post-{post.id}"
	tabindex="-1"
	aria-labelledby={headingId}
	data-post={post.id}
>
	<header class="header">
		<AuthorAvatar name={author} size={opening ? 44 : 36} />
		<div class="who">
			<p class="name" id={headingId}>
				{author}
				{#if mine}<span class="you">{m.forum_you()}</span>{/if}
				{#if fresh}<span class="new">{m.forum_new()}</span>{/if}
			</p>
			<p class="when">
				{#if replyingTo}
					<span class="to">{m.forum_in_reply_to({ name: replyingTo })} ·</span>
				{/if}
				<PostTime at={post.timeCreated} />
				{#if edited}
					<span title={formatDateTime(post.timeModified * 1000)}>· {m.forum_edited()}</span>
				{/if}
			</p>
		</div>
	</header>

	{#if post.isDeleted}
		<p class="deleted">{m.forum_post_deleted()}</p>
	{:else}
		<div class="body">
			<TeacherHtml html={post.message} />
		</div>

		{#if files.length > 0}
			<ul class="files" aria-label={m.forum_attachments()}>
				{#each files as file (file.url)}
					{@const Glyph = fileIcon(classifyFile(file.name, file.mime))}
					{@const href = attachmentHref(courseId, file)}
					{@const downloading = opener.downloading.has(file.url)}
					<li>
						<a class="file" {href} onclick={(event) => opener.click(event, { file, href })}>
							{#if downloading}
								<Spinner size={18} label={m.forum_attachment_downloading({ name: file.name })} />
							{:else}
								<Glyph aria-hidden="true" />
							{/if}
							<span class="file-name">{file.name}</span>
							{#if file.size}<span class="file-size">{formatBytes(file.size)}</span>{/if}
						</a>
					</li>
				{/each}
			</ul>
		{/if}

		{#if canReply || post.capabilities.edit || post.capabilities.delete || replies}
			<footer class="actions">
				{#if canReply}
					<Button
						size="sm"
						variant="ghost"
						icon={IconArrowBendUpLeft}
						onclick={onreply}
						aria-keyshortcuts="R"
					>
						{m.forum_reply()}
					</Button>
				{/if}
				{#if post.capabilities.edit}
					<Button
						size="sm"
						variant="ghost"
						icon={IconPencilSimple}
						onclick={onedit}
						aria-keyshortcuts="E"
					>
						{m.forum_edit()}
					</Button>
				{/if}
				{#if post.capabilities.delete}
					<Button
						size="sm"
						variant="ghost"
						icon={IconTrash}
						disabled={online.current === false}
						onclick={ondelete}
					>
						{m.forum_delete()}
					</Button>
				{/if}
				{#if replies}
					<button
						type="button"
						class={['toggle', { open: replies.open }]}
						aria-expanded={replies.open}
						aria-controls={replies.controls}
						onclick={replies.ontoggle}
					>
						{replies.open ? m.forum_hide_replies() : m.forum_show_replies({ count: replies.count })}
						<IconCaretDown aria-hidden="true" />
					</button>
				{/if}
			</footer>
		{/if}
	{/if}
</article>

<style>
	.post {
		position: relative;
		isolation: isolate;
		display: grid;
		gap: 10px;
		padding: 14px 16px 10px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--inner-light);
		scroll-margin-block: calc(var(--shell-inset-top) + 16px) 24px;
	}

	.post:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.opening {
		gap: 14px;
		padding: 20px 22px 14px;
		border-radius: var(--r-xl);
	}

	/* Under the content, over the card: only its opacity animates. */
	.highlighted::after {
		content: '';
		position: absolute;
		z-index: -1;
		inset: 0;
		border-radius: inherit;
		background: var(--course-container, var(--container));
		opacity: 0;
		animation: arrive 2.4s var(--ease-out);
		pointer-events: none;
	}

	@keyframes arrive {
		0%,
		30% {
			opacity: 1;
		}
	}

	.header {
		display: flex;
		align-items: center;
		gap: 12px;
	}

	.who {
		display: grid;
		min-inline-size: 0;
	}

	.name {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 4px 8px;
		font: var(--type-ui-strong);
	}

	.opening .name {
		font-size: 0.9375rem;
	}

	.you,
	.new {
		padding: 1px 8px;
		border-radius: var(--r-pill);
		font: var(--type-label);
		font-weight: 600;
	}

	.you {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.new {
		background: var(--accent);
		color: var(--on-accent);
	}

	.when {
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}

	.to {
		font-style: normal;
	}

	.body {
		min-inline-size: 0;
	}

	.opening .body :global(.prose) {
		font-size: 1.0625rem;
	}

	.deleted {
		color: var(--text-muted);
		font-style: italic;
	}

	.files {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.file {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		max-inline-size: 100%;
		min-block-size: 40px;
		padding: 6px 12px 6px 10px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
		color: var(--text);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.file :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
		color: var(--course-ink, var(--accent-text));
	}

	@media (hover: hover) {
		.file:hover {
			background: var(--surface-hover);
		}
	}

	.file-name {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.file-size {
		flex-shrink: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 2px;
		margin-inline: -8px -6px;
	}

	.toggle {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		min-block-size: 32px;
		margin-inline-start: auto;
		padding: 4px 10px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-ui);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.toggle :global(svg) {
		inline-size: 14px;
		block-size: 14px;
		rotate: 0deg;
		transition: rotate var(--dur-base) var(--spring-standard);
	}

	.toggle.open :global(svg) {
		rotate: 180deg;
	}

	@media (hover: hover) {
		.toggle:hover {
			background: var(--state-hover);
			color: var(--text);
		}
	}

	@media (pointer: coarse) {
		.toggle {
			min-block-size: 40px;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.highlighted::after {
			animation-duration: 0s;
		}

		.toggle :global(svg) {
			transition: none;
		}
	}
</style>
