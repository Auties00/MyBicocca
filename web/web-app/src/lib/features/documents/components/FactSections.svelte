<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { FactSection as Section } from '../factSections.ts';
	import FactSection from './FactSection.svelte';

	interface Props {
		sections: readonly Section[];
		level: 2 | 3;
		/** What heads the first column, such as the grade's card. */
		lead?: Snippet;
	}

	let { sections, level, lead }: Props = $props();
</script>

<!-- Two columns flowing like a newspaper's on wide panes, so short topics fill the gaps. -->
<div class="sections">
	{#if lead}<div class="lead">{@render lead()}</div>{/if}
	{#each sections as section (section.id)}
		<FactSection {section} {level} />
	{/each}
</div>

<style>
	.sections {
		column-gap: var(--sp-5);
	}

	.sections > :global(*) {
		margin-block-end: var(--sp-6);
	}

	.lead {
		break-inside: avoid;
	}

	@container documents (width >= 720px) {
		.sections {
			columns: 2;
		}
	}

	@container documents (width >= 1000px) {
		.sections {
			column-gap: var(--sp-7);
		}
	}
</style>
