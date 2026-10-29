import { accountKey } from '#lib/data/keys.ts';
import { keyString } from '#lib/data/queryCache.ts';
import { idbPersistence } from '#lib/data/queryStore.ts';
import { session } from '#lib/data/session.svelte.ts';

type Choices = Readonly<Record<number, boolean>>;

/**
 * The courses an account starred here. Moodle's own flag is only the default: a refresh of the
 * course list never overwrites a choice made in the app (Android loses them at the next sync).
 * Kept encrypted under the account's prefix, so signing out wipes them with its other data.
 */
export class AccountFavourites {
	/** Course id → starred, for the courses starred or unstarred here. */
	choices = $state.raw<Choices>({});
	readonly #key: string;
	#ready: Promise<void> | undefined;

	constructor(accountId: string) {
		this.#key = keyString(accountKey({ id: accountId }, 'elearning.favourites'));
	}

	/** Reads the stored choices, once; a choice made before they arrive wins over them. */
	load(): Promise<void> {
		this.#ready ??= idbPersistence
			.read(this.#key, true)
			.then((stored) => {
				if (stored && typeof stored.data === 'object' && stored.data !== null) {
					this.choices = { ...(stored.data as Choices), ...this.choices };
				}
			})
			.catch(() => undefined);
		return this.#ready;
	}

	/** Whether a course is starred: the choice made here, else Moodle's flag. */
	has(course: { id: number; moodleFavourite: boolean }): boolean {
		return this.choices[course.id] ?? course.moodleFavourite;
	}

	/** Stars or unstars courses: every edition of a teaching at once, from the list. */
	async set(ids: readonly number[], favourite: boolean): Promise<void> {
		const next = { ...this.choices };
		for (const id of ids) next[id] = favourite;
		this.choices = next;
		await this.load();
		await idbPersistence
			.write(this.#key, { data: this.choices, updatedAt: Date.now() }, true)
			.catch(() => undefined);
	}
}

/**
 * One per account signed in. A plain record, not reactive: nothing renders it, and a lookup from a
 * `$derived` that adds an account must not count as a state change.
 */
const byAccount: Record<string, AccountFavourites> = {};

/** The favourites of a signed-in account: a lookup, safe in a `$derived`. */
export function favouritesOf(accountId: string): AccountFavourites {
	byAccount[accountId] ??= new AccountFavourites(accountId);
	return byAccount[accountId];
}

/*
 * Follows the accounts signed in: reads each one's choices once, and forgets those of an account
 * signed out, whose stored choices went with the rest of its data.
 */
$effect.root(() => {
	$effect(() => {
		const signedIn = new Set(session.accounts.map((account) => account.id));
		for (const id of Object.keys(byAccount)) if (!signedIn.has(id)) delete byAccount[id];
		for (const id of signedIn) void favouritesOf(id).load();
	});
});
