<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import type { DeadlinesQuery } from '../deadlines.svelte.ts';
	import { streamList } from '../labels.ts';

	interface Props {
		deadlines: DeadlinesQuery;
	}

	let { deadlines }: Props = $props();
</script>

<!--
	Which sources the list lacks. A stream goes missing only when it first fails, so this renders
	with the first data and never pushes a list already on screen.
-->
{#if deadlines.data !== undefined && deadlines.data.length > 0 && deadlines.missing.length > 0}
	<Banner
		tone="warning"
		role="status"
		message={m.services_deadlines_partial({ sources: streamList(deadlines.missing) })}
	>
		{#snippet actions()}
			<Button
				variant="ghost"
				size="sm"
				icon={IconArrowClockwise}
				loading={deadlines.refreshing}
				onclick={() => void deadlines.retry()}
			>
				{m.common_retry()}
			</Button>
		{/snippet}
	</Banner>
{/if}
