<script lang="ts">
	import { serviceGroupLabels, type ServiceGroup } from '#lib/shell/catalog.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import Badge from '#lib/ui/Badge.svelte';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { groupCaptions, serviceBadge, servicesOf, type BadgeInput } from '../hub.ts';

	interface Props {
		group: ServiceGroup;
		badges: BadgeInput;
	}

	let { group, badges }: Props = $props();
</script>

<ListGroup title={serviceGroupLabels[group]()} description={groupCaptions[group]()}>
	{#each servicesOf(group) as service (service.id)}
		{@const flag = serviceBadge(service.id, badges)}
		<ListRow
			title={service.label()}
			titleLines={1}
			description={service.description()}
			icon={service.icon}
			iconTone={service.tone}
			href={shellHref(service.path)}
		>
			{#snippet badge()}
				{#if flag}<Badge tone={flag.tone}>{flag.label}</Badge>{/if}
			{/snippet}
		</ListRow>
	{/each}
</ListGroup>
