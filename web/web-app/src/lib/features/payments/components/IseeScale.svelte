<script lang="ts">
	import { formatCurrency } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { iseeMargin } from '../isee.ts';

	interface Props {
		isee: number;
		threshold: number;
	}

	let { isee, threshold }: Props = $props();

	/* Room past the larger value, so neither the fill nor the mark touches the end. */
	const max = $derived(Math.max(isee, threshold) * 1.2);
	const fill = $derived(Math.min(1, isee / max));
	const mark = $derived(threshold / max);
	const margin = $derived(iseeMargin({ isee, threshold }) ?? 0);

	const caption = $derived(
		margin > 0
			? m.payments_isee_below({ amount: formatCurrency(margin) })
			: margin < 0
				? m.payments_isee_above({ amount: formatCurrency(-margin) })
				: m.payments_isee_at()
	);
</script>

<!-- Where the ISEE sits against the threshold for benefits: a fact, told in words too. -->
<figure class="scale">
	<div
		class="track"
		role="img"
		aria-label={m.payments_isee_scale_value({
			isee: formatCurrency(isee),
			threshold: formatCurrency(threshold)
		})}
		style:--fill={fill}
		style:--mark={mark}
	>
		<span class={['bar', { over: margin < 0 }]}></span>
		<span class="mark"></span>
	</div>
	<p class="legend" aria-hidden="true">
		<span class="swatch"></span>
		{m.payments_isee_threshold()} · {formatCurrency(threshold)}
	</p>
	<figcaption class={{ over: margin < 0 }}>{caption}</figcaption>
</figure>

<style>
	.scale {
		display: grid;
		gap: var(--sp-2);
		margin: 0;
	}

	.track {
		position: relative;
		block-size: 10px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
	}

	.bar {
		position: absolute;
		inset: 0;
		border-radius: inherit;
		background: var(--accent);
		transform: scaleX(var(--fill));
		transform-origin: left;
	}

	.bar.over {
		background: var(--warning-solid);
	}

	@starting-style {
		.bar {
			transform: scaleX(0);
		}
	}

	@media (prefers-reduced-motion: no-preference) {
		.bar {
			transition: transform var(--dur-slow) var(--spring-standard);
		}
	}

	.mark {
		position: absolute;
		inset-block: -5px;
		inset-inline-start: calc(var(--mark) * 100%);
		inline-size: 2px;
		margin-inline-start: -1px;
		border-radius: 1px;
		background: var(--text);
	}

	.legend {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		color: var(--text-muted);
		font: var(--type-label);
	}

	.swatch {
		inline-size: 2px;
		block-size: 12px;
		border-radius: 1px;
		background: var(--text);
	}

	figcaption {
		font: var(--type-ui-strong);
	}

	figcaption.over {
		color: var(--warning);
	}
</style>
