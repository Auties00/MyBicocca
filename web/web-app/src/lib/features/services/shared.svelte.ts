import { createContext, onDestroy } from 'svelte';

/** What the pages under a layout share, each made once by the first that asks. */
class SharedOwner {
	readonly #made = new Map<() => unknown, unknown>();
	readonly #disposers: (() => void)[] = [];

	get<T>(make: () => T): T {
		if (!this.#made.has(make)) {
			let value: unknown;
			// Owned by the layout rather than the first reader, which may unmount first.
			this.#disposers.push(
				$effect.root(() => {
					value = make();
				})
			);
			this.#made.set(make, value);
		}
		return this.#made.get(make) as T;
	}

	dispose(): void {
		for (const dispose of this.#disposers) dispose();
	}
}

const [getOwner, setOwner, hasOwner] = createContext<SharedOwner>();

/** Lets the pages below share what they make with `shared`; call it while the layout initializes. */
export function provideShared(): void {
	const owner = setOwner(new SharedOwner());
	onDestroy(() => owner.dispose());
}

/**
 * What `make` makes, once for every page under the layout: pages that never ask never load it, and
 * moving between those that do reloads nothing. Outside the layout, a new one. Call it while a
 * component initializes.
 */
export function shared<T>(make: () => T): T {
	return hasOwner() ? getOwner().get(make) : make();
}
