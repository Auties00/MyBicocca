<script lang="ts">
	interface Props {
		value: number;
		max: number;
		/** Accessible name ("Crediti"). */
		label: string;
		/** Human reading, shown under the bar and announced ("129 di 180 CFU"). */
		valueText: string;
		/** Markers along the track, as values (e.g. the credits needed for each year). */
		marks?: readonly number[];
		hideLabel?: boolean;
	}

	let { value, max, label, valueText, marks = [], hideLabel = false }: Props = $props();

	const ratio = $derived(max > 0 ? Math.min(1, Math.max(0, value / max)) : 0);
	const complete = $derived(ratio >= 1);
</script>

<div class="meter">
	<div
		class={['track', { complete }]}
		role="meter"
		aria-label={label}
		aria-valuemin={0}
		aria-valuemax={max}
		aria-valuenow={value}
		aria-valuetext={valueText}
		style:--ratio={ratio}
	>
		<span class="fill"></span>
		{#each marks as mark (mark)}
			<span class="mark" style:--at={Math.min(1, mark / max)}></span>
		{/each}
	</div>
	{#if !hideLabel}<p class="text">{valueText}</p>{/if}
</div>

<style>
	.meter {
		display: grid;
		gap: 8px;
	}

	.track {
		position: relative;
		block-size: 8px;
		border-radius: var(--r-pill);
		background: var(--track);
		overflow: hidden;
	}

	.fill {
		position: absolute;
		inset: 0;
		border-radius: inherit;
		background: var(--accent);
		transform: scaleX(var(--ratio));
		transform-origin: left;
		transition: transform 600ms var(--spring-standard);
	}

	@starting-style {
		.fill {
			transform: scaleX(0);
		}
	}

	/* Complete: the bar settles with a small overshoot and takes a gold tip. */
	.complete .fill {
		background: linear-gradient(90deg, var(--accent) 82%, var(--gold));
		transition-timing-function: var(--spring-pop);
	}

	.mark {
		position: absolute;
		inset-block: 2px;
		inset-inline-start: calc(var(--at) * 100%);
		inline-size: 2px;
		margin-inline-start: -1px;
		border-radius: 1px;
		background: var(--surface);
		opacity: 0.6;
	}

	.text {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}
</style>
