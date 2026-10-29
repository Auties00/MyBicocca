<script lang="ts">
	import IconCaretRight from '~icons/ph/caret-right';
	import IconListChecks from '~icons/ph/list-checks';
	import IconReceipt from '~icons/ph/receipt';
	import IconCalendarCheck from '~icons/ph/calendar-check';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import { formatDate } from '#lib/format/dates.ts';
	import { dueTone } from '#lib/format/due.ts';
	import type { Icon } from '#lib/ui/types.ts';
	import { dayAnchor, isoDay } from '../calendar.ts';
	import type { Deadline, DeadlineSource } from '../deadlines.ts';
	import { deadlineDetail, deadlineKind, deadlineTitle, deadlineWhen } from '../labels.ts';
	import { deadlineHref } from '../links.ts';

	interface Props {
		item: Deadline;
		/** Another deadline of the same day sits above: the date isn't repeated. */
		continued?: boolean;
		/** Where the rail starts and ends. */
		first?: boolean;
		last?: boolean;
		/** Tighter, for previews. */
		dense?: boolean;
		/** The day's first row takes the day's id, so the calendar can jump to it. */
		anchor?: boolean;
	}

	let {
		item,
		continued = false,
		first = false,
		last = false,
		dense = false,
		anchor = false
	}: Props = $props();

	const icons: Record<DeadlineSource, Icon> = {
		result: IconListChecks,
		fee: IconReceipt,
		exam: IconCalendarCheck,
		booking: IconHourglass
	};

	const Glyph = $derived(icons[item.source]);
	const tone = $derived(item.source === 'fee' && item.expired ? 'danger' : dueTone(item.days));
	const month = $derived(formatDate(item.date, 'month').replace('.', ''));
	const iso = $derived(isoDay(item.date));
	const detail = $derived(deadlineDetail(item));
</script>

<li class={['row', tone, { continued, first, last, dense }]}>
	<!-- The numerals are for the eye: the link names the full date, heard in any list of links. -->
	<span class="date" aria-hidden="true">
		{#if !continued}
			<span class="day">{String(item.date.getDate()).padStart(2, '0')}</span>
			<span class="month">{month}</span>
		{/if}
	</span>
	<span class="rail" aria-hidden="true"><span class="node"></span></span>
	<a class="card" href={deadlineHref(item)} id={anchor && !continued ? dayAnchor(iso) : undefined}>
		<time class="sr-only" datetime={iso}>{formatDate(item.date, 'full')}</time>
		<span class="kicker">
			<Glyph aria-hidden="true" />
			<span>{deadlineKind(item)}</span>
			<span aria-hidden="true">·</span>
			<span class="when">{deadlineWhen(item)}</span>
		</span>
		<span class="title" translate={item.title === null ? undefined : 'no'}
			>{deadlineTitle(item)}</span
		>
		{#if detail}<span class="detail">{detail}</span>{/if}
		<span class="chevron" aria-hidden="true"><IconCaretRight /></span>
	</a>
</li>

<style>
	.row {
		--date-w: 52px;
		--rail-w: 24px;
		--node: var(--g9);
		--ink: var(--text-muted);
		display: grid;
		grid-template-columns: var(--date-w) var(--rail-w) minmax(0, 1fr);
		column-gap: 10px;
		padding-block-end: 10px;
	}

	.warning {
		--node: var(--warning-graphic);
		--ink: var(--warning);
	}

	.danger {
		--node: var(--danger);
		--ink: var(--danger);
	}

	.date {
		display: grid;
		align-content: start;
		justify-items: center;
		padding-block-start: 12px;
		line-height: 1;
	}

	.day {
		font: 600 1.75rem / 1 var(--font-ui);
		letter-spacing: -0.02em;
		font-variant-numeric: lining-nums tabular-nums;
	}

	.month {
		margin-block-start: 4px;
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: 0.08em;
		text-transform: uppercase;
	}

	.rail {
		position: relative;
		display: flex;
		justify-content: center;
	}

	/* The rail runs through every row of a section, from the first node to the last. */
	.rail::before {
		content: '';
		position: absolute;
		inset-block: 0 -10px;
		inset-inline-start: calc(50% - 1.5px);
		inline-size: 3px;
		border-radius: 2px;
		background: var(--hairline-strong);
	}

	.first .rail::before {
		inset-block-start: 22px;
	}

	.last .rail::before {
		inset-block-end: auto;
		block-size: 22px;
	}

	.first.last .rail::before {
		display: none;
	}

	.node {
		position: relative;
		margin-block-start: 14px;
		inline-size: 18px;
		block-size: 18px;
		border-radius: 50%;
		background: var(--bg);
		box-shadow:
			inset 0 0 0 4px var(--node),
			0 0 0 4px color-mix(in oklch, var(--node) 16%, transparent);
	}

	.continued .node {
		inline-size: 10px;
		block-size: 10px;
		margin-block-start: 18px;
		box-shadow: inset 0 0 0 3px var(--node);
	}

	.card {
		position: relative;
		display: grid;
		gap: 4px;
		padding: 13px 40px 14px 16px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.card:hover {
			background-image: linear-gradient(var(--state-hover));
			transition-duration: var(--dur-spring), 0s;
		}

		.card:hover .chevron {
			transform: translateX(2px);
			color: var(--text-muted);
		}
	}

	.card:active {
		background-image: linear-gradient(var(--state-press));
		transform: scale(0.99);
		transition-duration: var(--dur-instant);
	}

	.kicker {
		display: flex;
		align-items: center;
		gap: 6px;
		min-inline-size: 0;
		overflow: hidden;
		white-space: nowrap;
		color: var(--ink);
		font: 700 0.6875rem / 1.3 var(--font-ui);
		letter-spacing: 0.07em;
		text-transform: uppercase;
	}

	.kicker :global(svg) {
		flex-shrink: 0;
		inline-size: 14px;
		block-size: 14px;
		margin-inline-end: 1px;
	}

	.when {
		min-inline-size: 0;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.title {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: pretty;
	}

	.detail {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.chevron {
		position: absolute;
		inset-block: 0;
		inset-inline-end: 14px;
		display: flex;
		align-items: center;
		color: var(--text-subtle);
		transition:
			transform var(--dur-spring) var(--spring-pop),
			color var(--dur-fast) var(--ease-out);
	}

	.chevron :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.dense {
		--date-w: 40px;
		--rail-w: 18px;
		column-gap: 8px;
		padding-block-end: 8px;
	}

	.dense .rail::before {
		inset-block-end: -8px;
	}

	.dense .date {
		padding-block-start: 10px;
	}

	.dense .day {
		font-size: 1.5rem;
	}

	.dense .node {
		margin-block-start: 12px;
		inline-size: 14px;
		block-size: 14px;
		box-shadow:
			inset 0 0 0 3.5px var(--node),
			0 0 0 3px color-mix(in oklch, var(--node) 16%, transparent);
	}

	.dense .card {
		gap: 2px;
		padding: 10px 36px 11px 14px;
		border-radius: var(--r-md);
	}

	.dense .title {
		font: var(--type-row-title);
	}

	@container (width < 360px) {
		.row {
			--date-w: 40px;
			--rail-w: 18px;
			column-gap: 8px;
		}

		.day {
			font-size: 1.625rem;
		}

		.card {
			padding-inline: 14px 34px;
		}
	}
</style>
