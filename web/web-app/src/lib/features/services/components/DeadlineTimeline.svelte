<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import type { Deadline } from '../deadlines.ts';
	import { sectionsOf, type SectionId } from '../sections.ts';
	import DeadlineRow from './DeadlineRow.svelte';

	interface Props {
		deadlines: readonly Deadline[];
		today: Date;
		/** One plain list, for previews; otherwise grouped by week. */
		dense?: boolean;
		/** Each day's first row takes the day's id, for a calendar beside to jump to. */
		anchors?: boolean;
	}

	let { deadlines, today, dense = false, anchors = !dense }: Props = $props();

	const headings: Record<SectionId, () => string> = {
		overdue: m.services_section_overdue,
		week: m.services_section_week,
		next_week: m.services_section_next_week,
		later: m.services_section_later
	};

	const sections = $derived(
		dense ? [{ id: 'week' as const, items: [...deadlines] }] : sectionsOf(deadlines, today)
	);

	const sameDay = (a: Deadline | undefined, b: Deadline) => a?.date.getTime() === b.date.getTime();
</script>

<div class={['timeline', { dense }]}>
	{#each sections as section (section.id)}
		<section class="section" aria-labelledby={dense ? undefined : `deadlines-${section.id}`}>
			{#if !dense}
				<h2 class={['heading', section.id]} id="deadlines-{section.id}">
					{headings[section.id]()}
				</h2>
			{/if}
			<ol class="rows" role="list" aria-label={dense ? m.services_deadlines_timeline() : undefined}>
				{#each section.items as item, index (item.id)}
					<DeadlineRow
						{item}
						{dense}
						anchor={anchors}
						continued={sameDay(section.items[index - 1], item)}
						first={index === 0}
						last={index === section.items.length - 1}
					/>
				{/each}
			</ol>
		</section>
	{/each}
</div>

<style>
	.timeline {
		display: grid;
		gap: var(--sp-6);
	}

	.dense {
		gap: 0;
	}

	.section {
		display: grid;
		gap: var(--sp-3);
	}

	.heading {
		padding-inline-start: 4px;
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.heading.overdue {
		color: var(--danger);
	}

	.rows {
		margin: 0;
		padding: 0;
		list-style: none;
	}
</style>
