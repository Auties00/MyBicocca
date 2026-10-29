<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { HTMLInputAttributes } from 'svelte/elements';
	import Field from './Field.svelte';
	import type { Icon } from './types.ts';

	interface Props extends Omit<
		HTMLInputAttributes,
		'value' | 'size' | 'class' | 'children' | 'id'
	> {
		label: string;
		value?: string;
		description?: string;
		error?: string;
		/** Invalid without a message of its own: the one shown on a related field covers it. */
		invalid?: boolean;
		hideLabel?: boolean;
		optional?: boolean;
		/** Leading glyph inside the field. */
		icon?: Icon;
		/** Inside the field on the right: a unit, an icon button. */
		trailing?: Snippet;
		id?: string;
		/** The input element, for focusing it from outside. */
		input?: HTMLInputElement;
	}

	let {
		label,
		value = $bindable(''),
		description,
		error,
		invalid = false,
		hideLabel,
		optional,
		icon: Glyph,
		trailing,
		id,
		input = $bindable(),
		'aria-describedby': describedby,
		...rest
	}: Props = $props();
</script>

<Field {label} {description} {error} {hideLabel} {optional} {id}>
	{#snippet children(field)}
		<div class={['box', { invalid: field.invalid || invalid, disabled: rest.disabled }]}>
			{#if Glyph}<span class="glyph"><Glyph aria-hidden="true" /></span>{/if}
			<input
				{...rest}
				bind:this={input}
				bind:value
				id={field.id}
				aria-describedby={[field.describedby, describedby].filter(Boolean).join(' ') || undefined}
				aria-invalid={field.invalid || invalid || undefined}
			/>
			{#if trailing}<span class="trailing">{@render trailing()}</span>{/if}
		</div>
	{/snippet}
</Field>

<style>
	.box {
		display: flex;
		align-items: center;
		gap: 8px;
		min-block-size: var(--control-h);
		padding-inline: 12px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		cursor: text;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.box:hover:not(.disabled) {
			box-shadow: inset 0 0 0 1px var(--border-strong);
			transition-duration: 0s;
		}
	}

	.box:focus-within {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	.invalid,
	.invalid:hover {
		box-shadow: inset 0 0 0 1px var(--danger);
	}

	.disabled {
		opacity: 0.55;
		cursor: not-allowed;
	}

	/* No intrinsic width (20 characters by default): in a narrow grid column the field fits the column. */
	input {
		flex: 1;
		inline-size: 0;
		min-inline-size: 0;
		block-size: var(--control-h);
		padding: 0;
		border: 0;
		background: none;
		outline: none;
		font: var(--type-body);
	}

	input:focus-visible {
		outline: none;
	}

	@media (pointer: fine) {
		input {
			font-size: 0.9375rem;
		}
	}

	.glyph {
		display: grid;
		color: var(--text-muted);
	}

	.glyph :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	.trailing {
		display: flex;
		align-items: center;
		gap: 4px;
		margin-inline-end: -6px;
		color: var(--text-muted);
	}
</style>
