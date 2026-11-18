<script lang="ts" module>
	import type { Snippet } from 'svelte';
	import type { ChipTone, Href, Icon } from './types.ts';

	export interface DataColumn<T> {
		/** Unique within the table. */
		id: string;
		/** The column's heading. */
		label: string;
		/** The cell as text; also what the narrow rows show. */
		value: (row: T) => string;
		/** Draws the table's cell instead of the text (a status chip, a struck amount). */
		cell?: Snippet<[T]>;
		/** A smaller line under the value, in both forms (an instalment, a credit date). */
		sub?: (row: T) => string | undefined;
		/** end for amounts and counts. */
		align?: 'start' | 'end';
		/** Codes and identifiers: tabular figures, slashed zero, never translated. */
		code?: boolean;
		/** Long text that may wrap in the table; other cells keep to one line. */
		wrap?: boolean;
		/**
		 * Its place in a narrow row: the title (the first column by default), the value at the
		 * end of the first line, part of the second line (default), or left out.
		 */
		narrow?: 'title' | 'meta' | 'line' | 'hide';
	}

	export interface DataGroup<T> {
		id: string | number;
		title: string;
		/** Beside the title: a total, a count. */
		aside?: string;
		rows: readonly T[];
	}
</script>

<script lang="ts" generics="T">
	import ListGroup from './ListGroup.svelte';
	import ListRow from './ListRow.svelte';
	import { internalHref } from './types.ts';

	interface Props {
		columns: readonly DataColumn<T>[];
		/** The rows, or `groups` of them under a heading each (one tbody per group). */
		rows?: readonly T[];
		groups?: readonly DataGroup<T>[];
		key: (row: T) => string | number;
		/** Each row opens this link: the title cell holds it, and the whole row follows it. */
		href?: (row: T) => Href;
		/** The open row of a list beside its detail. */
		selected?: (row: T) => boolean;
		/** Before the title in both forms: a year tile, an avatar. */
		leading?: Snippet<[T]>;
		/** The narrow rows' icon chip, when there is no `leading`. */
		icon?: (row: T) => { icon: Icon; tone?: ChipTone } | undefined;
		/** The narrow rows' second line; by default the `line` columns' values, joined. */
		description?: (row: T) => string | undefined;
		/** Accessible name, or the id of the heading that names the table. */
		label?: string;
		labelledby?: string;
	}

	let {
		columns,
		rows,
		groups,
		key,
		href,
		selected,
		leading: lead,
		icon,
		description,
		label,
		labelledby
	}: Props = $props();

	const titleColumn = $derived(columns.find((c) => c.narrow === 'title') ?? columns[0]);
	const metaColumn = $derived(columns.find((c) => c.narrow === 'meta'));
	const lineColumns = $derived(
		columns.filter((c) => c !== titleColumn && c !== metaColumn && (c.narrow ?? 'line') === 'line')
	);
	const sections = $derived<readonly DataGroup<T>[]>(
		groups ?? [{ id: 'all', title: '', rows: rows ?? [] }]
	);

	function line(row: T): string | undefined {
		if (description) return description(row);
		const parts = [titleColumn.sub?.(row), ...lineColumns.map((c) => c.value(row))];
		return parts.filter((part) => part && part !== '—').join(' · ') || undefined;
	}
</script>

<!-- Both forms render; the container shows the table from 720 px and the rows below. -->
<div class="data-table">
	{#snippet content(column: DataColumn<T>, row: T)}
		{#if column.cell}{@render column.cell(row)}{:else}{column.value(row)}{/if}
		{#if column.sub?.(row)}<span class="sub">{column.sub(row)}</span>{/if}
	{/snippet}

	<table class="table" aria-label={label} aria-labelledby={labelledby}>
		<thead>
			<tr>
				{#each columns as column (column.id)}
					<th scope="col" class={{ end: column.align === 'end' }}>{column.label}</th>
				{/each}
			</tr>
		</thead>
		{#each sections as section (section.id)}
			<tbody>
				{#if groups}
					<tr class="group">
						<th scope="rowgroup" colspan={columns.length}>
							<span class="group-title">{section.title}</span>
							{#if section.aside}<span class="aside">{section.aside}</span>{/if}
						</th>
					</tr>
				{/if}
				{#each section.rows as row (key(row))}
					{@const open = selected?.(row) ?? false}
					{@const link = href?.(row)}
					<tr class={['row', { open, linked: !!link }]}>
						{#each columns as column (column.id)}
							{@const classes = {
								end: column.align === 'end',
								code: column.code,
								wrap: column.wrap
							}}
							{#if column === titleColumn}
								<th scope="row" class={['title', classes]}>
									<svelte:element
										this={link ? 'a' : 'span'}
										class="name"
										href={link}
										aria-current={link && open ? 'page' : undefined}
										rel={link && !internalHref(link) ? 'external noopener noreferrer' : undefined}
										target={link && !internalHref(link) ? '_blank' : undefined}
									>
										{#if lead}{@render lead(row)}{/if}
										<span class="name-text">{@render content(column, row)}</span>
									</svelte:element>
								</th>
							{:else}
								<td class={classes} translate={column.code ? 'no' : undefined}>
									{@render content(column, row)}
								</td>
							{/if}
						{/each}
					</tr>
				{/each}
			</tbody>
		{/each}
	</table>

	<div class="rows">
		{#each sections as section (section.id)}
			<ListGroup
				title={groups ? section.title : undefined}
				label={groups ? undefined : label}
				level={3}
			>
				{#snippet actions()}
					{#if section.aside}<span class="aside">{section.aside}</span>{/if}
				{/snippet}
				{#each section.rows as row (key(row))}
					{@const common = {
						title: titleColumn.value(row),
						description: line(row),
						meta: metaColumn?.value(row),
						href: href?.(row),
						selected: selected?.(row) ?? false
					}}
					{#if lead}
						<ListRow {...common}>
							{#snippet leading()}{@render lead(row)}{/snippet}
						</ListRow>
					{:else}
						{@const chip = icon?.(row)}
						<ListRow {...common} icon={chip?.icon} iconTone={chip?.tone} />
					{/if}
				{/each}
			</ListGroup>
		{/each}
	</div>
</div>

<style>
	.data-table {
		container: data-table / inline-size;
	}

	.table {
		display: none;
		inline-size: 100%;
		border-collapse: separate;
		border-spacing: 0;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		font: var(--type-ui);
		font-weight: 400;
		font-variant-numeric: tabular-nums;
		overflow: hidden;
	}

	.rows {
		display: grid;
		gap: var(--sp-5);
	}

	@container data-table (width >= 720px) {
		.table {
			display: table;
		}

		.rows {
			display: none;
		}
	}

	th,
	td {
		padding: var(--sp-3) var(--sp-4);
		text-align: start;
		vertical-align: middle;
	}

	td {
		white-space: nowrap;
	}

	.end {
		text-align: end;
	}

	.wrap {
		white-space: normal;
		text-wrap: pretty;
	}

	.code {
		color: var(--text-muted);
		font-variant-numeric: tabular-nums slashed-zero;
	}

	thead th {
		padding-block: var(--sp-3) var(--sp-2);
		border-block-end: 1px solid var(--hairline);
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		white-space: nowrap;
	}

	.group th {
		padding-block: var(--sp-4) var(--sp-2);
		font: var(--type-ui-strong);
	}

	tbody + tbody .group th {
		border-block-start: 1px solid var(--hairline);
	}

	.group-title {
		margin-inline-end: var(--sp-3);
	}

	.aside {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.row {
		position: relative;
	}

	.row + .row > * {
		border-block-start: 1px solid var(--hairline);
	}

	.title {
		font-weight: 500;
	}

	.name {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		color: inherit;
		text-decoration: none;
	}

	.name-text {
		min-inline-size: 0;
	}

	/* The whole row follows the title's link; the row shows its focus. */
	a.name::after {
		content: '';
		position: absolute;
		inset: 0;
	}

	a.name:focus-visible {
		outline: none;
	}

	.row:has(a.name:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.linked {
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.linked:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.row.open {
		background: var(--container);
		color: var(--on-container);
	}

	.row.open .code,
	.row.open .sub {
		color: color-mix(in oklch, var(--on-container) 80%, transparent);
	}

	.sub {
		display: block;
		color: var(--text-muted);
		font: var(--type-meta);
		font-weight: 400;
	}
</style>
