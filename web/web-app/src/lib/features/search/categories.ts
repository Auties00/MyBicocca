import { m } from '#lib/paraglide/messages.js';

/**
 * The groups results are listed under, in the order they take when their best rows score the
 * same: what the student has to do first, then Android's (actions, pages, courses, assignments,
 * quizzes, calendar, places, career), then the settings, and searching elsewhere last.
 */
const CATEGORIES = {
	todo: m.search_category_todo,
	actions: m.shell_palette_actions,
	pages: m.shell_palette_pages,
	services: m.shell_palette_services,
	courses: m.search_category_courses,
	assignments: m.search_category_assignments,
	quizzes: m.search_category_quizzes,
	calendar: m.search_category_calendar,
	places: m.search_category_places,
	career: m.search_category_career,
	settings: m.shell_palette_settings,
	elsewhere: m.search_category_elsewhere
} as const;

export type Category = keyof typeof CATEGORIES;

const ORDER = Object.keys(CATEGORIES) as Category[];

/** A row's `group` and `order` for its category. */
export function category(name: Category): { group: string; order: number } {
	return { group: CATEGORIES[name](), order: ORDER.indexOf(name) };
}
