import { createContext } from 'svelte';
import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { groupTitles, latestTitle, toTitles, type AcademicTitle } from './degrees.ts';
import { titlesQuery } from './queries.ts';

/** The person's qualifications, shared by the list and the title open beside it. */
export class Degrees {
	readonly query = query(() => session.current && titlesQuery(session.current));
	readonly titles: AcademicTitle[] = $derived(
		this.query.data === undefined ? [] : toTitles(this.query.data)
	);
	readonly groups = $derived(groupTitles(this.titles));
	readonly latest = $derived(latestTitle(this.titles));
	/** In the order the list shows them. */
	readonly hrefs: ResolvedPathname[] = $derived(
		this.groups.flatMap((group) => group.titles.map((title) => degreeHref(title.id)))
	);

	find(id: string): AcademicTitle | undefined {
		return this.titles.find((title) => title.id === id);
	}
}

export const [useDegrees, provideDegrees] = createContext<Degrees>();

export function degreeHref(id: string): ResolvedPathname {
	return resolve('/(app)/services/degrees/[id]', { id });
}
