<script lang="ts">
	import { goto } from '$app/navigation';
	import { resolve } from '$app/paths';
	import { page } from '$app/state';
	import IconRadioOff from '~icons/ph/circle';
	import IconRadioOn from '~icons/ph/radio-button-fill';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { italianTitleCase } from '#lib/format/names.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCredits } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { gradeText } from '../format.ts';
	import { Libretto } from '../libretto.svelte.ts';
	import type { AverageKind, SimulationBase } from '../simulator.ts';
	import { groupByYear, type LibrettoRow } from '../transcript.ts';
	import { staleMessage, yearTitle } from './labels.ts';
	import SimulatorPanel from './SimulatorPanel.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	const libretto = new Libretto();

	/** From this board width the panel docks beside the lists (the first container query below). */
	const DOCK_WIDTH = 860;

	let panel = $state<HTMLElement>();
	let boardWidth = $state(0);
	const docked = $derived(boardWidth >= DOCK_WIDTH);

	const kind = $derived<AverageKind>(
		page.url.searchParams.get('kind') === 'arithmetic' ? 'arithmetic' : 'weighted'
	);
	const pickedId = $derived(Number(page.url.searchParams.get('exam')) || undefined);

	const base = $derived<SimulationBase>({
		kind,
		current:
			(kind === 'weighted'
				? libretto.summary?.weightedAverage
				: libretto.summary?.arithmeticAverage) ?? null,
		rollup: libretto.rollup ?? null
	});

	/** What is left to take, by degree year: the exams the next grade can come from. */
	const pending = $derived(
		libretto.plan &&
			groupByYear(libretto.plan.filter((row) => row.state !== 'passed')).flatMap(
				(group) => group.rows
			)
	);
	/** The graded exams Esse3 averages, the whole career's, most recent first. */
	const graded = $derived(
		libretto.all
			?.filter((row) => row.state === 'passed' && row.grade !== null)
			.sort((a, b) => (b.date?.toString() ?? '').localeCompare(a.date?.toString() ?? ''))
	);
	const picked = $derived(pending?.find((row) => row.id === pickedId));

	/* A picked exam fills in its credits; typing over them keeps the typed ones until the next pick. */
	let credits = $derived(picked ? String(Math.round(picked.credits)) : '');

	/** The kind and the picked exam live in the URL; changing them replaces the history entry. */
	function navigate(next: { kind?: AverageKind; exam?: number | null }) {
		const exam = next.exam === undefined ? pickedId : next.exam;
		const query = [
			(next.kind ?? kind) === 'arithmetic' ? 'kind=arithmetic' : undefined,
			exam === null || exam === undefined ? undefined : `exam=${exam}`
		]
			.filter((part) => part !== undefined)
			.join('&');
		const target = query ? resolve(`profile/simulator?${query}`) : resolve('profile/simulator');
		void goto(target, { replace: true, reset: false });
	}

	function pick(row: LibrettoRow) {
		navigate({ exam: row.id === pickedId ? null : row.id });
		// Stacked, the panel sits above the list, out of sight: bring the result into view.
		if (panel && !docked && row.id !== pickedId) {
			panel.scrollIntoView({ block: 'start', behavior: reducedMotion.current ? 'auto' : 'smooth' });
		}
	}

	function status(row: LibrettoRow): string {
		return `${yearTitle(row.year)} · ${formatCredits(row.credits)}`;
	}
</script>

<svelte:head>
	<title>{m.profile_simulator_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<PageHeader
		title={m.profile_simulator_title()}
		subtitle={m.profile_simulator_subtitle()}
		back={{ href: resolve('profile'), label: m.profile_title() }}
	/>

	{#if libretto.staleError}
		<ErrorState
			variant="stale"
			message={staleMessage(libretto.staleError)}
			updatedAt={libretto.updatedAt}
			onretry={libretto.refresh}
			retrying={libretto.refreshing}
		/>
	{/if}

	<!-- Projections start from Esse3's average over the libretto's rows: both are needed. -->
	{#if libretto.failed || libretto.statsFailed}
		{@const error = libretto.error ?? libretto.statsError}
		<ErrorState
			title={libretto.failed ? m.profile_load_error() : m.profile_stats_error()}
			message={error ? errorMessage(error) : m.profile_refresh_error()}
			onretry={libretto.refresh}
			retrying={libretto.refreshing}
		/>
	{:else}
		<div class="board" bind:clientWidth={boardWidth}>
			<div class="layout">
				<div class="dock" bind:this={panel}>
					{#if libretto.summary === undefined || libretto.rollup === undefined}
						<div class="panel-skeleton" aria-busy="true">
							<Skeleton height="44px" radius="var(--r-pill)" />
							<Skeleton variant="text" width="40%" />
							<Skeleton height="56px" width="60%" />
							<Skeleton height="72px" radius="var(--r-sm)" />
							<Skeleton height="120px" radius="var(--r-sm)" />
						</div>
					{:else}
						<SimulatorPanel
							{base}
							bind:credits
							onkind={(next) => navigate({ kind: next })}
							exam={picked && { name: italianTitleCase(picked.name), credits: picked.credits }}
							onclearexam={() => navigate({ exam: null })}
						/>
					{/if}
				</div>

				<div class="lists">
					{#if pending === undefined || graded === undefined}
						<div class="skeleton-list" aria-busy="true">
							<Skeleton variant="text" width="35%" />
							{#each { length: 6 }, index (index)}
								<Skeleton height="64px" radius="var(--r-sm)" />
							{/each}
						</div>
					{:else}
						{#if pending.length > 0}
							<ListGroup title={m.profile_pick_title()} description={m.profile_pick_hint()}>
								{#each pending as row (row.id)}
									<ListRow
										title={italianTitleCase(row.name)}
										description={status(row)}
										selected={row.id === pickedId}
										onclick={() => pick(row)}
									>
										{#snippet trailing()}
											<span class={['pick', { on: row.id === pickedId }]} aria-hidden="true">
												{#if row.id === pickedId}<IconRadioOn />{:else}<IconRadioOff />{/if}
											</span>
										{/snippet}
									</ListRow>
								{/each}
							</ListGroup>
						{/if}
						{#if graded.length > 0}
							<ListGroup
								title={m.profile_graded_exams()}
								description={libretto.rollup
									? m.profile_graded_exams_summary({
											count: libretto.rollup.gradedExams,
											credits: formatCredits(libretto.rollup.gradedCredits)
										})
									: undefined}
							>
								{#each graded as row (row.id)}
									<ListRow
										title={italianTitleCase(row.name)}
										description={row.date
											? `${formatCredits(row.credits)} · ${formatDate(row.date)}`
											: formatCredits(row.credits)}
									>
										{#snippet trailing()}
											{#if row.grade}
												<span class={['grade', { laude: row.grade.cumLaude }]}>
													<span aria-hidden="true">{gradeText(row.grade)}</span>
													<span class="sr-only">{gradeText(row.grade, true)}</span>
												</span>
											{/if}
										{/snippet}
									</ListRow>
								{/each}
							</ListGroup>
						{/if}
					{/if}
				</div>
			</div>
		</div>
	{/if}
</Page>

<style>
	.board {
		container: board / inline-size;
	}

	.layout {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		gap: var(--sp-7);
		align-items: start;
	}

	.dock {
		scroll-margin-block-start: calc(var(--shell-inset-top) + 12px);
	}

	.lists,
	.skeleton-list,
	.panel-skeleton {
		display: grid;
		gap: var(--sp-6);
	}

	.skeleton-list {
		gap: 2px;
	}

	.panel-skeleton {
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-xl);
		background: var(--surface);
		box-shadow: var(--elev-1);
	}

	.pick {
		display: inline-flex;
		color: var(--text-subtle);
	}

	.pick :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.pick.on {
		color: var(--accent);
	}

	.grade {
		font: 600 1.0625rem / 1.2 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.grade.laude {
		color: var(--gold);
	}

	/* Wide: the grades on the left, the simulator docked beside them, always in view. */
	@container board (width >= 860px) {
		.layout {
			grid-template-columns: minmax(0, 1fr) minmax(360px, 420px);
		}

		.dock {
			position: sticky;
			inset-block-start: calc(var(--shell-inset-top) + var(--sp-4));
			grid-column: 2;
			grid-row: 1;
		}

		.lists {
			grid-column: 1;
			grid-row: 1;
		}
	}

	/* Large: the two lists side by side, so no row stretches far from its own values. */
	@container board (width >= 1120px) {
		.lists {
			grid-template-columns: repeat(2, minmax(0, 1fr));
			align-items: start;
		}
	}
</style>
