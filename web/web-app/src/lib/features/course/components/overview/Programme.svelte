<script lang="ts">
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { ProgrammePart } from '../../syllabus.ts';

	interface Props {
		parts: readonly ProgrammePart[];
	}

	let { parts }: Props = $props();

	/* A numbering the teacher wrote ("1. Introduzione", "Parte 2 -") gives way to the page's own. */
	const title = (text: string) =>
		text.replace(
			/^(?:(?:parte|part|modulo|module|unità|unit)\s+(?:\d+|[ivx]+)[.):\s-]*|(?:\d+|[ivx]+)[.):-])\s*/i,
			''
		) || text;
</script>

<ol class="parts">
	{#each parts as part, index (index)}
		<li class="part">
			<p class="number" aria-hidden="true">
				<span class="kicker">{m.course_part_kicker()}</span>
				<span class="digits">{formatNumber(index + 1, { minimumIntegerDigits: 2 })}</span>
			</p>
			<div class="body">
				{#if part.title}
					<h3 class="title">{title(part.title)}</h3>
				{/if}
				{#if part.items.length > 0}
					<p class="count">{m.course_topics_count({ count: part.items.length })}</p>
					<ul class="topics">
						{#each part.items as item, at (at)}
							<li>{item}</li>
						{/each}
					</ul>
				{/if}
			</div>
		</li>
	{/each}
</ol>

<style>
	.parts {
		display: grid;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.part {
		display: grid;
		grid-template-columns: 52px minmax(0, 1fr);
		gap: 16px;
		padding-block: 20px;
	}

	.part + .part {
		border-block-start: 1px solid var(--hairline);
	}

	.part:first-child {
		padding-block-start: 4px;
	}

	.number {
		display: grid;
		align-content: start;
		gap: 2px;
	}

	.kicker {
		color: var(--gold);
		font: var(--type-overline);
		font-size: 0.625rem;
		letter-spacing: 0.12em;
	}

	.digits {
		color: var(--course-ink);
		font: var(--type-title-m);
		font-size: 1.75rem;
		font-weight: 650;
		letter-spacing: -0.02em;
		font-variant-numeric: tabular-nums;
	}

	.body {
		display: grid;
		align-content: start;
		gap: 4px;
		min-inline-size: 0;
		padding-block-start: 14px;
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-wrap: balance;
	}

	.count {
		color: var(--text-muted);
		font: var(--type-meta);
		font-style: italic;
	}

	.topics {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
		margin: 8px 0 0;
		padding: 0;
		list-style: none;
	}

	.topics li {
		padding: 5px 12px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		font: var(--type-ui);
		font-weight: 450;
		overflow-wrap: anywhere;
	}
</style>
