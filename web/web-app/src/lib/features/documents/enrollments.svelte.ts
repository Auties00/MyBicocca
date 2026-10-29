import { createContext } from 'svelte';
import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { isOpen } from '#lib/data/account.ts';
import { today } from '#lib/data/clock.svelte.ts';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { renewalState, toEnrollments, type AnnualEnrollment } from './enrollments.ts';
import { annualEnrollmentQuery } from './queries.ts';

/** The active career's enrolments, shared by the list and the year open beside it. */
export class Enrollments {
	readonly query = query(() => session.current && annualEnrollmentQuery(session.current));
	readonly years: AnnualEnrollment[] = $derived(toEnrollments(this.query.data ?? []));
	readonly renewal = $derived(
		renewalState(
			this.years,
			today.current,
			session.current === undefined || isOpen(session.current.career.status)
		)
	);
	readonly hrefs: ResolvedPathname[] = $derived(
		this.years.map((year) => enrollmentHref(year.academicYear))
	);

	find(academicYear: number): AnnualEnrollment | undefined {
		return this.years.find((year) => year.academicYear === academicYear);
	}
}

export const [useEnrollments, provideEnrollments] = createContext<Enrollments>();

export function enrollmentHref(academicYear: number): ResolvedPathname {
	return resolve('/(app)/services/enrollments/[year]', { year: String(academicYear) });
}
