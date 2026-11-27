import { goto } from '$app/navigation';
import { page } from '$app/state';
import { reducedMotion } from '#lib/ui/media.ts';
import type { PageChrome } from '#lib/ui/pageChrome.svelte.ts';
import { internalHref } from '#lib/ui/types.ts';

/**
 * The destination already shown, chosen again, acts as Back: deep in it, it goes where the page's
 * Back goes; at its own page it returns to the top.
 */
export function reselectDestination(chrome: PageChrome): (event: MouseEvent) => void {
	return (event) => {
		if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
		event.preventDefault();
		const { header } = chrome;
		const back = header.path === page.url.pathname ? internalHref(header.back?.href) : undefined;
		// eslint-disable-next-line svelte/no-navigation-without-resolve -- a page's Back is resolved
		if (back) void goto(back);
		else window.scrollTo({ top: 0, behavior: reducedMotion.current ? 'auto' : 'smooth' });
	};
}
