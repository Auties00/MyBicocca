<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import {
		deadlineLabel,
		deadlineTone,
		invoiceHref,
		invoiceTitle,
		statusLabels,
		statusTones,
		yearLabel
	} from '../format.ts';
	import type { Invoice } from '../invoices.ts';
	import Paper from './Paper.svelte';

	interface Props {
		invoice: Invoice;
		today: Date;
		/** Open beside the list. */
		selected?: boolean;
	}

	let { invoice, today, selected = false }: Props = $props();

	const title = $derived(invoiceTitle(invoice));
	const installment = $derived(invoice.items[0]?.installment ?? null);
	const overdue = $derived(invoice.status === 'expired');
	const tone = $derived(invoice.expires ? deadlineTone(invoice.expires, today) : 'neutral');
</script>

<!-- An owed invoice reads as a slip to pay: what, by when, how much. -->
<a
	class={['ticket', { selected }]}
	href={invoiceHref(invoice.id)}
	aria-current={selected ? 'page' : undefined}
>
	<Paper ring={selected ? 'accent' : overdue ? 'danger' : 'default'}>
		<div class="body">
			<div class="top">
				<span class="number" translate="no">{m.payments_invoice_number({ id: invoice.id })}</span>
				<StatusChip
					size="sm"
					status={statusTones[invoice.status]}
					label={statusLabels[invoice.status]()}
				/>
			</div>
			<p class="title">{title}</p>
			<p class="meta">
				{[installment, invoice.academicYear === null ? null : yearLabel(invoice.academicYear)]
					.filter(Boolean)
					.join(' · ')}
			</p>
			<div class="rule" aria-hidden="true"></div>
			<div class="bottom">
				<div class="when">
					<span class="label">{m.payments_col_due()}</span>
					<span class="date">{invoice.expires ? formatDate(invoice.expires, 'medium') : '—'}</span>
					{#if invoice.expires}
						<span class={['countdown', tone]}>{deadlineLabel(invoice.expires, today)}</span>
					{/if}
				</div>
				<span class="amount">{formatCurrency(invoice.amount)}</span>
			</div>
		</div>
	</Paper>
</a>

<style>
	.ticket {
		display: block;
		color: inherit;
		text-decoration: none;
		border-radius: var(--r-sm);
		-webkit-tap-highlight-color: transparent;
		touch-action: manipulation;
		transition: transform var(--dur-instant) var(--ease-out);
	}

	.ticket:active {
		transform: scale(0.985);
	}

	.body {
		display: grid;
		gap: var(--sp-1);
		padding: var(--sp-4) var(--sp-5);
	}

	@media (hover: hover) {
		.ticket:hover {
			--paper-fill: var(--surface-hover);
		}
	}

	.top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-3);
	}

	.number {
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.02em;
	}

	.title {
		margin-block-start: var(--sp-2);
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		overflow-wrap: anywhere;
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.meta:empty {
		display: none;
	}

	.rule {
		margin-block: var(--sp-3) var(--sp-2);
		border-block-start: 2px dashed var(--hairline);
	}

	.bottom {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: var(--sp-4);
	}

	.when {
		display: grid;
		gap: 1px;
		min-inline-size: 0;
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.date {
		font: var(--type-ui-strong);
	}

	.countdown {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.countdown.warning {
		color: var(--warning);
	}

	.countdown.danger {
		color: var(--danger);
	}

	.amount {
		flex: none;
		font: var(--type-numeral-l);
		font-size: 2rem;
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
		white-space: nowrap;
	}

	.ticket:focus-visible {
		outline-offset: 2px;
	}

	@media (prefers-reduced-motion: reduce) {
		.ticket:active {
			transform: none;
		}
	}
</style>
