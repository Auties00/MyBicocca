import type {
	Esse3AnnualEnrollment,
	Esse3Certificate,
	Esse3PersonTitles
} from '@mybicocca/data-api/esse3';
import { accountKey, careerKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/* Each query wraps one SDK call and is named after it (foundation: cache key convention). */

const ENROLLMENT_ORDER = '-aaIscrId';

/**
 * The career's annual enrolments, newest first. Never `fields=ALL`: on Bicocca's Esse3 it is a
 * whitelist that matches nothing, while the default answer already carries every field.
 */
export function annualEnrollmentQuery(scope: SessionScope): QueryOptions<Esse3AnnualEnrollment[]> {
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'esse3.careers.getAnnualEnrollment', studentId, ENROLLMENT_ORDER),
		load: () => scope.api.esse3.careers.getAnnualEnrollment(studentId, { order: ENROLLMENT_ORDER }),
		policy: policies.live
	};
}

/**
 * The person's qualifications as the career sees them. `optionalFields=ALL` is what makes Esse3
 * include the three lists at all (unlike `fields=ALL` elsewhere).
 */
export function titlesQuery(scope: SessionScope): QueryOptions<Esse3PersonTitles> {
	const personId = scope.account.personId;
	const studentId = scope.career.id;
	return {
		key: careerKey(scope, 'esse3.personalData.getTitles', personId, studentId),
		load: () =>
			scope.api.esse3.personalData.getTitles(personId, { studentId, optionalFields: 'ALL' }),
		policy: policies.live
	};
}

/**
 * The self-certifications the portal lists, scraped with the portal's SSO session; they belong
 * to the person, whatever the career.
 */
export function certificatesQuery(scope: SessionScope): QueryOptions<Esse3Certificate[]> {
	return {
		key: accountKey(scope.account, 'esse3.certificates.getCertificates'),
		load: () =>
			scope.api.esse3Web(async (portal) => {
				const page = await portal.certificates.getCertificates();
				return page.sessionExpired ? null : page.certificates;
			}),
		policy: policies.live
	};
}
