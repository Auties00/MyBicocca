import { createContext } from 'svelte';

/** Where the libretto shows the open course: docked beside the list, or as a page of its own. */
export interface CourseFrame {
	readonly docked: boolean;
}

export const [getCourseFrame, setCourseFrame] = createContext<CourseFrame>();
