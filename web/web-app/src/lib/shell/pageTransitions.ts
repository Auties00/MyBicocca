import { afterNavigate, onNavigate } from '$app/navigation';
import { startTypedTransition } from '#lib/ui/viewTransition.ts';

function firstSegment(pathname: string): string {
	return pathname.split('/').filter(Boolean)[0] ?? '';
}

/**
 * The kind of page change, for the CSS in base.css: deeper is `forward` (slide in from the end),
 * shallower or history-back is `back`, another destination is `swap` (rise and fade).
 */
function direction(
	from: string,
	to: string,
	delta: number | undefined
): 'forward' | 'back' | 'swap' {
	if (firstSegment(from) !== firstSegment(to)) return 'swap';
	if (delta !== undefined && delta < 0) return 'back';
	if (to.startsWith(`${from.replace(/\/$/, '')}/`)) return 'forward';
	if (from.startsWith(`${to.replace(/\/$/, '')}/`)) return 'back';
	return delta !== undefined && delta > 0 ? 'forward' : 'swap';
}

/**
 * Where focus lands after a navigation: the heading of the part of the layout that is the page (a
 * `[data-vt-page]` region, such as the open detail of a list-detail layout), else the page's h1.
 */
function landingHeading(main: HTMLElement): HTMLElement | null {
	const regions = main.querySelectorAll<HTMLElement>('[data-vt-page]');
	const region = regions[regions.length - 1];
	return region?.querySelector<HTMLElement>('h1, h2[tabindex]') ?? main.querySelector('h1');
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
 * Page changes run in a view transition (only the content pane is named, see AppShell). Query-only
 * changes (a calendar date, a filter) and shallow navigations don't animate. Call during component
 * init.
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
export function setupPageTransitions() {
	onNavigate((navigation) => {
		if (!document.startViewTransition || navigation.willUnload || navigation.shallow) return;
		const from = navigation.from?.url.pathname;
		const to = navigation.to?.url.pathname;
		if (!from || !to || from === to) return;
		const type = direction(from, to, navigation.type === 'popstate' ? navigation.delta : undefined);

		return new Promise<void>((resolve) => {
			const update = async () => {
				resolve();
				await navigation.complete;
			};
			startTypedTransition(update, type);
		});
	});

	afterNavigate((navigation) => {
		if (navigation.type === 'enter' || navigation.shallow) return;
		if (navigation.type === 'link' && keptByLink(navigation.event)) return;
		const from = navigation.from?.url;
		const to = navigation.to?.url;
		const queryOnly = from?.pathname === to?.pathname;
		if (queryOnly && from?.search === to?.search) return;
		// After the callbacks registered later (child pages) have had their turn.
		queueMicrotask(() => {
			if (document.activeElement !== document.body) return;
			const main = document.getElementById('main');
			if (!main || (queryOnly && !main.querySelector('[data-focus-on-query]'))) return;
			(landingHeading(main) ?? main).focus({ preventScroll: true });
		});
	});
}
