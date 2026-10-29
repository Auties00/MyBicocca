<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { clockTime, displayTitle, kindLabel, timeRange } from '../format.ts';
	import { ICON_PADDING, type Density } from '../layout.ts';
	import { cardFonts, textWidth } from '../measure.ts';
	import { compactLabel, locationLine, type CalendarEvent } from '../model.ts';
	import { eventSwatch } from '../palette.ts';

	interface Props {
		event: CalendarEvent;
		density: Density;
		inProgress: boolean;
		/** The open event: it anchors the popover and stays highlighted. */
		selected: boolean;
		/** Width in px, which decides what a one-line card has room for. */
		widthPx: number;
		/** Height in px: a tall icon card adds the place, when it fits whole. */
		heightPx: number;
		/** Position in the day column, as CSS lengths. */
		top: string;
		height: string;
		left: string;
		width: string;
		onopen: (event: CalendarEvent, card: HTMLElement) => void;
	}

	let {
		event,
		density,
		inProgress,
		selected,
		widthPx,
		heightPx,
		top,
		height,
		left,
		width,
		onopen
	}: Props = $props();

	const swatch = $derived(eventSwatch(event, inProgress));
	const place = $derived(locationLine(event));
	const time = $derived(timeRange(event));
	const title = $derived(displayTitle(event));
	const iconPlace = $derived(
		density === 'icon' &&
			heightPx >= 56 &&
			place !== '' &&
			textWidth(place, cardFonts.small) <= widthPx - ICON_PADDING
	);
	/* One line: the time and the title where there is room, else Android's label and place. */
	const line = $derived(
		widthPx >= 70
			? `${clockTime(event.start)} · ${title}`
			: `${compactLabel(event)} · ${place || title.slice(0, 8)}`
	);
	const label = $derived(
		[
			event.cancelled ? m.calendar_detail_cancelled() : null,
			title,
			kindLabel(event),
			time,
			place,
			inProgress ? m.calendar_in_progress() : null
		]
			.filter(Boolean)
			.join(', ')
	);
</script>

<button
	type="button"
	class={['card', 'calendar-swatch', `d-${density}`, { cancelled: event.cancelled, selected }]}
	style:--sw-h={swatch.hue}
	style:--sw-c={swatch.chroma}
	style:top
	style:height
	style:left
	style:width
	style:anchor-name={selected ? '--calendar-event' : undefined}
	data-event={event.id}
	onclick={(e) => onopen(event, e.currentTarget)}
>
	<span class="sr-only">{label}</span>
	{#if density === 'dot'}
		<span class="dot" aria-hidden="true"></span>
	{:else if density === 'mini'}
		<span class="line" lang="it" aria-hidden="true">{line}</span>
	{:else if density === 'label' || density === 'icon'}
		<span class="lead" aria-hidden="true">
			<span class="short">{compactLabel(event)}</span>
			{#if density === 'icon'}<span class="start">{clockTime(event.start)}</span>{/if}
			{#if iconPlace}<span class="start">{place}</span>{/if}
		</span>
	{:else if density === 'compact'}
		<span class="lead" aria-hidden="true">
			<span class="time">{time}</span>
			<span class="title one" lang="it">{title}</span>
		</span>
	{:else}
		<span class="lead" aria-hidden="true">
			<span class="time">{time}</span>
			<span class="title" lang="it">{title}</span>
			<span class="kind">{kindLabel(event)}</span>
			{#if place}<span class="veil"></span>{/if}
		</span>
		{#if place}<span class="place" aria-hidden="true">{place}</span>{/if}
	{/if}
</button>

<style>
	.card {
		position: absolute;
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 2px;
		min-inline-size: 6px;
		min-block-size: 8px;
		overflow: clip;
		z-index: 1;
		padding: 6px 6px 6px 10px;
		border-radius: 6px;
		background: var(--sw-container);
		color: var(--sw-ink);
		text-align: start;
		box-shadow: inset 3px 0 0 var(--sw-accent);
		cursor: pointer;
		touch-action: manipulation;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			box-shadow var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.d-full,
	.d-tight {
		padding: 8px 10px 8px 13px;
		border-radius: 8px;
	}

	@media (hover: hover) {
		.card:hover {
			background: var(--sw-container-hover);
			transition-duration: 0s;
		}
	}

	.card:active {
		transform: scale(0.98);
		transition-duration: var(--dur-instant);
	}

	.card:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 1px;
		z-index: 3;
	}

	.selected {
		z-index: 2;
		box-shadow:
			inset 3px 0 0 var(--sw-accent),
			0 0 0 2px var(--sw-accent),
			var(--elev-2);
	}

	.cancelled {
		background: color-mix(in oklch, var(--sw-container) 55%, transparent);
		box-shadow: inset 3px 0 0 color-mix(in oklch, var(--sw-accent) 45%, transparent);
		outline: 1.5px dashed color-mix(in oklch, var(--sw-accent) 60%, transparent);
		outline-offset: -1.5px;
	}

	.cancelled .title {
		text-decoration: line-through;
	}

	/* A card cut by the top of the grid keeps its time and title in view, under the day heads. */
	.lead {
		position: sticky;
		inset-block-start: calc(var(--head-h, 0px) + 6px);
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		align-self: stretch;
		gap: 2px;
		background: inherit;
	}

	/*
	 * Pushed down over the place at the card's foot, the lead covers it whole rather than cutting
	 * its line in half.
	 */
	.d-full .lead,
	.d-tight .lead {
		container-type: scroll-state;
	}

	.veil {
		position: absolute;
		inset-block-start: 100%;
		inset-inline: 0;
		display: none;
		block-size: 2em;
		background: inherit;
	}

	@container scroll-state(stuck: top) {
		.veil {
			display: block;
		}
	}

	.dot {
		position: absolute;
		/* Centred on a short card, near the top of a tall narrow one. */
		inset-block-start: min(50%, 14px);
		inset-inline-start: 6px;
		inline-size: 8px;
		block-size: 8px;
		margin-block-start: -4px;
		border-radius: 50%;
		background: var(--sw-accent);
	}

	.d-dot {
		padding: 0;
	}

	.d-mini {
		justify-content: center;
		padding-inline: 8px 4px;
	}

	.line,
	.title.one,
	.short,
	.start,
	.time,
	.kind,
	.place {
		max-inline-size: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.line {
		font: 600 0.5625rem / 1.2 var(--font-ui);
	}

	.d-icon,
	.d-label {
		padding: 6px 2px 4px 7px;
	}

	.short {
		font: 600 0.625rem / 1.3 var(--font-ui);
		letter-spacing: 0.02em;
	}

	.start {
		font: 500 0.5625rem / 1.3 var(--font-ui);
		font-variant-numeric: tabular-nums;
		opacity: 0.8;
	}

	.time {
		color: var(--sw-ink-muted);
		font: 600 0.625rem / 1.3 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.d-full .time {
		font-size: 0.6875rem;
		letter-spacing: 0.03em;
	}

	/* Course names are Italian whatever the UI language, so they wrap as Italian; the density
	   prints a title only where its longest word fits whole (see cardDensity). */
	.title {
		display: -webkit-box;
		max-inline-size: 100%;
		overflow: hidden;
		font: 500 0.75rem / 1.3 var(--font-ui);
		text-overflow: ellipsis;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow-wrap: normal;
		hyphens: auto;
		hyphenate-limit-chars: 6 3 3;
	}

	.title.one {
		display: block;
	}

	.d-full .title {
		font: 600 0.875rem / 1.3 var(--font-ui);
		letter-spacing: -0.005em;
	}

	.d-tight .title {
		font-weight: 600;
	}

	.kind {
		color: var(--sw-ink-muted);
		font: var(--type-label);
		font-size: 0.6875rem;
	}

	.place {
		margin-block-start: auto;
		color: var(--sw-ink-muted);
		font: 500 0.6875rem / 1.3 var(--font-ui);
	}
</style>
