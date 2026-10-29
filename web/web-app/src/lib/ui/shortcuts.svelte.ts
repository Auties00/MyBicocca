import { SvelteMap } from 'svelte/reactivity';

/**
 * A key a page answers to, as the shortcuts dialog lists it. `keys` holds alternatives, each a
 * combination in Kbd's names: `[['j'], ['k']]`, `[['mod', 's']]`.
 */
export interface PageShortcut {
	keys: readonly (readonly string[])[];
	label: string;
}

type Source = () => readonly PageShortcut[] | false | undefined;

/* Each source with the order its component was created in: a layout before what it holds. */
const sources = new SvelteMap<Source, number>();
let created = 0;

/**
 * Lists the calling component's own keys in the `?` dialog while it is mounted. The getter is
 * reactive: a key that only works in some state (an open item) can come and go with it.
 */
export function registerShortcuts(source: Source): void {
	const order = created++;
	$effect(() => {
		sources.set(source, order);
		return () => sources.delete(source);
	});
}

/** Every mounted component's keys, outer components first, each label once. */
export function pageShortcuts(): PageShortcut[] {
	const list: PageShortcut[] = [];
	const ordered = [...sources].sort(([, a], [, b]) => a - b);
	for (const [source] of ordered) {
		for (const shortcut of source() || []) {
			if (!list.some((listed) => listed.label === shortcut.label)) list.push(shortcut);
		}
	}
	return list;
}
