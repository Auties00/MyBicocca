import { error } from '@sveltejs/kit';
import { dev } from '$app/env';

/** The component gallery exists for reviewers in development only. */
export function load(): void {
	if (!dev) error(404, 'Not found');
}
