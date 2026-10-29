import { updated } from '$app/state';

export type UpdateState =
	| { state: 'idle' }
	| { state: 'checking' }
	| { state: 'current'; at: number }
	| { state: 'offline' };

/**
 * "Check for updates" for a web app: whether a newer build has been deployed than the one
 * running, which a reload then loads. SvelteKit also notices on its own (`updated.current`).
 */
class UpdateCheck {
	status = $state<UpdateState>({ state: 'idle' });

	get available(): boolean {
		return updated.current;
	}

	async run(): Promise<void> {
		if (this.status.state === 'checking') return;
		if (!navigator.onLine) {
			this.status = { state: 'offline' };
			return;
		}
		this.status = { state: 'checking' };
		const [found] = await Promise.all([
			updated.check(),
			// Long enough to read as a check rather than a flicker.
			new Promise((resolve) => setTimeout(resolve, 600))
		]);
		this.status = found ? { state: 'idle' } : { state: 'current', at: Date.now() };
	}
}

export const updateCheck = new UpdateCheck();
