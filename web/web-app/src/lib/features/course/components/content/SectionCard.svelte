<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconInfo from '~icons/ph/info';
	import { shapePath, type ShapeName } from '#lib/features/elearning/shapes.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { CountKind, OutlineSection } from '../../content.ts';
	import type { ProgressMap } from '../../video/progress.ts';
	import TeacherHtml from '../TeacherHtml.svelte';
	import Blocks from './Blocks.svelte';

	interface Props {
		card: OutlineSection;
		open: boolean;
		courseId: number;
		language: string;
		progress: ProgressMap;
		lastSeen: number | undefined;
		now: Date;
		/** Done of tracked activities, when the section tracks any. */
		completion: { done: number; tracked: number } | undefined;
		/** Rendered only once near the screen: for sections far down a long course. */
		deferred?: boolean;
	}

	let {
		card,
		open = $bindable(),
		courseId,
		language,
		progress,
		lastSeen,
		now,
		completion,
		deferred = false
	}: Props = $props();

	/* An open section's badge turns into one of Android's expressive shapes, by its number. */
	const OPEN_SHAPES: readonly ShapeName[] = [
		'sunny',
		'cookie',
		'cookie4',
		'puffy',
		'burst',
		'cookie6'
	];

	const counts: Record<CountKind, (inputs: { count: number }) => string> = {
		video: m.course_count_video,
		pdf: m.course_count_pdf,
		file: m.course_count_file,
		quiz: m.course_count_quiz,
		assign: m.course_count_assign,
		forum: m.course_count_forum,
		link: m.course_count_link,
		folder: m.course_count_folder,
		page: m.course_count_page,
		other: m.course_count_other
	};

	const subtitle = $derived(
		card.counts.length === 0
			? m.course_no_resources()
			: card.counts.map(({ kind, count }) => counts[kind]({ count })).join(' · ')
	);
	const finished = $derived(
		completion !== undefined && completion.tracked > 0 && completion.done === completion.tracked
	);
	const shape = $derived(shapePath(OPEN_SHAPES[(card.ordinal - 1) % OPEN_SHAPES.length]));
	const id = $props.id();
</script>

<!-- The heading holds the toggle, so heading navigation finds every section, as on the Quizzes
     tab. -->
<div class={['section', { open, finished, deferred }]} id="section-{card.section.id}">
	<h3 class="heading">
		<button
			type="button"
			class="toggle"
			aria-expanded={open}
			aria-controls="{id}-body"
			onclick={() => (open = !open)}
		>
			<span class="badge" aria-hidden="true">
				<svg class="bloom" viewBox="0 0 100 100"><path d={shape} /></svg>
				<span class="digit">{formatNumber(card.ordinal)}</span>
			</span>
			<span class="titles">
				<span class="title"
					>{card.title ?? m.course_section_fallback({ number: card.ordinal })}</span
				>
				<span class="subtitle">
					{subtitle}
					{#if completion && completion.tracked > 0}
						<span class="done">
							· {m.course_section_done({ done: completion.done, total: completion.tracked })}
						</span>
					{/if}
				</span>
			</span>
			<IconCaretDown class="chevron" aria-hidden="true" />
		</button>
	</h3>
	<!-- Closed, the section stays findable: the browser's find opens it on a match. -->
	<div
		class="body"
		id="{id}-body"
		hidden={open ? undefined : 'until-found'}
		onbeforematch={() => (open = true)}
	>
		<div class="contents">
			{#if card.summary}
				<aside class="callout">
					<IconInfo class="callout-icon" aria-hidden="true" />
					<TeacherHtml html={card.summary} size="sm" />
				</aside>
			{/if}
			{#if card.blocks.length > 0}
				<Blocks blocks={card.blocks} {courseId} {language} {progress} {lastSeen} {now} />
			{:else if !card.summary}
				<p class="nothing">{m.course_section_empty()}</p>
			{/if}
		</div>
	</div>
</div>

<style>
	.section {
		--row-bg: color-mix(in oklch, var(--bg) 75%, var(--surface));
		--row-hover: var(--surface-hover);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		/* The root already keeps clear of the top bar; this clears the sticky tabs. */
		scroll-margin-block-start: calc(var(--control-h-lg) + 8px);
	}

	.deferred {
		content-visibility: auto;
		contain-intrinsic-size: auto 76px;
	}

	.heading {
		margin: 0;
		font: inherit;
	}

	.toggle {
		display: grid;
		grid-template-columns: 44px minmax(0, 1fr) auto;
		align-items: center;
		gap: 14px;
		inline-size: 100%;
		padding: 14px 18px 14px 14px;
		border: 0;
		border-radius: var(--r-lg);
		background: none;
		color: inherit;
		font: inherit;
		text-align: start;
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
	}

	@media (hover: hover) {
		.toggle:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.toggle:focus-visible {
		outline-offset: -2px;
	}

	.badge {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
		border-radius: 14px;
		background: var(--course-container);
		color: var(--course-ink);
		transition: background-color var(--dur-base) var(--ease-out);
	}

	.bloom {
		position: absolute;
		inset: -2px;
		inline-size: calc(100% + 4px);
		block-size: calc(100% + 4px);
		fill: var(--course-bar);
		opacity: 0;
		transform: scale(0.6) rotate(0deg);
		transition:
			opacity var(--dur-base) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.digit {
		position: relative;
		font: var(--type-title-m);
		font-variant-numeric: tabular-nums;
		transition: color var(--dur-base) var(--ease-out);
	}

	.open .badge {
		background: transparent;
	}

	.open .bloom {
		opacity: 1;
		transform: scale(1) rotate(120deg);
	}

	.open .digit {
		color: var(--surface);
	}

	.finished:not(.open) .badge {
		background: var(--course-bar);
		color: var(--surface);
	}

	.titles {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		overflow-wrap: anywhere;
	}

	.subtitle {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.done {
		white-space: nowrap;
	}

	.toggle :global(.chevron) {
		inline-size: var(--icon);
		block-size: var(--icon);
		color: var(--text-muted);
		transition: transform var(--dur-spring) var(--spring-standard);
	}

	.open .toggle :global(.chevron) {
		transform: rotate(180deg);
	}

	.contents {
		display: grid;
		gap: 16px;
		padding: 2px 10px 12px;
	}

	/* Opening, the contents settle in from under the header; closing is instant. */
	.open .contents {
		animation: contents-in var(--dur-base) var(--ease-out);
	}

	@keyframes contents-in {
		from {
			opacity: 0;
			transform: translateY(-6px);
		}
	}

	.callout {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 12px;
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--course-container);
	}

	.callout :global(.callout-icon) {
		inline-size: var(--icon);
		block-size: var(--icon);
		margin-block-start: 1px;
		color: var(--course-ink);
	}

	.nothing {
		padding: 8px 10px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	@media (prefers-reduced-motion: reduce) {
		.bloom,
		.toggle :global(.chevron) {
			transition: none;
		}

		.open .contents {
			animation: none;
		}
	}
</style>
