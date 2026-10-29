<script lang="ts">
	import { formatDate } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { usePaymentsView } from '../context.ts';
	import type { Invoice } from '../invoices.ts';
	import CopyCode from './CopyCode.svelte';

	interface Props {
		invoice: Invoice;
	}

	let { invoice }: Props = $props();

	const view = usePaymentsView();

	/** A code a bank, a tobacconist or a banking app asks for: shown with a copy button. */
	type InvoiceFact = Fact & { copy?: boolean };

	/* The due date, the amount and the invoice's number are the receipt's: these are what it leaves out. */
	const facts: InvoiceFact[] = $derived.by(() => {
		const rows: Array<InvoiceFact | null> = [
			invoice.noticeCode
				? { label: m.payments_fact_notice_code(), value: invoice.noticeCode, copy: true }
				: null,
			invoice.iuv ? { label: m.payments_fact_iuv(), value: invoice.iuv, copy: true } : null,
			invoice.issued
				? { label: m.payments_fact_issued(), value: formatDate(invoice.issued, 'long') }
				: null,
			invoice.paidOn
				? { label: m.payments_fact_paid(), value: formatDate(invoice.paidOn, 'long') }
				: null
		];
		return rows.filter((fact) => fact !== null);
	});

	const headingId = $props.id();
</script>

{#if facts.length > 0}
	<section class="details" aria-labelledby={headingId}>
		<svelte:element this={view.level} class="label" id={headingId}
			>{m.payments_details()}</svelte:element
		>
		<Facts items={facts}>
			{#snippet value(fact)}
				{#if fact.copy}<CopyCode value={fact.value} label={fact.label} />{:else}{fact.value}{/if}
			{/snippet}
		</Facts>
	</section>
{/if}

<style>
	.details {
		display: grid;
		gap: var(--sp-3);
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}
</style>
