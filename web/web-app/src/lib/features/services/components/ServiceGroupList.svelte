<script lang="ts">
	import { serviceGroupLabels, type ServiceGroup } from '#lib/shell/catalog.ts';
	import { shellHref } from '#lib/shell/nav.ts';
	import ListGroup from '#lib/ui/ListGroup.svelte';
	import ListRow from '#lib/ui/ListRow.svelte';
	import { groupCaptions, servicesOf } from '../hub.ts';

	interface Props {
		group: ServiceGroup;
	}

	let { group }: Props = $props();
</script>

<ListGroup title={serviceGroupLabels[group]()} description={groupCaptions[group]()}>
	{#each servicesOf(group) as service (service.id)}
		<ListRow
			title={service.label()}
			titleLines={1}
			description={service.description()}
			icon={service.icon}
			iconTone={service.tone}
			href={shellHref(service.path)}
		/>
	{/each}
</ListGroup>
