<script lang="ts">
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import DataTable, { type DataColumn } from '#lib/ui/DataTable.svelte';
	import { shortYear } from '../esse3.ts';
	import { iseeHref, yearLabel } from '../format.ts';
	import type { IseeYear } from '../isee.ts';
	import type { Isee } from '../stores.svelte.ts';
	import DeclareCard from './DeclareCard.svelte';

	interface Props {
		isee: Isee;
		beside: boolean;
		selected?: string;
	}

	let { isee, beside, selected }: Props = $props();

	const years = $derived(isee.list ?? []);
	const open = (year: IseeYear) => String(year.year) === selected;

	const columns: DataColumn<IseeYear>[] = [
		{
			id: 'year',
			label: m.payments_col_year(),
			value: (year) => m.payments_academic_year({ year: yearLabel(year.year) })
		},
		{
			id: 'isee',
			label: m.payments_isee_indicator(),
			value: (year) => formatCurrency(year.isee),
			cell: indicator,
			align: 'end',
			narrow: 'meta'
		},
		{
			id: 'threshold',
			label: m.payments_isee_threshold(),
			value: (year) => (year.threshold === null ? '—' : formatCurrency(year.threshold)),
			align: 'end',
			narrow: 'hide'
		},
		{
			id: 'exemption',
			label: m.payments_isee_exemption(),
			value: (year) => year.exemption ?? '—',
			wrap: true
		}
	];
</script>

{#snippet tile(year: IseeYear)}
	<span class={['tile', { open: open(year) }]} aria-hidden="true">{shortYear(year.year)}</span>
{/snippet}

{#snippet indicator(year: IseeYear)}
	<span class="strong">{formatCurrency(year.isee)}</span>
{/snippet}

<div class="isee">
	{#if isee.declarable !== null}
		<DeclareCard year={isee.declarable} compact={beside} />
	{/if}

	{#if years.length > 0}
		<DataTable
			label={m.payments_isee_list_label()}
			rows={years}
			{columns}
			key={(year) => year.year}
			href={(year) => iseeHref(year.year)}
			selected={open}
			leading={tile}
			description={(year) => year.exemption ?? year.course ?? undefined}
		/>
	{/if}
</div>

<style>
	.isee {
		display: grid;
		gap: var(--sp-5);
	}

	.tile {
		display: inline-grid;
		place-items: center;
		flex: none;
		inline-size: 44px;
		block-size: 44px;
		border-radius: var(--r-sm);
		background: var(--container);
		color: var(--on-container);
		font: var(--type-label);
		font-weight: 650;
		font-variant-numeric: tabular-nums;
	}

	/* The open row takes the tile's fill: the tile turns to the accent so it still reads. */
	.tile.open {
		background: var(--accent);
		color: var(--on-accent);
	}

	.strong {
		font-weight: 600;
	}
</style>
