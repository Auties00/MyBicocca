/** Apple platforms label the primary modifier ⌘ and use the Meta key for shortcuts. */
export const isApple =
	typeof navigator !== 'undefined' && /Mac|iPhone|iPad|iPod/i.test(navigator.userAgent);

/** True for Ctrl on Windows/Linux and ⌘ on Apple platforms. */
export function hasMod(event: KeyboardEvent | MouseEvent): boolean {
	return isApple ? event.metaKey : event.ctrlKey;
}

/** Single-key shortcuts must not fire while the user is typing. */
export function isTypingTarget(target: EventTarget | null): boolean {
	if (!(target instanceof HTMLElement)) return false;
	if (target.isContentEditable) return true;
	if (target instanceof HTMLTextAreaElement || target instanceof HTMLSelectElement) return true;
	if (target instanceof HTMLInputElement) {
		return !['checkbox', 'radio', 'button', 'submit', 'reset', 'range', 'color'].includes(
			target.type
		);
	}
	return false;
}

const keyLabels: Record<string, [apple: string, other: string]> = {
	mod: ['⌘', 'Ctrl'],
	shift: ['⇧', 'Shift'],
	alt: ['⌥', 'Alt'],
	ctrl: ['⌃', 'Ctrl'],
	enter: ['↵', 'Enter'],
	esc: ['esc', 'Esc'],
	escape: ['esc', 'Esc'],
	backspace: ['⌫', 'Backspace'],
	tab: ['⇥', 'Tab'],
	space: ['␣', 'Space'],
	up: ['↑', '↑'],
	down: ['↓', '↓'],
	left: ['←', '←'],
	right: ['→', '→']
};

/** Display label for a key name ('mod', 'shift', 'k', '/') on the current platform. */
export function keyLabel(key: string): string {
	const label = keyLabels[key.toLowerCase()];
	if (label) return isApple ? label[0] : label[1];
	return key.length === 1 ? key.toUpperCase() : key;
}

/** Key names whose `aria-keyshortcuts` value (a `KeyboardEvent.key`) isn't them capitalized. */
const ariaKeys: Record<string, string> = {
	esc: 'Escape',
	ctrl: 'Control',
	up: 'ArrowUp',
	down: 'ArrowDown',
	left: 'ArrowLeft',
	right: 'ArrowRight'
};

/** `aria-keyshortcuts` value for a key combination. */
export function ariaShortcut(keys: readonly string[]): string {
	return keys
		.map((k) => {
			const lower = k.toLowerCase();
			if (lower === 'mod') return isApple ? 'Meta' : 'Control';
			if (lower in ariaKeys) return ariaKeys[lower];
			return k.length === 1 ? k.toUpperCase() : k[0].toUpperCase() + k.slice(1);
		})
		.join('+');
}
