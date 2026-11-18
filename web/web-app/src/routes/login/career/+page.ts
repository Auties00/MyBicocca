import { redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import { session } from '#lib/data/session.svelte.ts';
import type { PageLoad } from './$types';

/** Only a signed-in account has careers to pick from. */
export const load: PageLoad = async ({ parent }) => {
	await parent();
	if (session.active === undefined) redirect(307, resolve('login'));
};
