<script lang="ts" generics="T extends string">
	interface RadioOption {
		value: T;
		label: string;
		description?: string;
		disabled?: boolean;
	}

	interface Props {
		/** The question the options answer; rendered as the fieldset legend. */
		label: string;
		options: readonly RadioOption[];
		value: T | undefined;
		hideLabel?: boolean;
		/** cards: each option is a bordered tile (for choices with descriptions). */
		variant?: 'list' | 'cards';
		error?: string;
		name?: string;
		onchange?: (value: T) => void;
	}

	let {
		label,
		options,
		value = $bindable(),
		hideLabel = false,
		variant = 'list',
		error,
		name,
		onchange
	}: Props = $props();

	const id = $props.id();
</script>

<fieldset class={['group', variant]} aria-describedby={error ? `${id}-error` : undefined}>
	<legend class={['legend', { 'sr-only': hideLabel }]}>{label}</legend>
	<div class="options">
		{#each options as option (option.value)}
			<label class={['option', { selected: value === option.value, disabled: option.disabled }]}>
				<input
					class="input"
					type="radio"
					name={name ?? id}
					value={option.value}
					checked={value === option.value}
					disabled={option.disabled}
					onchange={() => {
						value = option.value;
						onchange?.(option.value);
					}}
				/>
				<span class="dot" aria-hidden="true"></span>
				<span class="text">
					<span class="label">{option.label}</span>
					{#if option.description}<span class="description">{option.description}</span>{/if}
				</span>
			</label>
		{/each}
	</div>
	{#if error}<p class="error" id="{id}-error">{error}</p>{/if}
</fieldset>

<style>
	.group {
		display: grid;
		gap: 8px;
		min-inline-size: 0;
	}

	.legend {
		margin-block-end: 8px;
		padding: 0;
		font: var(--type-ui-strong);
	}

	.options {
		display: grid;
		gap: 4px;
	}

	.cards .options {
		gap: 8px;
	}

	.option {
		position: relative;
		display: flex;
		align-items: flex-start;
		gap: 12px;
		padding-block: 8px;
		cursor: pointer;
	}

	.cards .option {
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--border);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.cards .selected {
		background: var(--container);
		box-shadow: inset 0 0 0 1.5px var(--accent);
	}

	.disabled {
		opacity: 0.45;
		cursor: not-allowed;
	}

	.input {
		position: absolute;
		opacity: 0;
		inline-size: 1px;
		block-size: 1px;
	}

	.dot {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 22px;
		block-size: 22px;
		border-radius: 50%;
		background: var(--surface);
		box-shadow: inset 0 0 0 1.5px var(--border-strong);
	}

	.dot::after {
		content: '';
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: var(--on-accent);
		transform: scale(0);
		transition: transform var(--dur-spring) var(--spring-pop);
	}

	.input:checked + .dot {
		background: var(--accent);
		box-shadow: none;
	}

	.input:checked + .dot::after {
		transform: scale(1);
	}

	.input:focus-visible + .dot {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.text {
		display: grid;
		gap: 2px;
		padding-block-start: 1px;
	}

	.label {
		font: var(--type-ui);
		font-size: 0.9375rem;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.selected .description {
		color: inherit;
		opacity: 0.8;
	}

	.error {
		color: var(--danger);
		font: var(--type-meta);
		font-weight: 500;
	}
</style>
