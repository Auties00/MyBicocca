<script lang="ts" module>
	/** Wiring for the control a Field wraps. */
	export interface FieldControl {
		id: string;
		describedby: string | undefined;
		invalid: boolean;
	}
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import { m } from '#lib/paraglide/messages.js';

	interface Props {
		label: string;
		/** Helper text under the control. */
		description?: string;
		/** Validation message under the control; also marks the control invalid. */
		error?: string;
		/** Keeps the label for screen readers only (e.g. a search field with a visible icon). */
		hideLabel?: boolean;
		/** Marks the field "(facoltativo)" in a form where most fields are required. */
		optional?: boolean;
		/** Right side of the label row, e.g. a character counter or a link. */
		aside?: Snippet;
		id?: string;
		children: Snippet<[FieldControl]>;
	}

	let {
		label,
		description,
		error,
		hideLabel = false,
		optional = false,
		aside,
		id: givenId,
		children
	}: Props = $props();

	const generated = $props.id();
	const id = $derived(givenId ?? generated);
	const describedby = $derived(
		[description && `${id}-description`, error && `${id}-error`].filter(Boolean).join(' ') ||
			undefined
	);
</script>

<div class="field">
	<div class={['top', { 'sr-only': hideLabel }]}>
		<label class="label" for={id}>
			{label}
			{#if optional}<span class="optional">{m.ui_optional()}</span>{/if}
		</label>
		{#if aside}<span class="aside">{@render aside()}</span>{/if}
	</div>
	{@render children({ id, describedby, invalid: !!error })}
	{#if description}<p class="description" id="{id}-description">{description}</p>{/if}
	{#if error}
		<p class="error" id="{id}-error">
			<IconWarningCircle aria-hidden="true" />
			{error}
		</p>
	{/if}
</div>

<style>
	.field {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		gap: 6px;
		min-inline-size: 0;
	}

	.top {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 12px;
	}

	.label {
		font: var(--type-ui-strong);
	}

	.optional {
		margin-inline-start: 4px;
		color: var(--text-muted);
		font-weight: 400;
	}

	.aside {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.error {
		display: flex;
		align-items: flex-start;
		gap: 6px;
		color: var(--danger);
		font: var(--type-meta);
		font-weight: 500;
	}

	.error :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		margin-block-start: 1px;
	}
</style>
