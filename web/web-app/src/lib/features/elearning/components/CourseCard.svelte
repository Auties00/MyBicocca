<script lang="ts">
	import IconStar from '~icons/ph/star';
	import IconStarFill from '~icons/ph/star-fill';
	import IconClipboardText from '~icons/ph/clipboard-text';
	import IconExam from '~icons/ph/exam';
	import { calendarDays } from '#lib/format/dates.ts';
	import { dueLabel, dueTone, type DueTone } from '#lib/format/due.ts';
	import { formatPercent } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { courseSlot } from '#lib/ui/courseSlot.ts';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import { activeEditions } from '../choices.svelte.ts';
	import { periodLabel } from '../courseCode.ts';
	import type { CourseGroup, EnrolledCourse } from '../courses.ts';
	import type { CourseDeadline } from '../deadlines.ts';
	import { courseHref, deadlineHref } from '../links.ts';
	import CourseShapes from './CourseShapes.svelte';

	interface Props {
		group: CourseGroup;
		/** The deadlines of each course, by course id, already pruned to the recent ones. */
		deadlines: ReadonlyMap<number, CourseDeadline[]>;
		starred: boolean;
		onstar: (group: CourseGroup, starred: boolean) => void;
		now: Date;
		/** The heading level of the course title, under its section's. */
		level?: 2 | 3;
		/** Just enrolled in and brought into view: marked for a moment. */
		revealed?: boolean;
	}

	let { group, deadlines, starred, onstar, now, level = 3, revealed = false }: Props = $props();

	const MAX_DEADLINES = 3;
	const id = $props.id();

	const active = $derived(
		group.editions.find((edition) => edition.id === activeEditions.get(group.key)) ??
			group.editions[0]
	);
	const activeIndex = $derived(group.editions.indexOf(active));
	/* Set by a click on the star: the pop plays when starring, not whenever a starred card mounts. */
	let toggled = $state(false);
	const due = $derived(deadlines.get(active.id) ?? []);
	const shown = $derived(due.slice(0, MAX_DEADLINES));
	const extra = $derived(due.length - shown.length);
	const seed = $derived(active.code.code ?? active.fullName);
	const tabbed = $derived(group.editions.length > 1);

	/*
	 * Under its year's heading a lone edition's tab says what the heading doesn't: its year of
	 * study. Editions' tabs tell them apart by period, and two of one year (another programme's) by
	 * their code too.
	 */
	const tabLabels = $derived.by(() => {
		if (!tabbed) {
			const [edition] = group.editions;
			const { courseYear, stream } = edition.code;
			const label =
				courseYear === null
					? (periodLabel(edition.code) ?? m.elearning_period_none())
					: stream === null
						? m.elearning_filter_year({ year: courseYear })
						: m.elearning_tab_year_stream({ year: courseYear, stream });
			return new Map([[edition.id, label]]);
		}
		const periods = group.editions.map(
			(edition) => periodLabel(edition.code) ?? m.elearning_period_none()
		);
		return new Map(
			group.editions.map((edition, index) => [
				edition.id,
				periods.indexOf(periods[index]) === periods.lastIndexOf(periods[index])
					? periods[index]
					: `${periods[index]} · ${edition.code.code ?? edition.shortName}`
			])
		);
	});

	function select(edition: EnrolledCourse) {
		activeEditions.set(group.key, edition.id);
	}

	/* Arrow keys move between the edition tabs, as in any tab list. */
	function tabKey(event: KeyboardEvent, index: number) {
		const last = group.editions.length - 1;
		const next =
			event.key === 'ArrowRight'
				? index === last
					? 0
					: index + 1
				: event.key === 'ArrowLeft'
					? index === 0
						? last
						: index - 1
					: event.key === 'Home'
						? 0
						: event.key === 'End'
							? last
							: -1;
		if (next < 0) return;
		event.preventDefault();
		select(group.editions[next]);
		document.getElementById(`${id}-tab-${next}`)?.focus();
	}

	function dueOf(deadline: CourseDeadline): { text: string; tone: DueTone } {
		const days = calendarDays(now, new Date(deadline.dueAt));
		return { text: dueLabel(days), tone: dueTone(days) };
	}
</script>

<article
	class={['card', { revealed }]}
	data-course={courseSlot(active.id)}
	aria-labelledby="{id}-title"
>
	<div
		class="tabs"
		role={tabbed ? 'tablist' : undefined}
		aria-label={tabbed ? m.elearning_editions_label({ name: active.fullName }) : undefined}
	>
		{#each group.editions as edition, index (edition.id)}
			{#if tabbed}
				<button
					type="button"
					id="{id}-tab-{index}"
					class={['tab', { active: edition.id === active.id }]}
					data-course={courseSlot(edition.id)}
					role="tab"
					aria-selected={edition.id === active.id}
					aria-controls="{id}-hero"
					tabindex={edition.id === active.id ? 0 : -1}
					onclick={() => select(edition)}
					onkeydown={(event) => tabKey(event, index)}
				>
					{tabLabels.get(edition.id)}
				</button>
			{:else}
				<span class="tab active" data-course={courseSlot(edition.id)}>
					{tabLabels.get(edition.id)}
				</span>
			{/if}
		{/each}
	</div>

	<div
		class={['hero', { joined: shown.length > 0 }]}
		id="{id}-hero"
		role={tabbed ? 'tabpanel' : undefined}
		aria-labelledby={tabbed ? `${id}-tab-${activeIndex}` : undefined}
	>
		<CourseShapes courseId={active.id} {seed} count={3} />
		<svelte:element this={`h${level}`} class="title" id="{id}-title">
			<a class="link" href={courseHref(active.id)}>{active.fullName}</a>
		</svelte:element>
		<p class="meta">
			<span class="code" translate="no">{active.idNumber ?? active.shortName}</span>
			{#if active.progress !== null}
				<span
					class="percent"
					aria-label={m.elearning_progress({ percent: formatPercent(active.progress / 100) })}
				>
					{formatPercent(active.progress / 100)}
				</span>
			{/if}
		</p>
		{#if active.progress !== null}
			<span class="progress" style:--progress={active.progress / 100} aria-hidden="true"></span>
		{/if}
		<button
			type="button"
			class={['star', { starred, toggled }]}
			aria-pressed={starred}
			aria-label={starred ? m.elearning_remove_favorite() : m.elearning_add_favorite()}
			{@attach tooltip(starred ? m.elearning_remove_favorite() : m.elearning_add_favorite())}
			onclick={() => {
				toggled = true;
				onstar(group, !starred);
			}}
		>
			{#if starred}<IconStarFill aria-hidden="true" />{:else}<IconStar aria-hidden="true" />{/if}
		</button>
	</div>

	{#if shown.length > 0}
		<div class="body">
			<CourseShapes
				courseId={active.id}
				{seed}
				count={Math.ceil((shown.length + 1) / 2)}
				salt={1}
			/>
			<ul class="deadlines" aria-label={m.elearning_deadlines_label({ name: active.fullName })}>
				{#each shown as deadline (deadline.eventId)}
					{@const label = dueOf(deadline)}
					<li>
						<a class="deadline" href={deadlineHref(deadline)}>
							<span
								class="kind"
								title={deadline.kind === 'assign'
									? m.elearning_deadline_assign()
									: m.elearning_deadline_quiz()}
							>
								{#if deadline.kind === 'assign'}
									<IconClipboardText aria-hidden="true" />
								{:else}
									<IconExam aria-hidden="true" />
								{/if}
								<span class="sr-only">
									{deadline.kind === 'assign'
										? m.elearning_deadline_assign()
										: m.elearning_deadline_quiz()}:
								</span>
							</span>
							<span class="name">{deadline.title}</span>
							<span class={['due', label.tone]}>{label.text}</span>
						</a>
					</li>
				{/each}
				{#if extra > 0}
					<li class="extra">{m.elearning_deadline_extra({ count: extra })}</li>
				{/if}
			</ul>
		</div>
	{/if}
</article>

<style>
	/*
	 * Tints of the course hue at fixed lightness, so no course reads louder than another. The card
	 * spans three rows of the list's grid (tabs, box, deadlines) and shares them with its
	 * neighbours, so the boxes of a row line up whatever their titles and deadlines.
	 */
	.card {
		--box: light-dark(oklch(93% 0.042 var(--course-h)), oklch(29% 0.045 var(--course-h)));
		--box-hover: light-dark(oklch(91% 0.052 var(--course-h)), oklch(31.5% 0.052 var(--course-h)));
		--box-body: light-dark(oklch(96.5% 0.02 var(--course-h)), oklch(24.5% 0.028 var(--course-h)));
		--watermark: light-dark(oklch(88.5% 0.06 var(--course-h)), oklch(33.5% 0.06 var(--course-h)));
		display: grid;
		grid-row: span 3;
		grid-template-rows: subgrid;
		row-gap: 0;
		min-inline-size: 0;
	}

	/*
	 * The strip reaches under the box by the lift of the inactive tabs, which sink behind it; the
	 * lift stays inside the strip's padding, so it never scrolls vertically.
	 */
	.tabs {
		--sink: 4px;
		display: flex;
		align-items: flex-end;
		gap: 4px;
		min-block-size: 28px;
		margin-block-end: calc(-1 * var(--sink));
		padding: 0 18px var(--sink);
		overflow-x: auto;
		scrollbar-width: none;
	}

	.tab {
		--tab-ink: light-dark(oklch(100% 0 0), oklch(18% 0.02 var(--course-h)));
		flex: none;
		padding: 6px 10px 4px;
		border-radius: var(--r-sm) var(--r-sm) 0 0;
		background: light-dark(oklch(88% 0.055 var(--course-h)), oklch(34% 0.06 var(--course-h)));
		color: var(--course-ink);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
		transform: translateY(var(--sink));
		transition:
			transform var(--dur-base) var(--ease-out),
			background-color var(--dur-fast) var(--ease-out);
	}

	.tab.active {
		background: var(--course-bar);
		color: var(--tab-ink);
		transform: none;
	}

	button.tab {
		cursor: pointer;
		touch-action: manipulation;
	}

	button.tab:focus-visible {
		outline-offset: 2px;
	}

	@media (hover: hover) {
		button.tab:not(.active):hover {
			background: light-dark(oklch(84% 0.07 var(--course-h)), oklch(38% 0.07 var(--course-h)));
		}
	}

	.hero {
		position: relative;
		isolation: isolate;
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		grid-template-rows: auto 1fr;
		align-content: start;
		column-gap: 8px;
		row-gap: 6px;
		min-block-size: 112px;
		padding: 18px 12px 20px 18px;
		border-radius: var(--r-lg);
		background: var(--box);
		box-shadow: var(--inner-light);
		overflow: clip;
		transition: background-color var(--dur-base) var(--ease-out);
	}

	/* The mark of a course just enrolled in: a ring that glows twice and fades. */
	.hero::before {
		content: '';
		position: absolute;
		inset: 0;
		z-index: 1;
		border-radius: inherit;
		box-shadow: inset 0 0 0 2px var(--course-bar);
		opacity: 0;
		pointer-events: none;
	}

	.revealed .hero::before {
		animation: reveal 2.4s var(--ease-in-out) 300ms both;
	}

	@keyframes reveal {
		20%,
		60% {
			opacity: 1;
		}
		40% {
			opacity: 0.35;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.revealed .hero::before {
			animation: reveal-still 2.4s steps(1, end) 300ms both;
		}
	}

	@keyframes reveal-still {
		from {
			opacity: 1;
		}
		to {
			opacity: 0;
		}
	}

	.hero.joined {
		border-end-start-radius: var(--r-2xs);
		border-end-end-radius: var(--r-2xs);
	}

	.hero > :global(.layer) {
		z-index: -1;
	}

	.title {
		grid-column: 1;
		align-self: start;
		margin-block-start: 6px;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: balance;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
	}

	.link {
		color: var(--text);
		text-decoration: none;
		outline: none;
	}

	/* The whole box opens the course; the star sits above the stretched link. */
	.link::after {
		content: '';
		position: absolute;
		inset: 0;
		z-index: 1;
		border-radius: inherit;
	}

	@media (hover: hover) {
		.hero:has(.link:hover) {
			background: var(--box-hover);
			transition-duration: 0s;
		}
	}

	.hero:has(.link:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.meta {
		grid-column: 1 / -1;
		align-self: end;
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 12px;
		padding-inline-end: 6px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.code {
		min-inline-size: 0;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.04em;
	}

	.percent {
		flex: none;
		color: var(--course-ink);
		font-weight: 600;
		font-variant-numeric: tabular-nums;
	}

	.progress {
		position: absolute;
		inset: auto 18px 10px;
		block-size: 3px;
		border-radius: var(--r-pill);
		background: light-dark(oklch(85% 0.05 var(--course-h)), oklch(38% 0.06 var(--course-h)));
		overflow: hidden;
	}

	.progress::after {
		content: '';
		position: absolute;
		inset: 0;
		border-radius: inherit;
		background: var(--course-bar);
		transform: scaleX(var(--progress));
		transform-origin: left;
		transition: transform var(--dur-slow) var(--ease-out);
	}

	@starting-style {
		.progress::after {
			transform: scaleX(0);
		}
	}

	.star {
		position: relative;
		z-index: 2;
		grid-column: 2;
		grid-row: 1;
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		margin-block-start: -6px;
		border-radius: var(--r-pill);
		color: light-dark(oklch(0% 0 0 / 0.45), oklch(100% 0 0 / 0.5));
		cursor: pointer;
		touch-action: manipulation;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	.star :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.star.starred {
		color: light-dark(oklch(76% 0.17 70), oklch(84% 0.16 84));
	}

	.star.starred.toggled :global(svg) {
		animation: pop var(--dur-spring) var(--spring-pop);
	}

	@media (hover: hover) {
		.star:hover {
			background: var(--state-hover);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.star:active {
		transform: scale(0.88);
		transition-duration: var(--dur-instant), 0s;
	}

	.body {
		position: relative;
		isolation: isolate;
		margin-block-start: 2px;
		padding: 6px 6px 8px;
		border-radius: var(--r-2xs) var(--r-2xs) var(--r-lg) var(--r-lg);
		background: var(--box-body);
		box-shadow: var(--inner-light);
		overflow: clip;
		container-type: inline-size;
	}

	.body > :global(.layer) {
		z-index: -1;
		--watermark: light-dark(oklch(93.5% 0.03 var(--course-h)), oklch(28% 0.04 var(--course-h)));
	}

	.deadlines {
		display: grid;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.deadline {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: 10px;
		min-block-size: 40px;
		padding: 6px 12px;
		border-radius: var(--r-sm);
		color: var(--text);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.deadline:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.deadline:focus-visible {
		outline-offset: -2px;
	}

	.kind {
		display: grid;
		color: var(--course-ink);
	}

	.kind :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.name {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		font-weight: 450;
		text-wrap: pretty;
	}

	.due {
		font: var(--type-meta);
		white-space: nowrap;
	}

	.due.danger {
		color: var(--danger);
		font-weight: 600;
	}

	.due.warning {
		color: var(--warning);
		font-weight: 600;
	}

	.due.muted {
		color: var(--text-muted);
	}

	.extra {
		padding: 4px 12px 2px 38px;
		color: var(--text-muted);
		font: var(--type-label);
	}

	/* A narrow card puts the due label under the title rather than squeezing both onto a line. */
	@container (width < 340px) {
		.deadline {
			grid-template-columns: auto minmax(0, 1fr);
			row-gap: 0;
		}

		.kind {
			grid-row: span 2;
		}

		.due {
			grid-column: 2;
		}
	}

	@keyframes pop {
		from {
			transform: scale(0.4);
		}
	}
</style>
