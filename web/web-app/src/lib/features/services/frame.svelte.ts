import { createContext } from 'svelte';

/** The services column, beside every page wide enough to keep it. */
export const NAV_WIDTH = 288;

/**
 * The least every page keeps beside the column, its gutters included: about 720 px of content. One
 * width for every service, so the column never folds away under the page it just opened; a page
 * with a list and a detail of its own shows them one at a time beside it until both fit.
 */
export const PAGE_MIN = 800;

/** The services layout's columns, for the pages under it. */
export class ServicesFrame {
	/** The column and the page share the area; undefined until the first layout. */
	twoPanes = $state<boolean>();
}

const [getFrame, setFrame, hasFrame] = createContext<ServicesFrame>();

/** Call it while the services layout initializes. */
export function provideFrame(): ServicesFrame {
	return setFrame(new ServicesFrame());
}

/** The layout's frame, or undefined for a page shown outside it; call it while initializing. */
export function useFrame(): ServicesFrame | undefined {
	return hasFrame() ? getFrame() : undefined;
}
