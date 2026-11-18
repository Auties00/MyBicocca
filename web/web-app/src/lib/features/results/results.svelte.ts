import { createContext } from 'svelte';
import { today } from '#lib/data/clock.svelte.ts';
import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { publishedResultsQuery } from '#lib/features/services/sources.ts';
import { absence, splitResults, type ExamResult } from './model.ts';

/**
 * The published results of the active career. The query is the one the deadlines, the services
 * hub and Today read, so a decision taken here shows up there at once.
 */
export class Results {
	readonly query = query(() => session.current && publishedResultsQuery(session.current));
	readonly split = $derived(splitResults(this.query.data ?? [], today.current));
	/** Absences and withdrawals left out of the archive, where they can outnumber the grades. */
	hideAbsences = $state(false);
	readonly absences = $derived(
		this.split.archived.filter((result) => absence(result.grade)).length
	);
	readonly archive = $derived(
		this.hideAbsences
			? this.split.archived.filter((result) => !absence(result.grade))
			: this.split.archived
	);
	/** In the order the list shows them: what to decide, then the archive. */
	readonly ordered: ExamResult[] = $derived([...this.split.pending, ...this.archive]);

	/** Any result, shown in the list or not, so a link to a hidden one still opens it. */
	find(id: string): ExamResult | undefined {
		const { pending, archived } = this.split;
		return (
			pending.find((result) => result.id === id) ?? archived.find((result) => result.id === id)
		);
	}
}

/** How the results layout shows its pages, for the pages to fit in. */
export interface ResultsView {
	readonly results: Results;
	/** List and detail side by side; otherwise each page has the screen to itself. */
	readonly wide: boolean;
}

const [getView, setView] = createContext<ResultsView>();

export { getView as useResultsView, setView as provideResultsView };
