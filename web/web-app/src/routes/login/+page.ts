import { error, redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import { session } from '#lib/data/session.svelte.ts';
import type { PageLoad } from './$types';

/**
 * An account still owing its career pick finishes that first. The demo build has no sign-in: the
 * session opens the demo account itself.
 */
export const load: PageLoad = async ({ parent }) => {
	await parent();
	if (session.needsCareerPick) redirect(307, resolve('login/career'));
	if (__DEMO__) {
		if (session.active !== undefined) redirect(307, resolve('calendar'));
		error(503, 'The demo could not be opened');
	}
};
