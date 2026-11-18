import { MediaQuery } from 'svelte/reactivity';
import { startTypedTransition } from './viewTransition.ts';
import { reducedMotion } from './media.ts';

export type ThemeMode = 'system' | 'light' | 'dark';
export type Palette = 'bicocca' | 'oceano' | 'bosco';

export const themeModes: readonly ThemeMode[] = ['system', 'light', 'dark'];
export const palettes: readonly Palette[] = ['bicocca', 'oceano', 'bosco'];

/** Read before first paint by the inline script in app.html: keep both in sync. */
const storageKey = 'mybicocca:theme';

interface SavedTheme {
	mode: ThemeMode;
	palette: Palette;
}

function load(): SavedTheme {
	try {
		const raw: unknown = JSON.parse(localStorage.getItem(storageKey) ?? '{}');
		if (raw && typeof raw === 'object') {
			const { mode, palette } = raw as Partial<Record<keyof SavedTheme, unknown>>;
			return {
				mode: themeModes.find((m) => m === mode) ?? 'system',
				palette: palettes.find((p) => p === palette) ?? 'bicocca'
			};
		}
	} catch {
		/* Unreadable storage falls back to the defaults. */
	}
	return { mode: 'system', palette: 'bicocca' };
}

const prefersDark = new MediaQuery('(prefers-color-scheme: dark)', false);

/** Where a change came from: the reveal circle grows out of it. */
export type RevealOrigin = Element | { x: number; y: number } | Event;

function originPoint(origin: RevealOrigin | undefined): { x: number; y: number } | undefined {
	if (!origin) return undefined;
	if (origin instanceof Element) {
		const r = origin.getBoundingClientRect();
		return { x: r.left + r.width / 2, y: r.top + r.height / 2 };
	}
	if (origin instanceof MouseEvent && origin.detail > 0)
		return { x: origin.clientX, y: origin.clientY };
	if (origin instanceof Event) {
		return origin.currentTarget instanceof Element ? originPoint(origin.currentTarget) : undefined;
	}
	return origin;
}

class ThemeController {
	mode = $state<ThemeMode>('system');
	palette = $state<Palette>('bicocca');

	/** The scheme actually on screen. */
	resolved = $derived<'light' | 'dark'>(
		this.mode === 'system' ? (prefersDark.current ? 'dark' : 'light') : this.mode
	);

	constructor() {
		const saved = load();
		this.mode = saved.mode;
		this.palette = saved.palette;
	}

	/**
	 * Changes mode and/or palette, persists it and applies it to the document. With an origin and
	 * motion allowed, the new theme is revealed as a circle growing from that point.
	 */
	set(next: Partial<SavedTheme>, origin?: RevealOrigin) {
		const mode = next.mode ?? this.mode;
		const palette = next.palette ?? this.palette;
		if (mode === this.mode && palette === this.palette) return;

		const commit = () => {
			this.mode = mode;
			this.palette = palette;
			this.#apply();
		};
		try {
			localStorage.setItem(storageKey, JSON.stringify({ mode, palette }));
		} catch {
			/* Private mode: the change still applies for this session. */
		}

		const point = originPoint(origin);
		if (!point || reducedMotion.current || !document.startViewTransition) {
			commit();
			return;
		}
		const root = document.documentElement;
		const radius = Math.hypot(
			Math.max(point.x, innerWidth - point.x),
			Math.max(point.y, innerHeight - point.y)
		);
		root.style.setProperty('--reveal-x', `${point.x}px`);
		root.style.setProperty('--reveal-y', `${point.y}px`);
		root.style.setProperty('--reveal-r', `${radius}px`);
		root.dataset.vt = 'theme';
		const transition = startTypedTransition(commit, 'theme');
		transition.finished.finally(() => {
			delete root.dataset.vt;
		});
	}

	#apply() {
		const root = document.documentElement;
		if (this.mode === 'system') delete root.dataset.theme;
		else root.dataset.theme = this.mode;
		if (this.palette === 'bicocca') delete root.dataset.palette;
		else root.dataset.palette = this.palette;
	}
}

/**
 * Browser chrome (mobile address bar, installed window) follows the page background. Called from
 * an effect on `theme.resolved` and `theme.palette`, so it also follows the system scheme.
 */
export function syncThemeColor() {
	const color = getComputedStyle(document.body).backgroundColor;
	for (const meta of document.querySelectorAll<HTMLMetaElement>('meta[name="theme-color"]')) {
		meta.content = color;
		meta.removeAttribute('media');
	}
}

export const theme = new ThemeController();
