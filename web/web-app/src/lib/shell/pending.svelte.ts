import { navigating, page } from '$app/state';

/**
 * The path of a page change still loading (its code, its data), so the shell can answer a click
 * before the page arrives. Changes that keep the path (a calendar date, a filter) don't count.
 */
export function pendingPath(): string | undefined {
	const to = navigating.to?.url.pathname;
	return to !== undefined && to !== page.url.pathname ? to : undefined;
}

/** Where the person is going, or where they are: what the navigation shows as current. */
export function shownPath(): string {
	return pendingPath() ?? page.url.pathname;
}
