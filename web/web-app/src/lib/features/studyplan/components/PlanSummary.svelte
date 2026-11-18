<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Card from '#lib/ui/Card.svelte';
	import Meter from '#lib/ui/Meter.svelte';
	import Stat from '#lib/ui/Stat.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { planState } from '../labels.ts';
	import type { PlanYear, StudyPlan } from '../plan.ts';

	interface Props {
		plan: StudyPlan;
		years: readonly PlanYear[];
		/** Credits of the plan already earned; undefined until the record book has loaded. */
		earned: number | undefined;
	}

	let { plan, years, earned }: Props = $props();

	const total = $derived(Math.trunc(years.reduce((sum, year) => sum + year.credits, 0)));
	const state = $derived(planState(plan.state));
	const numbered = $derived(years.filter((year) => year.year > 0).length);
</script>

<Card padding="lg">
	<div class="summary">
		<div class="head">
			<Stat
				label={m.studyplan_total_label()}
				value={total}
				unit={m.studyplanedit_unit_credits()}
				caption={m.studyplan_total_caption({
					activities: m.studyplan_activities_count({ count: plan.courses.length }),
					years: m.studyplan_years_count({ count: Math.max(1, numbered) })
				})}
				countUp="studyplan-total"
			/>
			{#if state}
				<div class="state">
					<StatusChip status={state.status} label={state.label} />
					{#if plan.lastChange}
						<span class="since">
							{m.studyplan_state_since({ date: formatDate(plan.lastChange) })}
						</span>
					{/if}
				</div>
			{/if}
		</div>
		{#if earned !== undefined && total > 0}
			<Meter
				value={Math.min(earned, total)}
				max={total}
				label={m.studyplan_earned_label()}
				valueText={m.studyplan_earned_value({
					earned: formatNumber(Math.trunc(earned)),
					total: formatNumber(total)
				})}
			/>
		{/if}
	</div>
</Card>

<style>
	.summary {
		display: grid;
		gap: var(--sp-5);
	}

	.head {
		display: flex;
		flex-wrap: wrap;
		align-items: flex-start;
		justify-content: space-between;
		gap: var(--sp-4);
	}

	.state {
		display: grid;
		justify-items: end;
		gap: 6px;
	}

	.since {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	@container (width < 360px) {
		.state {
			justify-items: start;
		}
	}
</style>
