<script lang="ts">
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import type { DeadlinesQuery } from '../deadlines.svelte.ts';

	interface Props {
		deadlines: DeadlinesQuery;
	}

	let { deadlines }: Props = $props();

	const stale = $derived(deadlines.failures.find((failure) => failure.stale));
	const updatedAt = $derived(
		deadlines.updatedAt === undefined ? undefined : new Date(deadlines.updatedAt)
	);
</script>

<!--
	A refresh that failed over data already shown. It can come long after the list, so pages put it
	below the list, where it moves nothing.
-->
{#if deadlines.data !== undefined && stale}
	<ErrorState
		variant="stale"
		message={stale.error.kind === 'offline'
			? errorMessage(stale.error)
			: m.services_deadlines_refresh_failed()}
		{updatedAt}
		retrying={deadlines.refreshing}
		onretry={() => void deadlines.retry()}
	/>
{/if}
