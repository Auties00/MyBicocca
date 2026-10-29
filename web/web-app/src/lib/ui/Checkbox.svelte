<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';
	import IconCheck from '~icons/ph/check-bold';
	import IconMinus from '~icons/ph/minus-bold';

	interface Props extends Omit<HTMLInputAttributes, 'type' | 'checked' | 'class' | 'children'> {
		checked?: boolean;
		indeterminate?: boolean;
		/** Visible label. Inside a `ListRow` control, omit it: the row's title is the label. */
		label?: string;
		description?: string;
	}

	let {
		checked = $bindable(false),
		indeterminate = $bindable(false),
		label,
		description,
		...rest
	}: Props = $props();

	const id = $props.id();
	/* The description is drawn only beside a visible label; a caller's own ids are kept. */
	const describedby = $derived(
		[rest['aria-describedby'], label && description ? `${id}-description` : undefined]
			.filter(Boolean)
			.join(' ') || undefined
	);
</script>

{#snippet control()}
	<span class="checkbox">
		<input
			{...rest}
			class="input"
			type="checkbox"
			aria-describedby={describedby}
			bind:checked
			bind:indeterminate
		/>
		<span class="box" aria-hidden="true">
			{#if indeterminate}<IconMinus />{:else}<IconCheck />{/if}
		</span>
	</span>
{/snippet}

{#if label}
	<label class="labelled">
		{@render control()}
		<span class="text">
			<span class="label">{label}</span>
			{#if description}<span class="description" id="{id}-description">{description}</span>{/if}
		</span>
	</label>
{:else}
	{@render control()}
{/if}

<style>
	.labelled {
		display: flex;
		align-items: flex-start;
		gap: 12px;
		cursor: pointer;
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

	.checkbox {
		position: relative;
		display: inline-grid;
		flex-shrink: 0;
		inline-size: 22px;
		block-size: 22px;
	}

	.input {
		position: absolute;
		inset: -11px;
		margin: 0;
		opacity: 0;
		cursor: pointer;
		z-index: 1;
	}

	.input:disabled {
		cursor: not-allowed;
	}

	.box {
		display: grid;
		place-items: center;
		border-radius: 7px;
		background: var(--surface);
		box-shadow: inset 0 0 0 1.5px var(--border-strong);
		color: var(--on-accent);
		transition:
			background-color var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.box :global(svg) {
		inline-size: 14px;
		block-size: 14px;
		opacity: 0;
		transform: scale(0.5);
		transition:
			opacity var(--dur-fast) var(--ease-out),
			transform var(--dur-spring) var(--spring-pop);
	}

	.input:checked + .box,
	.input:indeterminate + .box {
		background: var(--accent);
		box-shadow: none;
	}

	.input:checked + .box :global(svg),
	.input:indeterminate + .box :global(svg) {
		opacity: 1;
		transform: none;
	}

	.input:active:not(:disabled) + .box {
		transform: scale(0.9);
		transition-duration: var(--dur-instant);
	}

	.input:focus-visible + .box {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.input:disabled + .box {
		opacity: 0.45;
	}
</style>
