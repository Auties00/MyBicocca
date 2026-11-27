import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { toLibrettoRow } from '#lib/features/profile/transcript.ts';
import { studyPlanExamHref as examHref } from '#lib/features/studyplan/links.ts';

/**
 * Where a recorded grade lives within Services: its exam in the study plan, found by the
 * activity's code, else the plan itself while the record book loads or lacks the exam.
 */
export function studyPlanExamHref(): (code: string | null) => ResolvedPathname {
	const records = query(() => session.current && recordBookRowsQuery(session.current));
	const byCode = $derived(
		// eslint-disable-next-line svelte/prefer-svelte-reactivity -- rebuilt whole by the derived
		new Map(
			(records.data ?? []).flatMap((row) => {
				const item = toLibrettoRow(row);
				return item.code ? [[item.code.toUpperCase(), item.id] as const] : [];
			})
		)
	);
	return (code) => {
		const id = code ? byCode.get(code.toUpperCase()) : undefined;
		return id === undefined ? resolve('services/study-plan') : examHref(id);
	};
}
