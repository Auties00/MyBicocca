<script lang="ts">
	import { untrack, type Snippet } from 'svelte';
	import { on } from 'svelte/events';
	import { addDays, weekdayIndex, type IsoDate } from '#lib/format/days.ts';
	import type { WallClock } from '#lib/format/rome.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { clockTime } from '../format.ts';
	import {
		cardDensity,
		clampZoom,
		layoutDay,
		oddHourAlpha,
		openingMinute,
		pinchScroll,
		type Placement,
		type TimeSpan
	} from '../layout.ts';
	import { cardText } from '../measure.ts';
	import { isInProgress, type CalendarEvent } from '../model.ts';
	import EventCard from './EventCard.svelte';

	interface Props {
		/** The days on screen: one (day view) or seven (week view). */
		days: IsoDate[];
		byDay: ReadonlyMap<IsoDate, readonly CalendarEvent[]>;
		window: TimeSpan;
		now: WallClock;
		zoom: number;
		/** Phones: swipe to the previous or next page, which `onpage` then selects. */
		swipe?: boolean;
		/** Hour labels as "08" in a narrow gutter (phones), or "08:00". */
		narrow?: boolean;
		/** The day headers above each page's columns; they stay in view while the grid scrolls. */
		header?: Snippet<[IsoDate[]]>;
		/** Skeleton cards while the period has never loaded. */
		loading?: boolean;
		/** What an empty period offers, under its serif line. */
		empty?: Snippet;
		selectedId: string | null;
		/** Room the floating tab bar takes at the bottom, as a CSS length. */
		bottomInset?: string;
		/** Names the scrolling grid for screen readers: "Impegni: 26 ottobre – 1 novembre 2026". */
		label: string;
		onopen: (event: CalendarEvent, card: HTMLElement) => void;
		onpage?: (delta: -1 | 1) => void;
		/** While swiping: how far, in pages, toward the next (positive) or previous one. */
		ondrag?: (fraction: number) => void;
	}

	let {
		days,
		byDay,
		window: span,
		now,
		zoom = $bindable(),
		swipe = false,
		narrow = false,
		header,
		loading = false,
		empty,
		selectedId,
		bottomInset = '0px',
		label,
		onopen,
		onpage,
		ondrag
	}: Props = $props();

	const step = $derived(days.length);
	const pages = $derived(
		swipe ? [-1, 0, 1].map((offset) => days.map((day) => addDays(day, offset * step))) : [days]
	);
	const allDays = $derived(pages.flat());
	const layouts = $derived(
		new Map(allDays.map((day) => [day, layoutDay(byDay.get(day) ?? [])] as const))
	);
	const hours = $derived(
		Array.from(
			{ length: Math.floor(span.end / 60) - Math.ceil(span.start / 60) + 1 },
			(_, index) => Math.ceil(span.start / 60) + index
		)
	);
	const minutePx = $derived(zoom);
	const bodyHeight = $derived((span.end - span.start) * minutePx);
	const oddAlpha = $derived(oddHourAlpha(minutePx));
	const inset = $derived(step === 1 ? 2 : 1);
	const nowVisible = $derived(now.minutes >= span.start && now.minutes <= span.end);
	const isEmpty = $derived(!loading && days.every((day) => (byDay.get(day)?.length ?? 0) === 0));

	let scroller = $state<HTMLElement>();
	let viewportWidth = $state(0);
	let headHeight = $state(0);
	let dragX = $state(0);
	let settling = $state(false);

	/* Wider than a phone, an empty Saturday or Sunday takes half a column: the weekdays get the room. */
	function weights(page: readonly IsoDate[]): number[] {
		return page.map((day) =>
			!narrow && page.length === 7 && weekdayIndex(day) >= 5 && !byDay.get(day)?.length ? 0.5 : 1
		);
	}
	const columns = $derived(
		new Map(
			pages.map((page) => {
				const shares = weights(page);
				const total = shares.reduce((sum, share) => sum + share, 0);
				return [
					page[0],
					{
						template: shares.map((share) => `minmax(0, ${share}fr)`).join(' '),
						widths: shares.map((share) => (viewportWidth * share) / total)
					}
				] as const;
			})
		)
	);

	/* A swipe settles on its page, then the new days arrive: only then may the track recentre. */
	$effect.pre(() => {
		void days[0];
		settling = false;
		dragX = 0;
	});

	/*
	 * Opens where the period's events are (see `openingMinute`), or at the top when the whole day
	 * nearly fits. The sources arrive one by one, so it follows them until the user scrolls or
	 * pages away from the period it opened on.
	 */
	const onScreen = $derived(days.flatMap((day) => byDay.get(day) ?? []));
	const openedOn = untrack(() => days[0]);
	let scrolledByUser = false;
	let quietUntil = 0;

	/** Scroll a grid this much taller than its viewport doesn't need: it opens at the top. */
	const FITS_SLACK = 150;

	function watchScroll(element: HTMLElement) {
		return on(
			element,
			'scroll',
			() => {
				if (performance.now() > quietUntil) scrolledByUser = true;
			},
			{ passive: true }
		);
	}

	/*
	 * Before the first paint, so the grid never shows at the top first; again in the next frame,
	 * where the page has laid the grid out in its frame after it attaches.
	 */
	$effect(() => {
		void onScreen;
		if (loading || !scroller || days[0] !== openedOn) return;
		const element = scroller;
		const open = () =>
			untrack(() => {
				if (!scrolledByUser) scrollTo(element);
			});
		open();
		const frame = requestAnimationFrame(open);
		return () => cancelAnimationFrame(frame);
	});

	function scrollTo(element: HTMLElement) {
		const grid = element.firstElementChild;
		const inset = grid ? parseFloat(getComputedStyle(grid).paddingBlockEnd) || 0 : 0;
		const head = element.querySelector<HTMLElement>('.page:not([inert]) > .head');
		const room = element.clientHeight - (head?.offsetHeight ?? 0) - inset;
		quietUntil = performance.now() + 200;
		if (bodyHeight - room <= FITS_SLACK) {
			element.scrollTop = 0;
			return;
		}
		const target = openingMinute({
			window: span,
			events: onScreen,
			today:
				days.includes(now.date) && nowVisible
					? { now: now.minutes, events: byDay.get(now.date) ?? [] }
					: null,
			visible: room / minutePx
		});
		element.scrollTop = (target - span.start) * minutePx;
	}

	function measure(element: HTMLElement) {
		const observer = new ResizeObserver(([entry]) => {
			viewportWidth = entry.contentRect.width;
		});
		observer.observe(element);
		return () => observer.disconnect();
	}

	/*
	 * The first height is read at once: the observer's first report can come a frame after the head
	 * is painted, and the gutter would then shift down. Later heights (the next week's head) come
	 * from the observer, with no layout forced.
	 */
	function measureHead(element: HTMLElement) {
		if (headHeight === 0) headHeight = element.offsetHeight;
		const observer = new ResizeObserver(([entry]) => {
			headHeight = entry.borderBoxSize[0]?.blockSize ?? entry.contentRect.height;
		});
		observer.observe(element);
		return () => observer.disconnect();
	}

	/** Zooms by `factor` around `focalY` (px from the scroller's top), keeping that spot still. */
	function zoomAround(factor: number, focalY: number) {
		if (!scroller) return;
		const next = clampZoom(zoom * factor);
		const applied = next / zoom;
		if (applied === 1) return;
		const max = Math.max(0, (span.end - span.start) * next + headHeight - scroller.clientHeight);
		const top = pinchScroll(scroller.scrollTop, focalY - headHeight, applied, max);
		zoom = next;
		scroller.scrollTop = top;
	}

	/** Zooms around the middle of the visible grid, as the zoom buttons and keys do. */
	export function zoomBy(factor: number) {
		if (!scroller) return;
		zoomAround(factor, headHeight + (scroller.clientHeight - headHeight) / 2);
	}

	/* Two fingers pinch the grid; Ctrl (⌘) with the wheel or a trackpad pinch does the same. */
	function gestures(element: HTMLElement) {
		let span0 = 0;
		/* Blocking touchmove would hold up every scroll: it listens only while two fingers are down. */
		let stopPinch: (() => void) | null = null;
		const distance = (touches: TouchList) => Math.abs(touches[1].clientY - touches[0].clientY);
		const focal = (touches: TouchList) =>
			(touches[0].clientY + touches[1].clientY) / 2 - element.getBoundingClientRect().top;
		const pinch = (event: TouchEvent) => {
			if (event.touches.length !== 2) return;
			event.preventDefault();
			const current = distance(event.touches);
			if (span0 > 8 && current > 8) zoomAround(current / span0, focal(event.touches));
			span0 = current;
		};
		const release = (event: TouchEvent) => {
			if (event.touches.length >= 2 || !stopPinch) return;
			stopPinch();
			stopPinch = null;
		};
		const offs = [
			on(
				element,
				'touchstart',
				(event) => {
					if (event.touches.length !== 2) return;
					span0 = distance(event.touches);
					stopPinch ??= on(element, 'touchmove', pinch, { passive: false });
				},
				{ passive: true }
			),
			on(element, 'touchend', release, { passive: true }),
			on(element, 'touchcancel', release, { passive: true }),
			() => stopPinch?.(),
			on(
				element,
				'wheel',
				(event) => {
					if (!event.ctrlKey && !event.metaKey) return;
					event.preventDefault();
					const delta = Math.max(-60, Math.min(60, event.deltaY));
					zoomAround(Math.exp(-delta * 0.004), event.clientY - element.getBoundingClientRect().top);
				},
				{ passive: false }
			)
		];
		return () => offs.forEach((off) => off());
	}

	/* Horizontal swipes page through days or weeks; vertical ones stay the browser's scroll. */
	function swiping(element: HTMLElement) {
		let start: { x: number; y: number; t: number; id: number } | null = null;
		let horizontal = false;
		const down = (event: PointerEvent) => {
			if (!swipe || event.pointerType === 'mouse' || !event.isPrimary || settling) return;
			start = { x: event.clientX, y: event.clientY, t: event.timeStamp, id: event.pointerId };
			horizontal = false;
		};
		const move = (event: PointerEvent) => {
			if (!start || event.pointerId !== start.id) return;
			const dx = event.clientX - start.x;
			const dy = event.clientY - start.y;
			if (!horizontal) {
				if (Math.abs(dy) > 10 && Math.abs(dy) > Math.abs(dx)) start = null;
				else if (Math.abs(dx) > 10) {
					horizontal = true;
					element.setPointerCapture(event.pointerId);
				}
				return;
			}
			dragX = dx;
			ondrag?.(-dx / Math.max(1, viewportWidth));
		};
		const up = (event: PointerEvent) => {
			if (!start || event.pointerId !== start.id) return;
			const dx = event.clientX - start.x;
			const velocity = dx / Math.max(1, event.timeStamp - start.t);
			start = null;
			if (!horizontal) return;
			horizontal = false;
			const delta = Math.abs(dx) > viewportWidth * 0.25 || Math.abs(velocity) > 0.45;
			settling = true;
			if (delta && event.type === 'pointerup') {
				const direction = dx < 0 ? 1 : -1;
				dragX = -direction * viewportWidth;
				ondrag?.(direction);
			} else {
				dragX = 0;
				ondrag?.(0);
			}
		};
		const settled = (event: TransitionEvent) => {
			if (event.target !== event.currentTarget || !settling) return;
			if (dragX === 0) {
				settling = false;
				return;
			}
			onpage?.(dragX < 0 ? 1 : -1);
		};
		const offs = [
			on(element, 'pointerdown', down),
			on(element, 'pointermove', move),
			on(element, 'pointerup', up),
			on(element, 'pointercancel', up),
			on(element.firstElementChild as HTMLElement, 'transitionend', settled)
		];
		return () => offs.forEach((off) => off());
	}

	function frame(placed: Placement<CalendarEvent>, columnWidth: number) {
		const top = (placed.start - span.start) * minutePx + 1;
		const height = Math.max(8, (placed.end - placed.start) * minutePx - 2);
		const width = Math.max(6, columnWidth * placed.width - inset * 2);
		return {
			top: `${top}px`,
			height: `${height}px`,
			left: `calc(${placed.x * 100}% + ${inset}px)`,
			width: `max(6px, calc(${placed.width * 100}% - ${inset * 2}px))`,
			widthPx: width,
			heightPx: height,
			density: cardDensity(width, height, cardText(placed.item))
		};
	}

	/*
	 * Plausible blocks for the skeleton, the same on every visit, in minutes after the top of the
	 * grid rather than at a time of day: the window can still grow while sources arrive, and a
	 * placeholder that moved with it would count as a layout shift.
	 */
	const skeletonBlocks = [
		[90, 210],
		[360, 480],
		[30, 150],
		[150, 270],
		[390, 510]
	] as const;
</script>

<div
	class={['timeline', { narrow }]}
	style:--minute-px={minutePx}
	style:--odd-alpha={oddAlpha}
	style:--body-h="{bodyHeight}px"
	style:--head-h="{headHeight}px"
	style:--bottom-inset={bottomInset}
>
	<div
		class="scroller"
		role="region"
		aria-label={label}
		bind:this={scroller}
		{@attach gestures}
		{@attach watchScroll}
	>
		<div class="grid">
			<div class="gutter" aria-hidden="true">
				{#if header}<div class="gutter-head"></div>{/if}
				<div class="labels">
					{#each hours as hour (hour)}
						{#if hour * 60 < span.end}
							<span
								class={['label', { odd: hour % 2 === 1, top: hour * 60 <= span.start }]}
								style:top="{Math.max(0, (hour * 60 - span.start) * minutePx)}px"
							>
								{narrow ? String(hour % 24).padStart(2, '0') : clockTime((hour % 24) * 60)}
							</span>
						{/if}
					{/each}
				</div>
			</div>
			<div class="viewport" {@attach measure} {@attach swiping}>
				<div
					class={['track', { settling, swiping: swipe }]}
					style:--pages={pages.length}
					style:--drag="{dragX}px"
				>
					{#each pages as page (page[0])}
						{@const current = page[0] === days[0]}
						{@const grid = columns.get(page[0])}
						<div
							class="page"
							style:--cols={page.length}
							style:--day-cols={grid?.template}
							inert={!current}
							aria-hidden={!current}
						>
							{#if header}
								<div class="head" {@attach current ? measureHead : undefined}>
									{@render header(page)}
								</div>
							{/if}
							<div class="body">
								{#each hours as hour (hour)}
									<span
										class={['hour', { odd: hour % 2 === 1 }]}
										style:top="{(hour * 60 - span.start) * minutePx}px"
									></span>
								{/each}
								{#if page.includes(now.date) && nowVisible && page.length > 1}
									<span
										class="now-faint"
										style:top="{(now.minutes - span.start) * minutePx}px"
										aria-hidden="true"
									></span>
								{/if}
								<div class="columns">
									{#each page as day, index (day)}
										<div class={['column', { first: index === 0 }]}>
											{#if loading}
												{@const block = skeletonBlocks[(index + (page.length === 1 ? 1 : 0)) % 5]}
												{#if index < 5 || page.length === 1}
													<span
														class="skeleton"
														style:top="{block[0] * minutePx}px"
														style:height="{(block[1] - block[0]) * minutePx}px"
													>
														<Skeleton height="100%" radius="8px" />
													</span>
												{/if}
											{:else}
												{#each layouts.get(day) ?? [] as placed (placed.item.id)}
													{@const geometry = frame(placed, grid?.widths[index] ?? 0)}
													<EventCard
														event={placed.item}
														density={geometry.density}
														inProgress={isInProgress(placed.item, now.date, now.minutes)}
														selected={placed.item.id === selectedId}
														widthPx={geometry.widthPx}
														heightPx={geometry.heightPx}
														top={geometry.top}
														height={geometry.height}
														left={geometry.left}
														width={geometry.width}
														{onopen}
													/>
												{/each}
											{/if}
											{#if day === now.date && nowVisible}
												<span
													class="now"
													style:top="{(now.minutes - span.start) * minutePx}px"
													role="img"
													aria-label={m.ui_now_at({ time: clockTime(now.minutes) })}
												></span>
												<span
													class="now-dot"
													style:top="{(now.minutes - span.start) * minutePx}px"
													aria-hidden="true"
												></span>
											{/if}
										</div>
									{/each}
								</div>
								{#if current && isEmpty}
									<div class="empty-wrap">
										<div class="empty">
											<p>{step === 1 ? m.calendar_free_day() : m.calendar_free_week()}</p>
											{#if empty}{@render empty()}{/if}
										</div>
									</div>
								{/if}
							</div>
						</div>
					{/each}
				</div>
			</div>
		</div>
	</div>
</div>

<style>
	.timeline {
		--gutter-w: 56px;
		position: relative;
		display: flex;
		flex-direction: column;
		min-block-size: 0;
		flex: 1;
	}

	.narrow {
		--gutter-w: 38px;
	}

	.scroller {
		flex: 1;
		min-block-size: 0;
		overflow-y: auto;
		overscroll-behavior-y: contain;
		scrollbar-gutter: stable;
		scrollbar-width: thin;
	}

	.narrow .scroller {
		scrollbar-width: none;
	}

	.narrow .grid {
		padding-inline-end: var(--sp-2);
	}

	.grid {
		display: grid;
		grid-template-columns: var(--gutter-w) minmax(0, 1fr);
		padding-block-end: calc(16px + var(--bottom-inset));
	}

	.gutter {
		position: relative;
	}

	.gutter-head {
		position: sticky;
		inset-block-start: 0;
		z-index: 3;
		block-size: var(--head-h);
		background: var(--calendar-bg, var(--bg));
	}

	.labels {
		position: relative;
		block-size: var(--body-h);
	}

	.label {
		position: absolute;
		inset-inline: 0 8px;
		color: var(--text-muted);
		font: 500 0.6875rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		text-align: end;
		transform: translateY(-50%);
	}

	.label.top {
		transform: none;
	}

	.narrow .label {
		inset-inline-end: 6px;
	}

	.label.odd {
		opacity: var(--odd-alpha);
	}

	.viewport {
		position: relative;
		min-inline-size: 0;
		overflow-x: clip;
		touch-action: pan-y;
	}

	.track {
		display: grid;
		grid-template-columns: repeat(var(--pages), 100%);
		transform: translateX(calc(var(--drag) - (var(--pages) - 1) / 2 * 100%));
	}

	.track.swiping {
		will-change: transform;
	}

	.track.settling {
		transition: transform var(--dur-slow) var(--ease-out-expo);
	}

	.page {
		min-inline-size: 0;
	}

	.head {
		position: sticky;
		inset-block-start: 0;
		z-index: 5;
		background: var(--calendar-bg, var(--bg));
	}

	.body {
		position: relative;
		block-size: var(--body-h);
	}

	.hour {
		position: absolute;
		inset-inline: 0;
		block-size: 1px;
		background: var(--hairline);
	}

	.hour.odd {
		opacity: var(--odd-alpha);
	}

	.columns {
		position: absolute;
		inset: 0;
		display: grid;
		grid-template-columns: var(--day-cols, repeat(var(--cols), minmax(0, 1fr)));
	}

	.column {
		position: relative;
		min-inline-size: 0;
	}

	.column:not(.first) {
		box-shadow: inset 1px 0 0 var(--hairline);
	}

	.skeleton {
		position: absolute;
		inset-inline: 2px;
		display: flex;
	}

	.skeleton > :global(*) {
		flex: 1;
	}

	/* The line runs under the cards, so an event in progress never reads as struck through; the
	   dot stays on top to mark the time. */
	.now,
	.now-faint {
		position: absolute;
		inset-inline: 0;
		z-index: 0;
		block-size: 2px;
		margin-block-start: -1px;
		pointer-events: none;
	}

	.now {
		background: var(--accent);
	}

	.now-dot {
		position: absolute;
		inset-inline-start: -4px;
		z-index: 4;
		inline-size: 8px;
		block-size: 8px;
		margin-block-start: -4px;
		border-radius: 50%;
		background: var(--accent);
		box-shadow: 0 0 0 1.5px var(--calendar-bg, var(--bg));
		pointer-events: none;
	}

	.now-faint {
		z-index: 0;
		block-size: 1px;
		margin-block-start: 0;
		background: color-mix(in oklch, var(--accent) 45%, transparent);
	}

	.empty-wrap {
		position: absolute;
		inset: 0;
		pointer-events: none;
	}

	.empty {
		position: sticky;
		inset-block-start: calc(var(--head-h) + 30%);
		display: grid;
		justify-items: center;
		gap: 12px;
		padding: 24px 16px;
		text-align: center;
	}

	.empty p {
		color: var(--text-muted);
		font: italic 400 1.375rem / 1.25 var(--font-display);
		letter-spacing: -0.01em;
	}

	.empty :global(*) {
		pointer-events: auto;
	}

	@media (prefers-reduced-motion: reduce) {
		.track.settling {
			transition-duration: 120ms;
		}
	}
</style>
