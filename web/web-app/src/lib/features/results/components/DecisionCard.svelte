<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import IconHourglass from '~icons/ph/hourglass-medium-fill';
	import IconWifiSlash from '~icons/ph/wifi-slash';
	import IconX from '~icons/ph/x-bold';
	import { dueLabel, dueTone } from '#lib/format/due.ts';
	import { network } from '#lib/data/network.svelte.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { decideByLine } from '../format.ts';
	import type { Choice, ExamResult } from '../model.ts';

	interface Props {
		result: ExamResult;
		/** Opens the confirmation of `choice`; the page runs the decision. */
		onchoose: (choice: Choice) => void;
		level: 2 | 3;
	}

	let { result, onchoose, level }: Props = $props();

	const days = $derived(Math.max(0, result.daysLeft ?? 0));
	const id = $props.id();
</script>

<section class="decision" aria-labelledby={id}>
	<div class="head">
		<svelte:element this={`h${level}`} class="title" {id}
			>{m.results_decide_heading()}</svelte:element
		>
		<p class={['countdown', dueTone(days)]}>
			<IconHourglass aria-hidden="true" />
			{dueLabel(days)}{#if result.deadline}<span class="sr-only"
					>, {decideByLine(result.deadline, 'long')}</span
				>{/if}
		</p>
	</div>
	<p class="hint">{m.results_decide_hint()}</p>

	{#if !network.online}
		<p class="offline" role="status">
			<IconWifiSlash aria-hidden="true" />
			{m.results_offline_hint()}
		</p>
	{/if}

	<!-- The safe choice is the wider one; the irreversible loss sits apart, on the leading side. -->
	<div class="actions">
		<Button
			size="lg"
			icon={IconX}
			full
			disabled={!network.online}
			onclick={() => onchoose('rejected')}
		>
			{m.results_reject()}
		</Button>
		<Button
			variant="solid"
			size="lg"
			icon={IconCheck}
			full
			disabled={!network.online}
			onclick={() => onchoose('accepted')}
		>
			{m.results_accept()}
		</Button>
	</div>
</section>

<style>
	.decision {
		display: grid;
		gap: var(--sp-4);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: color-mix(in oklch, var(--container) 55%, var(--surface));
		box-shadow:
			inset 0 0 0 1px color-mix(in oklch, var(--accent) 18%, transparent),
			var(--inner-light);
	}

	.head {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-2) var(--sp-3);
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.countdown {
		display: inline-flex;
		align-items: center;
		gap: 5px;
		padding: 3px 10px 3px 8px;
		border-radius: var(--r-pill);
		background: color-mix(in oklch, var(--text) 7%, transparent);
		color: var(--text-muted);
		font: var(--type-label);
		white-space: nowrap;
	}

	.countdown :global(svg) {
		inline-size: 14px;
		block-size: 14px;
	}

	.warning {
		background: var(--warning-container);
		color: var(--on-warning-container);
	}

	.danger {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.hint {
		max-inline-size: 65ch;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	.offline {
		display: flex;
		align-items: center;
		gap: 8px;
		color: var(--on-warning-container);
		font: var(--type-ui);
	}

	.offline :global(svg) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
	}

	.actions {
		display: grid;
		grid-template-columns: minmax(0, 1fr) minmax(0, 1.4fr);
		gap: var(--sp-2);
	}

	@supports (corner-shape: squircle) {
		.decision {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
