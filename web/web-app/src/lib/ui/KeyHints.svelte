<script lang="ts" module>
	/** A page's own shortcut beside the list's J and K: "N" to start a new discussion. */
	export interface KeyHint {
		keys: readonly string[];
		label: string;
	}
</script>

<script lang="ts">
	import IconKeyboard from '~icons/ph/keyboard';
	import { m } from '#lib/paraglide/messages.js';
	import Kbd from './Kbd.svelte';

	interface Props {
		/** Shortcuts of the page, after the list's own. */
		more?: readonly KeyHint[];
	}

	let { more = [] }: Props = $props();

	const hints = $derived<readonly KeyHint[]>([
		{ keys: ['J', 'K'], label: m.ui_keys_step() },
		...more
	]);
</script>

<!-- Only where a keyboard is likely: touch screens hide it. -->
<p class="keys">
	<IconKeyboard aria-hidden="true" />
	{#each hints as hint (hint.label)}
		<span>
			{#each hint.keys as key (key)}<Kbd keys={[key]} />{/each}
			{hint.label}
		</span>
	{/each}
</p>

<style>
	.keys {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px 16px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.keys > span {
		display: inline-flex;
		align-items: center;
		gap: 4px;
	}

	.keys :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (pointer: coarse) {
		.keys {
			display: none;
		}
	}
</style>
