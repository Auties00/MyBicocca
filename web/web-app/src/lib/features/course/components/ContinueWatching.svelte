<script lang="ts">
	import IconArrowElbowDownRight from '~icons/ph/arrow-elbow-down-right';
	import IconPlay from '~icons/ph/play-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { CourseState } from '../course.svelte.ts';
	import { formatClock, formatClockLong } from '../format.ts';
	import { coursePath, moduleAnchor } from '../links.ts';
	import { pickRecording, progressRatio } from '../video/progress.ts';
	import VideoThumbnail from './VideoThumbnail.svelte';

	interface Props {
		course: CourseState;
		/** One line, for a phone's cover: a link that plays, with the place reached. */
		row?: boolean;
	}

	let { course, row = false }: Props = $props();

	const item = $derived(pickRecording(course.playlist, course.progress.data ?? {}));
	const progress = $derived(item && course.progress.data?.[item.cmId]);
	const started = $derived(progress !== undefined && !progress.completed && progress.position > 0);
	const id = $props.id();
</script>

{#if row}
	{#if item}
		<a
			class="row"
			href={coursePath(course.id, 'video', item.cmId)}
			aria-label={started && progress
				? m.course_resume_at({ title: item.title, time: formatClockLong(progress.position) })
				: m.course_watch_title({ title: item.title })}
		>
			<span class="play" aria-hidden="true"><IconPlay /></span>
			<span class="text" aria-hidden="true">
				<span class="kicker">{started ? m.course_continue_kicker() : m.course_next_kicker()}</span>
				<span class="line">{item.title}</span>
			</span>
			{#if started && progress}
				<span class="at" aria-hidden="true">{formatClock(progress.position)}</span>
				<span class="bar" style:--progress={progressRatio(progress)} aria-hidden="true"></span>
			{/if}
		</a>
	{:else}
		<div class="row" aria-hidden="true">
			<span class="play"></span>
			<span class="text">
				<Skeleton variant="text" width="40%" />
				<Skeleton variant="text" width="75%" />
			</span>
		</div>
	{/if}
{:else if item}
	{@const href = coursePath(course.id, 'video', item.cmId)}
	<article class="watch" aria-labelledby="{id}-title">
		<a class="thumb" {href} tabindex="-1" aria-hidden="true">
			<VideoThumbnail courseId={course.id} entryId={progress?.entryId ?? null} />
			{#if started && progress}
				<span class="time">
					{formatClock(progress.position)} / {progress.duration > 0
						? formatClock(progress.duration)
						: '—:—'}
				</span>
				<span class="bar" style:--progress={progressRatio(progress)}></span>
			{/if}
		</a>
		<div class="text">
			<p class="kicker">{started ? m.course_continue_kicker() : m.course_next_kicker()}</p>
			<h2 class="title" id="{id}-title">{item.title}</h2>
			<p class="section">
				{item.sectionTitle ?? m.course_section_fallback({ number: item.sectionOrdinal })}
			</p>
		</div>
		<div class="actions">
			<Button
				variant="solid"
				size="lg"
				icon={IconPlay}
				{href}
				full
				label={started && progress
					? m.course_resume_at({ title: item.title, time: formatClockLong(progress.position) })
					: m.course_watch_title({ title: item.title })}
			>
				{started ? m.course_resume() : m.course_watch()}
			</Button>
			<IconButton
				icon={IconArrowElbowDownRight}
				label={m.course_go_to_lesson()}
				variant="tonal"
				size="lg"
				href={moduleAnchor(course.id, item.cmId)}
			/>
		</div>
	</article>
{:else}
	<!-- Until the contents say whether a recording comes, the card's place is kept by its shape. -->
	<div class="watch" aria-hidden="true">
		<span class="thumb"></span>
		<div class="text">
			<span class="kicker"><Skeleton variant="text" width="38%" /></span>
			<span class="title-lines"><Skeleton variant="text" lines={2} /></span>
			<span class="meta-line"><Skeleton variant="text" width="52%" /></span>
		</div>
		<div class="actions">
			<Skeleton height="var(--control-h-lg)" radius="var(--r-pill)" />
			<Skeleton width="var(--control-h-lg)" height="var(--control-h-lg)" radius="var(--r-pill)" />
		</div>
	</div>
{/if}

<style>
	.watch {
		display: grid;
		grid-template-areas: 'thumb' 'text' 'actions';
		gap: 14px;
		padding: 12px;
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-2), var(--inner-light);
	}

	.thumb {
		grid-area: thumb;
		position: relative;
		display: block;
		overflow: hidden;
		aspect-ratio: 16 / 10;
		border-radius: calc(var(--r-xl) - 12px);
		background: var(--surface-sunken);
	}

	.time {
		position: absolute;
		inset: auto 10px 14px auto;
		padding: 3px 10px;
		border-radius: var(--r-pill);
		background: oklch(0% 0 0 / 0.6);
		color: oklch(100% 0 0);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
	}

	.bar {
		position: absolute;
		inset: auto 0 0;
		block-size: 4px;
		background: oklch(100% 0 0 / 0.35);
	}

	.bar::after {
		content: '';
		position: absolute;
		inset: 0;
		background: var(--course-bar);
		transform: scaleX(var(--progress));
		transform-origin: left;
	}

	.text {
		grid-area: text;
		align-self: end;
		display: grid;
		gap: 2px;
		padding-inline: 6px;
	}

	.kicker {
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.title {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.title-lines {
		font: var(--type-title-s);
	}

	.meta-line {
		font: var(--type-meta);
	}

	.section {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
		white-space: nowrap;
		text-overflow: ellipsis;
	}

	.actions {
		grid-area: actions;
		align-self: start;
		display: flex;
		gap: 8px;
		padding: 2px;
	}

	.actions > :global(:first-child) {
		flex: 1;
	}

	.row {
		position: relative;
		display: flex;
		align-items: center;
		gap: 12px;
		block-size: 60px;
		padding: 8px 14px 8px 8px;
		overflow: hidden;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-2), var(--inner-light);
		color: var(--text);
		text-decoration: none;
	}

	.row .play {
		display: grid;
		flex: none;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
		border-radius: var(--r-pill);
		background: var(--course-bar);
		color: light-dark(oklch(100% 0 0), oklch(20% 0.02 var(--course-h)));
	}

	div.row .play {
		background: var(--surface-sunken);
	}

	.row .play :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.row .text {
		flex: 1;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		padding: 0;
	}

	.row .line {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.row .at {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.row .bar {
		inset-inline: 0;
		block-size: 3px;
		background: color-mix(in oklch, var(--course-bar) 20%, transparent);
	}

	@media (hover: hover) {
		a.row:hover {
			background: color-mix(in oklch, var(--surface) 92%, var(--course-bar));
		}
	}

	/* A wide card puts the frame on the left, the text and actions beside it. */
	@container (width >= 360px) {
		.watch {
			grid-template-areas: 'thumb text' 'thumb actions';
			grid-template-columns: minmax(140px, 42%) minmax(0, 1fr);
			column-gap: 16px;
		}

		.thumb {
			align-self: center;
		}
	}
</style>
