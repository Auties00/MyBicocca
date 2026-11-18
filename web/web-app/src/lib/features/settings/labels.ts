import { m } from '#lib/paraglide/messages.js';
import type { Palette, ThemeMode } from '#lib/ui/theme.svelte.ts';

/** The shell's own names, so the account menu and this page agree. */
export const modeLabels: Record<ThemeMode, () => string> = {
	system: m.shell_theme_system,
	light: m.shell_theme_light,
	dark: m.shell_theme_dark
};

/** Product names, as on Android: the same in every language (`translate="no"` where shown). */
export const paletteNames: Record<Palette, string> = {
	bicocca: 'Bicocca',
	oceano: 'Oceano',
	bosco: 'Bosco'
};
