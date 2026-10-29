<script module lang="ts">
	import type { Icon } from '#lib/ui/types.ts';

	export interface FilterOption {
		token: string;
		label: string;
		/** Course cards the filter shows. */
		count: number;
		icon?: Icon;
	}
</script>

<script lang="ts">
	import IconCheck from '~icons/ph/check-bold';

	interface Props {
		options: readonly FilterOption[];
		value: string;
		label: string;
		onchange: (token: string) => void;
	}

	let { options, value, label, onchange }: Props = $props();

	const name = $props.id();

	/* The chosen chip scrolls into view when the bar overflows (phones, many careers). */
	function reveal(bar: HTMLElement) {
		const selected = bar.querySelector<HTMLElement>('.selected');
		if (selected && bar.scrollWidth > bar.clientWidth) {
			bar.scrollLeft = selected.offsetLeft - bar.clientWidth / 2 + selected.offsetWidth / 2;
		}
	}
</script>

<div class="bar" role="radiogroup" aria-label={label} {@attach reveal}>
	{#each options as option (option.token)}
		{@const selected = option.token === value}
		<label class={['chip', { selected }]}>
			<input
				class="sr-only"
				type="radio"
				{name}
				value={option.token}
				checked={selected}
				onchange={() => onchange(option.token)}
			/>
			{#if selected}
				<span class="glyph check"><IconCheck aria-hidden="true" /></span>
			{:else if option.icon}
				<span class="glyph"><option.icon aria-hidden="true" /></span>
			{/if}
			<span class="text">{option.label}</span>
			<span class="count">{option.count}</span>
		</label>
	{/each}
</div>

<style>
	/*
	 * On phones the chips scroll edge to edge; the bar is sized by the header, never by its chips,
	 * so a long row can't widen the page.
	 */
	.bar {
		display: flex;
		flex: 0 0 calc(100% + 2 * var(--gutter));
		gap: 8px;
		contain: inline-size;
		margin-inline: calc(-1 * var(--gutter));
		padding: 4px var(--gutter);
		overflow-x: auto;
		overscroll-behavior-x: contain;
		scroll-padding-inline: var(--gutter);
		scrollbar-width: none;
		mask-image: linear-gradient(
			to right,
			transparent,
			black var(--gutter),
			black calc(100% - var(--gutter)),
			transparent
		);
	}

	.bar::-webkit-scrollbar {
		display: none;
	}

	@media (width >= 600px) {
		.bar {
			flex: 1 1 100%;
			flex-wrap: wrap;
			contain: none;
			margin-inline: 0;
			padding-inline: 0;
			overflow: visible;
			mask-image: none;
		}
	}

	.chip {
		--h: 36px;
		position: relative;
		display: inline-flex;
		flex: none;
		align-items: center;
		gap: 6px;
		block-size: var(--h);
		padding-inline: 14px;
		border-radius: var(--r-pill);
		background: var(--surface);
		color: var(--text);
		box-shadow: inset 0 0 0 1px var(--border);
		font: var(--type-ui);
		white-space: nowrap;
		cursor: pointer;
		user-select: none;
		touch-action: manipulation;
		-webkit-tap-highlight-color: transparent;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	@media (pointer: fine) {
		.chip {
			--h: 32px;
			padding-inline: 12px;
		}
	}

	.chip:has(input:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.glyph {
		display: grid;
	}

	.glyph :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.check {
		animation: pop var(--dur-spring) var(--spring-pop);
	}

	.text {
		text-box: trim-both cap alphabetic;
	}

	.count {
		color: var(--text-muted);
		font-variant-numeric: tabular-nums;
	}

	.selected {
		padding-inline-start: 10px;
		background: var(--container);
		color: var(--on-container);
		box-shadow: none;
	}

	.selected .count {
		color: inherit;
		opacity: 0.75;
	}

	@media (hover: hover) {
		.chip:hover {
			background: var(--surface-hover);
			transition-duration: var(--dur-spring), 0s;
		}

		.selected:hover {
			background: var(--container-hover);
		}
	}

	.chip:active {
		transform: scale(0.96);
		transition-duration: var(--dur-instant), 0s;
	}

	@keyframes pop {
		from {
			transform: scale(0.4);
			opacity: 0;
		}
	}
</style>
