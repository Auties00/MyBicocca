<script lang="ts">
	interface Props {
		/** Omit for an indeterminate bar (unknown duration). */
		value?: number;
		max?: number;
		/** Accessible name; shown above the bar unless `hideLabel`. */
		label: string;
		hideLabel?: boolean;
		/** Text for the right side and for screen readers, e.g. "3 di 8 file". */
		valueText?: string;
		size?: 'sm' | 'md';
		tone?: 'accent' | 'success' | 'warning' | 'danger';
	}

	let {
		value,
		max = 100,
		label,
		hideLabel = false,
		valueText,
		size = 'md',
		tone = 'accent'
	}: Props = $props();

	const ratio = $derived(value === undefined ? undefined : Math.min(1, Math.max(0, value / max)));
</script>

<div class="progress">
	{#if !hideLabel}
		<div class="top">
			<span class="label">{label}</span>
			{#if valueText}<span class="value">{valueText}</span>{/if}
		</div>
	{/if}
	<div
		class={['track', size, tone, { indeterminate: ratio === undefined }]}
		role="progressbar"
		aria-label={label}
		aria-valuemin={0}
		aria-valuemax={max}
		aria-valuenow={value}
		aria-valuetext={valueText}
		style:--ratio={ratio}
	>
		<span class="fill"></span>
	</div>
</div>

<style>
	.progress {
		display: grid;
		gap: 6px;
	}

	.top {
		display: flex;
		justify-content: space-between;
		gap: 12px;
		font: var(--type-ui);
	}

	.value {
		color: var(--text-muted);
		font-variant-numeric: tabular-nums;
	}

	.track {
		--fill: var(--accent);
		position: relative;
		block-size: 8px;
		border-radius: var(--r-pill);
		background: var(--track);
		overflow: hidden;
	}

	.sm {
		block-size: 4px;
	}

	.success {
		--fill: var(--success-solid);
	}

	.warning {
		--fill: var(--warning-solid);
	}

	.danger {
		--fill: var(--danger-solid);
	}

	.fill {
		position: absolute;
		inset: 0;
		border-radius: inherit;
		background: var(--fill);
		transform: scaleX(var(--ratio));
		transform-origin: left;
		transition: transform var(--dur-slow) var(--ease-out);
	}

	@starting-style {
		.fill {
			transform: scaleX(0);
		}
	}

	.indeterminate .fill {
		inset-inline-end: auto;
		inline-size: 40%;
		transform: none;
		animation: slide 1.4s var(--ease-in-out) infinite;
	}

	@media (prefers-reduced-motion: reduce) {
		.indeterminate .fill {
			inline-size: 100%;
			animation: pulse 2s ease-in-out infinite;
		}
	}

	@keyframes slide {
		from {
			translate: -100% 0;
		}
		to {
			translate: 250% 0;
		}
	}

	@keyframes pulse {
		50% {
			opacity: 0.4;
		}
	}
</style>
