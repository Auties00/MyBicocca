import { afterNavigate, onNavigate, type AfterNavigate } from '$app/navigation';
import { page } from '$app/state';
import { createContext, flushSync, onDestroy, onMount } from 'svelte';
import { SvelteMap } from 'svelte/reactivity';
import type { DestinationId } from '#lib/ui/destinations.ts';
import type { PageChrome } from '#lib/ui/pageChrome.svelte.ts';
import { activeDestination, shellHref } from './nav.ts';

interface PageState {
	scroll: number;
	regions: Map<string, number>;
	values: Map<string, unknown>;
}

interface LocalState {
	capture(): unknown;
	restore(value: unknown): void;
}

const [getTabMemory, setTabMemory, hasTabMemory] = createContext<TabMemory>();
const hrefOf = (url: Pick<URL, 'pathname' | 'search' | 'hash'>) =>
	url.pathname + url.search + url.hash;
const pageKey = (url: URL) => url.pathname + url.search;

function regions() {
	return document.querySelectorAll<HTMLElement>('[data-tab-scroll]');
}

function isDestinationLink(event: Event | undefined): boolean {
	return event?.target instanceof Element && !!event.target.closest('[data-shell-destination]');
}

function isCourseTabLink(event: Event | undefined): boolean {
	return event?.target instanceof Element && !!event.target.closest('[data-course-subtabs]');
}

/** Pages whose scroll is remembered, beyond which the least recently left is forgotten. */
const MAX_PAGES = 60;

export function keepCurrentDestination(
	event: MouseEvent & { currentTarget: HTMLAnchorElement }
): void {
	if (
		event.currentTarget.href === location.href &&
		!event.metaKey &&
		!event.ctrlKey &&
		!event.shiftKey &&
		!event.altKey
	)
		event.preventDefault();
}

export function isStuck(element: HTMLElement): boolean {
	return element.getBoundingClientRect().top <= parseFloat(getComputedStyle(element).top) + 1;
}

/** Lives for one signed-in shell, including when a full-screen page hides the navigation. */
export class TabMemory {
	readonly #chrome: PageChrome;
	readonly #tabs = new SvelteMap<DestinationId, string>();
	// eslint-disable-next-line svelte/prefer-svelte-reactivity -- read only while navigating: nothing renders from it
	readonly #pages = new Map<string, PageState>();
	// eslint-disable-next-line svelte/prefer-svelte-reactivity -- a registry read only while navigating
	readonly #local = new Map<string, LocalState>();
	#stopRestore?: () => void;
	#restoringRegions?: ReadonlyMap<string, number>;

	constructor(chrome: PageChrome) {
		this.#chrome = chrome;
		onNavigate(({ from, to }) => {
			this.#stopRestore?.();
			if (!from || !to || pageKey(from.url) === pageKey(to.url)) return;
			const id = activeDestination(from.url.pathname);
			if (!id) return;
			if (id !== activeDestination(to.url.pathname)) this.#tabs.set(id, hrefOf(from.url));
			const key = pageKey(from.url);
			// Most recent last, so the oldest page is the first to go once the memory is full.
			this.#pages.delete(key);
			if (this.#pages.size >= MAX_PAGES) this.#pages.delete(this.#pages.keys().next().value!);
			this.#pages.set(key, {
				scroll: window.scrollY,
				// eslint-disable-next-line svelte/prefer-svelte-reactivity -- a snapshot, never rendered
				regions: new Map([...regions()].map((node) => [node.dataset.tabScroll!, node.scrollTop])),
				// eslint-disable-next-line svelte/prefer-svelte-reactivity -- a snapshot, never rendered
				values: new Map([...this.#local].map(([key, state]) => [key, state.capture()]))
			});
		});

		afterNavigate((navigation) => {
			const saved = this.#savedFor(navigation);
			if (saved) this.#restore(saved, () => this.#pinnedCourseTabs(navigation));
		});
		onDestroy(() => this.#stopRestore?.());
	}

	href(id: DestinationId): string {
		if (activeDestination(page.url.pathname) === id) return hrefOf(page.url);
		return this.#tabs.get(id) ?? shellHref(id);
	}

	scrollFor(navigation: AfterNavigate): number {
		return this.#savedFor(navigation)?.scroll ?? 0;
	}

	scrollForRegion(key: string): number | undefined {
		return this.#restoringRegions?.get(key);
	}

	#savedFor(navigation: AfterNavigate) {
		const { from, to } = navigation;
		if (navigation.shallow || navigation.type === 'enter' || navigation.type === 'popstate')
			return undefined;
		if (!from || !to || from.url.pathname === to.url.pathname) return undefined;
		const shellLink = navigation.type === 'link' && isDestinationLink(navigation.event);
		/*
		 * A page is found where it was left only when returning to it: through the navigation's
		 * tabs, or going up to a parent. Opened again from a list, a page starts at its top.
		 */
		const tabLink = shellLink || (navigation.type === 'link' && isCourseTabLink(navigation.event));
		const up = from.url.pathname.startsWith(`${to.url.pathname.replace(/\/$/, '')}/`);
		if (!tabLink && !up) return undefined;
		// An explicit anchor still goes to that element. A shell link can contain the last URL's hash.
		if (to.url.hash && !shellLink) return undefined;
		return this.#pages.get(pageKey(to.url));
	}

	#pinnedCourseTabs(navigation: AfterNavigate): number {
		const target = navigation.type === 'link' && navigation.event?.target;
		const strip = target instanceof Element && target.closest<HTMLElement>('[data-course-subtabs]');
		return strip && isStuck(strip) ? window.scrollY : 0;
	}

	clear() {
		this.#stopRestore?.();
		this.#tabs.clear();
		this.#pages.clear();
	}

	register(key: string, state: LocalState) {
		this.#local.set(key, state);
		return () => {
			if (this.#local.get(key) === state) this.#local.delete(key);
		};
	}

	#restore(saved: PageState, scrollFloor: () => number) {
		this.#restoringRegions = saved.regions;
		const release = saved.scroll > 0 ? this.#chrome.holdScrollRestore() : undefined;
		// eslint-disable-next-line svelte/prefer-svelte-reactivity -- consumed while restoring, never rendered
		const pendingValues = new Map(saved.values);
		const input = new AbortController();
		const deadline = performance.now() + 2000;
		let timer: ReturnType<typeof setTimeout> | undefined;
		let stable = 0;
		const stop = () => {
			clearTimeout(timer);
			input.abort();
			this.#chrome.syncHeading();
			release?.();
			this.#restoringRegions = undefined;
			this.#stopRestore = undefined;
		};
		this.#stopRestore = stop;
		for (const event of ['pointerdown', 'wheel', 'keydown']) {
			window.addEventListener(event, stop, { capture: true, passive: true, signal: input.signal });
		}
		const restore = (top: number) => {
			if (input.signal.aborted) return;
			// Restore expanded content before trying its offset. Slow-loading panels may need a few
			// frames to mount, or to become tall enough for the browser to accept the position.
			for (const [key, value] of pendingValues) {
				const state = this.#local.get(key);
				if (!state) continue;
				flushSync(() => state.restore(value));
				pendingValues.delete(key);
			}
			let matchedRegions = 0;
			let adjusted = false;
			for (const node of regions()) {
				const key = node.dataset.tabScroll!;
				const top = saved.regions.get(key);
				if (top === undefined) continue;
				if (Math.abs(node.scrollTop - top) >= 1) {
					node.scrollTop = top;
					adjusted = true;
				}
				if (Math.abs(node.scrollTop - top) < 1) matchedRegions++;
			}
			if (Math.abs(window.scrollY - top) >= 1) {
				window.scrollTo({ top, behavior: 'instant' });
				adjusted = true;
			}
			this.#chrome.syncHeading();
			// A responsive pane can change height after the first restore and move the browser's
			// scroll anchor. Wait for a few matching samples before leaving it alone.
			const matched =
				Math.abs(window.scrollY - top) < 1 &&
				matchedRegions === saved.regions.size &&
				pendingValues.size === 0;
			stable = matched && !adjusted ? stable + 1 : 0;
			if (stable >= 3 || performance.now() >= deadline) stop();
			else timer = setTimeout(() => restore(top), 50);
		};
		queueMicrotask(() => restore(Math.max(saved.scroll, scrollFloor())));
	}
}

export function setupTabMemory(chrome: PageChrome): TabMemory {
	return setTabMemory(new TabMemory(chrome));
}

/** Read when a deferred scroller mounts, before its default position can paint. */
export function restoreTabRegion(key: string): () => number | undefined {
	const tabs = hasTabMemory() ? getTabMemory() : undefined;
	return () => tabs?.scrollForRegion(key);
}

/** Register layout state that must be ready when a tab restores its scroll position. */
export function rememberTabState<T>(
	key: string,
	capture: () => T,
	restore: (value: T) => void
): void {
	if (!hasTabMemory()) return;
	const tabs = getTabMemory();
	onMount(() => tabs.register(key, { capture, restore: (value) => restore(value as T) }));
}
