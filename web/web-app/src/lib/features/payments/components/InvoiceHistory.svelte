<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconProhibit from '~icons/ph/prohibit';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import DataTable, { type DataColumn, type DataGroup } from '#lib/ui/DataTable.svelte';
	import { invoiceHref, invoiceTitle, settledLabel, yearLabel } from '../format.ts';
	import type { Invoice, InvoiceYear } from '../invoices.ts';

	interface Props {
		years: readonly InvoiceYear[];
		/** The invoice open beside the list. */
		selected?: string;
		/** Accessible name of the table: the section's heading. */
		labelledby: string;
	}

	let { years, selected, labelledby }: Props = $props();

	const installment = (invoice: Invoice) => invoice.items[0]?.installment ?? undefined;
	const canceled = (invoice: Invoice) => invoice.status === 'canceled';
	const paid = (invoice: Invoice) =>
		canceled(invoice)
			? m.payments_status_canceled()
			: invoice.paidOn
				? formatDate(invoice.paidOn, 'medium')
				: m.payments_status_paid();

	const groups = $derived(
		years.map((group): DataGroup<Invoice> => ({
			id: group.year ?? 'unknown',
			title: yearLabel(group.year),
			aside:
				group.paid > 0 ? m.payments_year_paid({ amount: formatCurrency(group.paid) }) : undefined,
			rows: group.invoices
		}))
	);

	const columns: DataColumn<Invoice>[] = [
		{
			id: 'what',
			label: m.payments_col_description(),
			value: invoiceTitle,
			sub: installment,
			wrap: true
		},
		{
			id: 'id',
			label: m.payments_col_invoice(),
			value: (invoice) => String(invoice.id),
			code: true,
			narrow: 'hide'
		},
		{
			id: 'due',
			label: m.payments_col_due(),
			value: (invoice) => (invoice.expires ? formatDate(invoice.expires, 'medium') : '—'),
			narrow: 'hide'
		},
		{
			id: 'paid',
			label: m.payments_col_paid(),
			value: paid,
			cell: settled,
			narrow: 'hide'
		},
		{
			id: 'amount',
			label: m.payments_col_amount(),
			value: (invoice) => formatCurrency(invoice.amount),
			cell: amount,
			align: 'end',
			narrow: 'meta'
		}
	];
</script>

{#snippet settled(invoice: Invoice)}
	<span class={['settled', { canceled: canceled(invoice) }]}>
		{#if canceled(invoice)}<IconProhibit aria-hidden="true" />{:else}<IconCheckCircle
				aria-hidden="true"
			/>{/if}
		{paid(invoice)}
	</span>
{/snippet}

{#snippet amount(invoice: Invoice)}
	<span class={['amount', { canceled: canceled(invoice) }]}>{formatCurrency(invoice.amount)}</span>
{/snippet}

<DataTable
	{labelledby}
	{groups}
	{columns}
	key={(invoice) => invoice.id}
	href={(invoice) => invoiceHref(invoice.id)}
	selected={(invoice) => String(invoice.id) === selected}
	icon={(invoice) => ({
		icon: canceled(invoice) ? IconProhibit : IconCheckCircle,
		tone: canceled(invoice) ? 'neutral' : 'success'
	})}
	description={(invoice) =>
		[installment(invoice), settledLabel(invoice)].filter(Boolean).join(' · ')}
/>

<style>
	/* Icons are blocks (base.css): a flex row keeps the mark on the date's line. */
	.settled {
		display: inline-flex;
		align-items: baseline;
		gap: 6px;
	}

	.settled :global(svg) {
		flex: none;
		align-self: center;
		inline-size: 16px;
		block-size: 16px;
		color: var(--success);
	}

	.settled.canceled,
	.settled.canceled :global(svg) {
		color: var(--text-muted);
	}

	.amount {
		font-weight: 600;
	}

	.amount.canceled {
		color: var(--text-muted);
		text-decoration: line-through;
	}
</style>
