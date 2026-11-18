import { createContext } from 'svelte';

/** What the settings layout tells its pages. */
export interface SettingsLayout {
	/** Whether the list of settings stands beside the section; undefined before the first layout. */
	readonly twoPanes: boolean | undefined;
}

export const [getSettingsLayout, setSettingsLayout] = createContext<SettingsLayout>();

/**
 * Every window this wide shows two panes (the layout splits from 960 px of content), so /settings
 * goes straight to the first section before anything renders; narrower ones learn it from the layout.
 */
export const ALWAYS_TWO_PANES = '(width >= 1280px)';
