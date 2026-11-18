<script lang="ts">
	import { SvelteSet } from 'svelte/reactivity';
	import IconBookOpen from '~icons/ph/book-open-text';
	import IconCaretDown from '~icons/ph/caret-down';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconFolder from '~icons/ph/folder-simple';
	import IconMagnifyingGlass from '~icons/ph/magnifying-glass';
	import { academicYearLabel } from '#lib/data/academicYear.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { AreaTint } from '../../catalog/areas.ts';
	import type { CatalogCourse, CatalogSearchRow } from '../../catalog/model.ts';
	import { bareCode } from '../../courseCode.ts';
	import { catalogHref } from '../../links.ts';
	import EnrolButton from './EnrolButton.svelte';
	import Highlight from './Highlight.svelte';

	interface Props {
		rows: readonly CatalogSearchRow[];
		/** The query, for the rows that search a branch on its own. */
		query: string;
		/** The accent of the area a row lies in. */
		tintOf: (area: string) => AreaTint;
		enrolled: ReadonlySet<number>;
		offline: boolean;
		onenrol: (course: CatalogCourse) => void;
		label: string;
	}

	let { rows, query, tintOf, enrolled, offline, onenrol, label }: Props = $props();

	const id = $props.id();
	/** Course hits whose other editions are listed, by course id. */
	const expanded = new SvelteSet<number>();

	function toggle(course: number) {
		if (expanded.has(course)) expanded.delete(course);
		else expanded.add(course);
	}

	/** Where a depth's rail runs: under the chip of the row it comes from. */
	const railAt = (depth: number) => `calc(${depth} * var(--indent) + var(--rail))`;
</script>

{#snippet trail(names: readonly string[])}
	{#if names.length > 0}
		<span class="trail">{names.join(' › ')}</span>
	{/if}
{/snippet}

<ul class="tree" aria-label={label}>
	{#each rows as row (row.id)}
		{@const depth = row.rails.length}
		{@const tint = tintOf(row.area)}
		<li
			class="row"
			aria-level={depth + 1}
			style:--depth={depth}
			style:--area-h={tint.hue}
			style:--area-c={tint.chroma}
		>
			{#each row.rails as running, j (j)}
				{#if j > 0 && running}
					<span class="rail" style:inset-inline-start={railAt(j - 1)} aria-hidden="true"></span>
				{/if}
			{/each}
			{#if depth > 0}
				<span
					class={['elbow', { last: row.last }]}
					style:inset-inline-start={railAt(depth - 1)}
					aria-hidden="true"
				></span>
			{/if}
			{#if row.kind === 'category'}
				<a class={['item', 'category', { match: row.match }]} href={catalogHref(row.key)}>
					<span class="chip" aria-hidden="true"><IconFolder /></span>
					<span class="texts">
						{@render trail(row.trail)}
						<span class="name"><Highlight text={row.name} ranges={row.nameRanges} /></span>
					</span>
					<IconCaretRight class="caret" aria-hidden="true" />
				</a>
			{:else if row.kind === 'more'}
				<a class="item more" href={catalogHref(row.key, query)}>
					<span class="chip" aria-hidden="true"><IconMagnifyingGlass /></span>
					<span class="texts">
						<span class="name">
							{m.elearning_results_more({ count: row.count, place: row.name })}
						</span>
					</span>
					<IconCaretRight class="caret" aria-hidden="true" />
				</a>
			{:else}
				{@const open = expanded.has(row.course.id)}
				<div class="item course">
					<span class="chip" aria-hidden="true"><IconBookOpen /></span>
					<span class="texts">
						{@render trail(row.trail)}
						<span class="name"><Highlight text={row.course.name} ranges={row.nameRanges} /></span>
						<span class="code" translate="no">
							{#if row.codeRanges.length > 0}
								<Highlight text={row.course.code} ranges={row.codeRanges} />
							{:else}
								{bareCode(row.course.code)}
							{/if}
						</span>
						{#if row.editions.length > 0}
							<button
								type="button"
								class="editions-toggle"
								aria-expanded={open}
								aria-controls="{id}-editions-{row.course.id}"
								onclick={() => toggle(row.course.id)}
							>
								{m.elearning_editions_more({ count: row.editions.length })}
								<IconCaretDown
									class={open ? 'toggle-caret open' : 'toggle-caret'}
									aria-hidden="true"
								/>
							</button>
						{/if}
					</span>
					<span class="enrol">
						<EnrolButton
							course={row.course}
							enrolled={enrolled.has(row.course.id)}
							{offline}
							{onenrol}
						/>
					</span>
				</div>
				{#if row.editions.length > 0}
					<ul
						class="editions"
						id="{id}-editions-{row.course.id}"
						aria-label={m.elearning_editions_of({ name: row.course.name })}
						hidden={!open}
					>
						{#each row.editions as edition (edition.course.id)}
							{@const period =
								edition.year === null ? m.elearning_period_none() : academicYearLabel(edition.year)}
							<li class="edition">
								<span class="period">{period}</span>
								<span class="code" translate="no">{edition.course.code}</span>
								<EnrolButton
									course={edition.course}
									enrolled={enrolled.has(edition.course.id)}
									{offline}
									{onenrol}
									label={m.elearning_enrol_edition({ name: edition.course.name, period })}
								/>
							</li>
						{/each}
					</ul>
				{/if}
			{/if}
		</li>
	{/each}
</ul>

<style>
	/*
	 * On phones the tree indents little and folds its deeper waypoints into the rows' trails, so a
	 * hit keeps most of the width; its rails run in the gutter between a row and its children.
	 */
	.tree {
		--indent: 14px;
		--rail: 7px;
		display: grid;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	@media (width >= 600px) {
		.tree {
			--indent: 32px;
			--rail: 22px;
		}
	}

	.row {
		position: relative;
		padding-block: 2px;
		padding-inline-start: calc(var(--depth) * var(--indent));
		content-visibility: auto;
		contain-intrinsic-size: auto 60px;
		container-type: inline-size;
	}

	.rail,
	.elbow {
		position: absolute;
		inset-block: 0;
		inline-size: 2px;
		margin-inline-start: -1px;
		background: var(--hairline-strong);
	}

	/* ├ runs on to the next sibling, └ turns into this row. */
	.elbow::after {
		content: '';
		position: absolute;
		inset-block-start: calc(50% - 1px);
		inset-inline-start: 0;
		inline-size: calc(var(--indent) - var(--rail));
		block-size: 2px;
		background: inherit;
	}

	.elbow.last {
		inset-block-end: 50%;
		border-end-start-radius: 6px;
	}

	.item {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: 10px;
		min-block-size: 52px;
		padding: 8px 10px 8px 8px;
		border-radius: var(--r-md);
		color: var(--text);
		text-decoration: none;
	}

	.category,
	.more {
		color: var(--text-muted);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.category.match,
	.course {
		background: var(--surface);
		color: var(--text);
		box-shadow: var(--inner-light);
	}

	.category.match .name {
		font-weight: 600;
	}

	.more .name {
		color: light-dark(
			oklch(42% var(--area-c) var(--area-h)),
			oklch(82% calc(var(--area-c) * 0.7) var(--area-h))
		);
		font-weight: 550;
	}

	@media (hover: hover) {
		.category:hover,
		.more:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.category:focus-visible,
	.more:focus-visible {
		outline-offset: -2px;
	}

	.chip {
		display: grid;
		place-items: center;
		inline-size: 32px;
		block-size: 32px;
		border-radius: 10px;
		background: light-dark(
			oklch(93% calc(var(--area-c) * 0.4) var(--area-h)),
			oklch(30% calc(var(--area-c) * 0.45) var(--area-h))
		);
		color: light-dark(
			oklch(44% var(--area-c) var(--area-h)),
			oklch(82% calc(var(--area-c) * 0.7) var(--area-h))
		);
	}

	.category:not(.match) .chip,
	.more .chip {
		background: light-dark(
			oklch(95.5% calc(var(--area-c) * 0.25) var(--area-h)),
			oklch(26% calc(var(--area-c) * 0.3) var(--area-h))
		);
	}

	.chip :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.texts {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.trail {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		overflow-wrap: anywhere;
	}

	.name {
		font: var(--type-ui);
		text-wrap: pretty;
		overflow-wrap: anywhere;
		hyphens: auto;
	}

	.code {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.03em;
		overflow-wrap: anywhere;
	}

	.item :global(.caret) {
		inline-size: 16px;
		block-size: 16px;
		color: var(--text-subtle);
	}

	.editions-toggle {
		display: inline-flex;
		align-items: center;
		justify-self: start;
		gap: 4px;
		margin: 2px -6px -2px;
		padding: 2px 6px;
		border-radius: var(--r-xs);
		color: light-dark(
			oklch(42% var(--area-c) var(--area-h)),
			oklch(82% calc(var(--area-c) * 0.7) var(--area-h))
		);
		font: var(--type-label);
		cursor: pointer;
		touch-action: manipulation;
	}

	@media (hover: hover) {
		.editions-toggle:hover {
			background: var(--state-hover);
		}
	}

	.editions-toggle :global(.toggle-caret) {
		inline-size: 12px;
		block-size: 12px;
		transition: transform var(--dur-base) var(--ease-out);
	}

	.editions-toggle :global(.toggle-caret.open) {
		transform: rotate(180deg);
	}

	.editions {
		display: grid;
		gap: 2px;
		margin: 2px 0 0;
		padding: 0 0 0 calc(32px + 18px);
		list-style: none;
	}

	.editions[hidden] {
		display: none;
	}

	.edition {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: 10px;
		min-block-size: 48px;
		padding: 5px 10px 5px 12px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
	}

	.period {
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
	}

	/* A row too narrow for the knob beside its text puts it under. */
	@container (width < 260px) {
		.course {
			grid-template-columns: auto minmax(0, 1fr);
		}

		.enrol {
			grid-column: 2;
		}

		.editions {
			padding-inline-start: 0;
		}
	}

	.row :global(mark) {
		--mark: light-dark(
			oklch(86% calc(var(--area-c) * 0.6 + 0.04) var(--area-h)),
			oklch(42% calc(var(--area-c) * 0.6 + 0.04) var(--area-h))
		);
	}
</style>
