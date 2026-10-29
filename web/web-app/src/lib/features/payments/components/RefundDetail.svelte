<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Facts, { type Fact } from '#lib/ui/Facts.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import StatusChip from '#lib/ui/StatusChip.svelte';
	import { usePaymentsView, useRefunds } from '../context.ts';
	import { refundReason } from '../format.ts';
	import DetailSkeleton from './DetailSkeleton.svelte';
	import Missing from './Missing.svelte';

	interface Props {
		id: string;
	}

	let { id }: Props = $props();

	const view = usePaymentsView();

	const refunds = useRefunds();
	const refund = $derived(refunds.find(id));
	const gate = new LoadingGate(() => refunds.pending);

	/* Issued, processed, paid out, credited: each step done once it has a date. */
	const steps = $derived.by(() => {
		if (refund === undefined) return [];
		return [
			{ label: m.payments_refund_step_issued(), date: refund.issued },
			{ label: m.payments_refund_step_processed(), date: refund.processed },
			{ label: m.payments_refund_step_paid(), date: refund.paidOn },
			{ label: m.payments_refund_step_credited(), date: refund.creditedOn }
		];
	});
	const reached = $derived(steps.findLastIndex((step) => step.date !== null));

	const facts: Fact[] = $derived.by(() => {
		if (refund === undefined) return [];
		const rows: Array<Fact | null> = [
			refund.reasonCode === null
				? null
				: { label: m.payments_refund_reason(), value: refundReason(refund.reasonCode) },
			refund.mandateNumber === null
				? null
				: { label: m.payments_refund_mandate(), value: refund.mandateNumber, code: true },
			refund.collectedBy === null
				? null
				: { label: m.payments_refund_method(), value: refund.collectedBy },
			refund.invoiceId === null
				? null
				: { label: m.payments_refund_invoice(), value: String(refund.invoiceId), code: true },
			refund.note === null ? null : { label: m.payments_refund_note(), value: refund.note }
		];
		return rows.filter((fact) => fact !== null);
	});
</script>

{#if refund}
	<div class="detail">
		<section class="hero" aria-labelledby="refund-amount-{refund.key}">
			<svelte:element this={view.level} class="label" id="refund-amount-{refund.key}"
				>{m.payments_refund_amount()}</svelte:element
			>
			<p class="value">{refund.amount === null ? '—' : formatCurrency(refund.amount)}</p>
			<StatusChip
				status={refund.refunded ? 'success' : 'pending'}
				label={refund.refunded ? m.payments_refund_refunded() : m.payments_refund_processing()}
			/>
		</section>

		<section class="progress" aria-labelledby="refund-progress-{refund.key}">
			<svelte:element this={view.level} class="label muted" id="refund-progress-{refund.key}"
				>{m.payments_refund_progress()}</svelte:element
			>
			<ol class="steps">
				{#each steps as step, index (step.label)}
					{@const done = step.date !== null}
					<li class={['step', { done, last: index === reached }]}>
						<span class="dot" aria-hidden="true">
							{#if done}<IconCheck />{/if}
						</span>
						<span class="step-text">
							<span class="step-label">{step.label}</span>
							<span class="step-date">
								{step.date ? formatDate(step.date, 'medium') : m.payments_refund_step_waiting()}
							</span>
						</span>
					</li>
				{/each}
			</ol>
		</section>

		{#if facts.length > 0}
			<section class="facts" aria-labelledby="refund-details-{refund.key}">
				<svelte:element this={view.level} class="label muted" id="refund-details-{refund.key}"
					>{m.payments_refund_details()}</svelte:element
				>
				<Facts items={facts} />
			</section>
		{/if}
	</div>
{:else if refunds.loaded}
	<Missing
		title={m.payments_refund_missing_title()}
		body={m.payments_refund_missing_body()}
		back={{ href: shellHref('services/refunds'), label: m.payments_back_to_refunds() }}
	/>
{:else if gate.visible}
	<DetailSkeleton />
{/if}

<style>
	.detail {
		display: grid;
		gap: var(--sp-5);
	}

	.hero {
		display: grid;
		justify-items: start;
		gap: var(--sp-2);
		padding: var(--sp-5) var(--sp-5) var(--sp-6);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.label {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.label.muted {
		color: var(--text-muted);
	}

	.value {
		font: var(--type-numeral-xl);
		font-size: clamp(2.5rem, 2rem + 4cqi, 3.5rem);
		letter-spacing: var(--track-numeral);
		font-variant-numeric: lining-nums tabular-nums;
	}

	.progress,
	.facts {
		display: grid;
		gap: var(--sp-3);
	}

	.steps {
		--dot: 22px;
		display: grid;
		margin: 0;
		padding: var(--sp-4) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
		list-style: none;
	}

	.step {
		position: relative;
		display: grid;
		grid-template-columns: var(--dot) minmax(0, 1fr);
		gap: var(--sp-3);
		padding-block: var(--sp-2);
	}

	/* The rail between dots: solid up to the last step reached. */
	.step:not(:last-child)::after {
		content: '';
		position: absolute;
		inset-block: calc(var(--sp-2) + var(--dot)) calc(-1 * var(--sp-2));
		inset-inline-start: calc(var(--dot) / 2 - 1px);
		inline-size: 2px;
		background: var(--hairline);
	}

	.step.done:not(.last)::after {
		background: var(--accent);
	}

	.dot {
		display: grid;
		place-items: center;
		inline-size: var(--dot);
		block-size: var(--dot);
		border: 2px solid var(--border-strong);
		border-radius: 50%;
		background: var(--surface);
		color: var(--on-accent);
	}

	.dot :global(svg) {
		inline-size: 12px;
		block-size: 12px;
	}

	.done .dot {
		border-color: var(--accent);
		background: var(--accent);
	}

	.step-text {
		display: flex;
		flex-wrap: wrap;
		align-items: baseline;
		justify-content: space-between;
		gap: 0 var(--sp-3);
		min-block-size: var(--dot);
	}

	.step-label {
		font: var(--type-ui-strong);
	}

	.step-date {
		color: var(--text-muted);
		font: var(--type-ui);
		font-weight: 400;
		font-variant-numeric: tabular-nums;
	}

	.step:not(.done) .step-label {
		color: var(--text-muted);
	}

	.step:not(.done) .step-date {
		font-style: italic;
	}

	@container payments (width >= 560px) {
		.steps {
			grid-template-columns: repeat(4, minmax(0, 1fr));
			padding-block: var(--sp-5);
		}

		.step {
			grid-template-columns: minmax(0, 1fr);
			justify-items: start;
			padding-block: 0;
			padding-inline-end: var(--sp-3);
		}

		.step:not(:last-child)::after {
			inset-block: calc(var(--dot) / 2 - 1px) auto;
			inset-inline: var(--dot) 0;
			inline-size: auto;
			block-size: 2px;
		}

		.step-text {
			display: grid;
			gap: 2px;
		}
	}
</style>
