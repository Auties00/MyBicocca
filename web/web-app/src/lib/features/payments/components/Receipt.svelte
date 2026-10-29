<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { usePaymentsView } from '../context.ts';
	import {
		deadlineTone,
		invoiceTitle,
		paidOnLabel,
		relativeDeadline,
		statusLabels,
		statusTones,
		yearLabel
	} from '../format.ts';
	import { isPayable, type Invoice } from '../invoices.ts';
	import Paper from './Paper.svelte';

	interface Props {
		invoice: Invoice;
		today: Date;
	}

	let { invoice, today }: Props = $props();

	const view = usePaymentsView();

	const payable = $derived(isPayable(invoice.status));
	const items = $derived(
		invoice.items.length > 0
			? invoice.items
			: [
					{
						description: invoiceTitle(invoice),
						amount: invoice.amount,
						installment: null,
						expires: null
					}
				]
	);
	const sameDay = (a: Date | null, b: Date | null) => a?.getTime() === b?.getTime();

	const when = $derived.by(() => {
		if (invoice.status === 'paid' && invoice.paidOn) return paidOnLabel(invoice.paidOn, 'long');
		if (!payable || !invoice.expires) return undefined;
		const by = m.payments_due_on({
			date: formatDate(invoice.expires, 'long'),
			day: invoice.expires.getDate()
		});
		return `${by} · ${relativeDeadline(invoice.expires, today)}`;
	});
	const whenTone = $derived(
		payable && invoice.expires ? deadlineTone(invoice.expires, today) : 'neutral'
	);
</script>

<Paper tooth={16} ring={invoice.status === 'expired' ? 'danger' : 'default'}>
	<article class="receipt" aria-label={m.payments_receipt_label({ id: invoice.id })}>
		<header class="head">
			<div class="issuer">
				<span class="issuer-name">{m.payments_receipt_issuer()}</span>
				<span class="number" translate="no">
					{[
						m.payments_invoice_number({ id: invoice.id }),
						invoice.academicYear === null
							? null
							: m.payments_year_short({ year: yearLabel(invoice.academicYear) })
					]
						.filter(Boolean)
						.join(' · ')}
				</span>
			</div>
			<span class={['stamp', statusTones[invoice.status]]}>{statusLabels[invoice.status]()}</span>
		</header>

		<div class={['hero', { canceled: invoice.status === 'canceled' }]}>
			<span class="total">{formatCurrency(invoice.amount)}</span>
			{#if when}
				<span class={['when', whenTone]}>{when}</span>
			{/if}
		</div>

		<div class="rule" aria-hidden="true"></div>

		<section class="items" aria-labelledby="receipt-items-{invoice.id}">
			<svelte:element this={view.level} class="label" id="receipt-items-{invoice.id}"
				>{m.payments_receipt_items()}</svelte:element
			>
			<ul role="list">
				{#each items as item, index (index)}
					<li class="item">
						<span class="desc">{item.description}</span>
						<span class="amount">{formatCurrency(item.amount)}</span>
						{#if item.installment || (item.expires && !sameDay(item.expires, invoice.expires))}
							<span class="sub">
								{[
									item.installment,
									item.expires && !sameDay(item.expires, invoice.expires)
										? m.payments_item_due({ date: formatDate(item.expires, 'medium') })
										: null
								]
									.filter(Boolean)
									.join(' · ')}
							</span>
						{/if}
					</li>
				{/each}
			</ul>
			{#if items.length > 1}
				<div class="sum">
					<span>{m.payments_total()}</span>
					<span class="amount">{formatCurrency(invoice.amount)}</span>
				</div>
			{/if}
		</section>
	</article>
</Paper>

<style>
	.receipt {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5) var(--sp-5) var(--sp-6);
		container-type: inline-size;
	}

	.head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: var(--sp-4);
	}

	.issuer {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.issuer-name {
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		text-wrap: balance;
	}

	.number {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums slashed-zero;
	}

	/* A rubber stamp: the status, never by colour alone. */
	.stamp {
		flex: none;
		margin-block-start: 2px;
		padding: 3px 10px;
		border: 2px solid currentColor;
		border-radius: var(--r-xs);
		font: var(--type-overline);
		font-size: 0.8125rem;
		font-weight: 700;
		letter-spacing: 0.1em;
		text-transform: uppercase;
		white-space: nowrap;
		transform: rotate(-5deg);
		opacity: 0.92;
	}

	/* The tones of the status chips in the list, so a status reads the same on both screens. */
	.stamp.warning {
		color: var(--warning);
	}

	.stamp.danger {
		color: var(--danger);
	}

	.stamp.success {
		color: var(--success);
	}

	.stamp.neutral {
		color: var(--text-muted);
	}

	.hero {
		display: grid;
		gap: var(--sp-1);
		padding-block: var(--sp-2) 0;
	}

	.total {
		font: var(--type-numeral-xl);
		font-size: clamp(2.5rem, 2rem + 4cqi, 3.5rem);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.canceled .total {
		color: var(--text-muted);
	}

	.when {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.when.warning {
		color: var(--warning);
	}

	.when.danger {
		color: var(--danger);
	}

	.rule {
		border-block-start: 2px dashed var(--hairline);
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.items {
		display: grid;
		gap: var(--sp-3);
	}

	.items ul {
		display: grid;
		gap: var(--sp-3);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.item {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		gap: 2px var(--sp-4);
	}

	.desc {
		font: var(--type-body-strong);
		overflow-wrap: anywhere;
	}

	.amount {
		font: var(--type-body-strong);
		font-variant-numeric: tabular-nums;
		text-align: end;
		white-space: nowrap;
	}

	.sub {
		grid-column: 1 / -1;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.sum {
		display: flex;
		justify-content: space-between;
		gap: var(--sp-4);
		padding-block-start: var(--sp-3);
		border-block-start: 1px solid var(--hairline);
		font: var(--type-body-strong);
	}

	@container payments (width >= 480px) {
		.receipt {
			padding: var(--sp-6) var(--sp-7) var(--sp-7);
		}
	}
</style>
