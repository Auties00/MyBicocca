<script lang="ts" generics="T extends string">
	import type { Snippet } from 'svelte';
	import IconCheck from '~icons/ph/check-bold';

	interface Props {
		/** The radio group's name: cards with the same name are one choice. */
		name: string;
		value: T;
		checked: boolean;
		label: string;
		/** false for a product name, the same in every language. */
		translateLabel?: boolean;
		/** Shown where the card is wide enough to give it a line. */
		description?: string;
		/** The picture of the option. */
		preview: Snippet;
		/** Chosen; `card` is where a theme change reveals from. */
		onselect: (value: T, card: HTMLElement) => void;
	}

	let {
		name,
		value,
		checked,
		label,
		translateLabel = true,
		description,
		preview,
		onselect
	}: Props = $props();
</script>

<!-- A native radio under a picture: arrow keys move between the cards of a group, Space picks. -->
<label class={['card', { checked }]}>
	<input
		class="sr-only"
		type="radio"
		{name}
		{value}
		{checked}
		onchange={(event) =>
			onselect(value, event.currentTarget.closest('label') ?? event.currentTarget)}
	/>
	<span class="art">{@render preview()}</span>
	<span class="caption">
		<span class="knob" aria-hidden="true"><IconCheck /></span>
		<span class="text">
			<span class="label" translate={translateLabel ? undefined : 'no'}>{label}</span>
			{#if description}<span class="description">{description}</span>{/if}
		</span>
	</span>
</label>

<style>
	.card {
		container-type: inline-size;
		display: grid;
		gap: 10px;
		align-content: start;
		min-inline-size: 0;
		padding: 6px 6px 8px;
		border-radius: var(--r-lg);
		cursor: pointer;
		-webkit-tap-highlight-color: transparent;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.card:not(.checked):hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.card:has(input:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.art {
		display: block;
		border-radius: var(--r-md);
		outline: 2px solid transparent;
		outline-offset: 3px;
		transition: outline-color var(--dur-base) var(--ease-out);
	}

	.checked .art {
		outline-color: var(--accent);
	}

	.caption {
		display: flex;
		align-items: flex-start;
		gap: 8px;
		padding-inline: 4px;
	}

	.knob {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 18px;
		block-size: 18px;
		margin-block-start: 1px;
		border: 1.5px solid var(--border-strong);
		border-radius: 50%;
		color: transparent;
		transition:
			background-color var(--dur-fast) var(--ease-out),
			border-color var(--dur-fast) var(--ease-out);
	}

	.knob :global(svg) {
		inline-size: 11px;
		block-size: 11px;
		transform: scale(0);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.checked .knob {
		border-color: var(--accent);
		background: var(--accent);
		color: var(--on-accent);
	}

	.checked .knob :global(svg) {
		transform: scale(1);
	}

	.text {
		display: grid;
		gap: 1px;
		min-inline-size: 0;
	}

	.label {
		font: var(--type-ui-strong);
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	/* Three to a phone's row: the label alone, on one line. */
	@container (width < 160px) {
		.description {
			display: none;
		}
	}
</style>
