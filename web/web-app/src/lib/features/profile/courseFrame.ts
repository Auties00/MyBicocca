import { createContext } from 'svelte';
import type { Href } from '#lib/ui/types.ts';

/**
 * Where a page shows an open course: docked beside its list, or as a page of its own whose Back
 * returns to the page that opened it (the transcript when not given).
 */
export interface CourseFrame {
	readonly docked: boolean;
	readonly back?: { readonly href: Href; readonly label: string };
}

export const [getCourseFrame, setCourseFrame] = createContext<CourseFrame>();
