<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle';
	import IconHourglass from '~icons/ph/hourglass-medium';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import DataTable, { type DataColumn } from '#lib/ui/DataTable.svelte';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { reasonName, refundHref, yearLabel } from '../format.ts';
	import type { Refund } from '../refunds.ts';
	import type { Refunds } from '../stores.svelte.ts';

	interface Props {
		refunds: Refunds;
		beside: boolean;
		selected?: string;
	}

	let { refunds, beside, selected }: Props = $props();

	const list = $derived(refunds.list ?? []);
	const processingTotal = $derived(
		refunds.processing.reduce((sum, refund) => sum + (refund.amount ?? 0), 0)
	);

	const status = (refund: Refund) =>
		refund.refunded ? m.payments_refund_refunded() : m.payments_refund_processing();
	const line = (refund: Refund) =>
		[
			status(refund),
			refund.academicYear === null
				? null
				: m.payments_year_short({ year: yearLabel(refund.academicYear) })
		]
			.filter(Boolean)
			.join(' · ');
	const money = (refund: Refund) => (refund.amount === null ? '—' : formatCurrency(refund.amount));

	const columns: DataColumn<Refund>[] = [
		{
			id: 'what',
			label: m.payments_col_description(),
			value: (refund) => refund.description ?? m.payments_refund_fallback(),
			sub: (refund) =>
				refund.creditedOn
					? m.payments_refund_credited_on({
							date: formatDate(refund.creditedOn, 'medium'),
							day: refund.creditedOn.getDate()
						})
					: undefined,
			wrap: true
		},
		{
			id: 'year',
			label: m.payments_col_year(),
			value: (refund) => yearLabel(refund.academicYear),
			narrow: 'hide'
		},
		{
			id: 'reason',
			label: m.payments_refund_reason(),
			value: (refund) =>
				refund.reasonCode ? (reasonName(refund.reasonCode) ?? refund.reasonCode) : '—',
			cell: reason,
			wrap: true,
			narrow: 'hide'
		},
		{
			id: 'status',
			label: m.payments_col_status(),
			value: status,
			cell: state,
			narrow: 'hide'
		},
		{
			id: 'amount',
			label: m.payments_col_amount(),
			value: money,
			cell: amount,
			align: 'end',
			narrow: 'meta'
		}
	];
</script>

<div class="refunds">
	{#if refunds.total > 0 || processingTotal > 0}
		<div class={['summary', { beside }]}>
			{#if refunds.total > 0}
				<div class="stat">
					<span class="label">{m.payments_refunds_total()}</span>
					<span class="total">{formatCurrency(refunds.total)}</span>
				</div>
			{/if}
			{#if processingTotal > 0}
				<!-- With nothing refunded yet, what is on its way leads. -->
				<div class="stat">
					<span class="label">{m.payments_refunds_processing_total()}</span>
					<span class={refunds.total > 0 ? 'value' : 'total'}
						>{formatCurrency(processingTotal)}</span
					>
				</div>
			{/if}
		</div>
	{/if}

	<DataTable
		label={m.payments_refunds_list_label()}
		rows={list}
		{columns}
		key={(refund) => refund.key}
		href={(refund) => refundHref(refund.key)}
		selected={(refund) => refund.key === selected}
		icon={(refund) => ({
			icon: refund.refunded ? IconCheckCircle : IconHourglass,
			tone: refund.refunded ? 'success' : 'neutral'
		})}
		description={line}
	/>
</div>

{#snippet reason(refund: Refund)}
	{#if refund.reasonCode}
		{@const name = reasonName(refund.reasonCode)}
		{#if name}
			{name}
			<span class="sub code" translate="no">{refund.reasonCode}</span>
		{:else}
			<span class="code" translate="no">{refund.reasonCode}</span>
		{/if}
	{:else}
		—
	{/if}
{/snippet}

{#snippet state(refund: Refund)}
	<StatusChip size="sm" status={refund.refunded ? 'success' : 'pending'} label={status(refund)} />
{/snippet}

{#snippet amount(refund: Refund)}
	<span class="amount">{money(refund)}</span>
{/snippet}

<style>
	.refunds {
		display: grid;
		gap: var(--sp-5);
	}

	.summary {
		display: flex;
		flex-wrap: wrap;
		align-items: flex-end;
		gap: var(--sp-4) var(--sp-8);
		padding: var(--sp-5) var(--sp-6);
		border-radius: var(--r-xl);
		background: var(--container);
		color: var(--on-container);
	}

	.summary.beside {
		padding: var(--sp-4) var(--sp-5);
		border-radius: var(--r-lg);
	}

	/* Where the table spreads out, the card keeps to its figures rather than a band of colour. */
	@container payments (width >= 560px) {
		.summary:not(.beside) {
			justify-self: start;
			min-inline-size: 320px;
		}
	}

	.stat {
		display: grid;
		gap: var(--sp-1);
	}

	.label {
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		opacity: 0.86;
	}

	.total {
		font: 600 2.25rem / 1 var(--font-ui);
		letter-spacing: -0.02em;
		font-variant-numeric: lining-nums tabular-nums;
	}

	.beside .total {
		font-size: 2rem;
	}

	.value {
		font: var(--type-title-m);
		font-variant-numeric: tabular-nums;
	}

	.amount {
		font-weight: 600;
	}

	.code {
		color: var(--text-muted);
		font-variant-numeric: tabular-nums slashed-zero;
	}

	.sub {
		display: block;
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
