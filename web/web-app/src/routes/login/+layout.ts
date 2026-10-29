import { session } from '#lib/data/session.svelte.ts';

/** Who is signed in decides how the sign-in pages look and where they lead. */
export async function load(): Promise<void> {
	await session.load();
}
