import type { Esse3TranscriptRowWithQuestionnaireStatus } from '@mybicocca/data-api/esse3';
import IconCalendarCheck from '~icons/ph/calendar-check';
import { shortDate } from '#lib/features/calendar/format.ts';
import { statusLabel } from '#lib/features/questionnaires/format.ts';
import { activityHref } from '#lib/features/questionnaires/links.ts';
import { isPending, toActivities } from '#lib/features/questionnaires/model.ts';
import type { Deadline } from '#lib/features/services/deadlines.ts';
import { deadlineTitle, deadlineWhen, onDay } from '#lib/features/services/labels.ts';
import { deadlineHref } from '#lib/features/services/links.ts';
import { formatTime } from '#lib/format/dates.ts';
import { toIsoDate } from '#lib/format/days.ts';
import { formatGrade } from '#lib/format/grades.ts';
import { formatCurrency } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import { serviceCatalog } from '#lib/shell/catalog.ts';
import type { Icon } from '#lib/ui/types.ts';
import { category } from '../categories.ts';
import { aliasList, searchDoc, type SearchDoc } from '../rank.ts';

/** The hub's icon of a service, so a thing to do looks like the service it is done in. */
function serviceIcon(id: string): Icon | undefined {
	return serviceCatalog.find((entry) => entry.id === id)?.icon;
}

/** "Valuta Basi di Dati": each questionnaire still to fill in, as Android's search action. */
export function questionnaireDocs(
	rows: readonly Esse3TranscriptRowWithQuestionnaireStatus[]
): SearchDoc[] {
	return toActivities(rows)
		.filter((activity) => isPending(activity.status))
		.map((activity) =>
			searchDoc(
				{
					id: `questionnaire:${activity.id}`,
					title: m.search_todo_questionnaire({ course: activity.name }),
					subtitle: `${m.shell_service_questionnaires()} · ${statusLabel(activity.status)}`,
					icon: serviceIcon('questionnaires'),
					tone: 'accent',
					href: activityHref(activity.id),
					...category('todo')
				},
				[activity.name, activity.code, ...aliasList(m.search_todo_questionnaire_aliases())]
			)
		);
}

function resultDoc(item: Extract<Deadline, { source: 'result' }>): SearchDoc {
	const course = deadlineTitle(item);
	const grade =
		item.grade.kind === 'unknown'
			? null
			: item.grade.kind === 'numeric'
				? m.search_grade({ grade: formatGrade(item.grade) })
				: formatGrade(item.grade, { full: true });
	return searchDoc(
		{
			id: item.id,
			title: m.search_todo_result({ course }),
			subtitle: [grade, deadlineWhen(item)].filter((part) => part !== null).join(' · '),
			icon: serviceIcon('results'),
			tone: 'accent',
			href: deadlineHref(item),
			...category('todo')
		},
		[course, ...aliasList(m.search_todo_result_aliases())]
	);
}

function bookingDoc(item: Extract<Deadline, { source: 'booking' }>): SearchDoc {
	const course = deadlineTitle(item);
	const window =
		item.opensAt === null
			? m.search_todo_booking_closes(onDay(item.date))
			: m.search_todo_booking_opens(onDay(item.opensAt));
	const call = item.callDate === null ? null : m.search_todo_booking_call(onDay(item.callDate));
	return searchDoc(
		{
			id: item.id,
			title: m.search_todo_booking({ course }),
			subtitle: [window, call].filter((part) => part !== null).join(' · '),
			icon: serviceIcon('exams'),
			tone: 'accent',
			href: deadlineHref(item),
			...category('todo')
		},
		[course, item.code, ...aliasList(m.search_todo_booking_aliases())]
	);
}

function feeDoc(item: Extract<Deadline, { source: 'fee' }>): SearchDoc {
	const title = deadlineTitle(item);
	return searchDoc(
		{
			id: item.id,
			title: m.search_todo_fee({ fee: title }),
			subtitle: `${formatCurrency(item.amount)} · ${deadlineWhen(item)}`,
			icon: serviceIcon('fees'),
			tone: item.urgent ? 'warning' : 'accent',
			href: deadlineHref(item),
			...category('todo')
		},
		[title, item.installment, ...aliasList(m.search_todo_fee_aliases())]
	);
}

function examDoc(item: Extract<Deadline, { source: 'exam' }>): SearchDoc {
	const day = shortDate(toIsoDate(item.date));
	return searchDoc(
		{
			id: item.id,
			title: deadlineTitle(item),
			subtitle: [
				m.search_kind_exam(),
				item.time === null ? day : `${day}, ${formatTime(item.time)}`,
				item.room
			]
				.filter((part) => part !== null)
				.join(' · '),
			icon: IconCalendarCheck,
			href: deadlineHref(item),
			...category('calendar')
		},
		aliasList(m.search_exam_aliases())
	);
}

/** What the deadlines merge asks the student to act on: results to decide, calls to book, fees. */
export function todoDocs(deadlines: readonly Deadline[]): SearchDoc[] {
	return deadlines.flatMap((item) => {
		switch (item.source) {
			case 'result':
				return [resultDoc(item)];
			case 'booking':
				return [bookingDoc(item)];
			case 'fee':
				return [feeDoc(item)];
			case 'exam':
				return [];
		}
	});
}

/** The booked exams still to sit, listed with the calendar's events. */
export function examDocs(deadlines: readonly Deadline[]): SearchDoc[] {
	return deadlines.flatMap((item) => (item.source === 'exam' ? [examDoc(item)] : []));
}
