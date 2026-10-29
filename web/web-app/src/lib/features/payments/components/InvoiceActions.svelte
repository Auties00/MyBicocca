<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconArrowsClockwise from '~icons/ph/arrows-clockwise';
	import IconFilePdf from '~icons/ph/file-pdf';
	import IconReceipt from '~icons/ph/receipt';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import { network } from '#lib/data/network.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatDate } from '#lib/format/dates.ts';
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import ConfirmDialog from '#lib/ui/ConfirmDialog.svelte';
	import type { Tone } from '#lib/ui/types.ts';
	import { usePaymentsView } from '../context.ts';
	import { invoiceActions, type Invoice, type PaymentOutcome } from '../invoices.ts';
	import { actionError, checkPayment, openNotice, openReceipt, startPayment } from '../pagopa.ts';

	interface Props {
		invoice: Invoice;
	}

	let { invoice }: Props = $props();

	const view = usePaymentsView();

	type Task = 'notice' | 'receipt' | 'status';

	const available = $derived(invoiceActions(invoice));
	const any = $derived(available.pay || available.notice || available.receipt || available.status);
	const online = $derived(network.online);

	let confirming = $state(false);
	/** The checkout opened in another tab: the next step is checking how it went. */
	let started = $state(false);
	/** The checkout's address when the browser opened no tab. */
	let checkout = $state<string>();
	let busy = $state<Task>();
	let failure = $state<string>();
	let outcome = $state<PaymentOutcome>();

	async function pay() {
		const scope = session.current;
		if (!scope) return;
		checkout = (await startPayment(scope, invoice.id, m.payments_pay_waiting())) ?? undefined;
		outcome = undefined;
		failure = undefined;
		started = true;
	}

	async function run(task: Task, work: () => Promise<void>) {
		if (busy) return;
		busy = task;
		failure = undefined;
		try {
			await work();
		} catch (error) {
			failure = actionError(error);
		} finally {
			busy = undefined;
		}
	}

	function notice() {
		const scope = session.current;
		if (scope) void run('notice', () => openNotice(scope, invoice.id));
	}

	function receipt() {
		const scope = session.current;
		if (scope) void run('receipt', () => openReceipt(scope, invoice.id));
	}

	function status() {
		const scope = session.current;
		if (!scope) return;
		void run('status', async () => {
			outcome = await checkPayment(scope, invoice.id);
			started = false;
		});
	}

	function continueToCheckout() {
		if (checkout) window.open(checkout, '_blank', 'noopener');
		checkout = undefined;
	}

	const outcomeTones: Record<PaymentOutcome['kind'], Tone> = {
		completed: 'success',
		pending: 'info',
		failed: 'danger',
		unknown: 'neutral',
		none: 'neutral'
	};

	const outcomeTitle = $derived.by(() => {
		switch (outcome?.kind) {
			case 'completed':
				return m.payments_outcome_completed();
			case 'pending':
				return m.payments_outcome_pending();
			case 'failed':
				return m.payments_outcome_failed();
			case 'unknown':
				return m.payments_outcome_unknown();
			default:
				return m.payments_outcome_none();
		}
	});

	const outcomeBody = $derived.by(() => {
		if (outcome === undefined) return undefined;
		switch (outcome.kind) {
			case 'none':
				return m.payments_outcome_none_body();
			case 'pending':
				return m.payments_outcome_pending_body();
			case 'failed':
				return m.payments_outcome_failed_body();
			case 'completed': {
				const { paidAmount, paidOn } = outcome;
				if (paidAmount === null || paidOn === null) return outcome.description ?? undefined;
				return m.payments_outcome_paid({
					amount: formatCurrency(paidAmount),
					date: formatDate(paidOn, 'long'),
					day: paidOn.getDate()
				});
			}
			case 'unknown':
				return outcome.description ?? undefined;
		}
	});

	const headingId = $props.id();
</script>

{#if any}
	<section class="actions" aria-labelledby={headingId}>
		<svelte:element this={view.level} class="label" id={headingId}
			>{m.payments_actions_heading()}</svelte:element
		>

		{#if available.pay}
			<div class="primary">
				<Button
					variant="solid"
					size="lg"
					full
					trailingIcon={IconArrowSquareOut}
					disabled={!online}
					aria-describedby="{headingId}-pay"
					onclick={() => (confirming = true)}
				>
					{m.payments_pay()}
				</Button>
				<p class="hint" id="{headingId}-pay">{m.payments_pay_hint()}</p>
			</div>
		{/if}

		<!-- Each document's caption sits under its own button, and describes it. -->
		{#if available.notice || available.receipt || available.status}
			<div class="secondary">
				{#if available.notice}
					<div class="action">
						<Button
							full
							icon={IconFilePdf}
							loading={busy === 'notice'}
							disabled={!online}
							aria-describedby="{headingId}-notice"
							onclick={notice}
						>
							{m.payments_notice()}
						</Button>
						<p class="hint" id="{headingId}-notice">{m.payments_notice_hint()}</p>
					</div>
				{/if}
				{#if available.receipt}
					<div class="action">
						<Button
							full
							icon={IconReceipt}
							loading={busy === 'receipt'}
							disabled={!online}
							aria-describedby="{headingId}-receipt"
							onclick={receipt}
						>
							{m.payments_receipt()}
						</Button>
						<p class="hint" id="{headingId}-receipt">{m.payments_receipt_hint()}</p>
					</div>
				{/if}
				{#if available.status}
					<div class="action">
						<Button
							full
							variant={available.notice || available.receipt ? 'outline' : 'tonal'}
							icon={IconArrowsClockwise}
							loading={busy === 'status'}
							onclick={status}
						>
							{m.payments_check_status()}
						</Button>
					</div>
				{/if}
			</div>
		{/if}

		{#if !online}
			<p class="offline"><IconWifiSlash aria-hidden="true" />{m.payments_offline_actions()}</p>
		{/if}

		<div class="feedback" aria-live="polite">
			{#if failure}
				<Banner tone="danger" message={failure} ondismiss={() => (failure = undefined)} />
			{/if}
			{#if started}
				<Banner
					tone="info"
					title={m.payments_pay_started_title()}
					message={checkout ? m.payments_pay_blocked() : m.payments_pay_started_body()}
					ondismiss={() => (started = false)}
				>
					{#snippet actions()}
						{#if checkout}
							<Button
								size="sm"
								variant="solid"
								trailingIcon={IconArrowSquareOut}
								onclick={continueToCheckout}
							>
								{m.payments_pay_continue()}
							</Button>
						{:else}
							<Button size="sm" loading={busy === 'status'} onclick={status}>
								{m.payments_check_status()}
							</Button>
						{/if}
					{/snippet}
				</Banner>
			{/if}
			{#if outcome}
				<Banner
					tone={outcomeTones[outcome.kind]}
					title={outcomeTitle}
					message={outcomeBody}
					ondismiss={() => (outcome = undefined)}
				/>
			{/if}
		</div>
	</section>

	<ConfirmDialog
		bind:open={confirming}
		title={m.payments_pay_confirm_title()}
		message={m.payments_pay_confirm_message({
			id: invoice.id,
			amount: formatCurrency(invoice.amount)
		})}
		confirmLabel={m.payments_pay_confirm()}
		onconfirm={pay}
		errorMessage={actionError}
	/>
{/if}

<style>
	.actions {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.label {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.primary,
	.action {
		display: grid;
		align-content: start;
		gap: var(--sp-2);
	}

	/* Stacked on phones; side by side where there is room, each caption under its button. */
	.secondary {
		display: grid;
		gap: var(--sp-4);
	}

	@container payments (width >= 480px) {
		.secondary {
			grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
		}
	}

	.primary + .secondary {
		padding-block-start: var(--sp-4);
		border-block-start: 1px solid var(--hairline);
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.offline {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-2);
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.offline :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
		margin-block-start: 1px;
	}

	.feedback {
		display: grid;
		gap: var(--sp-3);
	}

	/*
	 * The live region stays in the page so what arrives is announced; empty, it leaves the card's
	 * grid, which would otherwise keep a gap for it.
	 */
	.feedback:not(:has(*)) {
		position: absolute;
	}
</style>
