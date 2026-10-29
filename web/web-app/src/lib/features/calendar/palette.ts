import { subjectBase } from './labels.ts';
import type { CalendarEvent, EventSource } from './model.ts';

/**
 * Event colours. Each swatch is a hue (and a chroma scale for the two greys), rendered with
 * Carta's tonal structure by the `calendar-swatch` class of `swatch.css`: the same lightness for
 * every hue, so no lesson shouts louder than another. The hues are those of Android's
 * `EventPalette`, and lessons pick from the pool with the same hash, so a course has the same
 * colour on the phone and here.
 */
export interface Swatch {
	hue: number;
	/** 1 for colours, near 0 for the neutral swatches. */
	chroma: number;
}

const swatch = (hue: number, chroma = 1): Swatch => ({ hue, chroma });

/**
 * Orange, amber, yellow, lime, green, teal, cyan, sky, blue, indigo, violet, purple, fuchsia,
 * pink, stone, zinc: Android's pool order, which the hash indexes. Red stays out, for exams.
 */
export const lessonPool: readonly Swatch[] = [
	swatch(45),
	swatch(68),
	swatch(92),
	swatch(128),
	swatch(150),
	swatch(182),
	swatch(212),
	swatch(238),
	swatch(262),
	swatch(278),
	swatch(294),
	swatch(306),
	swatch(324),
	swatch(355),
	swatch(70, 0.12),
	swatch(285, 0.14)
];

export const sourceSwatches: Readonly<Record<Exclude<EventSource, 'lesson'>, Swatch>> = {
	exam: swatch(25),
	deadline: swatch(55),
	appointment: swatch(276),
	library: swatch(186)
};

/** Emerald: whatever is happening right now. */
export const inProgressSwatch: Swatch = swatch(160);

/** Java's `String.hashCode`, over UTF-16 code units, as a signed 32-bit integer. */
export function javaHashCode(text: string): number {
	let hash = 0;
	for (let index = 0; index < text.length; index++) {
		hash = (Math.imul(31, hash) + text.charCodeAt(index)) | 0;
	}
	return hash;
}

/** The pool index of a lesson: its trimmed, lowercased title (its id when blank), hashed. */
export function lessonSwatchIndex(title: string, id: string): number {
	const key = title.trim().toLowerCase() || id;
	return (javaHashCode(key) & 0x7fffffff) % lessonPool.length;
}

/**
 * The swatch of `event`; `inProgress` overrides its own colour. A lesson hashes its course name
 * without EasyStaff's turno or group suffix, so the lectures and the lab of a course match.
 */
export function eventSwatch(event: CalendarEvent, inProgress = false): Swatch {
	if (inProgress) return inProgressSwatch;
	if (event.source === 'lesson') {
		return lessonPool[lessonSwatchIndex(subjectBase(event.title), event.id)];
	}
	return sourceSwatches[event.source];
}
