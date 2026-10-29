import type { Deadline } from './deadlines.ts';

export type SectionId = 'overdue' | 'week' | 'next_week' | 'later';

export interface DeadlineSection {
	id: SectionId;
	items: Deadline[];
}

/** Days from `today` to the Sunday closing its week; weeks start on Monday, as in Italy. */
function daysToSunday(today: Date): number {
	return (7 - today.getDay()) % 7;
}

/** The deadlines in time order, under "overdue", "this week", "next week" and "later". */
export function sectionsOf(deadlines: readonly Deadline[], today: Date): DeadlineSection[] {
	const sunday = daysToSunday(today);
	const sectionOf = (item: Deadline): SectionId => {
		if (item.days < 0) return 'overdue';
		if (item.days <= sunday) return 'week';
		if (item.days <= sunday + 7) return 'next_week';
		return 'later';
	};
	const sections: DeadlineSection[] = [];
	for (const item of deadlines) {
		const id = sectionOf(item);
		const last = sections.at(-1);
		if (last?.id === id) last.items.push(item);
		else sections.push({ id, items: [item] });
	}
	return sections;
}
