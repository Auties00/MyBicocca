/**
 * Time-of-day helpers shared by the server-rendered page and the client-side clock.
 *
 * The page is laid out as one day, so every moment is expressed as minutes since
 * midnight. Branding the number keeps it from being mixed up with pixels.
 */
export type Minutes = number & { readonly __unit: 'minutes' };

/** A clock string such as `"09:30"`. */
export type ClockTime = `${number}${number}:${number}${number}`;

export const DAY_START = minutes(7 * 60);
export const DAY_END = minutes(24 * 60);

export function minutes(value: number): Minutes {
  return value as Minutes;
}

export function parseClock(time: ClockTime): Minutes {
  const [hours = 0, mins = 0] = time.split(':').map(Number);
  return minutes(hours * 60 + mins);
}

/** Formats minutes as `HH:MM`, wrapping midnight back to `00:00`. */
export function formatClock(value: Minutes): string {
  const total = Math.floor(value) % (24 * 60);
  const hours = Math.floor(total / 60);
  const mins = total % 60;
  return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
}

/** How far through the page's day a moment falls, from 0 (07:00) to 1 (midnight). */
export function dayProgress(value: Minutes): number {
  return Math.min(1, Math.max(0, (value - DAY_START) / (DAY_END - DAY_START)));
}

export interface TimeAnchor {
  /** Vertical position in the document, in CSS pixels. */
  readonly offset: number;
  readonly time: Minutes;
}

/**
 * Maps a scroll position onto the day by interpolating linearly between the two
 * anchors around it. Anchors must be sorted by both offset and time; positions
 * outside the range clamp to the first or last anchor.
 */
export function timeAt(anchors: readonly TimeAnchor[], offset: number): Minutes {
  const first = anchors[0];
  const last = anchors.at(-1);
  if (!first || !last) return DAY_START;
  if (offset <= first.offset) return first.time;
  if (offset >= last.offset) return last.time;

  let low = 0;
  let high = anchors.length - 1;
  while (high - low > 1) {
    const mid = (low + high) >> 1;
    if (anchors[mid]!.offset <= offset) low = mid;
    else high = mid;
  }

  const from = anchors[low]!;
  const to = anchors[high]!;
  const span = to.offset - from.offset;
  const ratio = span === 0 ? 0 : (offset - from.offset) / span;
  return minutes(from.time + (to.time - from.time) * ratio);
}
