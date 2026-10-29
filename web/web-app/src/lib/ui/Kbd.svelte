<script lang="ts">
	import { keyLabel } from './platform.ts';

	interface Props {
		/** Key names: 'mod' is ⌘ on Apple platforms and Ctrl elsewhere; also 'shift', 'alt', 'enter', 'esc', arrows. */
		keys: readonly string[];
		/** 'inverse' for keycaps on a dark tooltip. */
		tone?: 'default' | 'inverse';
	}

	let { keys, tone = 'default' }: Props = $props();
</script>

<kbd class={['kbd', tone]}>
	{#each keys as key, i (i)}<kbd>{keyLabel(key)}</kbd>{/each}
</kbd>

<style>
	.kbd {
		display: inline-flex;
		gap: 3px;
		font: inherit;
	}

	kbd kbd {
		display: inline-grid;
		place-items: center;
		min-inline-size: 20px;
		block-size: 20px;
		padding-inline: 5px;
		border-radius: var(--r-xs);
		background: var(--surface);
		color: var(--text-muted);
		font: 500 0.6875rem / 1 var(--font-ui);
		letter-spacing: 0.02em;
		white-space: nowrap;
		box-shadow:
			0 0 0 1px var(--hairline-strong),
			inset 0 -1px 0 var(--hairline-strong);
	}

	.inverse kbd {
		background: color-mix(in oklch, currentColor 14%, transparent);
		color: inherit;
		box-shadow: inset 0 -1px 0 color-mix(in oklch, currentColor 25%, transparent);
	}
</style>
