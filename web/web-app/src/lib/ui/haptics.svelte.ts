const STORAGE_KEY = 'mybicocca:haptics';

/*
 * The Vibration API exists on desktop Chrome too, where it does nothing, so a touch screen is
 * required as well; Safari has no such API.
 */
const supported =
	typeof navigator !== 'undefined' &&
	typeof navigator.vibrate === 'function' &&
	matchMedia('(pointer: coarse)').matches;

function readEnabled(): boolean {
	try {
		return localStorage.getItem(STORAGE_KEY) !== 'off';
	} catch {
		return true;
	}
}

/**
 * Android's haptics where the web has them (Chrome on Android): a short vibration under the finger,
 * behind the user's on/off setting. On other devices every call does nothing.
 */
class Haptics {
	/** Whether this device can vibrate under a finger, so the setting is worth showing. */
	readonly supported = supported;

	#enabled = $state(readEnabled());

	/** The user's setting, remembered on this device; on until turned off. */
	get enabled(): boolean {
		return this.#enabled;
	}

	set enabled(value: boolean) {
		this.#enabled = value;
		try {
			localStorage.setItem(STORAGE_KEY, value ? 'on' : 'off');
		} catch {
			// Not remembered in private mode; it holds for this visit.
		}
	}

	/** A tap where a choice is made: an option picked, a threshold crossed. */
	tap(): void {
		this.vibrate(10);
	}

	/** The lightest touch, as a drag reaches or leaves an edge. */
	feather(): void {
		this.vibrate(4);
	}

	/** A pattern of its own, in milliseconds of vibration and pause. */
	vibrate(pattern: number | number[]): void {
		if (!this.supported || !this.#enabled) return;
		// Chrome refuses (and logs) vibrations before the first tap on the page.
		if (navigator.userActivation?.hasBeenActive === false) return;
		navigator.vibrate(pattern);
	}
}

export const haptics = new Haptics();
