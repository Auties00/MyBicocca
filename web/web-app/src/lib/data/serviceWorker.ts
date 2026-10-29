import { dev } from '$app/env';
import { resolve } from '$app/paths';

/**
 * Registers the service worker (offline start, cached code) once the first page has settled: a
 * couple of seconds in, when the main thread is idle. Registered at `load`, as SvelteKit would, its
 * download of the app's files would compete with the first page's own code and data.
 */
export function registerServiceWorker(): void {
	if (dev || !('serviceWorker' in navigator)) return;
	const register = () =>
		void navigator.serviceWorker
			.register(`${resolve('')}service-worker.js`, { type: 'module' })
			.catch(() => undefined);
	setTimeout(() => {
		if ('requestIdleCallback' in window) requestIdleCallback(register, { timeout: 5000 });
		else register();
	}, 2000);
}
