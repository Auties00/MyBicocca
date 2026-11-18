import type {
	Esse3ExamSessionEnrollment,
	Esse3Invoices,
	Esse3StudentDebit,
	Esse3StudyPlan,
	Esse3StudyPlanHeader
} from '@mybicocca/data-api/esse3';
import { accountKey, careerKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/*
 * The Esse3 reads the deadlines merge besides the exams feature's bookings and calls, each named
 * after its SDK call so the results, fees and study plan pages share the same cache entries
 * (foundation: cache key convention). Every entry holds the SDK's own answer, untouched, so any
 * page can map it its own way.
 */

export const PUBLISHED_RESULTS_FILTER = 'pubblId=gt=0';

/**
 * The bookings whose outcome is published, with the decision deadline (`dataRifEsitoStu`).
 * `q=BACHECA_ESITI` would leave out the outcomes still awaiting a decision. Its key extends the
 * bookings' key, so invalidating the bookings reloads it too.
 */
export function publishedResultsQuery(
	scope: SessionScope
): QueryOptions<Esse3ExamSessionEnrollment[]> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, 'esse3.transcript.getBookingsByMatId', matId, PUBLISHED_RESULTS_FILTER),
		load: () =>
			scope.api.esse3.transcript.getBookingsByMatId(matId, {
				actorCode: 'STU',
				filter: PUBLISHED_RESULTS_FILTER,
				order: '-dataOraTurno',
				optionalFields: 'adStuDes,desAppello,dataOraTurno,notaPubbl,dataRifEsitoStu,pubblId'
			}),
		policy: policies.live
	};
}

/** The person's invoices, of every career: Esse3 lists them by person, not by career. */
export function invoicesQuery(scope: SessionScope): QueryOptions<Esse3Invoices[]> {
	const personId = scope.account.personId;
	return {
		key: accountKey(scope.account, 'esse3.tuitionFees.getInvoicesList', personId),
		load: () => scope.api.esse3.tuitionFees.getInvoicesList({ personId }),
		policy: policies.live
	};
}

/** Esse3 pages charges 50 at a time by default and 100 at most. */
const CHARGES_PAGE = 100;

/** The career's charges, every page: they itemise the invoices and name them. */
export function chargesQuery(scope: SessionScope): QueryOptions<Esse3StudentDebit[]> {
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'esse3.tuitionFees.getStudentChargesList', studentId),
		load: async () => {
			const charges: Esse3StudentDebit[] = [];
			for (let start = 0; ; start += CHARGES_PAGE) {
				const page = await scope.api.esse3.tuitionFees.getStudentChargesList({
					studentId,
					start,
					limit: CHARGES_PAGE
				});
				charges.push(...page);
				if (page.length < CHARGES_PAGE) return charges;
			}
		},
		policy: policies.live
	};
}

/** The career's study plans, one header each, as the calendar reads them. */
export function planHeadersQuery(scope: SessionScope): QueryOptions<Esse3StudyPlanHeader[]> {
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'esse3.plans.getStudentPlanHeaders', studentId),
		load: () => scope.api.esse3.plans.getStudentPlanHeaders(studentId),
		policy: policies.fresh(30)
	};
}

/** One study plan with all its fields: the default omits `sceltaFlg`, the year and the weight. */
export function studentPlanQuery(
	scope: SessionScope,
	planId: number
): QueryOptions<Esse3StudyPlan> {
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'esse3.plans.getStudentPlan', studentId, planId),
		load: () => scope.api.esse3.plans.getStudentPlan(studentId, planId, { optionalFields: 'ALL' }),
		policy: policies.fresh(30)
	};
}
