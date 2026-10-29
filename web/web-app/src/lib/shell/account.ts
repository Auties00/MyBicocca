import type { Href, Status } from '#lib/ui/types.ts';

/** A career as the account menu shows it. */
export interface ShellCareer {
	id: number;
	/** Degree programme name. */
	title: string;
	/** "Matricola 900123 · A.A. 2025". */
	detail: string;
	/** Level pip: short text and its full name for screen readers. */
	level?: { short: string; full: string };
	/** Shown for careers that are not plainly active. */
	status?: { label: string; tone: Status };
}

export interface ShellAccount {
	id: string;
	name: string;
	email: string;
	photo?: string;
	careers: readonly ShellCareer[];
	selectedCareerId: number;
}

/** What the shell needs to show and change the signed-in accounts. */
export interface AccountModel {
	/** Active account first. */
	accounts: readonly ShellAccount[];
	activeId: string;
	switchAccount(accountId: string): Promise<void>;
	selectCareer(accountId: string, careerId: number): Promise<void>;
	signOut(accountId: string): Promise<void>;
	/** Where "Add another account" goes; absent where no other account can be added. */
	addAccountHref?: Href;
}
