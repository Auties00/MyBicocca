<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import Page from '#lib/ui/Page.svelte';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import { badgeInput, useAlerts } from '../alerts.svelte.ts';
	import type { DeadlinesQuery } from '../deadlines.svelte.ts';
	import { serviceGroups } from '../hub.ts';
	import DeadlinesCard from './DeadlinesCard.svelte';
	import ServiceGroupList from './ServiceGroupList.svelte';

	interface Props {
		deadlines: DeadlinesQuery;
	}

	let { deadlines }: Props = $props();

	const alerts = useAlerts();
	const badges = $derived(badgeInput(deadlines, alerts));
</script>

<Page width="dashboard">
	<PageHeader title={m.shell_services()} />
	<div class="hub">
		<DeadlinesCard {deadlines} />
		<div class="groups">
			{#each serviceGroups as group (group)}
				<ServiceGroupList {group} {badges} />
			{/each}
		</div>
	</div>
</Page>

<style>
	.hub {
		display: grid;
		gap: var(--sp-5);
		margin-block-start: calc(-1 * var(--sp-2));
	}

	.groups {
		display: grid;
		gap: var(--sp-6);
		align-items: start;
	}

	@container page (width >= 680px) {
		.groups {
			grid-template-columns: repeat(2, minmax(0, 1fr));
			gap: var(--sp-5);
		}
	}
</style>
