import { online } from 'svelte/reactivity/window';

/**
 * Whether the browser says it is online, as reactive state: writes (bookings, submissions, saves)
 * wait for it, and reading it in a template or a derived follows the online and offline events.
 */
export const network = {
	get online(): boolean {
		return online.current !== false;
	}
};
