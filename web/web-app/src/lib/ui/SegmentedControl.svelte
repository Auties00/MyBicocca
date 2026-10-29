<script lang="ts" generics="T extends string">
	import { haptics } from './haptics.svelte.ts';
	import type { SegmentedOption } from './types.ts';

	interface Props {
		options: readonly SegmentedOption<T>[];
		value: T;
		/** Accessible name of the group. */
		label: string;
		size?: 'md' | 'lg';
		/** Stretches to the container width; otherwise segments size to the widest label. */
		full?: boolean;
		/** Also gets the change event, e.g. to start a theme reveal from the chosen segment. */
		onchange?: (value: T, event: Event & { currentTarget: HTMLInputElement }) => void;
	}

	let {
		options,
		value = $bindable(),
		label,
		size = 'md',
		full = false,
		onchange
	}: Props = $props();

	const name = $props.id();
	const index = $derived(
		Math.max(
			0,
			options.findIndex((o) => o.value === value)
		)
	);
	const hasCaption = $derived(options.some((o) => o.caption));
</script>

<div
	class={['segmented', size, { full, captioned: hasCaption }]}
	role="radiogroup"
	aria-label={label}
	style:--count={options.length}
	style:--index={index}
>
	<span class="thumb" aria-hidden="true"></span>
	{#each options as option (option.value)}
		<label class={['segment', { selected: option.value === value }]}>
			<input
				class="sr-only"
				type="radio"
				{name}
				value={option.value}
				checked={option.value === value}
				disabled={option.disabled}
				onchange={(event) => {
					value = option.value;
					haptics.tap();
					onchange?.(option.value, event);
				}}
			/>
			{#if option.caption}<span class="caption">{option.caption}</span>{/if}
			<span class="label">
				{#if option.icon}<option.icon aria-hidden="true" />{/if}
				{option.label}
			</span>
		</label>
	{/each}
</div>

<style>
	.segmented {
		--pad: 4px;
		--h: var(--control-h);
		position: relative;
		display: inline-grid;
		grid-template-columns: repeat(var(--count), minmax(0, 1fr));
		grid-auto-flow: column;
		padding: var(--pad);
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline);
		isolation: isolate;
	}

	.segmented:not(.full) {
		justify-self: start;
	}

	.full {
		display: grid;
		inline-size: 100%;
	}

	.lg {
		--h: var(--control-h-lg);
		--pad: 5px;
	}

	.captioned {
		--h: 52px;
	}

	.thumb {
		position: absolute;
		inset-block: var(--pad);
		inset-inline-start: var(--pad);
		inline-size: calc((100% - 2 * var(--pad)) / var(--count));
		border-radius: var(--r-pill);
		background: var(--container);
		box-shadow: var(--inner-light);
		transform: translateX(calc(var(--index) * 100%));
		transition: transform var(--dur-spring) var(--spring-standard);
		z-index: -1;
	}

	.segment {
		position: relative;
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 2px;
		min-block-size: calc(var(--h) - 2 * var(--pad));
		padding-inline: 14px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-ui-strong);
		white-space: nowrap;
		cursor: pointer;
		user-select: none;
		touch-action: manipulation;
		transition: color var(--dur-fast) var(--ease-out);
	}

	.segment::after {
		content: '';
		position: absolute;
		inset-block: min(0px, calc((var(--h) - 2 * var(--pad) - var(--hit-min)) / 2));
		inset-inline: 0;
	}

	.label {
		display: inline-flex;
		align-items: center;
		gap: 6px;
	}

	.label :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.caption {
		font: var(--type-overline);
		font-size: 0.625rem;
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	@media (hover: hover) {
		.segment:hover {
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.selected,
	.selected:hover {
		color: var(--on-container);
	}

	.segment:has(:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 0;
	}

	.segment:has(:disabled) {
		opacity: 0.4;
		cursor: not-allowed;
	}
</style>
