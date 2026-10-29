// First: the language strategy must be defined before any message resolves a locale.
import '#lib/features/settings/languageStrategy.ts';
import type { ClientInit } from '@sveltejs/kit/hooks';
import { session } from '#lib/data/session.svelte.ts';

/**
 * Starts reading the stored accounts (and loading the SDK for the active one) as the app boots. It
 * is not awaited: the layouts that need the session await it in their `load`, while the page's
 * code downloads alongside.
 */
export const init: ClientInit = () => void session.load();
