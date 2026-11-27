import { createContext, untrack } from 'svelte';
import { page } from '$app/state';
import type { Attachment } from 'svelte/attachments';
import type { Href } from './types.ts';

interface HeaderContent {
	readonly title?: string;
	readonly back?: { readonly href: Href; readonly label: string };
	/** The top bar names the page from the start, not once the title scrolls under it (phones). */
	readonly inBar?: boolean;
}

interface HeaderState extends HeaderContent {
	readonly collapsed: boolean;
	/** The page this state was read from: until the next page's header reports, it is stale. */
	readonly path?: string;
}

/**
 * The top bar's height (`--topbar-h`, a constant), for a title watching when it passes under the
 * bar. Read when the module loads, before any page renders: read as a header mounts, it would force
 * a style recalculation of the whole page being built.
 */
const topbarHeight =
	parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--topbar-h')) || 56;

/**
 * What the current page tells the app chrome: its title (shown small in the top bar once the
 * large title scrolls away), where Back goes, and which bars step aside. `PageHeader` writes the
 * title and Back; `<Immersive />` and `<Page tabBar={false}>` hide bars while they are mounted.
 * The shell provides and reads it.
 */
export class PageChrome {
	#header = $state.raw<HeaderState>({ collapsed: false });
	// eslint-disable-next-line svelte/prefer-svelte-reactivity -- a registry of mounted headings, never rendered
	readonly #headings = new Map<HTMLElement, () => HeaderContent>();
	#scrollRestores = $state(0);

	get header(): Readonly<HeaderState> {
		return this.#header;
	}

	get restoringScroll(): boolean {
		return this.#scrollRestores > 0;
	}

	holdScrollRestore(): () => void {
		untrack(() => this.#scrollRestores++);
		return once(() => untrack(() => this.#scrollRestores--));
	}

	/** Read after scroll restoration so the first frame uses the destination's header state. */
	syncHeading = () => {
		for (const [heading, content] of [...this.#headings].reverse()) {
			if (!heading.isConnected) continue;
			const rect = heading.getBoundingClientRect();
			if (rect.height === 0) continue;
			const current = content();
			this.#setHeader(current, !!current.inBar || rect.bottom <= topbarHeight);
			return;
		}
		this.#setHeader({}, false);
	};

	#setHeader(content: HeaderContent, collapsed: boolean) {
		const header = this.#header;
		// The router's URL, as the top bar compares it: on popstate `location` moves first.
		const path = untrack(() => page.url.pathname);
		if (
			header.path !== path ||
			header.title !== content.title ||
			header.back?.href !== content.back?.href ||
			header.back?.label !== content.back?.label ||
			header.collapsed !== collapsed
		)
			this.#header = { ...content, collapsed, path };
	}

	watchHeading(content: () => HeaderContent): Attachment<HTMLElement> {
		return (heading) => {
			this.#headings.set(heading, content);
			const observer = new IntersectionObserver(this.syncHeading, {
				rootMargin: `-${topbarHeight}px 0px 0px 0px`
			});
			observer.observe(heading);
			$effect(() => {
				content();
				untrack(this.syncHeading);
			});
			return () => {
				observer.disconnect();
				this.#headings.delete(heading);
				queueMicrotask(this.syncHeading);
			};
		};
	}

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
