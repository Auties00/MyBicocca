import { redirect } from '@sveltejs/kit';
import { resolve } from '$app/paths';
import type { PageLoad } from './$types';

/** The bookings are listed on the library's home; without this, the folder would read as a site. */
export const load: PageLoad = () => {
	redirect(307, resolve('services/library'));
};
