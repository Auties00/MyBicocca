<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Tone } from './types.ts';

	interface Props {
		/** Short text. Omit it and pass `count` for a numeric badge. */
		children?: Snippet;
		/** Numeric badge; above `max` shows "max+". */
		count?: number;
		max?: number;
		tone?: Tone;
		/** soft (default) = tinted fill; solid = strong fill for counts that need attention. */
		variant?: 'soft' | 'solid' | 'outline';
		/** Accessible text when the visible text is terse (a count). */
		label?: string;
	}

	let { children, count, max = 99, tone = 'neutral', variant = 'soft', label }: Props = $props();
</script>

<span class={['badge', tone, variant, { numeric: count !== undefined }]} aria-label={label}>
	{#if count !== undefined}
		{count > max ? `${max}+` : count}
	{:else if children}
		{@render children()}
	{/if}
</span>

<style>
	.badge {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 4px;
		min-block-size: 20px;
		padding: 0 7px;
		border-radius: var(--r-xs);
		background: var(--b-bg);
		color: var(--b-fg);
		font: 600 0.75rem / 1 var(--font-ui);
		letter-spacing: 0.01em;
		white-space: nowrap;
		vertical-align: middle;
	}

	.numeric {
		min-inline-size: 20px;
		padding: 0 6px;
		border-radius: var(--r-pill);
		font-variant-numeric: tabular-nums;
	}

	.neutral {
		--b-bg: var(--surface-sunken);
		--b-fg: var(--text-muted);
		--b-solid: var(--g11);
	}

	.accent {
		--b-bg: var(--container);
		--b-fg: var(--on-container);
		--b-solid: var(--accent);
	}

	.info {
		--b-bg: var(--info-container);
		--b-fg: var(--on-info-container);
		--b-solid: var(--info-solid);
	}

	.success {
		--b-bg: var(--success-container);
		--b-fg: var(--on-success-container);
		--b-solid: var(--success-solid);
	}

	.warning {
		--b-bg: var(--warning-container);
		--b-fg: var(--on-warning-container);
		--b-solid: var(--warning-solid);
	}

	.danger {
		--b-bg: var(--danger-container);
		--b-fg: var(--on-danger-container);
		--b-solid: var(--danger-solid);
	}

	.gold {
		--b-bg: var(--gold-container);
		--b-fg: var(--on-gold-container);
		--b-solid: var(--gold);
	}

	.solid {
		background: var(--b-solid);
		color: oklch(100% 0 0);
	}

	.solid.warning {
		color: var(--on-warning-solid);
	}

	.solid.neutral,
	.solid.gold {
		color: var(--bg);
	}

	.outline {
		background: transparent;
		color: var(--b-fg);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, currentColor 35%, transparent);
	}

	.outline.gold {
		color: var(--gold);
	}
</style>
