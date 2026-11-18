<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconExam from '~icons/ph/exam';
	import { shapePath, type ShapeName } from '#lib/features/elearning/shapes.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { groupTitle } from '../format.ts';
	import { completionOf, sectionNumber, type QuizGroup, type QuizItem } from '../model.ts';
	import QuizRow from './QuizRow.svelte';

	interface Props {
		group: QuizGroup;
		/** The group's quizzes the filter keeps. */
		items: readonly QuizItem[];
		open: boolean;
		/** Rendered only once near the screen: for sections far down a long list. */
		deferred?: boolean;
		courseId: number;
		language: string;
		now: Date;
	}

	let {
		group,
		items,
		open = $bindable(),
		deferred = false,
		courseId,
		language,
		now
	}: Props = $props();

	/* As on the Content tab: an open section's badge blooms into one of Android's shapes. */
	const SHAPES: readonly ShapeName[] = ['sunny', 'cookie', 'cookie4', 'puffy', 'burst', 'cookie6'];

	const title = $derived(groupTitle(group));
	/* The section's number on the Content tab, so a filter never renumbers the badges. */
	const number = $derived(sectionNumber(group));
	const completion = $derived(completionOf(group.items));
	/* Under a filter the section says how many of its quizzes show, so none seem to be missing. */
	const subtitle = $derived(
		[
			items.length < group.items.length
				? m.quiz_count_shown({ shown: items.length, count: group.items.length })
				: m.quiz_count({ count: group.items.length }),
			completion.done > 0 && m.quiz_group_done({ count: completion.done })
		]
			.filter(Boolean)
			.join(' · ')
	);
	const finished = $derived(completion.total > 0 && completion.done === completion.total);
	const shape = $derived(shapePath(SHAPES[((group.ordinal ?? 1) - 1) % SHAPES.length]));
	const id = $props.id();
</script>

<!-- The heading holds the toggle, so heading navigation still finds every section. -->
<div class={['group', { open, finished, deferred }]} id="quiz-group-{group.key}">
	<h3 class="heading">
		<button
			type="button"
			class="toggle"
			aria-expanded={open}
			aria-controls="{id}-rows"
			onclick={() => (open = !open)}
		>
			<span class="badge" aria-hidden="true">
				<svg class="bloom" viewBox="0 0 100 100"><path d={shape} /></svg>
				{#if number !== null}
					<span class="digit">{formatNumber(number)}</span>
				{:else}
					<IconExam class="glyph" />
				{/if}
			</span>
			<span class="titles">
				<span class="title">{title}</span>
				<span class="subtitle">{subtitle}</span>
			</span>
			<IconCaretDown class="chevron" aria-hidden="true" />
		</button>
	</h3>
	<!-- Closed, the rows stay findable: the browser's find opens the section on a match. -->
	<div
		class="body"
		id="{id}-rows"
		hidden={open ? undefined : 'until-found'}
		onbeforematch={() => (open = true)}
	>
		<ul class="rows">
			{#each items as item (item.quiz.id)}
				<QuizRow {item} {courseId} {language} {now} />
			{/each}
		</ul>
	</div>
</div>

<style>
	.group {
		--row-fill: color-mix(in oklch, var(--bg) 75%, var(--surface));
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1);
		scroll-margin-block-start: calc(var(--topbar-h) + var(--control-h-lg) + 16px);
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
		font-variant-numeric: lining-nums tabular-nums;
		transition: color var(--dur-base) var(--ease-out);
	}

	.badge :global(.glyph) {
		position: relative;
		inline-size: var(--icon);
		block-size: var(--icon);
		transition: color var(--dur-base) var(--ease-out);
	}

	.open .badge {
		background: transparent;
	}

	.open .bloom {
		opacity: 1;
		transform: scale(1) rotate(120deg);
	}

	.open .digit,
	.open .badge :global(.glyph) {
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

	.toggle :global(.chevron) {
		inline-size: var(--icon);
		block-size: var(--icon);
		color: var(--text-muted);
		transition: transform var(--dur-spring) var(--spring-standard);
	}

	.open .toggle :global(.chevron) {
		transform: rotate(180deg);
	}

	.rows {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 2px 10px 10px;
		list-style: none;
	}

	/* Opening, the rows settle in from under the header; closing is instant. */
	.open .rows {
		animation: rows-in var(--dur-base) var(--ease-out);
	}

	@keyframes rows-in {
		from {
			opacity: 0;
			transform: translateY(-6px);
		}
	}

	.rows > :global(li) {
		border-radius: var(--r-2xs);
		box-shadow: none;
	}

	.rows > :global(li:first-child) {
		border-start-start-radius: var(--r-md);
		border-start-end-radius: var(--r-md);
	}

	.rows > :global(li:last-child) {
		border-end-start-radius: var(--r-md);
		border-end-end-radius: var(--r-md);
	}

	@media (prefers-reduced-motion: reduce) {
		.bloom,
		.toggle :global(.chevron) {
			transition: none;
		}

		.open .rows {
			animation: none;
		}
	}
</style>
