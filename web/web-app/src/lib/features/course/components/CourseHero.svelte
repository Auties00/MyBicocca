<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import IconStar from '~icons/ph/star';
	import IconStarFill from '~icons/ph/star-fill';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { getPageChrome, hasPageChrome } from '#lib/ui/pageChrome.svelte.ts';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { CourseState } from '../course.svelte.ts';
	import { coverTitle } from '../title.ts';
	import ContinueWatching from './ContinueWatching.svelte';
	import CourseShapes from './CourseShapes.svelte';
	import { expanded, compact } from '#lib/ui/media.ts';

	interface Props {
		course: CourseState;
	}

	let { course }: Props = $props();

	const chrome = hasPageChrome() ? getPageChrome() : undefined;
	const back = { href: shellHref('elearning'), label: m.course_back() };

	/* The last word stands apart, as on the course's Android cover: "Algoritmi e Strutture / Dati". */
	const split = $derived(coverTitle(course.title ?? ''));

	/* Until the contents arrive, the recording's skeleton holds its place. */
	const pending = $derived(
		course.contents.data === undefined && course.contents.status === 'pending'
	);
	const teacherPending = $derived(
		course.teacher === undefined &&
			((course.info.data === undefined && course.info.status === 'pending') ||
				(course.record.data === undefined && course.record.status === 'pending'))
	);
	const watching = $derived(course.playlist.length > 0);

	let refreshing = $state(false);

	async function refresh() {
		refreshing = true;
		await course.refresh();
		refreshing = false;
	}

	/* On phones a course is a nested page: the bar names it from the start, as any other. */
	const heading = chrome?.watchHeading(() => ({
		title: course.title,
		back,
		inBar: compact.current
	}));
</script>

<div class="hero">
	<a class="back" href={back.href}>
		<IconArrowLeft aria-hidden="true" />
		{back.label}
	</a>
	<header
		class={['cover', { watching, pending }]}
		data-course={course.slot}
		aria-busy={course.loading}
	>
		<CourseShapes courseId={course.id} spinning={course.loading} aside={watching || pending} />
		<div class="topline">
			<p class="overline">
				{#if course.period}<span>{course.period}</span>{/if}
				{#if course.code.code}<span translate="no">{course.code.code}</span>{/if}
			</p>
			<span class="tools">
				<!-- Below 1024 px a pull refreshes instead. -->
				{#if expanded.current}
					<IconButton
						icon={IconArrowClockwise}
						label={m.course_refresh()}
						loading={refreshing}
						onclick={refresh}
					/>
				{/if}
				{#if course.membership}
					<span class={['star', { on: course.favourite }]}>
						<IconButton
							icon={course.favourite ? IconStarFill : IconStar}
							label={course.favourite ? m.course_unfavourite() : m.course_favourite()}
							pressed={course.favourite}
							onclick={course.toggleFavourite}
						/>
					</span>
				{:else if course.enrolled.data === undefined && course.enrolled.status === 'pending'}
					<span class="star-slot"></span>
				{/if}
			</span>
		</div>
		<div class="text">
			<h1 class="title" tabindex="-1" {@attach heading}>
				{#if course.title !== undefined}
					{#if split.head}<span class="head">{split.head}</span>{/if}
					<span class="tail"
						>{split.tail}{#if split.tag}<span class="tag">{split.tag}</span>{/if}</span
					>
				{:else}
					<span class="placeholder" aria-label={m.course_loading()}>
						<Skeleton variant="text" lines={2} />
					</span>
				{/if}
			</h1>
			{#if course.teacher}
				<p class="teacher">{m.course_teacher({ name: course.teacher })}</p>
			{:else if teacherPending}
				<p class="teacher"><Skeleton variant="text" width="12em" /></p>
			{/if}
		</div>
		{#if watching || pending}
			<div class="recording"><ContinueWatching {course} /></div>
		{/if}
	</header>
</div>

<style>
	.hero {
		display: grid;
		gap: 12px;
		container: hero / inline-size;
	}

	.back {
		display: none;
		align-items: center;
		gap: 6px;
		justify-self: start;
		margin-block-start: -6px;
		margin-inline-start: -8px;
		padding: 6px 10px 6px 8px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.back :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (width >= 600px) {
		.back {
			display: inline-flex;
		}
	}

	@media (hover: hover) {
		.back:hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.cover {
		--shape-large: light-dark(
			oklch(86% 0.07 calc(var(--course-h) + 70) / 0.55),
			oklch(38% 0.07 calc(var(--course-h) + 70) / 0.5)
		);
		--shape-medium: light-dark(
			oklch(80% 0.08 var(--course-h) / 0.42),
			oklch(42% 0.08 var(--course-h) / 0.45)
		);
		--shape-small: light-dark(
			oklch(78% 0.09 calc(var(--course-h) - 70) / 0.5),
			oklch(46% 0.09 calc(var(--course-h) - 70) / 0.5)
		);
		position: relative;
		isolation: isolate;
		display: grid;
		grid-template-areas: 'top' 'text';
		gap: 14px;
		overflow: clip;
		padding: 22px 20px 20px;
		border-radius: var(--r-xl);
		background: var(--course-container);
		container-type: inline-size;
	}

	.cover.watching,
	.cover.pending {
		grid-template-areas: 'top' 'text' 'recording';
	}

	.topline {
		grid-area: top;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12px;
		min-block-size: var(--control-h);
	}

	.text {
		grid-area: text;
		align-self: end;
		display: grid;
		gap: 14px;
		min-inline-size: 0;
	}

	.overline {
		display: flex;
		flex-wrap: wrap;
		gap: 4px 12px;
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.tools {
		display: flex;
		gap: 2px;
		margin-inline-end: -8px;
		color: var(--course-ink);
	}

	.star-slot {
		inline-size: var(--control-h);
	}

	.star.on :global(svg) {
		color: var(--gold);
	}

	.title {
		display: grid;
		max-inline-size: 18ch;
		font-family: var(--font-display);
		font-size: clamp(2.375rem, 1.5rem + 4.5cqi, 4.25rem);
		line-height: 1.02;
		letter-spacing: -0.025em;
		text-wrap: balance;
		outline: none;
		overflow-wrap: break-word;
		hyphens: auto;
	}

	.head {
		font-weight: 350;
	}

	.tail {
		color: var(--course-ink);
		font-weight: 620;
	}

	.tag {
		display: inline-block;
		margin-inline-start: 0.45em;
		padding: 0.35em 0.7em;
		border-radius: var(--r-pill);
		background: color-mix(in oklch, var(--course-bar) 16%, transparent);
		color: var(--course-ink);
		font: var(--type-overline);
		font-size: clamp(0.6875rem, 0.6rem + 0.4cqi, 0.8125rem);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		vertical-align: 0.6em;
		white-space: nowrap;
	}

	.placeholder {
		display: block;
		inline-size: min(100%, 14ch);
	}

	.teacher {
		color: var(--text-muted);
		font: var(--type-ui-strong);
		font-size: 1rem;
	}

	.recording {
		grid-area: recording;
		align-self: end;
		min-inline-size: 0;
		container-type: inline-size;
	}

	/* Beside the title the cover is a band of one height, card or not: it fits the recording's card
	   with a two-line lesson title, so nothing moves when the contents arrive. */
	@container hero (width >= 720px) {
		.cover {
			--pad-x: 40px;
			--gap-x: 40px;
			--side-w: clamp(360px, 40cqi, 500px);
			grid-template-areas: 'top' 'text';
			grid-template-rows: auto 1fr;
			min-block-size: 304px;
			padding: 24px var(--pad-x) 28px;
		}

		.cover.watching,
		.cover.pending {
			grid-template-columns: minmax(0, 1fr) var(--side-w);
			grid-template-areas: 'top top' 'text recording';
			column-gap: var(--gap-x);
		}

		/* The title scales with the column it has beside a card, card or not: it keeps its size when
		   the contents arrive. */
		.title {
			max-inline-size: 15ch;
			font-size: clamp(
				2.5rem,
				1.25rem + 0.055 * (100cqi - var(--side-w) - 2 * var(--pad-x) - var(--gap-x)),
				4.25rem
			);
		}
	}

	@container hero (width >= 1100px) {
		.cover {
			--pad-x: 56px;
			min-block-size: 320px;
			padding: 28px var(--pad-x) 32px;
		}
	}
</style>
