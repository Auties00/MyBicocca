<script lang="ts">
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';

	interface Props {
		credits: number;
		/** Tinted: the activity is picked in the editor. */
		selected?: boolean;
	}

	let { credits, selected = false }: Props = $props();
</script>

<span class={['chip', { selected }]}>
	<span class="value">{formatNumber(credits, { maximumFractionDigits: 1 })}</span>
	<span class="unit">{m.studyplanedit_unit_credits()}</span>
</span>

<style>
	.chip {
		display: grid;
		place-content: center;
		justify-items: center;
		inline-size: 44px;
		block-size: 44px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		color: var(--text);
		transition:
			background-color var(--dur-fast) var(--ease-out),
			color var(--dur-fast) var(--ease-out);
	}

	.selected {
		background: var(--container);
		color: var(--on-container);
	}

	.value {
		font: 600 1rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
	}

	.unit {
		margin-block-start: 3px;
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.5625rem;
		letter-spacing: var(--track-overline);
	}

	.selected .unit {
		color: inherit;
		opacity: 0.8;
	}
</style>
