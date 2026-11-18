<script lang="ts">
	import type { MoodleForum } from '@mybicocca/data-api/elearning';
	import IconArrowRight from '~icons/ph/arrow-right';
	import IconMegaphone from '~icons/ph/megaphone-simple-fill';
	import IconPaperclip from '~icons/ph/paperclip';
	import IconPushPin from '~icons/ph/push-pin-fill';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { coursePath } from '#lib/features/course/links.ts';
	import { stripMlang } from '#lib/features/elearning/mlang.ts';
	import { shapePath } from '#lib/features/elearning/shapes.ts';
	import { htmlToLine } from '#lib/format/html.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { getLocale } from '#lib/paraglide/runtime.js';
	import Button from '#lib/ui/Button.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { discussionId, hasAttachments, lastActivity, latestDiscussions } from '../forum.ts';
	import { forumDiscussionsQuery } from '../queries.ts';
	import PostTime from './PostTime.svelte';

	interface Props {
		courseId: number;
		forum: MoodleForum;
		/** How many of the latest announcements to show. */
		count?: number;
	}

	let { courseId, forum, count = 3 }: Props = $props();

	const discussions = query(() => {
		const scope = session.current;
		return scope && forumDiscussionsQuery(scope, forum.id);
	});

	const name = $derived(stripMlang(forum.name, getLocale()).trim());
	const total = $derived(forum.numDiscussions ?? discussions.data?.discussions.length ?? 0);
	const latest = $derived(latestDiscussions(discussions.data?.discussions ?? [], count));
	const gate = new LoadingGate(
		() => discussions.data === undefined && discussions.status === 'pending'
	);
	const headingId = $props.id();
</script>

<section class="card" aria-labelledby={headingId}>
	<svg class="shape" viewBox="0 0 100 100" aria-hidden="true" focusable="false">
		<path d={shapePath('burst')} />
	</svg>
	<header class="header">
		<div class="titles">
			<p class="eyebrow"><span aria-hidden="true">✦</span> {m.forum_announcements_eyebrow()}</p>
			<h2 class="name" id={headingId}>
				<a href={coursePath(courseId, 'forum', forum.id)}>{name}</a>
			</h2>
			<p class="count">
				{total === 0 ? m.forum_announcements_none() : m.forum_announcements_count({ count: total })}
			</p>
		</div>
		<span class="glyph" aria-hidden="true"><IconMegaphone /></span>
	</header>

	{#if discussions.data === undefined && discussions.status === 'error' && discussions.error}
		<div class="problem" role="alert">
			<p>{m.forum_announcements_failed()} {errorMessage(discussions.error)}</p>
			<Button
				size="sm"
				variant="ghost"
				onclick={discussions.refresh}
				loading={discussions.refreshing}
			>
				{m.common_retry()}
			</Button>
		</div>
	{:else if discussions.data === undefined}
		{@const placeholders = Math.min(count, Math.max(total, 1))}
		<ol
			class="items"
			style:--items={placeholders}
			aria-busy="true"
			aria-label={m.forum_announcements_loading()}
		>
			{#each { length: placeholders }, index (index)}
				<li class="item skeleton">
					{#if gate.visible}
						<Skeleton variant="text" width="90px" height="0.75em" />
						<Skeleton variant="text" width="80%" />
						<Skeleton variant="text" lines={2} />
					{/if}
				</li>
			{/each}
		</ol>
	{:else if latest.length === 0}
		<p class="empty">{m.forum_announcements_empty()}</p>
	{:else}
		<ol class="items" style:--items={latest.length}>
			{#each latest as announcement (discussionId(announcement))}
				{@const preview = htmlToLine(announcement.message)}
				<li>
					<a
						class={['item', { unread: announcement.numUnread > 0 }]}
						href={coursePath(courseId, 'forum', forum.id, discussionId(announcement))}
					>
						<span class="when">
							{#if announcement.pinned}
								<IconPushPin aria-hidden="true" />
								<span class="sr-only">{m.forum_pinned()}</span>
							{/if}
							<PostTime at={lastActivity(announcement)} />
							{#if hasAttachments(announcement)}
								<IconPaperclip aria-hidden="true" />
								<span class="sr-only">{m.forum_has_attachments()}</span>
							{/if}
							{#if announcement.numUnread > 0}
								<span class="new">{m.forum_new()}</span>
							{/if}
						</span>
						<span class="subject">{announcement.subject || announcement.name}</span>
						{#if preview}<span class="preview">{preview}</span>{/if}
						<span class="by">{announcement.userFullName}</span>
					</a>
				</li>
			{/each}
		</ol>
	{/if}

	<a class="all" href={coursePath(courseId, 'forum', forum.id)}>
		{m.forum_announcements_all()}
		<IconArrowRight aria-hidden="true" />
	</a>
</section>

<style>
	.card {
		position: relative;
		display: grid;
		gap: var(--sp-4);
		align-content: start;
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--course-container);
		color: var(--on-container, var(--text));
		overflow: clip;
		isolation: isolate;
		container-type: inline-size;
	}

	.shape {
		position: absolute;
		z-index: -1;
		inset-block-start: -64px;
		inset-inline-end: -56px;
		inline-size: 210px;
		block-size: 210px;
		fill: var(--course-container-hover);
		rotate: 12deg;
	}

	.header {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: var(--sp-4);
	}

	.titles {
		display: grid;
		gap: 4px;
		min-inline-size: 0;
	}

	.eyebrow {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.name {
		font: var(--type-title-m);
		text-wrap: balance;
	}

	.name a {
		color: inherit;
		text-decoration: none;
	}

	.name a:hover {
		text-decoration: underline;
		text-decoration-thickness: 1px;
		text-underline-offset: 3px;
	}

	.count {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.glyph {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 48px;
		block-size: 48px;
		border-radius: 16px;
		background: var(--course-bar);
		color: light-dark(white, oklch(20% 0.03 var(--course-h)));
		rotate: -6deg;
	}

	.glyph :global(svg) {
		inline-size: 24px;
		block-size: 24px;
	}

	.items {
		display: grid;
		gap: 8px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	/* Alone on a wide page, the announcements share the width side by side, in one row. */
	@container (width >= 820px) {
		.items {
			grid-template-columns: repeat(var(--items, 1), minmax(0, 1fr));
		}

		.items > li {
			display: grid;
		}
	}

	.item {
		display: grid;
		align-content: start;
		gap: 4px;
		padding: 14px 16px;
		border-radius: var(--r-lg);
		background: color-mix(in oklab, var(--surface) 72%, transparent);
		color: var(--text);
		text-decoration: none;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			translate var(--dur-base) var(--spring-standard);
	}

	.skeleton {
		min-block-size: 112px;
	}

	@media (hover: hover) {
		a.item:hover {
			background: var(--surface);
			translate: 0 -1px;
		}
	}

	a.item:active {
		translate: 0 1px;
	}

	/* As tall with the New pill as without, so side-by-side subjects start on one line. */
	.when {
		display: flex;
		align-items: center;
		gap: 6px;
		min-block-size: calc(1lh + 2px);
		color: var(--course-ink);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
	}

	.when :global(svg) {
		inline-size: 14px;
		block-size: 14px;
		flex-shrink: 0;
	}

	.new {
		padding: 1px 7px;
		border-radius: var(--r-pill);
		background: var(--course-bar);
		color: light-dark(white, oklch(20% 0.03 var(--course-h)));
		font: var(--type-label);
		font-weight: 650;
	}

	.subject {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		font: var(--type-body-strong);
		text-wrap: pretty;
	}

	.unread .subject {
		font-weight: 650;
	}

	.preview {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		color: var(--text-muted);
		font: var(--type-ui);
		font-weight: 400;
	}

	.by {
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}

	.empty {
		padding: 18px 16px;
		border-radius: var(--r-lg);
		background: color-mix(in oklab, var(--surface) 55%, transparent);
		color: var(--text-muted);
		font-style: italic;
	}

	.problem {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: 8px 12px;
		padding: 12px 16px;
		border-radius: var(--r-lg);
		background: color-mix(in oklab, var(--surface) 55%, transparent);
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.all {
		display: inline-flex;
		align-items: center;
		justify-self: start;
		gap: 6px;
		margin-inline-start: -10px;
		padding: 8px 10px;
		border-radius: var(--r-pill);
		color: var(--course-ink);
		font: var(--type-ui-strong);
		text-decoration: none;
	}

	.all :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		transition: translate var(--dur-base) var(--spring-standard);
	}

	@media (hover: hover) {
		.all:hover {
			background: var(--course-container-hover);
		}

		.all:hover :global(svg) {
			translate: 3px 0;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.item,
		.all :global(svg) {
			transition: none;
		}
	}
</style>
