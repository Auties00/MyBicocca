<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';
	import IconCheck from '~icons/ph/check-bold';
	import { haptics } from './haptics.svelte.ts';

	interface Props extends Omit<
		HTMLInputAttributes,
		'type' | 'checked' | 'class' | 'children' | 'role'
	> {
		checked?: boolean;
		/**
		 * Visible label next to the switch. Inside a `ListRow` control, omit it: the row's title is
		 * the label.
		 */
		label?: string;
		description?: string;
		onchange?: (event: Event & { currentTarget: HTMLInputElement }) => void;
	}

	let { checked = $bindable(false), label, description, onchange, ...rest }: Props = $props();

	const id = $props.id();
	/* The description is drawn only beside a visible label; a caller's own ids are kept. */
	const describedby = $derived(
		[rest['aria-describedby'], label && description ? `${id}-description` : undefined]
			.filter(Boolean)
			.join(' ') || undefined
	);
</script>

{#snippet control()}
	<span class="switch">
		<input
			{...rest}
			class="input"
			type="checkbox"
			role="switch"
			aria-describedby={describedby}
			bind:checked
			onchange={(event) => {
				haptics.tap();
				onchange?.(event);
			}}
		/>
		<span class="track" aria-hidden="true">
			<span class="thumb"><IconCheck /></span>
		</span>
	</span>
{/snippet}

{#if label}
	<label class="labelled">
		<span class="text">
			<span class="label">{label}</span>
			{#if description}<span class="description" id="{id}-description">{description}</span>{/if}
		</span>
		{@render control()}
	</label>
{:else}
	{@render control()}
{/if}

<style>
	.labelled {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16px;
		cursor: pointer;
	}

	.text {
		display: grid;
		gap: 2px;
	}

	.label {
		font: var(--type-ui);
		font-size: 0.9375rem;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.switch {
		--w: 52px;
		--h: 32px;
		--knob: 24px;
		--knob-off: 16px;
		position: relative;
		display: inline-grid;
		flex-shrink: 0;
		inline-size: var(--w);
		block-size: var(--h);
	}

	@media (pointer: fine) {
		.switch {
			--w: 44px;
			--h: 26px;
			--knob: 20px;
			--knob-off: 14px;
		}
	}

	.input {
		position: absolute;
		inset: -9px;
		margin: 0;
		opacity: 0;
		cursor: pointer;
		z-index: 1;
	}

	.input:disabled {
		cursor: not-allowed;
	}

	.track {
		display: flex;
		align-items: center;
		padding-inline: calc((var(--h) - var(--knob)) / 2);
		border-radius: var(--r-pill);
		background: var(--g6);
		box-shadow: inset 0 0 0 1px var(--hairline);
		transition: background-color var(--dur-base) var(--ease-out);
	}

	.thumb {
		display: grid;
		place-items: center;
		inline-size: var(--knob);
		block-size: var(--knob);
		border-radius: 50%;
		background: oklch(100% 0 0);
		color: var(--accent);
		box-shadow: 0 1px 3px color-mix(in oklch, var(--shadow-color) 25%, transparent);
		transform: scale(calc(var(--knob-off) / var(--knob)));
		transition: transform var(--dur-spring) var(--spring-standard);
	}

	.thumb :global(svg) {
		inline-size: 60%;
		block-size: 60%;
		opacity: 0;
		transition: opacity var(--dur-fast) var(--ease-out);
	}

	.input:checked + .track {
		background: var(--accent);
		box-shadow: none;
	}

	.input:checked + .track .thumb {
		transform: translateX(calc(var(--w) - var(--h)));
	}

	.input:checked + .track .thumb :global(svg) {
		opacity: 1;
	}

	.input:active:not(:disabled) + .track .thumb {
		transform: scale(1.12);
	}

	.input:checked:active:not(:disabled) + .track .thumb {
		transform: translateX(calc(var(--w) - var(--h))) scale(1.12);
	}

	.input:focus-visible + .track {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.input:disabled + .track {
		opacity: 0.45;
	}
</style>
