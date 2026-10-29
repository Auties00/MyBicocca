import type { Attachment } from 'svelte/attachments';

/** How far the finger travels, damped, before letting go refreshes. */
export const PULL_THRESHOLD = 72;
const MAX_PULL = 112;
const RESISTANCE = 0.5;

/**
 * Pull-to-refresh on touch screens: dragging down from the top of the page arms a refresh that
 * runs on release. While it is attached the page's overscroll is contained, so the browser's own
 * pull-to-reload, which would reload the whole app, never starts alongside it.
 */
export class PullToRefresh {
	/** The damped pull in pixels, 0 at rest. */
	distance = $state(0);
	readonly #onrefresh: () => void;

	constructor(onrefresh: () => void) {
		this.#onrefresh = onrefresh;
	}

	get armed(): boolean {
		return this.distance >= PULL_THRESHOLD;
	}

	attach: Attachment<HTMLElement> = (node) => {
		if (!matchMedia('(pointer: coarse)').matches) return;
		const root = document.documentElement;
		const overscroll = root.style.overscrollBehaviorY;
		root.style.overscrollBehaviorY = 'contain';
		let startX = 0;
		let startY = 0;
		/* Undecided until the finger moves: a sideways swipe (the filter chips) is left alone. */
		let state: 'idle' | 'undecided' | 'pulling' = 'idle';

		const start = (event: TouchEvent) => {
			if (event.touches.length !== 1 || window.scrollY > 0) return;
			startX = event.touches[0].clientX;
			startY = event.touches[0].clientY;
			state = 'undecided';
		};
		const move = (event: TouchEvent) => {
			if (state === 'idle') return;
			const dx = event.touches[0].clientX - startX;
			const dy = event.touches[0].clientY - startY;
			if (state === 'undecided') {
				if (Math.abs(dx) < 6 && Math.abs(dy) < 6) return;
				state = dy > 0 && Math.abs(dy) > Math.abs(dx) && window.scrollY <= 0 ? 'pulling' : 'idle';
				if (state === 'idle') return;
			}
			// Once the page scrolls, Chrome makes the moves uncancelable: the pull then just follows.
			if (event.cancelable) event.preventDefault();
			this.distance = Math.min(MAX_PULL, Math.max(0, dy * RESISTANCE));
		};
		const end = () => {
			if (state === 'pulling' && this.armed) this.#onrefresh();
			state = 'idle';
			this.distance = 0;
		};

		node.addEventListener('touchstart', start, { passive: true });
		node.addEventListener('touchmove', move, { passive: false });
		node.addEventListener('touchend', end);
		node.addEventListener('touchcancel', end);
		return () => {
			root.style.overscrollBehaviorY = overscroll;
			node.removeEventListener('touchstart', start);
			node.removeEventListener('touchmove', move);
			node.removeEventListener('touchend', end);
			node.removeEventListener('touchcancel', end);
		};
	};
}
