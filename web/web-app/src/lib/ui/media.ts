import { MediaQuery } from 'svelte/reactivity';

/* The size classes of ia.md, read from the window: one query each, shared by every reader. */
export const compact = new MediaQuery('(width < 600px)', false);
export const expanded = new MediaQuery('(width >= 1024px)', false);
const large = new MediaQuery('(width >= 1440px)', false);

export const reducedMotion = new MediaQuery('(prefers-reduced-motion: reduce)', false);

export type SizeClass = 'compact' | 'medium' | 'expanded' | 'large';

/** The window's size class, for layouts that change shape, not only density. */
export const sizeClass = {
	get current(): SizeClass {
		if (compact.current) return 'compact';
		if (!expanded.current) return 'medium';
		return large.current ? 'large' : 'expanded';
	}
};
