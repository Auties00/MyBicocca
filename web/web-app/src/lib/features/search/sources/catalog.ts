import { resolve } from '$app/paths';
import IconBooks from '~icons/ph/books';
import IconCalculator from '~icons/ph/calculator';
import IconCalendarPlus from '~icons/ph/calendar-plus';
import IconClipboard from '~icons/ph/clipboard-text';
import IconHeadset from '~icons/ph/headset';
import IconPlus from '~icons/ph/plus';
import IconReceipt from '~icons/ph/receipt';
import IconTranslate from '~icons/ph/translate';
import IconTree from '~icons/ph/tree-structure';
import { m } from '#lib/paraglide/messages.js';
import { catalogItems } from '#lib/shell/catalog.ts';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import type { Href, Icon } from '#lib/ui/types.ts';
import { category } from '../categories.ts';
import { aliasList, searchDoc, type SearchDoc } from '../rank.ts';

interface Action {
	id: string;
	href: Href;
	icon: Icon;
	title: () => string;
	/** Where it happens: "Servizi · Esami". */
	place: () => string[];
	/** Comma-separated synonyms. */
	aliases: () => string;
}

/** Android's search actions the shell's catalogue doesn't hold (it has check-in, theme, account). */
const ACTIONS: readonly Action[] = [
	{
		id: 'book-exam',
		href: resolve('services/exams/book'),
		icon: IconCalendarPlus,
		title: m.search_action_book_exam,
		place: () => [m.shell_services(), m.shell_service_exams()],
		aliases: m.search_action_book_exam_aliases
	},
	{
		id: 'library-seat',
		href: resolve('services/library'),
		icon: IconBooks,
		title: m.search_action_library_seat,
		place: () => [m.shell_services(), m.shell_service_library()],
		aliases: m.search_action_library_seat_aliases
	},
	{
		id: 'appointment',
		href: resolve('services/appointments/book'),
		icon: IconHeadset,
		title: m.search_action_appointment,
		place: () => [m.shell_services(), m.shell_service_appointments()],
		aliases: m.search_action_appointment_aliases
	},
	{
		id: 'pay-fees',
		href: resolve('services/fees'),
		icon: IconReceipt,
		title: m.search_action_pay_fees,
		place: () => [m.shell_services(), m.shell_service_fees()],
		aliases: m.search_action_pay_fees_aliases
	},
	{
		id: 'questionnaire',
		href: resolve('services/questionnaires'),
		icon: IconClipboard,
		title: m.search_action_questionnaire,
		place: () => [m.shell_services(), m.shell_service_questionnaires()],
		aliases: m.search_action_questionnaire_aliases
	},
	{
		id: 'study-plan',
		href: resolve('services/study-plan/edit'),
		icon: IconTree,
		title: m.search_action_study_plan,
		place: () => [m.shell_services(), m.shell_service_study_plan()],
		aliases: m.search_action_study_plan_aliases
	},
	{
		id: 'add-course',
		href: resolve('elearning/catalog'),
		icon: IconPlus,
		title: m.search_action_add_course,
		place: () => [m.shell_elearning()],
		aliases: m.search_action_add_course_aliases
	},
	{
		id: 'average',
		href: resolve('profile/simulator'),
		icon: IconCalculator,
		title: m.search_action_average,
		place: () => [m.shell_profile()],
		aliases: m.search_action_average_aliases
	},
	{
		id: 'language',
		href: resolve('settings/language'),
		icon: IconTranslate,
		title: m.search_action_language,
		place: () => [m.shell_settings()],
		aliases: m.search_action_language_aliases
	}
];

/**
 * A row of the shell's, as a doc in the category its group names, so the group sorts among the
 * index's: a page of the catalogue, or one of the shell's own actions (the theme, the account).
 */
export function shellDoc({ keywords, ...item }: PaletteItem): SearchDoc {
	const name = (['actions', 'pages', 'services', 'settings'] as const).find(
		(name) => category(name).group === item.group
	);
	return searchDoc(name ? { ...item, ...category(name) } : item, keywords);
}

/**
 * The shell's catalogue (pages, services, settings, its actions) and the actions above, for the
 * search's matcher and history boosts. The ids are the shell's, so the copy the shell's own
 * provider returns merges with these.
 */
export function catalogDocs(): SearchDoc[] {
	const shell = catalogItems().map(shellDoc);
	const actions = ACTIONS.map((action) =>
		searchDoc(
			{
				id: `action:${action.id}`,
				title: action.title(),
				subtitle: action.place().join(' · '),
				icon: action.icon,
				href: action.href,
				...category('actions')
			},
			aliasList(action.aliases())
		)
	);
	return [...actions, ...shell];
}
