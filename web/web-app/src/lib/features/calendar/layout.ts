/**
 * The timeline engine shared by the day and week views, ported from Android's `EventLayout.kt`
 * so both apps place events identically.
 */

/** The default window, 08:00–22:00, in minutes from midnight. */
const WINDOW_START = 8 * 60;
const WINDOW_END = 22 * 60;
const DAY_END = 24 * 60;

/** Anything shorter is padded to this for placement (a 23:59 deadline stays readable). */
const MIN_BLOCK = 24;

export const ZOOM_MIN = 0.55;
export const ZOOM_MAX = 1.5;

export interface TimeSpan {
	start: number;
	end: number;
}

/**
 * The minutes an event occupies on the grid: at least {@link MIN_BLOCK}, never past midnight. A
 * block that would spill past midnight grows backwards instead (23:59 becomes 23:36–24:00).
 */
export function paddedRange(span: TimeSpan): TimeSpan {
	const end = Math.min(DAY_END, Math.max(span.end, span.start + MIN_BLOCK));
	return { start: Math.max(0, Math.min(span.start, end - MIN_BLOCK)), end };
}

/**
 * The vertical window every day shares, so they all agree on one scale: 08:00–22:00 grown, in
 * whole hours, to cover every event given.
 */
export function timelineWindow(events: Iterable<TimeSpan>): TimeSpan {
	let start = WINDOW_START;
	let end = WINDOW_END;
	for (const event of events) {
		const range = paddedRange(event);
		if (range.start < start) start = Math.floor(range.start / 60) * 60;
		if (range.end > end) end = Math.min(DAY_END, Math.ceil(range.end / 60) * 60);
	}
	return { start, end };
}

export interface Placement<T> {
	item: T;
	/** Padded minutes. */
	start: number;
	end: number;
	lane: number;
	/** Left edge and width as fractions of the day column. */
	x: number;
	width: number;
}

/**
 * Places one day's events in lanes: each, in start order, takes the first lane free at its
 * start; every event of a cluster of overlapping events then gets an equal share of the column,
 * so a pair splits it in halves while a lone event spans it whole.
 */
export function layoutDay<T extends TimeSpan>(events: readonly T[]): Placement<T>[] {
	const items = events
		.map((item) => ({ item, ...paddedRange(item), lane: 0 }))
		.sort((a, b) => a.start - b.start || a.end - b.end);

	const laneEnd: number[] = [];
	let maxLane = -1;
	for (const placed of items) {
		let lane = 0;
		while (lane <= maxLane && laneEnd[lane] > placed.start) lane++;
		laneEnd[lane] = placed.end;
		placed.lane = lane;
		maxLane = Math.max(maxLane, lane);
	}

	const parent = items.map((_, index) => index);
	const find = (index: number): number => {
		while (parent[index] !== index) {
			parent[index] = parent[parent[index]];
			index = parent[index];
		}
		return index;
	};
	for (let i = 0; i < items.length; i++) {
		for (let j = i + 1; j < items.length; j++) {
			if (items[j].start >= items[i].end) break;
			const a = find(i);
			const b = find(j);
			if (a !== b) parent[a] = b;
		}
	}

	const clusterLanes = new Map<number, number>();
	items.forEach((placed, index) => {
		const root = find(index);
		clusterLanes.set(root, Math.max(clusterLanes.get(root) ?? 0, placed.lane));
	});

	return items.map((placed, index) => {
		const width = 1 / ((clusterLanes.get(find(index)) ?? placed.lane) + 1);
		return { ...placed, x: placed.lane * width, width };
	});
}

/** Odd hour lines and labels fade out as the grid shrinks: hidden below ~36 px per hour. */
export function oddHourAlpha(minutePx: number): number {
	return Math.min(1, Math.max(0, (minutePx - 0.6) / (0.85 - 0.6)));
}

export type Density = 'dot' | 'mini' | 'label' | 'icon' | 'compact' | 'tight' | 'full';

/** The widths, in CSS px at the card's fonts, of what a card can print. */
export interface CardText {
	/** The title's longest word, at the full card's title font. */
	word: number;
	/** The time range, at the full card's time font. */
	range: number;
	/** The short label ("ASD"), at the icon card's font. */
	label: number;
	/** The start time, at the icon card's font. */
	start: number;
}

/** Horizontal padding of a full card and of an icon card, accent stripe included. */
export const FULL_PADDING = 23;
export const ICON_PADDING = 9;

/** The smaller type of compact and tight cards against the full card's: 12 and 10 px for 14 and 11. */
const TITLE_SMALL = 12 / 14;
const TIME_SMALL = 10 / 11;

/**
 * How much a card of this size can say, in CSS pixels. Android's height thresholds; across, a
 * card prints its title only where the title's longest word and the time range fit whole (in
 * smaller type if need be: `tight`), else the short label over the start time, the label alone,
 * or a dot when not even the label fits.
 */
export function cardDensity(width: number, height: number, text: CardText): Density {
	if (height < 14) return 'dot';
	if (height < 28) return 'mini';
	const room = width - FULL_PADDING;
	const fits = (scale: number, timeScale: number) =>
		width >= 50 && text.word * scale <= room && text.range * timeScale <= room;
	if (fits(TITLE_SMALL, TIME_SMALL)) {
		if (height < 40) return 'compact';
		return fits(1, 1) ? 'full' : 'tight';
	}
	const narrow = width - ICON_PADDING;
	if (text.label > narrow) return 'dot';
	return text.start > narrow ? 'label' : 'icon';
}

export interface Opening {
	window: TimeSpan;
	/** The events of the days on screen. */
	events: readonly TimeSpan[];
	/** When today is on screen: the minute now and today's events. */
	today: { now: number; events: readonly TimeSpan[] } | null;
	/** How many minutes the grid shows at once. */
	visible: number;
}

/**
 * The minute the grid opens at. While today has something to come: an hour before now, down to
 * the half hour, or the start of what is under way if earlier, as long as now stays on screen. Otherwise half an hour
 * before the first event, moved up if needed so the screen doesn't end in empty hours the last
 * event (a point in time aside, such as a 23:59 deadline) doesn't reach. A top edge that still
 * cuts a card lands on a whole hour, in line with the grid.
 */
export function openingMinute({ window, events, today, visible }: Opening): number {
	const lowest = Math.max(window.start, window.end - visible);
	const clamp = (minute: number) => Math.min(lowest, Math.max(window.start, minute));
	const ranges = events.map(paddedRange);
	const now = today?.now ?? null;
	const ahead =
		today !== null &&
		(ranges.length === 0 || today.events.some((event) => paddedRange(event).end > today.now));
	let target: number;
	if (today !== null && ahead) {
		const running = today.events
			.map(paddedRange)
			.filter((range) => range.start <= today.now && today.now < range.end)
			.map((range) => range.start - 10);
		const hourBefore = Math.floor((today.now - 60) / 30) * 30;
		target = clamp(Math.max(Math.min(hourBefore, ...running), today.now + 30 - visible));
	} else if (ranges.length === 0) {
		return window.start;
	} else {
		const first = Math.min(...ranges.map((range) => range.start));
		const timed = events.filter((event) => event.end > event.start).map(paddedRange);
		const last = Math.max(...(timed.length > 0 ? timed : ranges).map((range) => range.end));
		target = clamp(Math.min(first - 30, last + 30 - visible));
	}
	const cuts = ranges.some((range) => range.start < target && range.end > target + 15);
	if (!cuts || target % 60 === 0) return target;
	const hour = Math.floor(target / 60) * 60;
	const keepsNow = ahead && now !== null ? hour + visible >= now + 30 : true;
	return clamp(keepsNow ? hour : hour + 60);
}

export function clampZoom(zoom: number): number {
	return Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, zoom));
}

/**
 * The scroll offset that keeps the content under a pinch's focal point still as the zoom
 * changes by `factor`.
 */
export function pinchScroll(scroll: number, focalY: number, factor: number, max: number): number {
	return Math.min(max, Math.max(0, (scroll + focalY) * factor - focalY));
}
