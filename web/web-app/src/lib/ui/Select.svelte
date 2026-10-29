<script lang="ts" generics="T extends string">
	import type { HTMLSelectAttributes } from 'svelte/elements';
	import IconCaretUpDown from '~icons/ph/caret-up-down';
	import Field from './Field.svelte';

	interface SelectOption {
		value: T;
		label: string;
		disabled?: boolean;
	}

	interface Props extends Omit<
		HTMLSelectAttributes,
		'value' | 'class' | 'children' | 'id' | 'size' | 'onchange'
	> {
		label: string;
		options: readonly SelectOption[];
		value: T;
		description?: string;
		error?: string;
		hideLabel?: boolean;
		optional?: boolean;
		id?: string;
		onchange?: (value: T) => void;
	}

	let {
		label,
		options,
		value = $bindable(),
		description,
		error,
		hideLabel,
		optional,
		id,
		onchange,
		...rest
	}: Props = $props();
</script>

<Field {label} {description} {error} {hideLabel} {optional} {id}>
	{#snippet children(field)}
		<div class={['box', { invalid: field.invalid }]}>
			<select
				{...rest}
				id={field.id}
				aria-describedby={field.describedby}
				aria-invalid={field.invalid || undefined}
				bind:value
				onchange={() => onchange?.(value)}
			>
				{#each options as option (option.value)}
					<option value={option.value} disabled={option.disabled}>{option.label}</option>
				{/each}
			</select>
			<IconCaretUpDown class="caret" aria-hidden="true" />
		</div>
	{/snippet}
</Field>

<style>
	.box {
		position: relative;
		display: grid;
		align-items: center;
	}

	select {
		grid-area: 1 / 1;
		inline-size: 100%;
		min-block-size: var(--control-h);
		padding: 0 36px 0 12px;
		border: 0;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-body);
		-webkit-appearance: none;
		appearance: none;
		cursor: pointer;
		transition: box-shadow var(--dur-fast) var(--ease-out);
	}

	@media (pointer: fine) {
		select {
			font-size: 0.9375rem;
		}
	}

	@media (hover: hover) {
		select:hover:not(:disabled) {
			box-shadow: inset 0 0 0 1px var(--border-strong);
			transition-duration: 0s;
		}
	}

	select:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: -1px;
	}

	select:disabled {
		opacity: 0.55;
		cursor: not-allowed;
	}

	.invalid select {
		box-shadow: inset 0 0 0 1px var(--danger);
	}

	.box :global(.caret) {
		grid-area: 1 / 1;
		justify-self: end;
		inline-size: 16px;
		block-size: 16px;
		margin-inline-end: 12px;
		color: var(--text-muted);
		pointer-events: none;
	}
</style>
