<script lang="ts">
	import Facts from '#lib/ui/Facts.svelte';
	import IconChip from '#lib/ui/IconChip.svelte';
	import type { FactSection } from '../factSections.ts';

	interface Props {
		section: FactSection;
		/** 2 under a page's own title, 3 under a pane's heading. */
		level: 2 | 3;
	}

	let { section, level }: Props = $props();

	const headingId = $props.id();
</script>

<section class="section" aria-labelledby={headingId}>
	<div class="head">
		<IconChip icon={section.icon} tone="accent" size="sm" />
		<svelte:element this={`h${level}`} id={headingId} class="title">{section.title}</svelte:element>
	</div>
	{#if section.note !== undefined}
		<p class="note">{section.note}</p>
	{:else}
		<Facts items={section.facts} />
	{/if}
</section>

<style>
	.section {
		display: grid;
		align-content: start;
		gap: var(--sp-3);
		min-inline-size: 0;
		break-inside: avoid;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 10px;
		padding-inline: 2px;
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.note {
		padding: var(--sp-4) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		white-space: pre-line;
		overflow-wrap: anywhere;
		text-wrap: pretty;
	}

	@supports (corner-shape: squircle) {
		.note {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
