import { redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import { session } from '#lib/data/session.svelte.ts';

/** Signed-out visitors go to the login page; an account still owing a career pick, to the picker. */
export async function load(): Promise<void> {
	await session.load();
	if (session.active === undefined) redirect(307, resolve('login'));
	if (session.needsCareerPick) redirect(307, resolve('login/career'));
}
