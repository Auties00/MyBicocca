import type { Esse3TranscriptRow } from '@mybicocca/data-api/esse3';
import { courseHref, rowStatus } from '#lib/features/profile/components/labels.ts';
import { toLibrettoRow } from '#lib/features/profile/transcript.ts';
import { displayName } from '#lib/features/services/labels.ts';
import { formatGradeValue } from '#lib/format/grades.ts';
import { formatCredits } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import { category } from '../categories.ts';
import { searchDoc, type SearchDoc } from '../rank.ts';

/** The record book's activities, found by name and code: "Voto 28 · 6 CFU", as the libretto. */
export function careerDocs(rows: readonly Esse3TranscriptRow[]): SearchDoc[] {
	return rows.map((raw) => {
		const row = toLibrettoRow(raw);
		const status = rowStatus(row, undefined);
		const outcome = row.grade
			? m.search_grade({ grade: formatGradeValue(row.grade) })
			: status.label;
		return searchDoc(
			{
				id: `transcript:${row.id}`,
				title: displayName(row.name),
				subtitle: [outcome, row.credits > 0 ? formatCredits(row.credits) : null]
					.filter((part) => part !== null)
					.join(' · '),
				icon: status.icon,
				tone: status.tone,
				href: courseHref(row, 'grades'),
				...category('career')
			},
			[row.code]
		);
	});
}
