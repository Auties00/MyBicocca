<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCaretDown from '~icons/ph/caret-down';
	import IconCaretLeft from '~icons/ph/caret-left';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconMagnifyingGlassMinus from '~icons/ph/magnifying-glass-minus';
	import IconMagnifyingGlassPlus from '~icons/ph/magnifying-glass-plus';
	import type { IsoDate } from '#lib/format/days.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Popover from '#lib/ui/Popover.svelte';
	import SegmentedControl from '#lib/ui/SegmentedControl.svelte';
	import type { CalendarEvent } from '../model.ts';
	import type { CalendarView } from '../nav.svelte.ts';
	import MiniMonth from './MiniMonth.svelte';

	interface Props {
		view: CalendarView;
		/** The period on screen, as a title: "Ottobre 2026". */
		title: string;
		date: IsoDate;
		today: IsoDate;
		range: readonly IsoDate[];
		byDay: ReadonlyMap<IsoDate, readonly CalendarEvent[]>;
		/** Whether today is on screen: the Today button rests then. */
		showsToday: boolean;
		refreshing: boolean;
		zoom: number;
		zoomMin: number;
		zoomMax: number;
		onview: (view: CalendarView) => void;
		onstep: (delta: -1 | 1) => void;
		ontoday: () => void;
		onpick: (date: IsoDate) => void;
		onzoom: (factor: number) => void;
		onrefresh: () => void;
	}

	let {
		view,
		title,
		date,
		today,
		range,
		byDay,
		showsToday,
		refreshing,
		zoom,
		zoomMin,
		zoomMax,
		onview,
		onstep,
		ontoday,
		onpick,
		onzoom,
		onrefresh
	}: Props = $props();

	let picking = $state(false);

	const views = $derived([
		{ value: 'day' as const, label: m.calendar_view_day() },
		{ value: 'week' as const, label: m.calendar_view_week() },
		{ value: 'month' as const, label: m.calendar_view_month() }
	]);

	const previous = $derived(
		view === 'day'
			? m.calendar_previous_day()
			: view === 'week'
				? m.calendar_previous_week()
				: m.calendar_previous_month()
	);
	const next = $derived(
		view === 'day'
			? m.calendar_next_day()
			: view === 'week'
				? m.calendar_next_week()
				: m.calendar_next_month()
	);
</script>

<div class="toolbar">
	<div class="layout">
		<div class="period">
			<h1 class="sr-only">{m.calendar_title()}</h1>
			<Popover
				bind:open={picking}
				label={m.calendar_pick_day()}
				width="300px"
				placement="bottom-start"
			>
				{#snippet trigger(props)}
					<button
						type="button"
						class="title"
						{...props}
						aria-label={`${title}, ${m.calendar_jump_to_date()}`}
					>
						<span aria-live="polite">{title}</span>
						<IconCaretDown aria-hidden="true" />
					</button>
				{/snippet}
				<div class="picker">
					{#if picking}
						<MiniMonth
							selected={date}
							{today}
							{range}
							{byDay}
							autofocus
							onpick={(day) => {
								picking = false;
								onpick(day);
							}}
						/>
					{/if}
				</div>
			</Popover>
			<IconButton
				icon={IconArrowClockwise}
				label={refreshing ? m.calendar_refreshing() : m.calendar_refresh()}
				size="sm"
				loading={refreshing}
				onclick={onrefresh}
			/>
		</div>
		<div class="nav">
			<Button variant={showsToday ? 'ghost' : 'tonal'} onclick={ontoday} aria-keyshortcuts="T">
				{m.calendar_today()}
			</Button>
			<div class="steps">
				<IconButton
					icon={IconCaretLeft}
					label={previous}
					shortcut={['left']}
					onclick={() => onstep(-1)}
				/>
				<IconButton
					icon={IconCaretRight}
					label={next}
					shortcut={['right']}
					onclick={() => onstep(1)}
				/>
			</div>
		</div>
		<!-- Month has no density, but keeps the room, so nothing moves between views. -->
		<div
			class={['zoom', { idle: view === 'month' }]}
			role="group"
			aria-label={m.calendar_density()}
		>
			<IconButton
				icon={IconMagnifyingGlassMinus}
				label={m.calendar_zoom_out()}
				shortcut={['-']}
				disabled={zoom <= zoomMin}
				onclick={() => onzoom(1 / 1.2)}
			/>
			<IconButton
				icon={IconMagnifyingGlassPlus}
				label={m.calendar_zoom_in()}
				shortcut={['+']}
				disabled={zoom >= zoomMax}
				onclick={() => onzoom(1.2)}
			/>
		</div>
		<div class="views">
			<SegmentedControl
				options={views}
				value={view}
				label={m.calendar_view_label()}
				onchange={(value) => onview(value)}
			/>
		</div>
	</div>
</div>

<style>
	.toolbar {
		container: calendar-toolbar / inline-size;
	}

	/* Two rows, the same in every view: the period and the density, then the paging and the views.
	   One row once the whole set fits. */
	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		grid-template-areas:
			'period zoom'
			'nav views';
		align-items: center;
		gap: 8px 16px;
	}

	@container calendar-toolbar (width >= 900px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) auto auto auto;
			grid-template-areas: 'period nav zoom views';
		}
	}

	.period {
		grid-area: period;
		display: flex;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
	}

	.nav {
		grid-area: nav;
		display: flex;
		align-items: center;
		gap: 8px;
	}

	.views {
		grid-area: views;
		justify-self: end;
	}

	.title {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
		margin-inline-start: -8px;
		padding: 4px 10px 4px 8px;
		border-radius: var(--r-md);
		color: var(--text);
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
		text-align: start;
		cursor: pointer;
	}

	.title span {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.title :global(svg) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		color: var(--text-muted);
		transition: transform var(--dur-base) var(--ease-out);
	}

	.title[aria-expanded='true'] :global(svg) {
		transform: rotate(180deg);
	}

	@media (hover: hover) {
		.title:hover {
			background: var(--state-hover);
		}
	}

	.title:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.picker {
		padding: 8px;
		min-block-size: 300px;
	}

	.steps,
	.zoom {
		display: flex;
		gap: 2px;
	}

	.zoom {
		grid-area: zoom;
		justify-self: end;
	}

	.zoom.idle {
		visibility: hidden;
	}

	@container calendar-toolbar (width >= 900px) {
		.zoom {
			padding-inline: 4px;
			border-inline: 1px solid var(--hairline);
		}
	}
</style>
