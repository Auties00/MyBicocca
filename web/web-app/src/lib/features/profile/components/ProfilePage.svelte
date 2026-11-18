<script lang="ts">
	import { resolve } from '$app/paths';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconBooks from '~icons/ph/books-duotone';
	import IconCalculator from '~icons/ph/calculator';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatAverage } from '#lib/format/grades.ts';
	import { formatCredits } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { careerTitle } from '#lib/format/names.ts';
	import Button from '#lib/ui/Button.svelte';
	import Card from '#lib/ui/Card.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { Libretto } from '../libretto.svelte.ts';
	import { personPhotoQuery } from '../queries.ts';
	import {
		groupByYear,
		progress,
		trendPoints,
		type CareerStats,
		type GradeRollup
	} from '../transcript.ts';
	import { creditsOf, staleMessage } from './labels.ts';
	import StatTile from './StatTile.svelte';
	import StudentCard from './StudentCard.svelte';
	import TrendChart from './TrendChart.svelte';
	import YearSummary from './YearSummary.svelte';

	const libretto = new Libretto();

	/* The photo is on the back of the card: it loads once the back may be seen. */
	let revealed = $state(false);
	const photo = query(() => revealed && session.current && personPhotoQuery(session.current));
	let photoUrl = $state<string | null>(null);

	$effect(() => {
		const bytes = photo.data;
		if (!bytes) return;
		const url = URL.createObjectURL(new Blob([new Uint8Array(bytes)]));
		photoUrl = url;
		return () => {
			URL.revokeObjectURL(url);
			photoUrl = null;
		};
	});

	const scope = $derived(session.current);
	const summary = $derived(libretto.summary);
	const rollup = $derived(libretto.rollup);
	const points = $derived(libretto.plan && trendPoints(libretto.plan));
	const groups = $derived(libretto.plan && groupByYear(libretto.plan));
	const statsLoading = $derived(summary === undefined && !libretto.statsFailed);
	const rowsLoading = $derived(groups === undefined && !libretto.failed);
	const empty = $derived(groups?.length === 0);

	/** Esse3's base-110 figure when it sends one, else the credits the average spans. */
	function weightedCaption(stats: CareerStats, sums: GradeRollup): string {
		if (stats.weightedAverage === null) return m.profile_tile_no_grades();
		if (stats.weightedAverage110 !== null) {
			return m.profile_base_110({ value: formatAverage(stats.weightedAverage110) });
		}
		return m.profile_tile_credits_basis({ credits: formatCredits(sums.gradedCredits) });
	}

	const ids = $props.id();
</script>

<svelte:head>
	<title>{m.profile_title()} · {m.app_name()}</title>
</svelte:head>

<Page width="dashboard">
	<PageHeader title={m.profile_title()} subtitle={scope ? careerTitle(scope.career) : undefined}>
		{#snippet actions()}
			<IconButton
				icon={IconArrowClockwise}
				label={m.common_refresh()}
				variant="outline"
				loading={libretto.refreshing}
				onclick={libretto.refresh}
			/>
		{/snippet}
	</PageHeader>

	{#if libretto.staleError}
		<ErrorState
			variant="stale"
			message={staleMessage(libretto.staleError)}
			updatedAt={libretto.updatedAt}
			onretry={libretto.refresh}
			retrying={libretto.refreshing}
		/>
	{/if}

	<div class="board">
		<div class={['grid', { failed: libretto.failed, empty }]}>
			{#if scope}
				<div class="badge">
					<StudentCard
						accountId={scope.account.id}
						name={scope.account.displayName}
						number={scope.career.studentNumber}
						username={scope.account.username}
						photo={photoUrl}
						onreveal={() => (revealed = true)}
					/>
				</div>
			{/if}

			{#if libretto.failed}
				<div class="aside">
					<Card tone="sunken">
						<ErrorState
							title={m.profile_load_error()}
							message={libretto.error ? errorMessage(libretto.error) : m.profile_refresh_error()}
							onretry={libretto.refresh}
							retrying={libretto.refreshing}
						/>
					</Card>
				</div>
			{:else if empty}
				<!-- No activity at all: one line for the whole page, not an empty state per section. -->
				<div class="aside">
					<Card tone="sunken">
						<EmptyState
							icon={IconBooks}
							title={m.profile_libretto_empty_title()}
							body={m.profile_libretto_empty_body()}
						>
							{#snippet action()}
								<Button href={resolve('services/study-plan')}>
									{m.profile_libretto_empty_action()}
								</Button>
							{/snippet}
						</EmptyState>
					</Card>
				</div>
			{:else}
				<section class="stats" aria-labelledby="{ids}-stats" aria-busy={statsLoading}>
					<!-- Beside the card the tiles need no heading: it stays for screen readers. -->
					<div class="stats-head">
						<SectionHeader id="{ids}-stats" title={m.profile_statistics()} />
					</div>
					<div class="tiles">
						{#if libretto.statsFailed}
							<!-- Only the averages and totals are missing: the trend and the years stay. -->
							<div class="tiles-error">
								<Card tone="sunken">
									<ErrorState
										title={m.profile_stats_error()}
										message={libretto.statsError
											? errorMessage(libretto.statsError)
											: m.profile_refresh_error()}
										onretry={libretto.stats.refresh}
										retrying={libretto.stats.refreshing}
									/>
								</Card>
							</div>
						{:else if summary && rollup}
							<StatTile
								href={resolve('profile/simulator?kind=arithmetic')}
								label={m.profile_arithmetic_average()}
								value={summary.arithmeticAverage}
								decimals={2}
								footer={{
									caption:
										summary.arithmeticAverage === null
											? m.profile_tile_no_grades()
											: m.profile_tile_exams_basis({ count: rollup.gradedExams })
								}}
								action={m.profile_calc_hypothetical()}
								icon={IconCalculator}
								countUp="profile.arithmetic"
							/>
							<StatTile
								href={resolve('profile/simulator?kind=weighted')}
								label={m.profile_weighted_average()}
								value={summary.weightedAverage}
								decimals={2}
								footer={{ caption: weightedCaption(summary, rollup) }}
								action={m.profile_calc_hypothetical()}
								icon={IconCalculator}
								countUp="profile.weighted"
							/>
							<StatTile
								href={resolve('profile/exams')}
								label={m.profile_exams_taken()}
								value={summary.passedExams}
								unit="/ {summary.plannedExams}"
								action={m.profile_libretto_open()}
								footer={{
									ratio: progress(summary.passedExams, summary.plannedExams),
									text: m.profile_exams_passed_of({
										passed: summary.passedExams,
										total: summary.plannedExams
									})
								}}
								countUp="profile.exams"
							/>
							<StatTile
								href={resolve('profile/exams?view=credits')}
								label={m.profile_credits_earned()}
								value={Math.trunc(summary.passedCredits)}
								unit="/ {Math.trunc(summary.totalCredits)}"
								action={m.profile_libretto_open()}
								footer={{
									ratio: progress(summary.passedCredits, summary.totalCredits),
									text: creditsOf(summary.passedCredits, summary.totalCredits)
								}}
								countUp="profile.credits"
							/>
						{:else}
							{#each { length: 4 }, index (index)}
								<div class="tile-placeholder">
									<Skeleton height="100%" radius="var(--r-lg)" />
								</div>
							{/each}
						{/if}
					</div>
				</section>

				<section class="trend" aria-labelledby="{ids}-trend" aria-busy={rowsLoading}>
					<SectionHeader id="{ids}-trend" title={m.profile_trend()} />
					<Card>
						<TrendChart {points} average={summary?.arithmeticAverage ?? null} />
					</Card>
				</section>

				<section class="years" aria-labelledby="{ids}-years" aria-busy={rowsLoading}>
					<SectionHeader
						id="{ids}-years"
						title={m.profile_libretto_title()}
						link={groups
							? { href: resolve('profile/exams'), label: m.profile_libretto_open() }
							: undefined}
					/>
					{#if groups === undefined}
						<div class="rows">
							{#each { length: 3 }, index (index)}
								<Skeleton height="66px" radius="var(--r-sm)" />
							{/each}
						</div>
					{:else}
						<YearSummary {groups} />
					{/if}
				</section>
			{/if}
		</div>
	</div>
</Page>

<style>
	.board {
		container: board / inline-size;
	}

	.grid {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		grid-template-areas: 'badge' 'stats' 'trend' 'years';
		gap: var(--sp-7) var(--sp-6);
		align-items: start;
	}

	.grid.failed,
	.grid.empty {
		grid-template-areas: 'badge' 'aside';
	}

	.badge {
		grid-area: badge;
		justify-self: center;
		inline-size: min(100%, 420px);
	}

	.aside {
		grid-area: aside;
	}

	.stats {
		position: relative;
		grid-area: stats;
	}

	.trend {
		grid-area: trend;
	}

	.years {
		grid-area: years;
	}

	section {
		display: grid;
		gap: var(--sp-4);
		min-inline-size: 0;
	}

	/* Tiles and their placeholders share one height, so nothing moves when the numbers arrive. */
	.tiles {
		--tile-h: 136px;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		grid-auto-rows: 1fr;
		gap: var(--sp-3);
		container: tiles / inline-size;
	}

	.tile-placeholder {
		min-block-size: var(--tile-h);
	}

	.tiles-error {
		display: grid;
		grid-column: 1 / -1;
		grid-row: span 2;
		min-block-size: calc(2 * var(--tile-h) + var(--sp-3));
	}

	@container tiles (width >= 460px) {
		.tiles > :global(*) {
			--tile-h: 148px;
		}
	}

	.rows {
		display: grid;
		gap: 2px;
	}

	/*
	 * Tablets and up: the card and the four tiles form one band under the page title. The tiles
	 * stretch to the card's height where it is the taller; otherwise the card centres on them.
	 */
	@container board (width >= 640px) {
		.grid {
			grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
			grid-template-areas: 'badge stats' 'trend trend' 'years years';
		}

		.grid.failed,
		.grid.empty {
			grid-template-areas: 'badge aside';
		}

		.badge {
			align-self: center;
			inline-size: 100%;
		}

		.stats {
			align-self: stretch;
		}

		.stats-head {
			position: absolute;
			inline-size: 1px;
			block-size: 1px;
			overflow: hidden;
			clip-path: inset(50%);
			white-space: nowrap;
		}

		/*
		 * Two rows of these fit the card's height from 400 px wide (248 px), so the edges meet;
		 * narrower, the card centres on them. The band holds from two 308 px columns.
		 */
		.tiles,
		.tiles > :global(*) {
			--tile-h: 112px;
			--tile-pad: 14px;
		}
	}

	/*
	 * Desktops: identity and libretto on the left, numbers and their trend on the right. Both
	 * columns of the lower row end together: the chart grows into a taller libretto, and a taller
	 * chart sets the simulator's entry at the libretto's foot.
	 */
	@container board (width >= 900px) {
		.grid {
			grid-template-columns: minmax(340px, 400px) minmax(0, 1fr);
			grid-template-areas: 'badge stats' 'years trend';
			column-gap: var(--sp-7);
		}

		.trend,
		.years {
			grid-template-rows: auto 1fr;
			align-self: stretch;
		}
	}
</style>
