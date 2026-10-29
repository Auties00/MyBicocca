import { redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import { ALWAYS_TWO_PANES } from '#lib/features/settings/layout.ts';

/** Beside the list there is always a section open, so wide screens start on the first one. */
export function load(): void {
	if (matchMedia(ALWAYS_TWO_PANES).matches) redirect(307, resolve('settings/appearance'));
}
