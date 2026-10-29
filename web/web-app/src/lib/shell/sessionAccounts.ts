import type { Account, Career, CareerStatus } from '#lib/data/account.ts';
import { academicYearLabel } from '#lib/data/academicYear.ts';
import { session } from '#lib/data/session.svelte.ts';
import { careerTitle, personName } from '#lib/format/names.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Status } from '#lib/ui/types.ts';
import type { AccountModel, ShellAccount, ShellCareer } from './account.ts';
import { shellHref } from './nav.ts';

const statuses: Partial<Record<CareerStatus, { label: () => string; tone: Status }>> = {
	suspended: { label: m.shell_career_suspended, tone: 'warning' },
	provisional: { label: m.shell_career_provisional, tone: 'info' },
	graduated: { label: m.shell_career_graduated, tone: 'gold' },
	interrupted: { label: m.shell_career_interrupted, tone: 'neutral' }
};

/* Esse3 sends names and programmes in capitals; text already in mixed case is kept as written. */
const shouting = (text: string) => !/\p{Ll}/u.test(text);

function toCareer(career: Career): ShellCareer {
	const status = statuses[career.status];
	return {
		id: career.id,
		title: careerTitle(career),
		detail: m.shell_career_details({
			number: career.studentNumber,
			year: academicYearLabel(career.academicYear)
		}),
		level:
			career.level === 'bachelor'
				? { short: m.shell_level_bachelor_short(), full: m.shell_level_bachelor() }
				: career.level === 'master'
					? { short: m.shell_level_master_short(), full: m.shell_level_master() }
					: undefined,
		status: status && { label: status.label(), tone: status.tone }
	};
}

function toAccount(account: Account): ShellAccount {
	return {
		id: account.id,
		name: shouting(account.displayName) ? personName(account.displayName) : account.displayName,
		email: account.username,
		careers: account.careers.map(toCareer),
		selectedCareerId: account.selectedCareerId
	};
}

/** The signed-in accounts from the session, active first, as the shell shows them. Reactive when read in `$derived`. */
export function sessionAccountModel(): AccountModel | undefined {
	const active = session.active;
	if (!active) return undefined;
	return {
		accounts: [active, ...session.accounts.filter((a) => a.id !== active.id)].map(toAccount),
		activeId: active.id,
		switchAccount: (id) => session.switchAccount(id),
		selectCareer: (accountId, careerId) => session.selectCareer(accountId, careerId),
		signOut: (id) => session.signOut(id),
		// The demo build has the demo account only.
		...(__DEMO__ ? {} : { addAccountHref: shellHref('login') })
	};
}
