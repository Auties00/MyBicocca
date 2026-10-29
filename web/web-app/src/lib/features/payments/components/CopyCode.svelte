<script lang="ts">
	import IconCheck from '~icons/ph/check';
	import IconCopy from '~icons/ph/copy';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';

	interface Props {
		/** The code as it is copied. */
		value: string;
		/** What the code is ("Codice avviso"), for the button and the confirmation. */
		label: string;
	}

	let { value, label }: Props = $props();

	let shown = $state<HTMLElement>();
	let copied = $state(false);

	/* Groups of four, as notice codes are printed; the copy keeps the bare digits. */
	const grouped = $derived(value.replace(/(.{4})(?=.)/g, '$1 '));

	async function copy() {
		try {
			await navigator.clipboard.writeText(value);
			copied = true;
			toast.success(m.payments_copied({ label }));
			setTimeout(() => (copied = false), 1600);
		} catch {
			/* Selected, the code is one Ctrl+C away. */
			if (shown) window.getSelection()?.selectAllChildren(shown);
			toast.error(m.payments_copy_failed({ label }));
		}
	}
</script>

<span class="code">
	<span class="value" translate="no" bind:this={shown}>{grouped}</span>
	<IconButton
		icon={copied ? IconCheck : IconCopy}
		label={m.payments_copy({ label })}
		size="sm"
		onclick={copy}
	/>
</span>

<style>
	/* The code under its label, the button at the cell's end without making the row taller. */
	.code {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: var(--sp-2);
	}

	.code :global(.btn) {
		flex: none;
		margin-block: -6px;
		margin-inline-end: -8px;
	}

	.value {
		font-variant-numeric: tabular-nums slashed-zero;
		letter-spacing: 0.02em;
		overflow-wrap: anywhere;
	}
</style>
