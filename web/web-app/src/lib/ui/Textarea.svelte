<script lang="ts">
	import type { HTMLTextareaAttributes } from 'svelte/elements';
	import { m } from '#lib/paraglide/messages.js';
	import Field from './Field.svelte';

	interface Props extends Omit<HTMLTextareaAttributes, 'value' | 'class' | 'children' | 'id'> {
		label: string;
		value?: string;
		description?: string;
		error?: string;
		hideLabel?: boolean;
		optional?: boolean;
		id?: string;
		/** Minimum visible lines; the field grows with its content up to `maxRows`. */
		rows?: number;
		maxRows?: number;
	}

	let {
		label,
		value = $bindable(''),
		description,
		error,
		hideLabel,
		optional,
		id,
		rows = 3,
		maxRows = 12,
		maxlength,
		...rest
	}: Props = $props();
</script>

<Field {label} {description} {error} {hideLabel} {optional} {id}>
	{#snippet aside()}
		{#if maxlength}
			<span aria-hidden="true">{m.ui_char_count({ count: value.length, max: maxlength })}</span>
		{/if}
	{/snippet}
	{#snippet children(field)}
		<textarea
			{...rest}
			id={field.id}
			aria-describedby={field.describedby}
			aria-invalid={field.invalid || undefined}
			class={{ invalid: field.invalid }}
			style:--rows={rows}
			style:--max-rows={maxRows}
			{maxlength}
			bind:value></textarea>
	{/snippet}
</Field>

<style>
	textarea {
		inline-size: 100%;
		min-block-size: calc(var(--rows) * 1lh + 20px);
		max-block-size: calc(var(--max-rows) * 1lh + 20px);
		field-sizing: content;
		padding: 10px 12px;
		border: 0;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-body);
		resize: vertical;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	@media (pointer: fine) {
		textarea {
			font-size: 0.9375rem;
		}
	}

	@media (hover: hover) {
		textarea:hover:not(:disabled) {
			box-shadow: inset 0 0 0 1px var(--border-strong);
			transition-duration: 0s;
		}
	}

	textarea:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	.invalid {
		box-shadow: inset 0 0 0 1px var(--danger);
	}
</style>
