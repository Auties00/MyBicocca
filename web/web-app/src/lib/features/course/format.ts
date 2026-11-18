import { cached, intlLocale } from '#lib/format/locale.ts';

function parts(seconds: number) {
	const total = Math.max(0, Math.floor(seconds));
	return {
		hours: Math.floor(total / 3600),
		minutes: Math.floor((total % 3600) / 60),
		seconds: total % 60
	};
}

function durationFormat(style: 'digital' | 'long') {
	const tag = intlLocale();
	const options = { style, hoursDisplay: 'auto' } as const;
	return cached('duration', tag, options, () => new Intl.DurationFormat(tag, options));
}

/** A position in a recording as a player shows it: 38:00, 1:35:00. */
export function formatClock(seconds: number): string {
	return durationFormat('digital').format(parts(seconds));
}

/** The same position in words, for screen readers: "38 minuti", "1 ora e 35 minuti". */
export function formatClockLong(seconds: number): string {
	const { hours, minutes, seconds: rest } = parts(seconds);
	return durationFormat('long').format(
		hours === 0 && minutes === 0 ? { seconds: rest } : { hours, minutes }
	);
}
