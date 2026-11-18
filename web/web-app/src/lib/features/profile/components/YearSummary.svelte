<script lang="ts">
	import { resolve } from '$app/paths';
	import IconCalculator from '~icons/ph/calculator';
	import { m } from '#lib/paraglide/messages.js';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import Meter from '#lib/ui/Meter.svelte';
	import type { YearGroup } from '../transcript.ts';
	import { yearAnchor, yearProgress, yearSummary, yearTitle } from './labels.ts';

	interface Props {
		groups: readonly YearGroup[];
	}

	let { groups }: Props = $props();
</script>

<div class="years">
	<ListGroup label={m.profile_years_label()}>
		{#each groups as group (group.year)}
			{@const title = yearTitle(group.year)}
			{@const summary = yearSummary(group)}
			<ListRow
				{title}
				description={summary}
				href={resolve(`profile/exams#${yearAnchor(group.year)}`)}
				chevron
			>
				{#snippet trailing()}
					<span class="meter">
						<Meter
							value={yearProgress(group)}
							max={1}
							label={title}
							valueText={summary}
							hideLabel
						/>
					</span>
				{/snippet}
			</ListRow>
		{/each}
	</ListGroup>
	<ListGroup label={m.profile_simulator_title()}>
		<ListRow
			title={m.profile_simulator_title()}
			description={m.profile_simulator_subtitle()}
			icon={IconCalculator}
			iconTone="accent"
			href={resolve('profile/simulator')}
		/>
	</ListGroup>
</div>

<style>
	/* Stretched beside a taller chart, the simulator's entry settles at the foot. */
	.years {
		display: grid;
		align-content: space-between;
		gap: var(--sp-4);
	}

	.meter {
		inline-size: 72px;
	}
</style>
