import { onNavigate } from '$app/navigation';
import { resolve } from '$app/paths';
import { startTypedTransition } from '#lib/ui/viewTransition.ts';

/**
 * Leaving the sign-in pages for the app cross-fades the whole page, as Android swaps its root
 * phases, and the wordmark flies into the shell's (the shell names its own under `auth-enter`);
 * steps between the sign-in pages fade their content only. Call during the sign-in layout's init.
 */
export function setupAuthTransitions(): void {
	onNavigate((navigation) => {
		if (!document.startViewTransition || navigation.willUnload) return;
		const from = navigation.from?.url.pathname;
		const to = navigation.to?.url.pathname;
		if (!from || !to || from === to) return;
		const entering = !to.startsWith(resolve('login'));

		return new Promise<void>((done) => {
			startTypedTransition(
				async () => {
					done();
					await navigation.complete;
				},
				entering ? 'auth-enter' : 'auth-step'
			);
		});
	});
}
