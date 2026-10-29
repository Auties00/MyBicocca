<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';
	import type { Icon } from './types.ts';

	interface Props {
		label: string;
		selected?: boolean;
		icon?: Icon;
		/** Shown after the label, e.g. how many items the filter matches. */
		count?: number;
		disabled?: boolean;
		onchange?: (selected: boolean) => void;
	}

	let {
		label,
		selected = $bindable(false),
		icon: LeadingIcon,
		count,
		disabled = false,
		onchange
	}: Props = $props();

	function toggle() {
		selected = !selected;
		onchange?.(selected);
	}
</script>

<button
	type="button"
	class={['chip', { selected }]}
	aria-pressed={selected}
	{disabled}
	onclick={toggle}
>
	{#if selected}
		<span class="check"><IconCheck aria-hidden="true" /></span>
	{:else if LeadingIcon}
		<LeadingIcon aria-hidden="true" />
	{/if}
	<span class="text">{label}</span>
	{#if count !== undefined}<span class="count">{count}</span>{/if}
</button>

<style>
	.chip {
		--h: 32px;
		position: relative;
		display: inline-flex;
		align-items: center;
		gap: 6px;
		block-size: var(--h);
		padding-inline: 12px;
		border-radius: var(--r-pill);
		background: var(--surface);
		color: var(--text);
		box-shadow: inset 0 0 0 1px var(--border);
		font: var(--type-ui);
		white-space: nowrap;
		touch-action: manipulation;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	.chip::after {
		content: '';
		position: absolute;
		inset: min(0px, calc((var(--h) - var(--hit-min)) / 2));
	}

	.chip :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.text {
		text-box: trim-both cap alphabetic;
	}

	.count {
		color: var(--text-muted);
		font-variant-numeric: tabular-nums;
	}

	.selected {
		padding-inline-start: 8px;
		background: var(--container);
		color: var(--on-container);
		box-shadow: none;
	}

	.selected .count {
		color: inherit;
		opacity: 0.75;
	}

	.check {
		display: grid;
		animation: pop var(--dur-spring) var(--spring-pop);
	}

	@media (hover: hover) {
		.chip:hover:not(:disabled) {
			background: var(--surface-hover);
			transition-duration: var(--dur-spring), 0s;
		}

		.selected:hover:not(:disabled) {
			background: var(--container-hover);
		}
	}

	.chip:active:not(:disabled) {
		transform: scale(0.96);
		transition-duration: var(--dur-instant), 0s;
	}

	.chip:disabled {
		opacity: 0.45;
		cursor: not-allowed;
	}

	@keyframes pop {
		from {
			transform: scale(0.4);
			opacity: 0;
		}
	}
</style>
