import type { ImageMetadata } from 'astro';
import type { Locale } from '../i18n/index.ts';

export const shots = ['calendar', 'elearning', 'map', 'exams', 'library', 'profile'] as const;
export type Shot = (typeof shots)[number];
export type WebSize = 'desktop' | 'tablet' | 'mobile';
export type TrackId = 'web-desktop' | 'web-tablet' | 'web-mobile' | 'android';

export interface LocalizedScreen {
	it: ImageMetadata;
	en: ImageMetadata;
}

const modules = import.meta.glob<{ default: ImageMetadata }>('../assets/screens/**/dark/*.webp', {
	eager: true
});

function lookup(path: string): ImageMetadata {
	const found = modules[path];
	if (!found) {
		throw new Error(`Missing imported screenshot at ${path}. Run "npm run screens" first.`);
	}
	return found.default;
}

export function webScreen(size: WebSize, locale: Locale, shot: Shot): ImageMetadata {
	return lookup(`../assets/screens/web/${size}/${locale}/dark/${shot}.webp`);
}

export function androidScreen(locale: Locale, shot: Shot): ImageMetadata {
	return lookup(`../assets/screens/android/${locale}/dark/${shot}.webp`);
}

export function webScreens(size: WebSize, shot: Shot): LocalizedScreen {
	return {
		it: webScreen(size, 'it', shot),
		en: webScreen(size, 'en', shot)
	};
}

export function androidScreens(shot: Shot): LocalizedScreen {
	return {
		it: androidScreen('it', shot),
		en: androidScreen('en', shot)
	};
}

export function trackScreens(track: TrackId, shot: Shot): LocalizedScreen {
	if (track === 'android') return androidScreens(shot);
	const size = track.replace('web-', '') as WebSize;
	return webScreens(size, shot);
}
