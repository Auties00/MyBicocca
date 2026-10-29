import { query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { careerStatsQuery, recordBookRowsQuery } from './queries.ts';
import { careerStats, gradeRollup, loadedAt, studyPlanRows, toLibrettoRow } from './transcript.ts';

/**
 * The active career's record book and Esse3's statistics, as every profile page reads them.
 * Create it while a component initializes.
 */
export class Libretto {
	readonly rows = query(() => session.current && recordBookRowsQuery(session.current));
	readonly stats = query(() => session.current && careerStatsQuery(session.current));

	/** Every row of the career, supernumerary ones included. */
	readonly all = $derived(this.rows.data?.map(toLibrettoRow));
	/** The study plan's activities, oldest exam first. */
	readonly plan = $derived(this.all && studyPlanRows(this.all));
	readonly summary = $derived(this.stats.data && careerStats(this.stats.data));
	readonly rollup = $derived(this.all && gradeRollup(this.all));

	/** The rows failed to load with none to show: every view of the libretto needs them. */
	readonly failed = $derived(this.rows.status === 'error');
	readonly error = $derived(this.rows.error);
	/** Esse3's totals failed to load with none to show, while the rows may still be there. */
	readonly statsFailed = $derived(this.stats.status === 'error');
	readonly statsError = $derived(this.stats.error);
	/** The last failure while older data stays on screen. */
	readonly staleError = $derived(
		this.failed
			? undefined
			: ((this.rows.data && this.rows.error) ?? (this.stats.data && this.stats.error))
	);
	readonly refreshing = $derived(this.rows.refreshing || this.stats.refreshing);
	/** When the older of the two was loaded. */
	readonly updatedAt = $derived(loadedAt(this.rows.updatedAt, this.stats.updatedAt));

	refresh = async (): Promise<void> => {
		await Promise.all([this.rows.refresh(), this.stats.refresh()]);
	};
}
