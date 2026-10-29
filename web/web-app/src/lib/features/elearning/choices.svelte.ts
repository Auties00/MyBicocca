import { SvelteMap } from 'svelte/reactivity';
import { ALL, filterToken, parseFilter, type CourseFilter } from './courses.ts';

const FILTER_KEY = 'mybicocca:elearning-filter';

function readStored(): CourseFilter {
	try {
		return parseFilter(localStorage.getItem(FILTER_KEY)) ?? ALL;
	} catch {
		return ALL;
	}
}

/**
 * The course list's last filter, shared by every account of this browser as on Android; the
 * `?filter=` of a link wins over it.
 */
export const storedFilter = {
	get(): CourseFilter {
		return readStored();
	},
	set(filter: CourseFilter): void {
		try {
			localStorage.setItem(FILTER_KEY, filterToken(filter));
		} catch {
			// Storage refused (private mode): the filter still lives in the URL.
		}
	}
};

/**
 * Which edition each course card shows, by group key, for this visit. A remembered edition
 * that is gone falls back to the latest.
 */
export const activeEditions = new SvelteMap<string, number>();
