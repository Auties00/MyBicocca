import { onDestroy } from 'svelte';
import { afterNavigate, beforeNavigate, type AfterNavigate } from '$app/navigation';
import { compact, reducedMotion } from '#lib/ui/media.ts';
import { activeDestination } from './nav.ts';

function contentPane(main: HTMLElement): HTMLElement {
	return (
		Array.from(main.querySelectorAll<HTMLElement>('[data-vt-page]'))
			.reverse()
			.find((region) => region.getClientRects().length > 0) ?? main
	);
}

/**
 * Where focus lands after a navigation: the heading of the part of the layout that is the page (a
 * `[data-vt-page]` region, such as the open detail of a list-detail layout), else the page's h1.
 */
function landingHeading(main: HTMLElement): HTMLElement | null {
	return (
		contentPane(main).querySelector<HTMLElement>('h1, h2[tabindex]') ?? main.querySelector('h1')
	);
}

/** A link click that asked SvelteKit to keep focus and scroll (`data-sveltekit-reset="false"`). */
function keptByLink(event: Event | undefined): boolean {
	const target = event?.target;
	return (
		target instanceof Element &&
		target.closest('[data-sveltekit-reset]')?.getAttribute('data-sveltekit-reset') === 'false'
	);
}

/**
 * Fade the incoming content pane in place. Anchors, restored scroll and query-only changes settle
 * immediately. Call during component init.
 *
 * Focus follows one rule, so screen readers start on the new page: once a navigation has settled
 * and every `afterNavigate` callback has run, if focus rests on `<body>` (nothing kept or claimed
 * it), it moves to the landing heading (see `landingHeading`). So it stays where it is when:
 * - the navigation kept it: `goto(url, { reset: false })` or a link inside
 *   `data-sveltekit-reset="false"` (also when the browser doesn't focus links on click);
 * - an `autofocus` element took it, or the page moved it itself, during the navigation or
 *   synchronously in its own `afterNavigate`;
 * - only the search params changed, unless the page opts in with `<Page focusOnQuery>` (a detail
 *   opened with `?open=…`), or only the hash changed;
 * - it is the first load, or a shallow navigation.
 */
export function setupPageTransitions(
	savedScroll: (navigation: AfterNavigate) => number,
	syncHeader: () => void
) {
	let stopWaitingForAnchor: (() => void) | undefined;
	let entrance: Animation | undefined;
	beforeNavigate(() => stopWaitingForAnchor?.());
	onDestroy(() => {
		stopWaitingForAnchor?.();
		entrance?.cancel();
	});

	afterNavigate((navigation) => {
		entrance?.cancel();
		const hash = navigation.to?.url.hash;
		if (hash && !navigation.shallow && navigation.type !== 'popstate') {
			let id: string;
			try {
				id = decodeURIComponent(hash.slice(1));
			} catch {
				return;
			}
			const anchor = document.getElementById(id);
			if (anchor) {
				if (navigation.type === 'enter') anchor.scrollIntoView({ behavior: 'instant' });
			} else {
				// On a cold visit the fragment can be rendered by data that arrives after Kit's
				// initial scroll. Stop waiting if the user takes over or leaves the page.
				const main = document.getElementById('main');
				if (!main) return;
				const input = new AbortController();
				const observer = new MutationObserver(() => {
					const target = document.getElementById(id);
					if (!target) return;
					stop();
					target.scrollIntoView({ behavior: 'instant' });
					syncHeader();
				});
				const stop = () => {
					clearTimeout(timeout);
					input.abort();
					observer.disconnect();
					stopWaitingForAnchor = undefined;
				};
				const timeout = setTimeout(stop, 8000);
				stopWaitingForAnchor = stop;
				observer.observe(main, { childList: true, subtree: true });
				for (const event of ['pointerdown', 'wheel', 'keydown'])
					window.addEventListener(event, stop, {
						capture: true,
						passive: true,
						signal: input.signal
					});
			}
			return;
		}
		if (navigation.type === 'enter' || navigation.shallow) return;
		const from = navigation.from?.url;
		const to = navigation.to?.url;
		const queryOnly = from?.pathname === to?.pathname;
		if (queryOnly && from?.search === to?.search) return;
		const fromTab = from && activeDestination(from.pathname);
		const toTab = to && activeDestination(to.pathname);
		// Keep full-height mobile surfaces steady as their scroll position and chrome settle.
		const fixedViewportSwitch =
			compact.current &&
			fromTab !== toTab &&
			(fromTab === 'map' || toTab === 'map' || toTab === 'calendar');
		// After the callbacks registered later (child pages) have had their turn.
		queueMicrotask(() => {
			const main = document.getElementById('main');
			if (
				main &&
				!queryOnly &&
				!fixedViewportSwitch &&
				!hash &&
				!reducedMotion.current &&
				!window.scrollY &&
				!navigation.to?.scroll?.y &&
				!savedScroll(navigation)
			) {
				const pane = contentPane(main);
				const style = getComputedStyle(pane);
				const duration = style.getPropertyValue('--view-enter-duration').trim();
				entrance = pane.animate(
					{ opacity: [style.getPropertyValue('--view-enter-opacity'), '1'] },
					{
						duration: parseFloat(duration) * (duration.endsWith('ms') ? 1 : 1000),
						easing: style.getPropertyValue('--ease-out').trim()
					}
				);
			}
			if (
				document.activeElement !== document.body ||
				(navigation.type === 'link' && keptByLink(navigation.event))
			)
				return;
			if (!main || (queryOnly && !main.querySelector('[data-focus-on-query]'))) return;
			(landingHeading(main) ?? main).focus({ preventScroll: true });
		});
	});
}
