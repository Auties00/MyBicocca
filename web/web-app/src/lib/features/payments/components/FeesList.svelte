<script lang="ts">
	import IconChecks from '~icons/ph/checks';
	import { today } from '#lib/data/clock.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import SectionHeader from '#lib/ui/SectionHeader.svelte';
	import type { Fees } from '../stores.svelte.ts';
	import InvoiceHistory from './InvoiceHistory.svelte';
	import InvoiceTicket from './InvoiceTicket.svelte';

	interface Props {
		fees: Fees;
		/** An invoice is open beside the list, which is then a narrow column. */
		beside: boolean;
		selected?: string;
	}

	let { fees, beside, selected }: Props = $props();

	const toPay = $derived(fees.split.toPay);
	const toPayId = $props.id();
	const historyId = `${toPayId}-history`;
</script>

<div class="fees">
	<section class="section" aria-labelledby={toPayId}>
		<SectionHeader
			id={toPayId}
			title={m.payments_to_pay_heading()}
			description={toPay.length > 0 && !beside ? m.payments_to_pay_description() : undefined}
		/>
		{#if toPay.length > 0}
			<ul class={['tickets', { beside }]} role="list">
				{#each toPay as invoice (invoice.id)}
					<li>
						<InvoiceTicket
							{invoice}
							today={today.current}
							selected={String(invoice.id) === selected}
						/>
					</li>
				{/each}
			</ul>
		{:else}
			<p class="settled">
				<IconChecks aria-hidden="true" />
				<span>
					<span class="settled-title">{m.payments_nothing_due_title()}</span>
					<span class="settled-body">{m.payments_nothing_due_body()}</span>
				</span>
			</p>
		{/if}
	</section>

	{#if fees.years.length > 0}
		<section class="section" aria-labelledby={historyId}>
			<SectionHeader
				id={historyId}
				title={m.payments_history_heading()}
				description={beside ? undefined : m.payments_history_description()}
			/>
			<InvoiceHistory years={fees.years} {selected} labelledby={historyId} />
		</section>
	{/if}
</div>

<style>
	.fees {
		display: grid;
		gap: var(--sp-7);
	}

	.section {
		display: grid;
		gap: var(--sp-4);
	}

	.tickets {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(min(100%, 340px), 1fr));
		gap: var(--sp-4);
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.tickets.beside {
		grid-template-columns: minmax(0, 1fr);
		gap: var(--sp-3);
	}

	.settled {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-3);
		padding: var(--sp-4);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.settled :global(svg) {
		flex: none;
		inline-size: 22px;
		block-size: 22px;
		margin-block-start: 1px;
		color: var(--success);
	}

	.settled > span {
		display: grid;
		gap: 2px;
	}

	.settled-title {
		font-family: var(--font-display);
		font-size: 1.1875rem;
		font-style: italic;
		line-height: 1.25;
	}

	.settled-body {
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}
</style>
