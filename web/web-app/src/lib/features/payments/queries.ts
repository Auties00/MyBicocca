import type { Esse3EnrollmentForTuition, Esse3Refunds } from '@mybicocca/data-api/esse3';
import { accountKey, careerKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/*
 * Each query is named after its SDK call (foundation: cache key convention). The invoices and the
 * charges are the services feature's, which the deadlines and Today read too: an invoice paid here
 * leaves their lists at the same time.
 */
export { chargesQuery, invoicesQuery } from '#lib/features/services/sources.ts';

/** One row per enrolment year, with the ISEE Esse3 acquired for it. */
export function iseeQuery(scope: SessionScope): QueryOptions<Esse3EnrollmentForTuition[]> {
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'esse3.tuitionFees.getEnrollmentsForTaxes', studentId),
		load: () => scope.api.esse3.tuitionFees.getEnrollmentsForTaxes(studentId),
		policy: policies.live
	};
}

/** `rimborsatoFlg=2`: refunds both credited and still in progress. */
const ALL_REFUNDS = 2;

/** The person's refunds, whatever the career: Esse3 lists them by person. */
export function refundsQuery(scope: SessionScope): QueryOptions<Esse3Refunds[]> {
	const personId = scope.account.personId;
	return {
		key: accountKey(scope.account, 'esse3.tuitionFees.getRefundsList', personId, ALL_REFUNDS),
		load: () => scope.api.esse3.tuitionFees.getRefundsList(personId, { refundedFlag: ALL_REFUNDS }),
		policy: policies.live
	};
}
