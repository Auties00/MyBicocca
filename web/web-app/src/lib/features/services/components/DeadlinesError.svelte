<script lang="ts">
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { m } from '#lib/paraglide/messages.js';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import type { DeadlinesQuery } from '../deadlines.svelte.ts';
	import { streamList } from '../labels.ts';

	interface Props {
		deadlines: DeadlinesQuery;
	}

	let { deadlines }: Props = $props();

	/** The sources that did answer hold nothing, which says nothing about those that didn't. */
	const unchecked = $derived(deadlines.data !== undefined);
</script>

<!-- Nothing loaded, or nothing in the sources that did: never an all-clear either way. -->
<ErrorState
	title={unchecked ? m.services_deadlines_unchecked_title() : m.services_deadlines_load_failed()}
	message={unchecked
		? m.services_deadlines_unchecked({ sources: streamList(deadlines.missing) })
		: deadlines.error
			? errorMessage(deadlines.error)
			: m.services_deadlines_load_failed()}
	retrying={deadlines.refreshing}
	onretry={() => void deadlines.retry()}
/>
