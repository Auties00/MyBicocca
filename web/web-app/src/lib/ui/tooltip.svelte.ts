import type { Attachment } from 'svelte/attachments';
import { ariaShortcut } from './platform.ts';

export type TooltipPlacement = 'top' | 'bottom' | 'left' | 'right';

export interface TooltipOptions {
	/** Key names as in `Kbd` (e.g. `['mod', 'b']`), shown as keycaps after the text. */
	shortcut?: readonly string[];
	placement?: TooltipPlacement;
}

interface Shown {
	text: string;
	shortcut?: readonly string[];
	placement: TooltipPlacement;
	anchor: HTMLElement;
}

/** Id of the single tooltip element rendered by `TooltipHost`. */
export const tooltipId = 'ui-tooltip';

const showDelay = 450;
/** Moving between triggers soon after a tooltip hid shows the next one at once. */
const warmWindow = 400;

class TooltipState {
	current = $state<Shown | null>(null);
	#timer: ReturnType<typeof setTimeout> | undefined;
	#hiddenAt = 0;

	request(next: Shown, immediate: boolean) {
		clearTimeout(this.#timer);
		const warm = performance.now() - this.#hiddenAt < warmWindow || this.current !== null;
		if (immediate || warm) this.current = next;
		else this.#timer = setTimeout(() => (this.current = next), showDelay);
	}

	release(anchor: HTMLElement) {
		clearTimeout(this.#timer);
		if (this.current?.anchor === anchor) {
			this.current = null;
			this.#hiddenAt = performance.now();
		}
	}
}

export const tooltipState = new TooltipState();

/**
 * Shows a tooltip on hover (mouse and pen only) and keyboard focus. Rendered by the one
 * `TooltipHost` in the root layout. When the text repeats the element's accessible name
 * (icon buttons), it is not wired as a description, so screen readers don't hear it twice.
 *
 * ```svelte
 * <button aria-label="Chiudi" {@attach tooltip('Chiudi', { shortcut: ['esc'] })}>…</button>
 * ```
 */
export function tooltip(
	text: string | undefined,
	options: TooltipOptions = {}
): Attachment<HTMLElement> {
	return (node) => {
		if (!text) return;
		const shown: Shown = {
			text,
			shortcut: options.shortcut,
			placement: options.placement ?? 'top',
			anchor: node
		};
		const describes = node.getAttribute('aria-label') !== text && node.textContent?.trim() !== text;
		if (describes) node.setAttribute('aria-describedby', tooltipId);
		if (options.shortcut) node.setAttribute('aria-keyshortcuts', ariaShortcut(options.shortcut));

		const enter = (e: PointerEvent) => {
			if (e.pointerType !== 'touch') tooltipState.request(shown, false);
		};
		const focus = () => {
			if (node.matches(':focus-visible')) tooltipState.request(shown, true);
		};
		const leave = () => tooltipState.release(node);
		const key = (e: KeyboardEvent) => {
			if (e.key === 'Escape') leave();
		};

		node.addEventListener('pointerenter', enter);
		node.addEventListener('pointerleave', leave);
		node.addEventListener('pointerdown', leave);
		node.addEventListener('focus', focus);
		node.addEventListener('blur', leave);
		node.addEventListener('keydown', key);
		return () => {
			/* Teardown can run inside an update that reads the tooltip state: write it after that. */
			queueMicrotask(leave);
			if (describes) node.removeAttribute('aria-describedby');
			node.removeEventListener('pointerenter', enter);
			node.removeEventListener('pointerleave', leave);
			node.removeEventListener('pointerdown', leave);
			node.removeEventListener('focus', focus);
			node.removeEventListener('blur', leave);
			node.removeEventListener('keydown', key);
		};
	};
}
