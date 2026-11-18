import { createSubscriber } from 'svelte/reactivity';
import { clockTime, displayTitle, timeRange } from './format.ts';
import type { CardText } from './layout.ts';
import { compactLabel, type CalendarEvent } from './model.ts';

/**
 * Text widths at the event cards' fonts, measured on a canvas so a card knows what it can print
 * before it renders. They are measured again once the web fonts arrive.
 */
let context: CanvasRenderingContext2D | null | undefined;
let family = '';
let remPx = 16;
const widths = new Map<string, number>();

if (typeof document !== 'undefined') {
	document.fonts.addEventListener('loadingdone', () => widths.clear());
}

/* Whoever measured in an effect or a derived measures again once a font arrives. */
const fontsLoaded = createSubscriber((update) => {
	document.fonts.addEventListener('loadingdone', update);
	return () => document.fonts.removeEventListener('loadingdone', update);
});

/** Card fonts as weight, size in rem and tracking in em; they match EventCard's styles. */
export const cardFonts = {
	title: [600, 0.875, -0.005],
	time: [600, 0.6875, 0.03],
	label: [600, 0.625, 0.02],
	small: [500, 0.5625, 0]
} as const satisfies Record<string, readonly [number, number, number]>;

type CardFont = (typeof cardFonts)[keyof typeof cardFonts];

function canvas(): CanvasRenderingContext2D | null {
	if (context !== undefined) return context;
	context = document.createElement('canvas').getContext('2d');
	const root = getComputedStyle(document.documentElement);
	family = root.getPropertyValue('--font-ui').trim() || 'sans-serif';
	remPx = parseFloat(root.fontSize) || 16;
	return context;
}

/** The width of `text` in CSS px, rounded up; an estimate where there is no canvas. */
export function textWidth(text: string, [weight, size, tracking]: CardFont): number {
	if (typeof document !== 'undefined') fontsLoaded();
	const key = `${weight}/${size}/${text}`;
	const known = widths.get(key);
	if (known !== undefined) return known;
	const ctx = typeof document === 'undefined' ? null : canvas();
	const px = size * remPx;
	let width: number;
	if (ctx) {
		ctx.font = `${weight} ${px}px ${family}`;
		width = ctx.measureText(text).width + text.length * tracking * px;
	} else {
		width = text.length * px * 0.55;
	}
	width = Math.ceil(width);
	widths.set(key, width);
	return width;
}

/** What a card would print, measured for {@link cardDensity}. */
export function cardText(event: CalendarEvent): CardText {
	const words = displayTitle(event).split(/\s+/);
	return {
		word: Math.max(0, ...words.map((word) => textWidth(word, cardFonts.title))),
		range: textWidth(timeRange(event), cardFonts.time),
		label: textWidth(compactLabel(event), cardFonts.label),
		start: textWidth(clockTime(event.start), cardFonts.small)
	};
}
