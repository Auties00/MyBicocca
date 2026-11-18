import { hasMod, isTypingTarget } from '#lib/ui/platform.ts';
import { allDestinations } from '#lib/ui/destinations.ts';
import type { ShellPath } from './nav.ts';

interface ShortcutActions {
	openPalette(): void;
	toggleSidebar(): void;
	openShortcuts(): void;
	go(path: ShellPath): void;
}

/** A `g` waits this long for its second key. */
const chordWindow = 1500;

/**
 * App-wide keys: ⌘K/Ctrl+K and `/` open the palette, Ctrl/⌘+B folds the sidebar, `?` lists the
 * shortcuts, `g` then a letter jumps to a destination. Only ⌘K works while typing: Ctrl/⌘+B is an
 * editor's bold there. Listens in the capture phase so a page's own single-key shortcuts see
 * `defaultPrevented` for keys the shell took. Returns the cleanup.
 */
export function installShortcuts(actions: ShortcutActions): () => void {
	let chordAt = 0;

	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.isComposing) return;
		const key = event.key.toLowerCase();
		const mod = hasMod(event) && !event.altKey && !event.shiftKey;

		if (key === 'k' && mod) {
			event.preventDefault();
			actions.openPalette();
			return;
		}
		if (isTypingTarget(event.target) || document.querySelector('dialog:modal')) return;
		if (key === 'b' && mod) {
			event.preventDefault();
			actions.toggleSidebar();
			return;
		}
		if (event.ctrlKey || event.metaKey || event.altKey) return;

		if (chordAt && performance.now() - chordAt < chordWindow) {
			chordAt = 0;
			const destination = allDestinations.find((d) => d.key === key);
			if (destination) {
				event.preventDefault();
				actions.go(destination.path);
			}
			return;
		}
		if (key === 'g') {
			chordAt = performance.now();
			event.preventDefault();
		} else if (event.key === '/') {
			event.preventDefault();
			actions.openPalette();
		} else if (event.key === '?') {
			event.preventDefault();
			actions.openShortcuts();
		}
	}

	window.addEventListener('keydown', keydown, { capture: true });
	return () => window.removeEventListener('keydown', keydown, { capture: true });
}
