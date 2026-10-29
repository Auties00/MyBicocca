<script lang="ts">
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import type { CalendarData } from '../data.svelte.ts';

	interface Props {
		data: CalendarData;
	}

	let { data }: Props = $props();
</script>

{#if data.failed}
	<div class="failure">
		<ErrorState
			title={m.calendar_sync_failed()}
			message={data.error ? errorMessage(data.error) : m.ui_error_generic()}
			onretry={data.refresh}
			retrying={data.refreshing}
		/>
	</div>
{:else if data.error && data.hydrated}
	<div class="stale">
		<ErrorState
			variant="stale"
			message={errorMessage(data.error)}
			updatedAt={data.updatedAt ? new Date(data.updatedAt) : undefined}
			onretry={data.refresh}
			retrying={data.refreshing}
		/>
	</div>
{/if}

<style>
	/* The month never loaded: the error takes the calendar's place. */
	.failure {
		flex: 1;
		display: grid;
		place-items: center;
		padding: var(--sp-6) var(--gutter);
	}

	.stale {
		flex: none;
		padding: 0 var(--calendar-inline-end, var(--gutter)) var(--sp-3)
			var(--calendar-inline-start, var(--sp-5));
	}
</style>
