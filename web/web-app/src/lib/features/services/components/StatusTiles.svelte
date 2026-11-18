<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { serviceGroupLabels, type ServiceEntry, type ServiceGroup } from '#lib/shell/catalog.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import IconChip from '#lib/ui/IconChip.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import type { DeadlinesQuery, DeadlineStream } from '../deadlines.svelte.ts';
	import type { ServiceStatus } from '../deadlines.ts';
	import { serviceEntry } from '../hub.ts';
	import { onDay } from '../labels.ts';

	interface Props {
		deadlines: DeadlinesQuery;
		/** The page's skeletons are up: the tiles hold their place with theirs. */
		loading: boolean;
	}

	let { deadlines, loading }: Props = $props();

	interface Tile {
		group: ServiceGroup;
		service: ServiceEntry;
		/** The stream the tile's figure comes from: without it, the tile can't tell. */
		stream: DeadlineStream;
		/** Another stream the second line reads, and what that line says without it. */
		also?: { stream: DeadlineStream; unverified: () => string };
		/** How many things wait: none is a calm tile. */
		count: (status: ServiceStatus) => number;
		value: (status: ServiceStatus) => string;
		/** The second line, and its tone when it warns; empty when all is calm. */
		meta: (status: ServiceStatus) => { text: string; tone?: 'warning' | 'danger' };
	}

	const firstDue = (count: number, due: Date | null, many: typeof m.services_tile_due) =>
		due === null ? '' : count === 1 ? m.services_tile_due(onDay(due)) : many(onDay(due));

	/*
	 * Aggregates only, as the timeline below names each item. Documents has no live state to
	 * report, so it has no tile.
	 */
	const tiles: Tile[] = [
		{
			group: 'teaching',
			service: serviceEntry('results'),
			stream: 'results',
			count: (s) => s.pendingResults,
			value: (s) =>
				s.pendingResults > 0
					? m.services_tile_results({ count: s.pendingResults })
					: m.services_tile_results_none(),
			/* The day is the last to reject: a grade not rejected by then stands. */
			meta: (s) => ({
				text:
					s.resultsDue === null
						? ''
						: s.pendingResults === 1
							? m.services_tile_result_due(onDay(s.resultsDue))
							: m.services_tile_results_first(onDay(s.resultsDue))
			})
		},
		{
			group: 'bookings',
			service: serviceEntry('exams'),
			stream: 'exams',
			also: { stream: 'bookings', unverified: m.services_tile_closings_unverified },
			count: (s) => s.bookedExams,
			value: (s) =>
				s.bookedExams > 0
					? m.services_tile_exams({ count: s.bookedExams })
					: m.services_tile_exams_none(),
			meta: (s) => {
				if (s.closingBookings > 0)
					return {
						text: m.services_tile_closing({ count: s.closingBookings }),
						tone: 'warning'
					};
				if (s.nextExam === null) return { text: '' };
				return {
					text: m.services_tile_exams_next({ date: formatDate(s.nextExam, 'weekdayDayMonth') })
				};
			}
		},
		{
			group: 'fees',
			service: serviceEntry('fees'),
			stream: 'fees',
			count: (s) => s.owedFees,
			value: (s) =>
				s.owedFees > 0
					? m.services_tile_fees({ amount: formatCurrency(s.owedAmount) })
					: m.services_tile_fees_none(),
			meta: (s) =>
				s.overdueFees > 0
					? { text: m.services_tile_fees_overdue({ count: s.overdueFees }), tone: 'danger' }
					: {
							text: firstDue(s.owedFees, s.feesDue, m.services_tile_fees_first)
						}
		}
	];

	/** The tile's two lines, or undefined when its own stream is missing and it can't tell. */
	function lines(tile: Tile, status: ServiceStatus | undefined) {
		if (status === undefined || deadlines.missing.includes(tile.stream)) return undefined;
		const meta =
			tile.also && deadlines.missing.includes(tile.also.stream)
				? { text: tile.also.unverified(), tone: 'warning' as const }
				: tile.meta(status);
		return { calm: tile.count(status) === 0, value: tile.value(status), meta };
	}
</script>

<div class="strip">
	<ul class="tiles" role="list">
		{#each tiles as tile (tile.group)}
			{@const shown = lines(tile, deadlines.services)}
			<li>
				<a class="tile" href={shellHref(tile.service.path)}>
					<span class="head">
						<IconChip icon={tile.service.icon} tone={tile.service.tone} size="sm" />
						<span class="label">{serviceGroupLabels[tile.group]()}</span>
					</span>
					<!-- Every line keeps its height from skeleton to text, so nothing below moves. -->
					{#if loading}
						<span class="value"><Skeleton variant="text" width="70%" /></span>
						<span class="meta"><Skeleton variant="text" width="45%" /></span>
					{:else if shown}
						<span class={['value', { calm: shown.calm }]}>{shown.value}</span>
						<span class={['meta', shown.meta.tone]}>{shown.meta.text}</span>
					{:else}
						<span class="value unavailable">{m.services_tile_unavailable()}</span>
						<span class="meta">{m.services_tile_unavailable_meta()}</span>
					{/if}
				</a>
			</li>
		{/each}
	</ul>
</div>

<style>
	/* One connected strip, as a ListGroup laid on its side; it stacks when narrow. */
	.strip {
		container-type: inline-size;
	}

	.tiles {
		--outer: var(--r-lg);
		--inner: var(--r-2xs);
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	li {
		display: grid;
		border-radius: var(--inner);
		background: var(--surface);
		box-shadow: var(--inner-light);
		overflow: clip;
	}

	li:first-child {
		border-start-start-radius: var(--outer);
		border-start-end-radius: var(--outer);
	}

	li:last-child {
		border-end-start-radius: var(--outer);
		border-end-end-radius: var(--outer);
	}

	.tile {
		display: grid;
		align-content: start;
		gap: 2px;
		padding: 14px 16px 16px;
		color: var(--text);
		text-decoration: none;
		touch-action: manipulation;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.tile:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		.tile:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.tile:active {
		background: var(--state-press);
		transition-duration: 0s;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 10px;
		margin-block-end: 8px;
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.value,
	.meta {
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.value {
		font: var(--type-body-strong);
		font-size: 1.0625rem;
		line-height: 1.4;
		letter-spacing: -0.006em;
		font-variant-numeric: tabular-nums;
	}

	/* Nothing waiting reads quieter, on the same line height so the tiles stay level. */
	.value.calm,
	.value.unavailable {
		color: var(--text-muted);
		font: var(--type-ui);
		font-size: 0.9375rem;
		line-height: calc(1.0625rem * 1.4);
	}

	/* An empty line still holds its place, so calm and busy tiles stand the same height. */
	.meta {
		min-block-size: 1lh;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.meta.warning {
		color: var(--warning);
	}

	.meta.danger {
		color: var(--danger);
	}

	@container (width >= 600px) {
		.tiles {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}

		li:first-child {
			border-radius: var(--outer) var(--inner) var(--inner) var(--outer);
		}

		li:last-child {
			border-radius: var(--inner) var(--outer) var(--outer) var(--inner);
		}
	}
</style>
