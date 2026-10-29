import type { QueryKey } from './queryCache.ts';

type Param = string | number | boolean;

/**
 * Query keys are `[scope, account, career, feature, ...params]`, so signing out wipes the prefix
 * `['acct', accountId]` and a career's data never mixes with another's. Put the feature first and
 * narrow it with params: `careerKey(scope, 'exams', 'booked')`. Invalidating a shorter key reloads
 * everything under it: `invalidate(careerKey(scope, 'exams'))` after booking an exam.
 */
export function careerKey(
	scope: { account: { id: string }; career: { id: number } },
	feature: string,
	...params: Param[]
): QueryKey {
	return ['acct', scope.account.id, scope.career.id, feature, ...params];
}

/** Data of an account that no career scopes, such as its Moodle courses. */
export function accountKey(account: { id: string }, feature: string, ...params: Param[]): QueryKey {
	return ['acct', account.id, '-', feature, ...params];
}

/** Data identical for every user, such as EasyStaff timetables or Affluences sites. */
export function publicKey(feature: string, ...params: Param[]): QueryKey {
	return ['public', '-', '-', feature, ...params];
}

/** Data of this browser whatever the account, such as desk appointments and library seats. */
export function deviceKey(feature: string, ...params: Param[]): QueryKey {
	return ['device', '-', '-', feature, ...params];
}

/** Everything of one account, for wiping it. */
export function accountPrefix(accountId: string): QueryKey {
	return ['acct', accountId];
}
