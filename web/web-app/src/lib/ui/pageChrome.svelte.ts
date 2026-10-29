import { createContext, untrack } from 'svelte';
import type { Href } from './types.ts';

/**
 * The top bar's height (`--topbar-h`, a constant), for a title watching when it passes under the
 * bar. Read when the module loads, before any page renders: read as a header mounts, it would force
 * a style recalculation of the whole page being built.
 */
export const topbarHeight =
	parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--topbar-h')) || 56;

/**
 * What the current page tells the app chrome: its title (shown small in the top bar once the
 * large title scrolls away), where Back goes, and which bars step aside. `PageHeader` writes the
 * title and Back; `<Immersive />` and `<Page tabBar={false}>` hide bars while they are mounted.
 * The shell provides and reads it.
 */
export class PageChrome {
	title = $state<string>();
	back = $state<{ href: Href; label: string }>();
	/** The large title has scrolled under the top bar. */
	collapsed = $state(false);

	/* Counted, so a page arriving before the one it replaces has let go keeps its hold. */
	#immersive = $state(0);
	#tabBarHidden = $state(0);

	/** Full-screen viewers (video, files): every bar steps aside. */
	get immersive(): boolean {
		return this.#immersive > 0;
	}

	/** Focused flows (editors, multi-step forms): the phone tab bar steps aside, the top bar stays. */
	get tabBarHidden(): boolean {
		return this.#tabBarHidden > 0;
	}

	/** Hides every bar until the returned release is called. Untracked, so an effect can hold it. */
	holdImmersive(): () => void {
		untrack(() => this.#immersive++);
		return once(() => untrack(() => this.#immersive--));
	}

	/** Hides the phone tab bar until the returned release is called. Untracked, as above. */
	holdTabBarHidden(): () => void {
		untrack(() => this.#tabBarHidden++);
		return once(() => untrack(() => this.#tabBarHidden--));
	}
}

function once(release: () => void): () => void {
	let done = false;
	return () => {
		if (done) return;
		done = true;
		release();
	};
}

export const [getPageChrome, setPageChrome, hasPageChrome] = createContext<PageChrome>();
