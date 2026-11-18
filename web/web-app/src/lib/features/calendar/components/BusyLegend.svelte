<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';

	interface Props {
		/** wide: the quieter tint of the wide month grid. */
		variant?: 'compact' | 'wide';
	}

	let { variant = 'compact' }: Props = $props();

	const buckets = $derived([
		m.calendar_busy_light(),
		m.calendar_busy_moderate(),
		m.calendar_busy_heavy(),
		m.calendar_busy_full()
	]);
</script>

<figure class={['legend', variant]}>
	<figcaption>
		<span class="caption">{m.calendar_legend()}</span>
		<span class="sr-only">{m.calendar_busy_caption()}: {buckets.join(', ')}</span>
	</figcaption>
	<div class="scale" aria-hidden="true">
		<span class="bar"></span>
		<span class="labels">
			{#each buckets as bucket (bucket)}<span>{bucket}</span>{/each}
		</span>
	</div>
</figure>

<style>
	.legend {
		--heat: var(--accent);
		--from: color-mix(in oklch, var(--heat) 25%, var(--track));
		--to: var(--heat);
		display: grid;
		gap: 8px;
	}

	.wide {
		--from: color-mix(in oklch, var(--heat) 6%, var(--surface));
		--to: color-mix(in oklch, var(--heat) 27%, var(--surface));
		grid-template-columns: auto minmax(160px, 260px);
		align-items: center;
		gap: 12px;
	}

	.caption {
		color: var(--text-muted);
		font: var(--type-overline);
		font-size: 0.6875rem;
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.scale {
		display: grid;
		gap: 6px;
	}

	.bar {
		block-size: 8px;
		border-radius: 4px;
		background: linear-gradient(to right, var(--from), var(--to));
	}

	.wide .bar {
		block-size: 6px;
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.labels {
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		color: var(--text-muted);
		font: 500 0.6875rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		text-align: center;
	}
</style>
