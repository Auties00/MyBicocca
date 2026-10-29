<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { useEnrollments } from '../enrollments.svelte.ts';
	import ProgrammeSummary from './ProgrammeSummary.svelte';

	const enrollments = useEnrollments();
	const latest = $derived(enrollments.years.at(0));
	const first = $derived(enrollments.years.at(-1));
	const loading = $derived(enrollments.query.data === undefined);
</script>

<!-- Beside the years with none open: the programme the latest one belongs to, at a glance. -->
{#if loading || (latest && first)}
	<div class="overview">
		<ProgrammeSummary {latest} {first} />
		<p class="hint">{m.documents_enrollments_overview_select()}</p>
	</div>
{/if}

<style>
	.overview {
		display: grid;
		gap: var(--sp-5);
		max-inline-size: 640px;
		padding-block: var(--sp-2) var(--sp-4);
	}

	.hint {
		color: var(--text-muted);
		text-wrap: pretty;
	}
</style>
