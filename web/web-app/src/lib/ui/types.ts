import type { ResolvedPathname } from '$app/types';
import type { Component } from 'svelte';
import type { SvelteHTMLElements } from 'svelte/elements';

/** A Phosphor icon component: `import IconHouse from '~icons/ph/house'`. */
export type Icon = Component<SvelteHTMLElements['svg']>;

/** Semantic colour of a status, badge, notice or icon chip. */
export type Tone = 'neutral' | 'accent' | 'info' | 'success' | 'warning' | 'danger' | 'gold';

/** Icon chip fills: the tones, plus category hues that carry no status (violet for documents). */
export type ChipTone = Tone | 'violet';

export type Size = 'sm' | 'md' | 'lg';

/** What a `StatusChip` reports; each status has its own glyph, so colour is never the only cue. */
export type Status = Tone | 'pending';

/** Course colour slot (see `courseSlot`): equal-contrast hues 1–8. */
export type CourseSlot = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8;

export type ExternalHref = `${'https' | 'http' | 'mailto' | 'tel'}:${string}`;

/** In-app links must come from `resolve()`; anything else is an absolute external URL. */
export type Href = ResolvedPathname | ExternalHref;

export function isExternal(href: Href): href is ExternalHref {
	return /^[a-z][a-z+.-]*:/i.test(href);
}

export function internalHref(href: Href | undefined): ResolvedPathname | undefined {
	return href !== undefined && !isExternal(href) ? href : undefined;
}

export function externalHref(href: Href | undefined): ExternalHref | undefined {
	return href !== undefined && isExternal(href) ? href : undefined;
}

export interface SegmentedOption<V extends string = string> {
	value: V;
	label: string;
	/** Small line above the label (e.g. "Giorno" over "Gio 18"). */
	caption?: string;
	icon?: Icon;
	disabled?: boolean;
}
