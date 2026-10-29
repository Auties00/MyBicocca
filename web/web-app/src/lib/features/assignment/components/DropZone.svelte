<script lang="ts">
	import IconUploadSimple from '~icons/ph/upload-simple';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';

	interface Props {
		/** The picker's `accept`. */
		accept: string | undefined;
		multiple: boolean;
		disabled: boolean;
		/** What the zone takes: count, size, types. */
		hint: string;
		/** Id of the element naming the zone. */
		labelledby: string;
		/** A slim row with just the button, for when the list is full and picks replace. */
		compact?: boolean;
		/** The button's label; "Choose files" by default. */
		action?: string;
		onfiles: (files: File[]) => void;
	}

	let {
		accept,
		multiple,
		disabled,
		hint,
		labelledby,
		compact = false,
		action,
		onfiles
	}: Props = $props();

	let input = $state<HTMLInputElement>();
	let depth = $state(0);
	const over = $derived(depth > 0 && !disabled);
	const hintId = $props.id();

	const carriesFiles = (event: DragEvent) => event.dataTransfer?.types.includes('Files') === true;

	function enter(event: DragEvent) {
		if (!carriesFiles(event)) return;
		event.preventDefault();
		depth++;
	}

	function overZone(event: DragEvent) {
		if (!carriesFiles(event)) return;
		event.preventDefault();
		if (event.dataTransfer) event.dataTransfer.dropEffect = disabled ? 'none' : 'copy';
	}

	function leave(event: DragEvent) {
		if (!carriesFiles(event)) return;
		depth = Math.max(0, depth - 1);
	}

	function drop(event: DragEvent) {
		if (!carriesFiles(event)) return;
		event.preventDefault();
		depth = 0;
		if (disabled) return;
		const files = [...(event.dataTransfer?.files ?? [])];
		if (files.length > 0) onfiles(multiple ? files : files.slice(0, 1));
	}

	function picked() {
		const files = [...(input?.files ?? [])];
		if (input) input.value = '';
		if (files.length > 0) onfiles(files);
	}
</script>

<div
	class={['zone', { over, disabled, compact }]}
	role="group"
	aria-labelledby={labelledby}
	aria-describedby="{hintId}-hint"
	ondragenter={enter}
	ondragover={overZone}
	ondragleave={leave}
	ondrop={drop}
>
	{#if !compact}
		<span class="glyph" aria-hidden="true"><IconUploadSimple /></span>
		<p class="lead">
			<span class="drag"
				>{over ? m.assignment_editor_drop_release() : m.assignment_editor_drop()}</span
			>
			<span class="or">{m.assignment_editor_or()}</span>
		</p>
	{/if}
	<Button
		{disabled}
		size={compact ? 'sm' : 'md'}
		icon={compact ? IconUploadSimple : undefined}
		onclick={() => input?.click()}
	>
		{action ?? m.assignment_editor_choose()}
	</Button>
	<p class="hint" id="{hintId}-hint">{hint}</p>
	<input
		bind:this={input}
		class="input"
		type="file"
		{accept}
		{multiple}
		{disabled}
		tabindex="-1"
		aria-hidden="true"
		onchange={picked}
	/>
</div>

<style>
	.zone {
		display: grid;
		justify-items: center;
		gap: 10px;
		padding: 24px 20px 20px;
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: inset 0 0 0 1.5px var(--border);
		text-align: center;
	}

	.compact {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px 14px;
		padding: 10px 14px;
		text-align: start;
	}

	.compact .hint {
		flex: 1 1 200px;
	}

	/* One accent, the button's: the zone lights up in it while files hover over. */
	.zone.over {
		background: color-mix(in oklch, var(--accent) 6%, var(--surface));
		box-shadow: inset 0 0 0 2px var(--accent);
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
		border-radius: 50%;
		background: var(--surface-sunken);
		color: var(--text-muted);
		transition: transform var(--dur-base) var(--spring-pop);
	}

	.over .glyph {
		background: var(--accent);
		color: var(--on-accent);
		transform: translateY(-3px) scale(1.06);
	}

	.glyph :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	.lead {
		display: grid;
		gap: 2px;
	}

	.drag {
		font: var(--type-ui-strong);
	}

	.or {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.hint {
		max-inline-size: 44ch;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.disabled {
		opacity: 0.6;
	}

	.input {
		display: none;
	}

	/* Touch screens have no files to drag: the button says it all. */
	@media (hover: none) {
		.lead {
			display: none;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.over .glyph {
			transform: none;
		}
	}
</style>
